package com.joshcough

package object dwrai {

  trait BoxedInt{ val value: Int }

  case class MapId(value: Int) extends BoxedInt

  case class Point(mapId: MapId, x: Int, y: Int)

  object Bytes {
    // HI_NIBBLE(b) (((b) >> 4) & 0x0F)
    def hiNibble(b: Int): Int = math.floor(b/16).toInt & 0x0F
    // LO_NIBBLE(b) (((b) & 0x0F)
    def loNibble(b: Int): Int = b & 0x0F
  }

}
