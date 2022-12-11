package com.joshcough

package object dwrai {

  case class CurrentHP(value: Int)
  case class MaxHP(value: Int)
  case class CurrentMP(value: Int)
  case class MaxMP(value: Int)
  case class Xp(value: Int)
  case class Gold(value: Int)
  case class LevelId(id: Int)
  case class Level(id: LevelId, xp: Xp)
  case class Strength(value: Int)
  case class Agility(value: Int)
  case class AttackPower(value: Int)
  case class DefensePower(value: Int)

  case class RamAddress(value: Int) {
    def +(i: Int): RamAddress      = RamAddress(value + i)
    def -(i: Int): RamAddress      = RamAddress(value - i)
    def <(a: RamAddress): Boolean  = value < a.value
    def <=(a: RamAddress): Boolean = value <= a.value
  }

  case class RomAddress(value: Int) {
    def +(i: Int): RomAddress      = RomAddress(value + i)
    def -(i: Int): RomAddress      = RomAddress(value - i)
    def <(a: RomAddress): Boolean  = value < a.value
    def <=(a: RomAddress): Boolean = value <= a.value
  }

  case class MapId(value: Int)

  object MapId {
    val CharlockId: MapId           = MapId(2)
    val HauksnessId: MapId          = MapId(3)
    val TantegelId: MapId           = MapId(4)
    val TantegelThroneRoomId: MapId = MapId(5)
    val CharlockThroneRoomId: MapId = MapId(6)
    val KolId: MapId                = MapId(7)
    val BrecconaryId: MapId         = MapId(8)
    val GarinhamId: MapId           = MapId(9)
    val CantlinId: MapId            = MapId(10)
    val RimuldarId: MapId           = MapId(11)
    val TantegelBasementId: MapId   = MapId(12)
    val NorthernShrineId: MapId     = MapId(13)
    val SouthernShrineId: MapId     = MapId(14)
    val CharlockCaveLv1Id: MapId    = MapId(15)
    val CharlockCaveLv2Id: MapId    = MapId(16)
    val CharlockCaveLv3Id: MapId    = MapId(17)
    val CharlockCaveLv4Id: MapId    = MapId(18)
    val CharlockCaveLv5Id: MapId    = MapId(19)
    val CharlockCaveLv6Id: MapId    = MapId(20)
    val SwampCaveId: MapId          = MapId(21)
    val MountainCaveLv1Id: MapId    = MapId(22)
    val MountainCaveLv2Id: MapId    = MapId(23)
    val GarinsGraveLv1Id: MapId     = MapId(24)
    val GarinsGraveLv2Id: MapId     = MapId(25)
    val GarinsGraveLv3Id: MapId     = MapId(26)
    val GarinsGraveLv4Id: MapId     = MapId(27)
    val ErdricksCaveLv1Id: MapId    = MapId(28)
    val ErdricksCaveLv2Id: MapId    = MapId(29)

    val ALL_MAP_IDS = List(
      CharlockId,
      HauksnessId,
      TantegelId,
      TantegelThroneRoomId,
      CharlockThroneRoomId,
      KolId,
      BrecconaryId,
      GarinhamId,
      CantlinId,
      RimuldarId,
      TantegelBasementId,
      NorthernShrineId,
      SouthernShrineId,
      CharlockCaveLv1Id,
      CharlockCaveLv2Id,
      CharlockCaveLv3Id,
      CharlockCaveLv4Id,
      CharlockCaveLv5Id,
      CharlockCaveLv6Id,
      SwampCaveId,
      MountainCaveLv1Id,
      MountainCaveLv2Id,
      GarinsGraveLv1Id,
      GarinsGraveLv2Id,
      GarinsGraveLv3Id,
      GarinsGraveLv4Id,
      ErdricksCaveLv1Id,
      ErdricksCaveLv2Id
    )
  }

  object Point {
    implicit val pointOrdering: Ordering[Point] = (a: Point, b: Point) => a.compare(b)
  }

  case class Point(mapId: MapId, x: Int, y: Int) {
    def <(p: Point): Boolean = compare(p) < 0

    def compare(p: Point): Int =
      (mapId.value.compare(p.mapId.value), x.compare(p.x), y.compare(p.y)) match {
        case (0, 0, y_) => y_
        case (0, x_, _) => x_
        case (i, _, _)  => i
      }
  }

  object Bytes {
    // HI_NIBBLE(b) (((b) >> 4) & 0x0F)
    def hiNibble(b: Int): Int = b >> 4 & 0x0f
    // LO_NIBBLE(b) (((b) & 0x0F)
    def loNibble(b: Int): Int = b & 0x0f
  }

}
