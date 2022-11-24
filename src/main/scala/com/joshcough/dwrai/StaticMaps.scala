package com.joshcough.dwrai

import com.joshcough.dwrai.Bytes.{hiNibble, loNibble}
import com.joshcough.dwrai.Locations.ImportantLocationType
import ImportantLocationType._
import com.joshcough.dwrai.MapId._

object Warps {
  //    case class Warp(src: Point, dest: Point) {
  //      def swap: Warp = Warp(dest, src)
  //      override def equals(o: Any): Boolean = o match {
  //        case w: Warp =>
  //          (src.equals(w.src) && dest.equals(w.dest)) || src.equals(w.dest) && dest.equals(w.src)
  //        case _ => false
  //      }
  //    }

  val STATIC_WARPS: Map[Point, Point] = List(
    Point(CharlockId, 10, 1)          -> Point(CharlockCaveLv1Id, 9, 0),
    Point(CharlockId, 4, 14)          -> Point(CharlockCaveLv1Id, 8, 13),
    Point(CharlockId, 15, 14)         -> Point(CharlockCaveLv1Id, 17, 15),
    Point(TantegelThroneRoomId, 1, 8) -> Point(TantegelId, 1, 7),
    Point(TantegelThroneRoomId, 8, 8) -> Point(TantegelId, 7, 7),
    // 9 = garinham, 24 = GarinsGrave -- this one has to be discovered, like the basement
    // -> Point(9, 19, 0) -> Point(24, 6, 11))
    Point(CharlockCaveLv1Id, 15, 1)  -> Point(CharlockCaveLv2Id, 8, 0),
    Point(CharlockCaveLv1Id, 13, 7)  -> Point(CharlockCaveLv2Id, 4, 4),
    Point(CharlockCaveLv1Id, 19, 7)  -> Point(CharlockCaveLv2Id, 9, 8),
    Point(CharlockCaveLv1Id, 14, 9)  -> Point(CharlockCaveLv2Id, 8, 9),
    Point(CharlockCaveLv1Id, 2, 14)  -> Point(CharlockCaveLv2Id, 0, 1),
    Point(CharlockCaveLv1Id, 2, 4)   -> Point(CharlockCaveLv2Id, 0, 0),
    Point(CharlockCaveLv1Id, 8, 19)  -> Point(CharlockCaveLv2Id, 5, 0),
    Point(CharlockCaveLv2Id, 3, 0)   -> Point(CharlockCaveLv3Id, 7, 0),
    Point(CharlockCaveLv2Id, 9, 1)   -> Point(CharlockCaveLv3Id, 2, 2),
    Point(CharlockCaveLv2Id, 0, 8)   -> Point(CharlockCaveLv3Id, 5, 4),
    Point(CharlockCaveLv2Id, 1, 9)   -> Point(CharlockCaveLv3Id, 0, 9),
    Point(CharlockCaveLv3Id, 1, 6)   -> Point(CharlockCaveLv4Id, 0, 9),
    Point(CharlockCaveLv3Id, 7, 7)   -> Point(CharlockCaveLv4Id, 7, 7),
    Point(CharlockCaveLv4Id, 2, 2)   -> Point(CharlockCaveLv5Id, 9, 0),
    Point(CharlockCaveLv4Id, 8, 1)   -> Point(CharlockCaveLv5Id, 4, 0),
    Point(CharlockCaveLv5Id, 5, 5)   -> Point(CharlockCaveLv6Id, 0, 0),
    Point(CharlockCaveLv5Id, 0, 0)   -> Point(CharlockCaveLv6Id, 0, 6),
    Point(CharlockCaveLv6Id, 9, 0)   -> Point(CharlockCaveLv6Id, 0, 0),
    Point(CharlockCaveLv6Id, 9, 6)   -> Point(CharlockThroneRoomId, 10, 29),
    Point(MountainCaveLv1Id, 0, 0)   -> Point(MountainCaveLv2Id, 0, 0),
    Point(MountainCaveLv1Id, 6, 5)   -> Point(MountainCaveLv2Id, 6, 5),
    Point(MountainCaveLv1Id, 12, 12) -> Point(MountainCaveLv2Id, 12, 12),
    Point(GarinsGraveLv1Id, 1, 18)   -> Point(GarinsGraveLv2Id, 11, 2),
    Point(GarinsGraveLv2Id, 1, 1)    -> Point(GarinsGraveLv3Id, 1, 16),
    Point(GarinsGraveLv2Id, 12, 1)   -> Point(GarinsGraveLv3Id, 18, 1),
    Point(GarinsGraveLv2Id, 5, 6)    -> Point(GarinsGraveLv3Id, 6, 11),
    Point(GarinsGraveLv2Id, 1, 10)   -> Point(GarinsGraveLv3Id, 2, 17),
    Point(GarinsGraveLv2Id, 12, 10)  -> Point(GarinsGraveLv3Id, 18, 13),
    Point(GarinsGraveLv3Id, 9, 5)    -> Point(GarinsGraveLv4Id, 0, 4),
    Point(GarinsGraveLv3Id, 10, 9)   -> Point(GarinsGraveLv4Id, 5, 4),
    Point(ErdricksCaveLv1Id, 9, 9)   -> Point(ErdricksCaveLv2Id, 8, 9)
  ).flatMap(t => List(t, t.swap)).toMap
}

