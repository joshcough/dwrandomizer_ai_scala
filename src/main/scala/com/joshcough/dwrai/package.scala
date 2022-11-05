package com.joshcough

package object dwrai {

  case class MapId(id: Int)

  case class Point(mapId: MapId, x: Int, y: Int)

}
