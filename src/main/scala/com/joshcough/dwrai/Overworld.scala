package com.joshcough.dwrai

import cats.effect.IO
import cats.implicits._
import com.joshcough.dwrai.Bytes._

case class Overworld(tiles: IndexedSeq[IndexedSeq[Overworld.Tile]])

object Overworld {

  val OverworldId: MapId = MapId(1)

  sealed trait Tile
  object Grass    extends Tile { override def toString: String = "Grass"    } // 0
  object Desert   extends Tile { override def toString: String = "Desert"   } // 1
  object Hills    extends Tile { override def toString: String = "Hills"    } // 2
  object Mountain extends Tile { override def toString: String = "Mountain" } // 3
  object Water    extends Tile { override def toString: String = "Water"    } // 4
  object Stone    extends Tile { override def toString: String = "Stone"    } // 5
  object Forest   extends Tile { override def toString: String = "Forest"   } // 6
  object Swamp    extends Tile { override def toString: String = "Swamp"    } // 7
  object Town     extends Tile { override def toString: String = "Town"     } // 8
  object Cave     extends Tile { override def toString: String = "Cave"     } // 9
  object Castle   extends Tile { override def toString: String = "Castle"   } // 10
  object Bridge   extends Tile { override def toString: String = "Bridge"   } // 11
  object Stairs   extends Tile { override def toString: String = "Stairs"   } // 12

  val OVERWORLD_TILES: IndexedSeq[Tile] =
    IndexedSeq(
      Grass,
      Desert,
      Hills,
      Mountain,
      Water,
      Stone,
      Forest,
      Swamp,
      Town,
      Cave,
      Castle,
      Bridge,
      Stairs
    )

  // This implementation that reads from NES memory basically.
  // We could have an implementation that does it differently
  // such as reading an overworld from a file, or just generating one
  // randomly or whatever... but for now this is all we have.
  def readOverworldFromROM(memory: Memory): IO[Overworld] = {

    // 1D6D - 2662  | Overworld map          | RLE encoded, 1st nibble is tile, 2nd how many - 1
    // 2663 - 26DA  | Overworld map pointers | 16 bits each - address of each row of the map. (value - 0x8000 + 16)
    def decodeOverworldPointer(p: Int): IO[Address] = {
      val pointerAddr = Address((0x2663 - 16) + (p * 2))
      // mcgrew: Keep in mind they are in little endian format.
      // mcgrew: So it's LOW_BYTE, HIGH_BYTE
      // Also keep in mind they are addresses as the NES sees them, so to get the address in
      // ROM you'll need to subtract 0x8000
      for {
        lowByte  <- memory.readROM(pointerAddr)
        highByte <- memory.readROM(pointerAddr + 1)
      } yield Address((highByte * 256) + lowByte - 0x8000)
    }

    def getOverworldTileRow(overworldPointer: Address): IO[List[Tile]] = {
      def loop(totalCount: Int, currentAddr: Address): IO[List[Tile]] =
        if (totalCount < 120) for {
          tileIdAndCount <- memory.readROM(currentAddr)
          tileId = hiNibble(tileIdAndCount)
          count  = loNibble(tileIdAndCount) + 1
          tiles  = Range(0, count).toList.map(_ => OVERWORLD_TILES(tileId))
          rest <- loop(totalCount + count, currentAddr + 1)
        } yield tiles ++ rest
        else List().pure[IO]
      loop(0, overworldPointer)
    }

    for {
      overworldPointers <- Range(0, 120).toList.traverse(decodeOverworldPointer)
      res               <- overworldPointers.traverse(getOverworldTileRow)
    } yield Overworld(res.toIndexedSeq.map(_.toIndexedSeq))
  }

}
