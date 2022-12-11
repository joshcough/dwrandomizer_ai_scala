package com.joshcough.dwrai

import cats.effect.IO
import cats.implicits._
import com.joshcough.dwrai.Bytes._
import com.joshcough.dwrai.Overworld.OverworldId

case class Overworld(tiles: IndexedSeq[IndexedSeq[Overworld.Tile]]) {

  def getTileAt(x: Int, y: Int): Overworld.Tile = tiles(y)(x)

  def getVisibleOverworldGrid(currentLoc: Point): IndexedSeq[IndexedSeq[(Point, Overworld.Tile)]] =
    if (currentLoc.mapId == OverworldId) getVisibleOverworldGrid(currentLoc.x, currentLoc.y)
    else
      throw new RuntimeException(
        "tried to get the visible overworld grid when we aren't on the overworld"
      )

  def getVisibleOverworldGrid(currentX: Int,
                              currentY: Int
  ): IndexedSeq[IndexedSeq[(Point, Overworld.Tile)]] = {
    val upperLeftX   = math.max(0, currentX - 8)
    val upperLeftY   = math.max(0, currentY - 7)
    val bottomRightX = math.min(119, currentX + 7)
    val bottomRightY = math.min(119, currentY + 7)
    Range.inclusive(upperLeftY, bottomRightY).map { y =>
      Range.inclusive(upperLeftX, bottomRightX).map { x =>
        (Point(OverworldId, x, y), tiles(y)(x))
      }
    }
  }
}

object Overworld {

  val OverworldId: MapId = MapId(1)

  sealed trait Tile { def walkable: Boolean = true }
  object Grass  extends Tile { override def toString: String = "Grass"  }
  object Desert extends Tile { override def toString: String = "Desert" }
  object Hills  extends Tile { override def toString: String = "Hills"  }
  object Mountain extends Tile {
    override def toString: String  = "Mountain"
    override def walkable: Boolean = false
  }
  object Water extends Tile {
    override def toString: String  = "Water"
    override def walkable: Boolean = false
  }
  object Stone  extends Tile { override def toString: String = "Stone"  }
  object Forest extends Tile { override def toString: String = "Forest" }
  object Swamp  extends Tile { override def toString: String = "Swamp"  }
  object Town   extends Tile { override def toString: String = "Town"   }
  object Cave   extends Tile { override def toString: String = "Cave"   }
  object Castle extends Tile { override def toString: String = "Castle" }
  object Bridge extends Tile { override def toString: String = "Bridge" }
  object Stairs extends Tile { override def toString: String = "Stairs" }

  val OVERWORLD_TILES: IndexedSeq[Tile] =
    IndexedSeq(
      Grass,    // 0
      Desert,   // 1
      Hills,    // 2
      Mountain, // 3
      Water,    // 4
      Stone,    // 5
      Forest,   // 6
      Swamp,    // 7
      Town,     // 8
      Cave,     // 9
      Castle,   // 10
      Bridge,   // 11
      Stairs    // 12
    )

  // This implementation that reads from NES memory basically.
  // We could have an implementation that does it differently
  // such as reading an overworld from a file, or just generating one
  // randomly or whatever... but for now this is all we have.
  def readOverworldFromROM(memory: Memory): IO[Overworld] = {

    // 1D6D - 2662  | Overworld map          | RLE encoded, 1st nibble is tile, 2nd how many - 1
    // 2663 - 26DA  | Overworld map pointers | 16 bits each - address of each row of the map. (value - 0x8000 + 16)
    def decodeOverworldPointer(p: Int): IO[RomAddress] = {
      val pointerAddr = RomAddress((0x2663 - 16) + (p * 2))
      // mcgrew: Keep in mind they are in little endian format.
      // mcgrew: So it's LOW_BYTE, HIGH_BYTE
      // Also keep in mind they are addresses as the NES sees them, so to get the address in
      // ROM you'll need to subtract 0x8000
      for {
        lowByte  <- memory.readROM(pointerAddr)
        highByte <- memory.readROM(pointerAddr + 1)
      } yield RomAddress((highByte * 256) + lowByte - 0x8000)
    }

    def getOverworldTileRow(overworldPointer: RomAddress): IO[List[Tile]] = {
      def loop(totalCount: Int, currentAddr: RomAddress): IO[List[Tile]] =
        if (totalCount < 120) for {
          tileIdAndCount <- memory.readROM(currentAddr)
          tileId = hiNibble(tileIdAndCount)
          count  = loNibble(tileIdAndCount) + 1
          tiles  = Range(0, count).toList.map(_ => OVERWORLD_TILES(tileId))
          rest <- loop(totalCount + count, currentAddr + 1)
        } yield tiles ++ rest
        else List().pure[IO]
      for {
        res <- loop(0, overworldPointer)
      } yield res.take(120)
    }

    for {
      overworldPointers <- Range(0, 120).toList.traverse(decodeOverworldPointer)
      res               <- overworldPointers.traverse(getOverworldTileRow)
    } yield Overworld(res.toIndexedSeq.map(_.toIndexedSeq))
  }

}
