package com.joshcough.dwrai

import nintaco.api.API

import Stats._

case class Memory(api: API) {

  def readRAM(addr: Address): Byte               = api.readCPU(addr.value).toByte
  def writeRAM(addr: Address, value: Byte): Unit = api.writeCPU(addr.value, value)

  def readRAM16(addr: Address): Short               = api.readCPU16(addr.value).toShort
  def writeRAM16(addr: Address, value: Short): Unit = api.writeCPU16(addr.value, value)

  def readROM(addr: Address): Int               = api.readPrgRom(addr.value)
  def writeROM(addr: Address, value: Int): Unit = api.writePrgRom(addr.value, value)

  // we should separate the stuff above here into its own interface.

  val OverWorldId: MapId     = MapId(1)
  val ENEMY_ID_ADDR: Address = Address(0x3c)

  def getX: Byte         = readRAM(Address(0x8e))
  def getY: Byte         = readRAM(Address(0x8f))
  def getMapId: MapId    = MapId(readRAM(Address(0x45)))
  def getLocation: Point = Point(getMapId, getX, getY)

  def getCoordinates: Point = Point(OverWorldId, readRAM(Address(0xe114)), readRAM(Address(0xe11a)))

  // get the id of the current enemy, if it exists
  // no idea what gets returned if not in battle
  def getEnemyId: Byte                = readRAM(ENEMY_ID_ADDR)
  def setEnemyId(enemyId: Byte): Unit = writeRAM(ENEMY_ID_ADDR, enemyId)

  def getRadiantTimer: Byte          = readRAM(Address(0xda))
  def setRadiantTimer(n: Byte): Unit = writeRAM(Address(0xda), n)
  def getRepelTimer: Byte            = readRAM(Address(0xdb))
  def setRepelTimer(n: Byte): Unit   = writeRAM(Address(0xdb), n)

  // DB10 - DB1F | "Return" placement code
  // The notes of dwr say that that block has the codes, but
  // I've narrowed it down to these exact addresses:
  val RETURN_WARP_X_ADDR: Address = Address(0xdb15)
  val RETURN_WARP_Y_ADDR: Address = Address(0xdb1d)

  def setReturnWarpLocation(x: Int, y: Int): Unit = {
    writeROM(RETURN_WARP_X_ADDR, x)
    writeROM(RETURN_WARP_Y_ADDR, y)
  }

  def getNumberOfHerbs: Byte        = readRAM(Address(0xc0))
  def getNumberOfKeys: Byte         = readRAM(Address(0xbf))
  def getCurrentHP: CurrentHP       = CurrentHP(readRAM(Address(0xc5)))
  def getMaxHP: MaxHP               = MaxHP(readRAM(Address(0xca)))
  def getCurrentMP: CurrentMP       = CurrentMP(readRAM(Address(0xc6)))
  def getMaxMP: MaxMP               = MaxMP(readRAM(Address(0xcb)))
  def getXP: Xp                     = Xp(readRAM16(Address(0xba)))
  def getGold: Gold                 = Gold(readRAM16(Address(0xbc)))
  def getLevel: Level               = Level(readRAM(Address(0xc7)))
  def getStrength: Strength         = Strength(readRAM(Address(0xc8)))
  def getAgility: Agility           = Agility(readRAM(Address(0xc9)))
  def getAttackPower: AttackPower   = AttackPower(readRAM(Address(0xcc)))
  def getDefensePower: DefensePower = DefensePower(readRAM(Address(0xcd)))

  def getStats: Stats = Stats(
    getLevel,
    getCurrentHP,
    getMaxHP,
    getCurrentMP,
    getMaxMP,
    getGold,
    getXP,
    getStrength,
    getAgility,
    getAttackPower,
    getDefensePower
  )

  def getLevels: List[Int] = Iterator
    .unfold(Address(0xf35b)) { i: Address =>
      if (i.value <= 0xf395)
        Some((readROM(Address(i.value + 1)) * 256 + readROM(i), Address(i.value + 2)))
      else None
    }
    .toList

  def all: Map[String, Any] = Map(
    "nrHerbs" -> getNumberOfHerbs,
    "nrKeys"  -> getNumberOfKeys,
    "loc"     -> getLocation,
    "levels"  -> getLevels,
    "stats"   -> getStats
  )

  /*
  def getItems()
    val slots = {}
    val b12 = readRAM(0xc1)
    slots[1] = loNibble(b12)
    slots[2] = hiNibble(b12)
    val b34 = readRAM(0xc2)
    slots[3] = loNibble(b34)
    slots[4] = hiNibble(b34)
    val b56 = readRAM(0xc3)
    slots[5] = loNibble(b56)
    slots[6] = hiNibble(b56)
    val b78 = readRAM(0xc4)
    slots[7] = loNibble(b78)
    slots[8] = hiNibble(b78)
    = Items(self:getItemNumberOfHerbs(), self:getItemNumberOfKeys(), slots)


  def getStatuses()
    = Statuses(readRAM(0xcf), readRAM(0xdf))


  def getEquipment()
    val b = readRAM(0xbe)
    val weaponId = bitwise_and(b, 224)
    val armorId = bitwise_and(b, 28)
    val shieldId = bitwise_and(b, 3)
    = Equipment(weaponId, armorId, shieldId)

  def readStats()
    = Stats(
      self:getCurrentHP(),
      self:getMaxHP(),
      self:getCurrentMP(),
      self:getMaxMP(),
      self:getXP(),
      self:getGold(),
      self:getLevel(),
      self:getStrength(),
      self:getAgility(),
      self:getAttackPower(),
      self:getDefensePower()
    )

  def spells()
    = Spells(readRAM(0xce),  readRAM(0xcf))


  def readPlayerData()
    = PlayerData(self:getLocation(), readStats(), self:getEquipment(),
                      self:spells(), self:getItems(), self:getStatuses(), readLevels())


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


  def readChests()
    val chests = {}
    -- 5DDD - 5E58  | Chest Data  | Four bytes long: Map,X,Y,Contents
    val firstChestAddr = 0x5ddd
    for i = 0,30 do
      val addr = firstChestAddr + i * 4
      val mapId = readROM(addr)
      val x = readROM(addr + 1)
      val y = readROM(addr + 2)
      val contents = readROM(addr + 3)
      val chest = Chest(Point(mapId, x, y), CHEST_CONTENT[contents])
      -- log.debug("chest", chest)
      table.insert(chests, chest)
    end
    = Chests(chests)



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
