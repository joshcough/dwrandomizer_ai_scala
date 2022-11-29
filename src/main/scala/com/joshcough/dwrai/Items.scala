package com.joshcough.dwrai

object Items {

  // =====================
  // ======= Items =======
  // =====================

  val TorchByte: Byte            = 0x1
  val FairyWaterByte: Byte       = 0x2
  val WingsByte: Byte            = 0x3
  val DragonScaleByte: Byte      = 0x4
  val FairyFluteByte: Byte       = 0x5
  val FightersRingByte: Byte     = 0x6
  val ErdricksTokenByte: Byte    = 0x7
  val GwaelinsLoveByte: Byte     = 0x8
  val CursedBeltByte: Byte       = 0x9
  val SilverHarpByte: Byte       = 0xa
  val DeathNecklaceByte: Byte    = 0xb
  val StonesOfSunlightByte: Byte = 0xc
  val StaffOfRainByte: Byte      = 0xd
  val RainbowDropByte: Byte      = 0xe
  val HerbByte: Byte             = 0xf
  val MagicKeyByte: Byte         = 0x10

  case class Item(byte: Byte, name: String)

  val Torch: Item            = Item(TorchByte, "Torch")
  val FairyWater: Item       = Item(FairyWaterByte, "Fairy Water")
  val Wings: Item            = Item(WingsByte, "Wings")
  val DragonScale: Item      = Item(DragonScaleByte, "Dragon's Scale")
  val FairyFlute: Item       = Item(FairyFluteByte, "Fairy Flute")
  val FightersRing: Item     = Item(FightersRingByte, "Fighter's Ring")
  val ErdricksToken: Item    = Item(ErdricksTokenByte, "Erdrick's Token")
  val GwaelinsLove: Item     = Item(GwaelinsLoveByte, "Gwaelin's Love")
  val CursedBelt: Item       = Item(CursedBeltByte, "Cursed Belt")
  val SilverHarp: Item       = Item(SilverHarpByte, "Silver Harp")
  val DeathNecklace: Item    = Item(DeathNecklaceByte, "Death Necklace")
  val StonesOfSunlight: Item = Item(StonesOfSunlightByte, "Stones of Sunlight")
  val StaffOfRain: Item      = Item(StaffOfRainByte, "Staff of Rain")
  val RainbowDrop: Item      = Item(RainbowDropByte, "Rainbow Drop")
  val Herb: Item             = Item(HerbByte, "Herb")
  val MagicKey: Item         = Item(MagicKeyByte, "Magic Key")

  // these are in order by their byte id
  val itemsByByte: Vector[Item] = Vector(
    Torch,
    FairyWater,
    Wings,
    DragonScale,
    FairyFlute,
    FightersRing,
    ErdricksToken,
    GwaelinsLove,
    CursedBelt,
    SilverHarp,
    DeathNecklace,
    StonesOfSunlight,
    StaffOfRain,
    RainbowDrop,
    // these are not really used:
    Herb,
    MagicKey
  )

  case class ItemInventory(nrHerbs: Int, nrKeys: Int, items: List[Item])

  object ItemInventory {
    def fromSlots(nrHerbs: Int, nrKeys: Int, slots: List[Int]): ItemInventory =
      ItemInventory(nrHerbs, nrKeys, slots.map(z => itemsByByte(z - 1)))

    /*
  function Items:__tostring()
    local res = "=== Items ===\n"
    res = res .. "Keys: " .. self.nrKeys .. "\n"
    res = res .. "Herbs: " .. self.nrHerbs .. "\n"
    for idx = 1,#(self.slots) do
      res = res .. idx .. ": " .. tostring(self.slots[idx]) .. "\n"
    end
    return res
  end

  function Items:itemIndex(item)
    if not self:contains(item) then return nil end
    local herbOffset = self:haveHerbs() and 1 or 0
    local keyOffset = self:haveKeys() and 1 + herbOffset or 0
    local indexOffset = keyOffset
    if item == Herb and self:haveHerbs() then return 1
    elseif item == MagicKey and self:haveKeys() then return keyOffset
    else
      local slotIndex = list.indexOf(self.slots, item)
      return indexOffset + slotIndex
    end
  end

  function Items:contains(item)
    if item == Herb then return self:haveHerbs()
    elseif item == MagicKey then return self:haveKeys()
    else return list.any(self.slots, function(i) return i == item end)
    end
  end

  def numberOfTorches = list.count(self.slots, function(i) return i == Torch end)
  def numberOfFairyWaters = list.count(self.slots, function(i) return i == FairyWater end)
  def numberOfWings = list.count(self.slots, function(i) return i == Wings end)
  def hasWings = self:numberOfWings() > 0
  def hasDragonScale = self:contains(DragonScale) end
  def hasFairyFlute = self:contains(FairyFlute) end
  def hasFightersRing = self:contains(FightersRing)
  def hasErdricksToken = self:contains(ErdricksToken)
  def hasGwaelinsLove = self:contains(GwaelinsLove)
  def numberOfCursedBelts = list.count(self.slots, function(i) return i == CursedBelt end)
  def hasSilverHarp = self:contains(SilverHarp)
  def hasDeathNecklace = self:contains(DeathNecklace)
  def hasStonesOfSunlight = self:contains(StonesOfSunlight)
  def hasStaffOfRain = self:contains(StaffOfRain)
  def hasRainbowDrop = self:contains(RainbowDrop)
  def haveKeys = self.nrKeys > 0
  def haveHerbs = self.nrHerbs > 0

  // TODO: why did i ever need this function?
  function Items:equals(i)
    return self.nrHerbs == i.nrHerbs and
           self.nrKeys == i.nrKeys and
           #(self.slots) == #(i.slots) and
           list.all(list.zipWith(self.slots, i.slots), function (is) return is[1] == is[2] end)
  end
     */
  }

// =====================
// ===== Equipment =====
// =====================