case class Width(value: Int)
case class Height(value: Int)
case class MapSize(width: Width, height: Height)

trait MapType
object MapType {
  case object Town    extends MapType
  case object Dungeon extends MapType
  case object Both    extends MapType
  case object Other   extends MapType
}

// 'to' is the source, and `warpRomAddr` contains the address to read the `from`
// also seen Entrance right below.
case class EntranceMetadata(to: Point, warpRomAddr: Address, entranceType: ImportantLocationType)

case class StaticMapMetadata(id: MapId,
                             name: String,
                             mapType: MapType,
                             size: MapSize,
                             romAddress: Address,
                             entrances: List[EntranceMetadata],
                             childrenIds: List[MapId] = List(),
                             immobileNpcs: List[(Int, Int)] = List()
)

object StaticMapMetadata {

  val STATIC_MAP_METADATA: Map[MapId, StaticMapMetadata] = Map(
    CharlockId -> StaticMapMetadata(
      CharlockId,
      "Charlock",
      MapType.Both,
      MapSize(Width(20), Height(20)),
      romAddress = Address(0xc0),
      entrances = List(EntranceMetadata(Point(CharlockId, 10, 19), Address(0xf3ea), CHARLOCK))
    ),
    HauksnessId -> StaticMapMetadata(
      HauksnessId,
      "Hauksness",
      MapType.Both,
      MapSize(Width(20), Height(20)),
      romAddress = Address(0x188),
      entrances = List(EntranceMetadata(Point(HauksnessId, 0, 10), Address(0xf3f6), TOWN))
    ),
    TantegelId -> StaticMapMetadata(
      TantegelId,
      "Tantegel",
      MapType.Town,
      MapSize(Width(30), Height(30)),
      romAddress = Address(0x250),
      entrances = List(EntranceMetadata(Point(TantegelId, 11, 29), Address(0xf3e4), TANTEGEL)),
      immobileNpcs = List((2, 8), (8, 6), (8, 8), (27, 5), (26, 15), (9, 27), (12, 27), (15, 20))
    ),
    TantegelThroneRoomId -> StaticMapMetadata(
      TantegelThroneRoomId,
      "Tantegel Throne Room",
      MapType.Other,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0x412),
      entrances = List(),
      immobileNpcs = List((3, 3), (3, 6), (5, 6))
    ),
    CharlockThroneRoomId -> StaticMapMetadata(
      CharlockThroneRoomId,
      "Charlock Throne Room",
      MapType.Dungeon,
      MapSize(Width(30), Height(30)),
      romAddress = Address(0x444),
      entrances = List()
    ),
    KolId -> StaticMapMetadata(
      KolId,
      "Kol",
      MapType.Town,
      MapSize(Width(24), Height(24)),
      romAddress = Address(0x606),
      entrances = List(EntranceMetadata(Point(KolId, 19, 23), Address(0xf3de), TOWN))
    ),
    BrecconaryId -> StaticMapMetadata(
      BrecconaryId,
      "Brecconary",
      MapType.Town,
      MapSize(Width(30), Height(30)),
      romAddress = Address(0x726),
      entrances = List(EntranceMetadata(Point(BrecconaryId, 0, 15), Address(0xf3e1), TOWN)),
      immobileNpcs = List((1, 13), (4, 7), (10, 26), (20, 23), (28, 1))
    ),
    GarinhamId -> StaticMapMetadata(
      GarinhamId,
      "Garinham",
      MapType.Town,
      MapSize(Width(20), Height(20)),
      romAddress = Address(0xaaa),
      entrances = List(EntranceMetadata(Point(GarinhamId, 0, 14), Address(0xf3d8), TOWN)),
      immobileNpcs = List((1, 13), (4, 7), (10, 26), (20, 23), (28, 1))
    ),
    CantlinId -> StaticMapMetadata(
      CantlinId,
      "Cantlin",
      MapType.Town,
      MapSize(Width(30), Height(30)),
      romAddress = Address(0x8e8),
      entrances = List(EntranceMetadata(Point(CantlinId, 5, 15), Address(0xf3f9), TOWN)),
      immobileNpcs = List((0, 0))
    ),
    RimuldarId -> StaticMapMetadata(
      RimuldarId,
      "Rimuldar",
      MapType.Town,
      MapSize(Width(30), Height(30)),
      romAddress = Address(0xb72),
      entrances = List(EntranceMetadata(Point(RimuldarId, 29, 14), Address(0xf3f3), TOWN)),
      immobileNpcs = List((2, 4), (27, 0))
    ),
    TantegelBasementId -> StaticMapMetadata(
      TantegelBasementId,
      "Tantegel Basement",
      MapType.Other,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xd34),
      entrances = List(
        EntranceMetadata(
          Point(TantegelBasementId, 0, 4),
          Address(0xf40b),
          CAVE
        )
      )
    ),
    NorthernShrineId -> StaticMapMetadata(
      NorthernShrineId,
      "Northern Shrine",
      MapType.Other,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xd66),
      entrances = List(EntranceMetadata(Point(NorthernShrineId, 4, 9), Address(0xf3db), CAVE))
    ),
    NorthernShrineId -> StaticMapMetadata(
      NorthernShrineId,
      "Northern Shrine",
      MapType.Other,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xd66),
      entrances = List(EntranceMetadata(Point(NorthernShrineId, 4, 9), Address(0xf3db), CAVE))
    ),
    SouthernShrineId -> StaticMapMetadata(
      SouthernShrineId,
      "Southern Shrine",
      MapType.Other,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xd98),
      entrances = List(EntranceMetadata(Point(SouthernShrineId, 0, 4), Address(0xf3fc), CAVE))
    ),
    CharlockCaveLv1Id -> StaticMapMetadata(
      CharlockCaveLv1Id,
      "Charlock Cave Lv 1",
      MapType.Dungeon,
      MapSize(Width(20), Height(20)),
      romAddress = Address(0xdca),
      entrances = List()
    ),
    CharlockCaveLv2Id -> StaticMapMetadata(
      CharlockCaveLv2Id,
      "Charlock Cave Lv 2",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xe92),
      entrances = List()
    ),
    CharlockCaveLv3Id -> StaticMapMetadata(
      CharlockCaveLv3Id,
      "Charlock Cave Lv 3",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xec4),
      entrances = List()
    ),
    CharlockCaveLv4Id -> StaticMapMetadata(
      CharlockCaveLv4Id,
      "Charlock Cave Lv 4",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xef6),
      entrances = List()
    ),
    CharlockCaveLv5Id -> StaticMapMetadata(
      CharlockCaveLv5Id,
      "Charlock Cave Lv 5",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xf28),
      entrances = List()
    ),
    CharlockCaveLv6Id -> StaticMapMetadata(
      CharlockCaveLv6Id,
      "Charlock Cave Lv 6",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0xf5a),
      entrances = List()
    ),
    SwampCaveId -> StaticMapMetadata(
      SwampCaveId,
      "Swamp Cave",
      MapType.Dungeon,
      MapSize(Width(6), Height(30)),
      romAddress = Address(0xf8c),
      entrances = List(
        EntranceMetadata(Point(SwampCaveId, 0, 0), Address(0xf3e7), CAVE),
        EntranceMetadata(Point(SwampCaveId, 0, 29), Address(0xf3ed), CAVE)
      )
    ),
    MountainCaveLv1Id -> StaticMapMetadata(
      MountainCaveLv1Id,
      "Mountain Cave",
      MapType.Dungeon,
      MapSize(Width(14), Height(14)),
      romAddress = Address(0xfe6),
      entrances = List(
        EntranceMetadata(
          Point(MountainCaveLv1Id, 0, 7),
          Address(0xf3f0),
          CAVE
        )
      )
    ),
    MountainCaveLv2Id -> StaticMapMetadata(
      MountainCaveLv2Id,
      "Mountain Cave Lv 2",
      MapType.Dungeon,
      MapSize(Width(14), Height(14)),
      romAddress = Address(0x1048),
      entrances = List()
    ),
    GarinsGraveLv1Id -> StaticMapMetadata(
      GarinsGraveLv1Id,
      "Garin's Grave Lv 1",
      MapType.Dungeon,
      MapSize(Width(20), Height(20)),
      romAddress = Address(0x10aa),
      entrances = List(
        EntranceMetadata(
          Point(GarinsGraveLv1Id, 6, 11),
          Address(0xf411),
          CAVE
        )
      )
    ),
    GarinsGraveLv2Id -> StaticMapMetadata(
      GarinsGraveLv2Id,
      "Garin's Grave Lv 2",
      MapType.Dungeon,
      MapSize(Width(14), Height(12)),
      romAddress = Address(0x126c),
      entrances = List()
    ),
    GarinsGraveLv3Id -> StaticMapMetadata(
      GarinsGraveLv3Id,
      "Garin's Grave Lv 3",
      MapType.Dungeon,
      MapSize(Width(20), Height(20)),
      romAddress = Address(0x1172),
      entrances = List()
    ),
    GarinsGraveLv4Id -> StaticMapMetadata(
      GarinsGraveLv4Id,
      "Garin's Grave Lv 4",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0x123a),
      entrances = List()
    ),
    ErdricksCaveLv1Id -> StaticMapMetadata(
      ErdricksCaveLv1Id,
      "Erdrick's Cave",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0x12c0),
      entrances = List(
        EntranceMetadata(
          Point(ErdricksCaveLv1Id, 0, 0),
          Address(0xf3ff),
          CAVE
        )
      )
    ),
    ErdricksCaveLv2Id -> StaticMapMetadata(
      ErdricksCaveLv2Id,
      "Erdrick's Cave Lv 2",
      MapType.Dungeon,
      MapSize(Width(10), Height(10)),
      romAddress = Address(0x12f2),
      entrances = List()
    )
  )
}

