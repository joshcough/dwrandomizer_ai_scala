package com.joshcough.dwrai

import Items._

object Chests {

  trait ChestItem
  case class ItemChestItem(i: Item)  extends ChestItem
  case object GoldChestItem          extends ChestItem
  case object ErdricksArmorChestItem extends ChestItem
  case object ErdricksSwordChestItem extends ChestItem

  // this is also used for search spots.
  val chestItemsByByte: Map[Int, ChestItem] = Map(
    1  -> ErdricksArmorChestItem,
    2  -> ItemChestItem(Herb),
    3  -> ItemChestItem(MagicKey),
    4  -> ItemChestItem(Torch),
    5  -> ItemChestItem(FairyWater),
    6  -> ItemChestItem(Wings),
    7  -> ItemChestItem(DragonScale),
    8  -> ItemChestItem(FairyFlute),
    9  -> ItemChestItem(FightersRing),
    10 -> ItemChestItem(ErdricksToken),
    11 -> ItemChestItem(GwaelinsLove),
    12 -> ItemChestItem(CursedBelt),
    13 -> ItemChestItem(SilverHarp),
    14 -> ItemChestItem(DeathNecklace),
    15 -> ItemChestItem(StonesOfSunlight),
    16 -> ItemChestItem(StaffOfRain),
    17 -> ErdricksSwordChestItem,
    18 -> GoldChestItem // TODO: umm... how much gold? though, not sure it actually matters.
  )

//  case class Chest(location: Point, item: ChestItem) {
//    // vars that the old ai had that i will redo a different way
//    val currentlyOpen = false
//    val everOpened    = false
//  }
}
