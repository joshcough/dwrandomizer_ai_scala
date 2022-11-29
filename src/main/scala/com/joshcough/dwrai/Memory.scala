package com.joshcough.dwrai

import cats.implicits._
import nintaco.api.API
import cats.effect.IO
import com.joshcough.dwrai.Bytes.{hiNibble, loNibble}
import com.joshcough.dwrai.Chests.{ChestItem, chestItemsByByte}
import com.joshcough.dwrai.Items.{Armor, ItemInventory, Shield, Weapon}

case class Memory(api: API) {

  def readRAM(addr: Address): IO[Byte]                  = IO(api.readCPU(addr.value).toByte)
  def writeRAM(addr: Address, value: Byte): IO[Unit]    = IO(api.writeCPU(addr.value, value))
  def readRAM16(addr: Address): IO[Short]               = IO(api.readCPU16(addr.value).toShort)
  def writeRAM16(addr: Address, value: Short): IO[Unit] = IO(api.writeCPU16(addr.value, value))
  def readROM(addr: Address): IO[Int]                   = IO(api.readPrgRom(addr.value))
  def writeROM(addr: Address, value: Int): IO[Unit]     = IO(api.writePrgRom(addr.value, value))

  // we should separate the stuff above here into its own interface.

  val OverWorldId: MapId     = MapId(1)

  def getX: IO[Byte]         = readRAM(Address(0x8e))
  def getY: IO[Byte]         = readRAM(Address(0x8f))
  def getMapId: IO[MapId]    = readRAM(Address(0x45)).map(MapId(_))
  def getLocation: IO[Point] = for { m <- getMapId; x <- getX; y <- getY } yield Point(m, x, y)

  def getCoordinates: IO[Point] = for {
    coorX <- readRAM(Address(0xe114))
    coorY <- readRAM(Address(0xe11a))
  } yield Point(OverWorldId, coorX, coorY)

  // get the id of the current enemy, if it exists
  // no idea what gets returned if not in battle
  val ENEMY_ID_ADDR: Address = Address(0x3c)
  def getEnemyId: IO[EnemyId]             = readRAM(ENEMY_ID_ADDR).map(EnemyId)
  def setEnemyId(enemyId: Byte): IO[Unit] = writeRAM(ENEMY_ID_ADDR, enemyId)

  def getRadiantTimer: IO[Byte]          = readRAM(Address(0xda))
  def setRadiantTimer(n: Byte): IO[Unit] = writeRAM(Address(0xda), n)
  def getRepelTimer: IO[Byte]            = readRAM(Address(0xdb))
  def setRepelTimer(n: Byte): IO[Unit]   = writeRAM(Address(0xdb), n)

  // DB10 - DB1F | "Return" placement code
  // The notes of dwr say that that block has the codes, but
  // I've narrowed it down to these exact addresses:
  val RETURN_WARP_X_ADDR: Address = Address(0xdb15)
  val RETURN_WARP_Y_ADDR: Address = Address(0xdb1d)

  def setReturnWarpLocation(x: Int, y: Int): IO[Unit] = {
    writeROM(RETURN_WARP_X_ADDR, x)
    writeROM(RETURN_WARP_Y_ADDR, y)
  }

  def getNumberOfHerbs: IO[Byte]        = readRAM(Address(0xc0))
  def getNumberOfKeys: IO[Byte]         = readRAM(Address(0xbf))
  def getCurrentHP: IO[CurrentHP]       = readRAM(Address(0xc5)).map(CurrentHP(_))
  def getMaxHP: IO[MaxHP]               = readRAM(Address(0xca)).map(MaxHP(_))
  def getCurrentMP: IO[CurrentMP]       = readRAM(Address(0xc6)).map(CurrentMP(_))
  def getMaxMP: IO[MaxMP]               = readRAM(Address(0xcb)).map(MaxMP(_))
  def getXP: IO[Xp]                     = readRAM16(Address(0xba)).map(Xp(_))
  def getGold: IO[Gold]                 = readRAM16(Address(0xbc)).map(Gold(_))
  def getLevel: IO[LevelId]             = readRAM(Address(0xc7)).map(LevelId(_))
  def getStrength: IO[Strength]         = readRAM(Address(0xc8)).map(Strength(_))
  def getAgility: IO[Agility]           = readRAM(Address(0xc9)).map(Agility(_))
  def getAttackPower: IO[AttackPower]   = readRAM(Address(0xcc)).map(AttackPower(_))
  def getDefensePower: IO[DefensePower] = readRAM(Address(0xcd)).map(DefensePower(_))

  def getStats: IO[Stats] = for {
    lvl   <- getLevel
    hp    <- getCurrentHP
    maxHp <- getMaxHP
    mp    <- getCurrentMP
    maxMp <- getMaxMP
    gold  <- getGold
    xp    <- getXP
    str   <- getStrength
    agi   <- getAgility
    atk   <- getAttackPower
    df    <- getDefensePower
  } yield Stats(lvl, hp, maxHp, mp, maxMp, gold, xp, str, agi, atk, df)

  def getLevels: IO[Seq[Level]] = {
    val addrs: Seq[Address] = Iterator
      .unfold(Address(0xf35b)) { i: Address =>
        if (i <= Address(0xf395)) Some(i, i + 2) else None
      }
      .toList
    addrs.zipWithIndex.traverse { case (addr, id) =>
      for {
        high <- readROM(addr + 1)
        low  <- readROM(addr)
      } yield Level(LevelId(id + 1), Xp(high * 256 + low))
    }
  }

  def readChests: IO[Map[Point, ChestItem]] =
    Range(0, 30).toList.traverse(readChest).map(_.toMap)

  def readChest(chestId: Int): IO[(Point, ChestItem)] = {
    val firstChestAddr = Address(0x5ddd) - 16
    val addr           = firstChestAddr + chestId * 4
    for {
      mapId  <- readROM(addr).map(MapId(_))
      x      <- readROM(addr + 1)
      y      <- readROM(addr + 2)
      itemId <- readROM(addr + 3)
    } yield (Point(mapId, x, y), chestItemsByByte(itemId))
  }

  def getItemNumberOfKeys: IO[Byte]  = readRAM(Address(0xbf))
  def getItemNumberOfHerbs: IO[Byte] = readRAM(Address(0xc0))

  def getItems: IO[ItemInventory] = for {
    b12 <- readRAM(Address(0xc1))
    b34 <- readRAM(Address(0xc2))
    b56 <- readRAM(Address(0xc3))
    b78 <- readRAM(Address(0xc4))
    slots: List[Int] = List(b12, b34, b56, b78)
      .flatMap(b => List(loNibble(b), hiNibble(b)))
      .filter(_ > 0)
    nrHerbs <- getItemNumberOfHerbs
    nrKeys  <- getItemNumberOfKeys
  } yield ItemInventory.fromSlots(nrHerbs, nrKeys, slots)

  def getStatuses: IO[Set[Status]] = for {
    cf <- readRAM(Address(0xcf))
    df <- readRAM(Address(0xdf))
  } yield Status.getStatuses(cf, df)

  def getEquipment: IO[Equipment] = for {
    b <- readRAM(Address(0xbe))
    weaponId = Weapon.weaponByByte(b & 224)
    armorId  = Armor.armorByByte(b & 28)
    shieldId = Shield.shieldByByte(b & 3)
  } yield Equipment(weaponId, armorId, shieldId)

  def getSpells: IO[Set[Spell]] = for {
    ce <- readRAM(Address(0xce))
    cf <- readRAM(Address(0xcf))
  } yield Spell.fromBytes(ce, cf)

  def getPlayerData: IO[PlayerData] = for {
    loc    <- getLocation
    stats  <- getStats
    eq     <- getEquipment
    spells <- getSpells
    items  <- getItems
    status <- getStatuses
  } yield PlayerData(loc, stats, eq, spells, items, status)

  /*
  -- ShopItemsTbl:
  -- ;Koll weapons and armor shop.
  -- L9991:  .byte $02, $03, $0A, $0B, $0E, $FD
  -- ;Brecconary weapons and armor shop.
  -- L9997:  .byte $00, $01, $02, $07, $08, $0E, $FD
  -- ;Garinham weapons and armor shop.
  -- L999E:  .byte $01, $02, $03, $08, $09, $0A, $0F, $FD
  -- ;Cantlin weapons and armor shop 1.
  -- L99A6:  .byte $00, $01, $02, $08, $09, $0F, $FD
  -- ;Cantlin weapons and armor shop 2.
  -- L99AD:  .byte $03, $04, $0B, $0C, $FD
  -- ;Cantlin weapons and armor shop 3.
  -- L99B2:  .byte $05, $10, $FD
  -- ;Rimuldar weapons and armor shop.
  -- L99B5:  .byte $02, $03, $04, $0A, $0B, $0C, $FD
  def readWeaponAndArmorShops()
    function readSlots(start)
      val slots = {}
      val counter = 1
      val nextSlot = readROM(start)
      while nextSlot ~= 253 and counter <= 6 do
        table.insert(slots, nextSlot)
        nextSlot = readROM(start+counter)
        counter = counter + 1
      end
      = {slots, start+counter}
    end

    val t = {}
    -- 19A1-19CB | Weapon Shop Inventory |
    val addr = 0x19A1
    for i = 1,7 do
      val rs = readSlots(addr)
      t[i] = rs[1]
      addr = rs[2]
    end

    = WeaponAndArmorShops(
      t[2], -- Brecconary
      t[4], -- Cantlin1
      t[5], -- Cantlin2
      t[6], -- Cantlin3
      t[3], -- Garinham
      t[1], -- Kol
      t[7]  -- Rimuldar
    )


  def readSearchSpots()
    function readSpot(b, loc)
      val id = readRAM(b)
      if id == 0 then = SearchSpot(loc, nil)
      else = SearchSpot(loc, CHEST_CONTENT[readRAM(b)])
      end
    end
    --  03:E11D: A9 01 LDA #$01  (rom position for overworld search spot)
    --  03:E13C: A9 00 LDA #$00  (rom position for kol search spot)
    --  03:E152: A9 11 LDA #$11  (rom position for hauksness search spot)
    = SearchSpots(
      readSpot(0xe11e, self:getCoordinates()),
      readSpot(0xe13d, Point(Kol, 9, 6)),
      readSpot(0xe153, Point(Hauksness, 18, 12))
    )

  -- 0x51 - ??       | NPC Data                | 16 bits ------------------------->   3 bits -> sprite
  --                 |                         | 5 bits -> x coordinate
  --                 |                         | 3 bits -> direction
  --                 |                         | 5 bits -> y coordinate

  -- .alias NPCXPos          $51     ;Through $8A. NPC X block position on current map. Also NPC type.
  -- .alias NPCYPos          $52     ;Through $8B. NPC Y block position on current map. Also NPC direction.
  -- .alias NPCMidPos        $53     ;Through $8C. NPC offset from current tile. Used only when moving.
  def readNPCs()
    = {
      readNPC(0x51), -- 123
      readNPC(0x54), -- 456
      readNPC(0x57), -- 789
      readNPC(0x5A), -- abc
      readNPC(0x5D), -- def
      readNPC(0x60), -- 012
      readNPC(0x63), -- 345
      readNPC(0x66), -- 678
      readNPC(0x69), -- 9ab
      readNPC(0x6C), -- cde
      readNPC(0x6F), -- f01
      readNPC(0x72), -- 234
      readNPC(0x75), -- 567
      readNPC(0x78), -- 89a
      readNPC(0x7B), -- bcd
      readNPC(0x7E), -- ef0
      readNPC(0x81), -- 123
      readNPC(0x84), -- 456
      readNPC(0x87), -- 789
      readNPC(0x8A), -- abc
    }


  def readNPC(byte)
    val b1 = readRAM(byte)
    val b2 = readRAM(byte + 1)
    = NPC(byte, AND(b1, 31), AND(b2, 31))
    -- if we ever care about sprite and direction (i doubt we will), we can use these:
    -- log.debug("SSS? ", AND(b1, 224), decimalToHex(bitwise_and(b1, 224)))
    -- log.debug("DDD? ", AND(b2, 224), decimalToHex(bitwise_and(b2, 224)))


  NPC = class(function(a, byte, x, y)
    a.byte = byte
    a.x = x
    a.y = y
  end)

  function NPC:__tostring()
    = "NPC {x:" .. self.x .. ", y:" .. self.y .. "}"

  def printNPCs()
    val npcs = readNPCs()
    for _, npc in ipairs(npcs) do
      log.debug(npc)
    end

  def printDoorsAndChests()
    -- .alias DoorXPos         $600C   ;Through $601A. X and y positions of doors-->
    -- .alias DoorYPos         $600D   ;Through $601B. opened on the current map.
    -- .alias TrsrXPos         $601C   ;Through $602A. X and y positions of treasure-->
    -- .alias TrsrYPos         $601D   ;Through $602B. chests picked up on the current map.
    log.debug("=== Doors:")
    val i = 0x600C
    while i <= 0x601A do
      log.debug(readRAM(i), readRAM(i+1))
      i = i + 2
    end

    log.debug("=== Chests:")
    val i = 0x601C
    while i <= 0x602A do
      log.debug(readRAM(i), readRAM(i+1))
      i = i + 2
    end


  -- .alias CharDirection    $602F   ;Player's facing direction, 0-up, 1-right, 2-down, 3-left.
  def readPlayerDirection()
    val dir = readRAM(0x602F)
    if     dir == 0 then = UP
    elseif dir == 1 then = RIGHT
    elseif dir == 2 then = DOWN
    else                 = LEFT
    end
   */
}

//  def debug: Map[String, Any] = Map(
//    "nrHerbs" -> getNumberOfHerbs,
//    "nrKeys"  -> getNumberOfKeys,
//    "loc"     -> getLocation,
//    "levels"  -> getLevels,
//    "stats"   -> getStats,
//    "chests"  -> readChests,
//    "items"   -> getItems
//  )