object Entrance {
  def readFromRom(memory: Memory, m: EntranceMetadata): Entrance = {
    val from = Point(
      MapId(memory.readROM(m.warpRomAddr - 16)),
      memory.readROM(m.warpRomAddr + 1 - 16),
      memory.readROM(m.warpRomAddr + 2 - 16)
    )
    Entrance(from, m.to, m.entranceType)
  }
}

// NOTE: 'from' is the "overworld"
// (""s because the entrance could be to a basement and so from might actually be tantegel or garinham)
case class Entrance(from: Point, to: Point, entranceType: ImportantLocationType)

// TODO: i think i want to get rid of the notion of walkable with keys.
// instead, just add the neighbors for a door tile to the graph always
// (and the warps too actually)
// and when calculating the shortest path, just like...
// dont follow paths if you dont have keys.
// (or dont follow a path if you haven't actually discovered it yet)
case class StaticMapTile(tileId: Int,
                         name: String,
                         walkable: Boolean,
                         walkableWithKeys_ : Option[Boolean] = None
) {
  val walkableWithKeys: Boolean = walkableWithKeys_.getOrElse(walkable)
  // i think 1 here is ok. if its not walkable it wont end up in the graph at all
  // the only small problem is charlock has some swamp and desert, but... they aren't really
  // avoidable anyway, and so... it should just be fine to always use 1.
  val weight = 1
  def isDoor = tileId == 11
}

