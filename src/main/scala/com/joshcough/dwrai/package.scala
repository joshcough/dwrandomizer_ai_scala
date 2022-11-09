package com.joshcough

package object dwrai {

  case class Address(value: Int) {
    def +(i: Int) = Address(value + i)
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

  }

  case class Point(mapId: MapId, x: Int, y: Int)

  object Bytes {
    // HI_NIBBLE(b) (((b) >> 4) & 0x0F)
    def hiNibble(b: Int): Int = math.floor(b / 16).toInt & 0x0f
    // LO_NIBBLE(b) (((b) & 0x0F)
    def loNibble(b: Int): Int = b & 0x0f
  }

}
