package com.joshcough.dwrai

import com.joshcough.dwrai.Bytes._

object Overworld {

  sealed trait OverworldTile
  object Grass    extends OverworldTile { override def toString: String = "Grass"    } // 0
  object Desert   extends OverworldTile { override def toString: String = "Desert"   } // 1
  object Hills    extends OverworldTile { override def toString: String = "Hills"    } // 2
  object Mountain extends OverworldTile { override def toString: String = "Mountain" } // 3
  object Water    extends OverworldTile { override def toString: String = "Water"    } // 4
  object Stone    extends OverworldTile { override def toString: String = "Stone"    } // 5
  object Forest   extends OverworldTile { override def toString: String = "Forest"   } // 6
  object Swamp    extends OverworldTile { override def toString: String = "Swamp"    } // 7
  object Town     extends OverworldTile { override def toString: String = "Town"     } // 8
  object Cave     extends OverworldTile { override def toString: String = "Cave"     } // 9
  object Castle   extends OverworldTile { override def toString: String = "Castle"   } // 10
  object Bridge   extends OverworldTile { override def toString: String = "Bridge"   } // 11
  object Stairs   extends OverworldTile { override def toString: String = "Stairs"   } // 12

  val OVERWORLD_TILES: IndexedSeq[OverworldTile] =
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
  def readOverworldFromROM(memory: Memory): IndexedSeq[IndexedSeq[OverworldTile]] = {

    // 1D6D - 2662  | Overworld map          | RLE encoded, 1st nibble is tile, 2nd how many - 1
    // 2663 - 26DA  | Overworld map pointers | 16 bits each - address of each row of the map. (value - 0x8000 + 16)
    def decodeOverworldPointer(p: Int): Address = {
      val pointerAddr = Address((0x2663 - 16) + (p * 2))
      // mcgrew: Keep in mind they are in little endian format.
      // mcgrew: So it's LOW_BYTE, HIGH_BYTE
      // Also keep in mind they are addresses as the NES sees them, so to get the address in
      // ROM you'll need to subtract 0x8000
      val lowByte  = memory.readROM(pointerAddr)
      val highByte = memory.readROM(pointerAddr + 1)
      Address((highByte * 256) + lowByte - 0x8000)
    }

    def getOverworldTileRow(overworldPointer: Address): IndexedSeq[OverworldTile] = {
      IndexedSeq
        .unfold((0, overworldPointer)) { case (totalCount, currentAddr) =>
          if (totalCount < 120) {
            val tileId = hiNibble(memory.readROM(currentAddr))
            val count  = loNibble(memory.readROM(currentAddr)) + 1
            Some(
              (
                Range(0, count).map(_ => OVERWORLD_TILES(tileId)),
                (totalCount + count, currentAddr + 1)
              )
            )
          } else None
        }
        .flatten
    }

    def getOverworldPointers: IndexedSeq[Address] = Range(0, 120).map(decodeOverworldPointer)

    getOverworldPointers.map(getOverworldTileRow)
  }

}