object StaticMapTile {
  // zzz lets make two different tile types maybe?
  val NON_DUNGEON_TILES: Seq[StaticMapTile] = Vector(
    StaticMapTile(0, "Grass", walkable = true),
    StaticMapTile(1, "Sand", walkable = true),
    StaticMapTile(2, "Water", walkable = false),
    StaticMapTile(3, "Chest", walkable = true),
    StaticMapTile(4, "Stone", walkable = false),
    StaticMapTile(5, "Up", walkable = true),
    StaticMapTile(6, "Brick", walkable = true),
    StaticMapTile(7, "Down", walkable = true),
    StaticMapTile(8, "Trees", walkable = true),
    StaticMapTile(9, "Swamp", walkable = true),
    StaticMapTile(10, "Field", walkable = true),
    StaticMapTile(11, "Door", walkable = false, Some(true)), // walkableWithKeys
    StaticMapTile(12, "Weapon", walkable = false),
    StaticMapTile(13, "Inn", walkable = false),
    StaticMapTile(14, "Bridge", walkable = true),
    StaticMapTile(15, "Tile", walkable = false)
  )

  val DUNGEON_TILES: Seq[StaticMapTile] = Vector(
    StaticMapTile(0, "Stone", walkable = false),
    StaticMapTile(1, "Up", walkable = true),
    StaticMapTile(2, "Brick", walkable = true),
    StaticMapTile(3, "Down", walkable = true),
    StaticMapTile(4, "Chest", walkable = true),
    StaticMapTile(5, "Door", walkable = false, Some(true)), // walkableWithKeys
    // in swamp cave, we get id six where the princess is. its the only 6 we get in any dungeon.
    StaticMapTile(6, "Brick", walkable = true)
  )
}

