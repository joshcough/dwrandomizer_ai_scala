package com.joshcough.dwrai

import nintaco.api.API

/*
require 'Class'
require 'controller'
require 'helpers'
require 'player_data'
require 'shops'
 */

case class Memory(api: API) {

  val OverWorldId: MapId = MapId(1)
  val ENEMY_ID_ADDR: Int = 0x3c

  def readRAM(addr: Int): Int = api.readCPU(addr)
  def writeRAM(addr: Int, value: Int): Unit = api.writeCPU(addr, value)

  def readROM(addr: Int): Int = api.readPrgRom(addr)
  def writeROM(addr: Int, value: Int): Unit = api.writePrgRom(addr, value)

  def getX: Int = readRAM(0x8e)
  def getY: Int = readRAM(0x8f)
  def getMapId: MapId = MapId(readRAM(0x45))
  def getLocation: Point = Point(getMapId, getX, getY)

  def getCoordinates: Point = Point(OverWorldId, readRAM(0xe114), readRAM(0xe11A))

  // get the id of the current enemy, if it exists
  // no idea what gets returned if not in battle
  def getEnemyId: Int = readRAM(ENEMY_ID_ADDR)
  def setEnemyId(enemyId: Int): Unit = writeRAM(ENEMY_ID_ADDR, enemyId)

  def getRadiantTimer: Int = readRAM(0xDA)
  def setRadiantTimer(n: Int): Unit = writeRAM(0xDA, n)
  def getRepelTimer: Int = readRAM(0xDB)
  def setRepelTimer(n: Int): Unit = writeRAM(0xDB, n)

  // DB10 - DB1F | "Return" placement code
  // The notes of dwr say that that block has the codes, but
  // I've narrowed it down to these exact addresses:
  val RETURN_WARP_X_ADDR: Int = 0xDB15
  val RETURN_WARP_Y_ADDR: Int = 0xDB1D

  def setReturnWarpLocation(x: Int, y: Int): Unit = {
    writeROM(RETURN_WARP_X_ADDR, x)
    writeROM(RETURN_WARP_Y_ADDR, y)
  }

  def getNumberOfHerbs: Int = readRAM(0xc0)
  def getNumberOfKeys: Int = readRAM(0xbf)
  def getCurrentHP: Int = readRAM(0xc5)
  def getMaxHP: Int = readRAM(0xca)
  def getCurrentMP: Int = readRAM(0xc6)
  def getMaxMP: Int = readRAM(0xcb)
  def getXP: Int = api.readCPU16(0xba)
  def getGold: Int = api.readCPU16(0xbc)
  def getLevel: Int = readRAM(0xc7)
  def getStrength: Int = readRAM(0xc8)
  def getAgility: Int = readRAM(0xc9)
  def getAttackPower: Int = readRAM(0xcc)
  def getDefensePower: Int = readRAM(0xcd)

  def getLevels: List[Int] = Iterator.unfold(0xF35B){ i =>
    if (i <= 0xF395) Some((readROM(i + 1) * 256 + readROM(i), i + 2)) else None
  }.toList

  def all: Map[String, Any] = Map(
    "nrHerbs" -> getNumberOfHerbs,
    "nrKeys" -> getNumberOfKeys,
    "hp" -> getCurrentHP,
    "mp" -> getCurrentMP,
    "maxHp" -> getMaxHP,
    "maxMp" -> getMaxMP,
    "xp" -> getXP,
    "gold" -> getGold,
    "level" -> getLevel,
    "str" -> getStrength,
    "agi" -> getAgility,
    "atk" -> getAttackPower,
    "def" -> getDefensePower,
    "loc" -> getLocation,
    "levels" -> getLevels,
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