  trait EquipmentItem {
    val name: String
    val byte: Int
  }

  case class Weapon(byte: Int, name: String) extends EquipmentItem

  object Weapon {
    val NoWeaponByte      = 0x0  //  = 0   = 00000000
    val BambooPoleByte    = 0x20 //  = 32  = 00100000
    val ClubByte          = 0x40 //  = 64  = 01000000
    val CopperSwordByte   = 0x60 //  = 96  = 01100000
    val HandAxeByte       = 0x80 //  = 128 = 10000000
    val BroadSwordByte    = 0xa0 //  = 160 = 10100000
    val FlameSwordByte    = 0xc0 //  = 192 = 11000000
    val ErdricksSwordByte = 0xe0 //  = 224 = 11100000

    val NoWeapon: Weapon      = Weapon(NoWeaponByte, "Nothing")
    val BambooPole: Weapon    = Weapon(BambooPoleByte, "Bamboo Pole")
    val Club: Weapon          = Weapon(ClubByte, "Club")
    val CopperSword: Weapon   = Weapon(CopperSwordByte, "Copper Sword")
    val HandAxe: Weapon       = Weapon(HandAxeByte, "Hand Axe")
    val BroadSword: Weapon    = Weapon(BroadSwordByte, "Broad Sword")
    val FlameSword: Weapon    = Weapon(FlameSwordByte, "Flame Sword")
    val ErdricksSword: Weapon = Weapon(ErdricksSwordByte, "Erdrick's Sword")

    val weaponByByte: Map[Int, Weapon] =
      List(NoWeapon, BambooPole, Club, CopperSword, HandAxe, BroadSword, FlameSword, ErdricksSword)
        .map(w => w.byte -> w)
        .toMap

  }

  case class Armor(byte: Int, name: String) extends EquipmentItem

  object Armor {
    val NoArmorByte        = 0x0  //  = 0  = 00000000
    val ClothesByte        = 0x4  //  = 4  = 00000100
    val LeatherArmorByte   = 0x8  //  = 8  = 00001000
    val ChainMailByte      = 0xc  //  = 12 = 00001100
    val HalfPlateArmorByte = 0x10 //  = 16 = 00010000
    val FullPlateArmorByte = 0x14 //  = 20 = 00010100
    val MagicArmorByte     = 0x18 //  = 24 = 00011000
    val ErdricksArmorByte  = 0x1c //  = 28 = 00011100

    val NoArmor: Armor        = Armor(NoArmorByte, "Nothing")
    val Clothes: Armor        = Armor(ClothesByte, "Clothes")
    val LeatherArmor: Armor   = Armor(LeatherArmorByte, "Leather Armor")
    val ChainMail: Armor      = Armor(ChainMailByte, "Chain Mail")
    val HalfPlateArmor: Armor = Armor(HalfPlateArmorByte, "Half Plate Armor")
    val FullPlateArmor: Armor = Armor(FullPlateArmorByte, "Full Plate Armor")
    val MagicArmor: Armor     = Armor(MagicArmorByte, "Magic Armor")
    val ErdricksArmor: Armor  = Armor(ErdricksArmorByte, "Erdrick's Armor")

    val armorByByte: Map[Int, Armor] =
      List(
        NoArmor,
        Clothes,
        LeatherArmor,
        ChainMail,
        HalfPlateArmor,
        FullPlateArmor,
        MagicArmor,
        ErdricksArmor
      ).map(a => a.byte -> a).toMap

  }

  case class Shield(byte: Int, name: String) extends EquipmentItem

  object Shield {
    val NoShieldByte     = 0x0 // = 0 = 00000000
    val SmallShieldByte  = 0x1 // = 1 = 00000001
    val LargeShieldByte  = 0x2 // = 2 = 00000010
    val SilverShieldByte = 0x3 // = 3 = 00000011

    val NoShield: Shield     = Shield(NoShieldByte, "Nothing")
    val SmallShield: Shield  = Shield(SmallShieldByte, "Small Shield")
    val LargeShield: Shield  = Shield(LargeShieldByte, "Large Shield")
    val SilverShield: Shield = Shield(SilverShieldByte, "Silver Shield")

    val shieldByByte: Map[Int, Shield] =
      List(NoShield, SmallShield, LargeShield, SilverShield).map(a => a.byte -> a).toMap
  }

  /*

Equipment = class(function(a,swordId,armorId,shieldId)
  a.weapon = WEAPONS[swordId]
  a.armor = ARMOR[armorId]
  a.shield = SHIELDS[shieldId]
end)

function Equipment:__tostring()
  local res = "=== Equipment ===\n"
  res = res .. "Weapon: " .. tostring(self.weapon) .. "\n"
  res = res .. "Armor: "  .. tostring(self.armor) .. "\n"
  res = res .. "Shield: " .. tostring(self.shield) .. "\n"
  return res
end

function Equipment:equals(e)
  return self.swordId == e.swordId and
         self.armorId == e.armorId and
         self.shieldId == e.shieldId
end

   */
}