object StaticMap {
//    def apply(metadata: StaticMapMetadata, rows: IndexedSeq[IndexedSeq[Int]], allWarps: List[Warps.Warp]): StaticMap = {
////      val warps = getWarpsForMap(mapId, allWarps)
////      val immobileScps = getImmobileNPCsForMap(mapId)
//    }

  def readAllStaticMapsFromRom(memory: Memory): Map[MapId, StaticMap] =
    StaticMapMetadata.STATIC_MAP_METADATA.view.mapValues(readStaticMapFromRom(memory, _)).toMap

  def readStaticMapFromRom(memory: Memory, mapId: MapId): StaticMap =
    readStaticMapFromRom(memory, StaticMapMetadata.STATIC_MAP_METADATA(mapId))

  def readStaticMapFromRom(memory: Memory, mapMetadata: StaticMapMetadata): StaticMap = {
    val tileSet =
      if (mapMetadata.id.value < 15) StaticMapTile.NON_DUNGEON_TILES
      else StaticMapTile.DUNGEON_TILES

    // returns the tile id for the given (x,y) for the current map
    def readTileIdAt(x: Int, y: Int): StaticMapTile = {
      val offset = (y * mapMetadata.size.width.value) + x
      val addr   = Address(mapMetadata.romAddress.value - 16 + math.floor(offset / 2).toInt)
      val value  = memory.readROM(addr)
      val tile   = if (offset % 2 == 0) hiNibble(value) else loNibble(value)
      tileSet(if (mapMetadata.id.value < 12) tile else tile & 7)
    }

    // returns a two dimensional grid of tile ids for the current map
    val tiles: IndexedSeq[IndexedSeq[StaticMapTile]] = {
      val h = mapMetadata.size.height.value
      val w = mapMetadata.size.width.value
      Range(0, h).map(y => Range(0, w).map(x => readTileIdAt(x, y)))
    }

    val entrances = mapMetadata.entrances.map(Entrance.readFromRom(memory, _))

    StaticMap(mapMetadata, entrances, tiles)
  }

}

