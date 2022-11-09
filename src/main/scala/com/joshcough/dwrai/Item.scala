package com.joshcough.dwrai

object Item {
  val TorchByte: Int            = 0x1
  val FairyWaterByte: Int       = 0x2
  val WingsByte: Int            = 0x3
  val DragonScaleByte: Int      = 0x4
  val FairyFluteByte: Int       = 0x5
  val FightersRingByte: Int     = 0x6
  val ErdricksTokenByte: Int    = 0x7
  val GwaelinsLoveByte: Int     = 0x8
  val CursedBeltByte: Int       = 0x9
  val SilverHarpByte: Int       = 0xa
  val DeathNecklaceByte: Int    = 0xb
  val StonesOfSunlightByte: Int = 0xc
  val StaffOfRainByte: Int      = 0xd
  val RainbowDropByte: Int      = 0xe
  val HerbByte: Int             = 0xf
  val MagicKeyByte: Int         = 0x10

  val Torch            = Item(TorchByte, "Torch")
  val FairyWater       = Item(FairyWaterByte, "Fairy Water")
  val Wings            = Item(WingsByte, "Wings")
  val DragonScale      = Item(DragonScaleByte, "Dragon's Scale")
  val FairyFlute       = Item(FairyFluteByte, "Fairy Flute")
  val FightersRing     = Item(FightersRingByte, "Fighter's Ring")
  val ErdricksToken    = Item(ErdricksTokenByte, "Erdrick's Token")
  val GwaelinsLove     = Item(GwaelinsLoveByte, "Gwaelin's Love")
  val CursedBelt       = Item(CursedBeltByte, "Cursed Belt")
  val SilverHarp       = Item(SilverHarpByte, "Silver Harp")
  val DeathNecklace    = Item(DeathNecklaceByte, "Death Necklace")
  val StonesOfSunlight = Item(StonesOfSunlightByte, "Stones of Sunlight")
  val StaffOfRain      = Item(StaffOfRainByte, "Staff of Rain")
  val RainbowDrop      = Item(RainbowDropByte, "Rainbow Drop")
  val Herb             = Item(HerbByte, "Herb")
  val MagicKey         = Item(MagicKeyByte, "Magic Key")

}

case class Item(byte: Int, name: String)