case class StaticMap(metadata: StaticMapMetadata,
                     entrances: List[Entrance],
                     rows: IndexedSeq[IndexedSeq[StaticMapTile]]
) {
  def mapId: MapId                  = metadata.id
  def mapName: String               = metadata.name
  def mapType: MapType              = metadata.mapType
  def width: Width                  = metadata.size.width
  def height: Height                = metadata.size.height
  def immobileScps: Set[(Int, Int)] = metadata.immobileNpcs.toSet
  // def warps = getWarpsForMap(mapId, allWarps)

  def quickPrint: String =
    mapName + "\n" + rows.map(_.map(_.name).mkString("|")).mkString("\n")

  def getTileAt(x: Int, y: Int): StaticMapTile = rows(y)(x)

  // we can walk on a square if theres no immobile npc there
  // and if the tile is inherently walkable (like grass, or whatever)
  def isWalkableAt(x: Int, y: Int): Boolean =
    !immobileScps.contains((x, y)) && getTileAt(x, y).walkable
}

/*

TantegelEntrance       = Point(Tantegel, 11, 29)
TantegelBasementStairs = Point(Tantegel, 29, 29)
SwampNorthEntrance     = Point(SwampCave, 0, 0)
SwampSouthEntrance     = Point(SwampCave, 0, 29)

function getWarpsForMap(mapId, allWarps)
  local res = {}
  local warpsForMapId = list.filter(allWarps, function(w)
    return w.src.mapId == mapId
  end)
  for _,w in ipairs(warpsForMapId) do
    if res[w.src.x] == nil then res[w.src.x] = {} end
    if res[w.src.x][w.src.y] == nil then res[w.src.x][w.src.y] = {} end
    table.insert(res[w.src.x][w.src.y], w.dest)
  end
  return res
end


function StaticMapMetadata:readEntranceCoordinates(memory)
  if self.entrances == nil then return nil end
  local res = list.map(self.entrances, function(e) return e:convertToEntrance(memory) end)
  return res
end

function getAllEntranceCoordinates(memory)
  local res = {}
  for i, meta in pairs(STATIC_MAP_METADATA) do
    res[i] = meta:readEntranceCoordinates(memory)
  end
  return res
end

mockEntranceCoordinates = {
   [2]= {Entrance(Point( 2, 19, 10), Point(1, 54,  87), CHARLOCK)},
   [3]= {Entrance(Point( 3, 10,  0), Point(1, 29, 112), Town)},
   [4]= {Entrance(Point( 4, 29, 11), Point(1, 85,  90), TANTEGEL)},
   [7]= {Entrance(Point( 7, 23, 19), Point(1, 55,  67), Town)},
   [8]= {Entrance(Point( 8, 15,  0), Point(1, 98,  98), Town)},
   [9]= {Entrance(Point( 9, 14,  0), Point(1, 74, 108), Town)},
   [10]={Entrance(Point(10, 15,  5), Point(1, 36,  44), Town)},
   [11]={Entrance(Point(11, 14, 29), Point(1, 74, 110), Town)},
   [12]={Entrance(Point(12,  4,  0), Point(4, 29,  29), CAVE)},
   [13]={Entrance(Point(13,  9,  4), Point(1, 58, 106), CAVE)},
   [14]={Entrance(Point(14,  4,  0), Point(1, 82,   8), CAVE)},
   [21]={Entrance(Point(21,  0,  0), Point(1, 90,  78), CAVE), Entrance(Point(21, 29, 0), Point(1, 93, 63), CAVE)},
   [22]={Entrance(Point(22,  7,  0), Point(1, 96,  99), CAVE)},
   [24]={Entrance(Point(24, 11,  6), Point(9, 19,   0), CAVE)},
   [28]={Entrance(Point(28,  0,  0), Point(1, 86,  84), CAVE)}
  }

function StaticMap:resetWarps (allWarps)
  self.warps = getWarpsForMap(self.mapId, allWarps)
end

function StaticMap:setTileAt(x, y, newTileId)
  self.rows[y][x] = newTileId
end

function StaticMap:markSeenByPlayer(allStaticMaps)
  log.debug("now seen by player: ", self.mapName)
  self.seenByPlayer = true
  for _,childId in pairs(self:childrenIds()) do
    log.debug("now seen by player: ", allStaticMaps[childId].mapName)
    allStaticMaps[childId].seenByPlayer = true
  end
end

PRINT_TILE_NAME    = 1
PRINT_TILE_NO_KEYS = 2
PRINT_TILE_KEYS    = 3

function StaticMap:__tostring (printStrat)
  function printTile(t)
    if printStrat == PRINT_TILE_NAME or printStrat == nil then return t.name
    elseif printStrat == PRINT_TILE_NO_KEYS then return t.walkable and "O" or " "
    else return (t.walkableWithKeys or t.walkable) and "O" or " "
    end
  end

  local tileSet = self:getTileSet()
  local res = ""
  for y = 0,self.height-1 do
    local row = ""
    for x = 0,self.width-1 do
      local t = tileSet[self.rows[y][x]]
      row = row .. " | " .. (printTile(t))
    end
    res = res .. row .. " |\n"
  end
  return self.mapName .. "\n" .. res
end

function StaticMap:writeTileNamesToFile (file)
  file:write(self:__tostring() .. "\n")
end

MAP_DIRECTORY = "/Users/joshcough/work/dwrandomizer_ai/maps/"
STATIC_MAPS_FILE = MAP_DIRECTORY .. "static_maps.txt"

function StaticMap:saveIdsToFile ()
  local mapFileName = MAP_DIRECTORY .. self.mapName
  table.save(self.rows, mapFileName)
end

function StaticMap:saveGraphToFile ()
  local graphNoKeysFileName = MAP_DIRECTORY .. self.mapName .. ".graph"
  local graphWithKeysFileName = MAP_DIRECTORY .. self.mapName .. ".with_keys.graph"
  table.save(self:mkGraph(false), graphNoKeysFileName)
  table.save(self:mkGraph(true), graphWithKeysFileName)
end

-- TODO: i dont really think the next two functions work anymore.
function quickPrintGraph(mapId, allWarps)
  log.debug(loadStaticMapFromFile(mapId, allWarps):mkGraph(false))
  log.debug(loadStaticMapFromFile(mapId, allWarps):mkGraph(true))
end

function loadStaticMapFromFile (mapId, allWarps)
  if mapId < 2 then return nil end
  local mapData = STATIC_MAP_METADATA[mapId]
  local mapName = mapData.name
  local mapFileName = MAP_DIRECTORY .. mapName
  -- TODO: these overworld coordinates are wrong. we definitely have a problem
  -- reading from files now.
  return StaticMap(mapId, mapName, mapData.mapType, mapData.overworldCoordinates,
                   mapData.size.width, mapData.size.height, table.load(mapFileName), allWarps)
end

function readAllStaticMaps(memory, allWarps)
  local res = {}
  for i = 2, 29 do
    res[i] = readStaticMapFromRom(memory, i, allWarps)
  end
  return res
end

function saveStaticMaps(memory, allWarps)
  local file = io.open(STATIC_MAPS_FILE, "w")
  local maps = readAllStaticMaps(memory, allWarps)
  for i = 2, 29 do
    maps[i]:writeTileNamesToFile(file)
    maps[i]:saveIdsToFile()
    maps[i]:saveGraphToFile()
  end
  file:close()
end
 */

//  function StaticMap:childrenIds()
//  if     self.mapId ==  CharlockId then return {CharlockThroneRoomId,CharlockCaveLv1Id,CharlockCaveLv1Id,CharlockCaveLv1Id,CharlockCaveLv1Id,CharlockCaveLv1Id,CharlockCaveLv1Id}
//  elseif self.mapId ==  TantegelId then return {TantegelThroneRoomId}
//  elseif self.mapId ==  TantegelThroneRoomId then return {TantegelId}
//  elseif self.mapId == MountainCaveLv1Id then return {MountainCaveLv1Id}
//  elseif self.mapId == GarinsGraveLv1Id then return {GarinsGraveLv2Id,GarinsGraveLv3Id,GarinsGraveLv4Id}
//  elseif self.mapId == ErdricksCaveLv1Id then return {ErdricksCaveLv2Id}
