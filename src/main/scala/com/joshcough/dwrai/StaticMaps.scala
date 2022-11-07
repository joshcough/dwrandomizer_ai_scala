package com.joshcough.dwrai

import com.joshcough.dwrai.Bytes.{hiNibble, loNibble}
import com.joshcough.dwrai.Locations.ImportantLocationType
import com.joshcough.dwrai.MapId._

class StaticMaps {

  object Warps {
      case class Warp(src: Point, dest: Point){
        def swap = Warp(dest, src)
        override def equals(o: Any): Boolean = o match {
          case w: Warp => (src.equals(w.src) && dest.equals(w.dest)) || src.equals(w.dest) && dest.equals(w.src)
          case _ => false
        }
      }

      val WARPS = List(
         Warp(Point(CharlockId, 10, 1),          Point(CharlockCaveLv1Id, 9, 0))
       , Warp(Point(CharlockId, 4, 14),          Point(CharlockCaveLv1Id, 8, 13))
       , Warp(Point(CharlockId, 15, 14),         Point(CharlockCaveLv1Id, 17, 15))
       , Warp(Point(TantegelThroneRoomId, 1, 8), Point(TantegelId, 1, 7))
       , Warp(Point(TantegelThroneRoomId, 8, 8), Point(TantegelId, 7, 7))
       // 9 = garinham, 24 = GarinsGrave -- this one has to be discovered, like the basement
       // , Warp(Point(9, 19, 0), Point(24, 6, 11))
       , Warp(Point(CharlockCaveLv1Id, 15,  1),  Point(CharlockCaveLv2Id,  8,  0))
       , Warp(Point(CharlockCaveLv1Id, 13,  7),  Point(CharlockCaveLv2Id,  4,  4))
       , Warp(Point(CharlockCaveLv1Id, 19,  7),  Point(CharlockCaveLv2Id,  9,  8))
       , Warp(Point(CharlockCaveLv1Id, 14,  9),  Point(CharlockCaveLv2Id,  8,  9))
       , Warp(Point(CharlockCaveLv1Id,  2, 14),  Point(CharlockCaveLv2Id,  0,  1))
       , Warp(Point(CharlockCaveLv1Id,  2,  4),  Point(CharlockCaveLv2Id,  0,  0))
       , Warp(Point(CharlockCaveLv1Id,  8, 19),  Point(CharlockCaveLv2Id,  5,  0))
       , Warp(Point(CharlockCaveLv2Id,  3,  0),  Point(CharlockCaveLv3Id,  7,  0))
       , Warp(Point(CharlockCaveLv2Id,  9,  1),  Point(CharlockCaveLv3Id,  2,  2))
       , Warp(Point(CharlockCaveLv2Id,  0,  8),  Point(CharlockCaveLv3Id,  5,  4))
       , Warp(Point(CharlockCaveLv2Id,  1,  9),  Point(CharlockCaveLv3Id,  0,  9))
       , Warp(Point(CharlockCaveLv3Id,  1,  6),  Point(CharlockCaveLv4Id,  0,  9))
       , Warp(Point(CharlockCaveLv3Id,  7,  7),  Point(CharlockCaveLv4Id,  7,  7))
       , Warp(Point(CharlockCaveLv4Id,  2,  2),  Point(CharlockCaveLv5Id,  9,  0))
       , Warp(Point(CharlockCaveLv4Id,  8,  1),  Point(CharlockCaveLv5Id,  4,  0))
       , Warp(Point(CharlockCaveLv5Id,  5,  5),  Point(CharlockCaveLv6Id,  0,  0))
       , Warp(Point(CharlockCaveLv5Id,  0,  0),  Point(CharlockCaveLv6Id,  0,  6))
       , Warp(Point(CharlockCaveLv6Id,  9,  0),  Point(CharlockCaveLv6Id,  0,  0))
       , Warp(Point(CharlockCaveLv6Id,  9,  6),  Point(CharlockThroneRoomId, 10, 29))
       , Warp(Point(MountainCaveLv1Id,  0,  0),  Point(MountainCaveLv2Id,  0,  0))
       , Warp(Point(MountainCaveLv1Id,  6,  5),  Point(MountainCaveLv2Id,  6,  5))
       , Warp(Point(MountainCaveLv1Id, 12, 12),  Point(MountainCaveLv2Id, 12, 12))
       , Warp(Point(GarinsGraveLv1Id,   1, 18),  Point(GarinsGraveLv2Id,  11,  2))
       , Warp(Point(GarinsGraveLv2Id,   1,  1),  Point(GarinsGraveLv3Id,   1, 16))
       , Warp(Point(GarinsGraveLv2Id,  12,  1),  Point(GarinsGraveLv3Id,  18,  1))
       , Warp(Point(GarinsGraveLv2Id,   5,  6),  Point(GarinsGraveLv3Id,   6, 11))
       , Warp(Point(GarinsGraveLv2Id,   1, 10),  Point(GarinsGraveLv3Id,   2, 17))
       , Warp(Point(GarinsGraveLv2Id,  12, 10),  Point(GarinsGraveLv3Id,  18, 13))
       , Warp(Point(GarinsGraveLv3Id,   9,  5),  Point(GarinsGraveLv4Id,   0,  4))
       , Warp(Point(GarinsGraveLv3Id,  10,  9),  Point(GarinsGraveLv4Id,   5,  4))
       , Warp(Point(ErdricksCaveLv1Id,  9,  9),  Point(ErdricksCaveLv2Id,  8,  9))
      )
  }

  case class Width (value: Int)
  case class Height (value: Int)
  case class MapSize (width: Width, height: Height)

  trait MapType
  object MapType {
    case object Town extends MapType
    case object Dungeon extends MapType
    case object Both extends MapType
    case object Other extends MapType
  }
  

  // 'to' is the source, and `warpRomAddr` contains the address to read the `from`
  // also seen Entrance right below.
  case class EntranceMetadata(to: Point, warpRomAddr: Address, entranceType: ImportantLocationType)

  case class StaticMapMetadata(id: MapId, name: String, mapType: MapType, size: MapSize, romAddr: Address, entrances: List[EntranceMetadata])


  val STATIC_MAP_METADATA: Map[MapId, StaticMapMetadata] = Map(
    CharlockId -> StaticMapMetadata(CharlockId,  "Charlock",             MapType.Both,    MapSize(Width(20), Height(20)), Address(0xC0),  List(EntranceMetadata(Point( CharlockId, 10, 19), Address(0xF3EA), ImportantLocationType.CHARLOCK))),
    HauksnessId -> StaticMapMetadata(HauksnessId,  "Hauksness",            MapType.Both,    MapSize(Width(20), Height(20)), Address(0x188),  List(EntranceMetadata(Point( HauksnessId,  0, 10), Address(0xF3F6), ImportantLocationType.TOWN))),
    TantegelId -> StaticMapMetadata(TantegelId,  "Tantegel",             MapType.Town,    MapSize(Width(30), Height(30)), Address(0x250),  List(EntranceMetadata(Point( TantegelId, 11, 29), Address(0xF3E4), ImportantLocationType.TANTEGEL))),
    TantegelThroneRoomId -> StaticMapMetadata(TantegelThroneRoomId,  "Tantegel Throne Room", MapType.Other,   MapSize(Width(10), Height(10)), Address(0x412), List()),
    CharlockThroneRoomId -> StaticMapMetadata(CharlockThroneRoomId,  "Charlock Throne Room", MapType.Dungeon, MapSize(Width(30), Height(30)), Address(0x444), List()),
    KolId -> StaticMapMetadata(KolId,  "Kol",                  MapType.Town,    MapSize(Width(24), Height(24)), Address(0x606),  List(EntranceMetadata(Point(  KolId, 19, 23), Address(0xF3DE), ImportantLocationType.TOWN))),
    BrecconaryId -> StaticMapMetadata(BrecconaryId,  "Brecconary",           MapType.Town,    MapSize(Width(30), Height(30)), Address(0x726),  List(EntranceMetadata(Point(  BrecconaryId,  0, 15), Address(0xF3E1), ImportantLocationType.TOWN))),
    GarinhamId -> StaticMapMetadata(GarinhamId,  "Garinham",             MapType.Town,    MapSize(Width(20), Height(20)), Address(0xAAA),  List(EntranceMetadata(Point(  GarinhamId,  0, 14), Address(0xF3D8), ImportantLocationType.TOWN))),
    CantlinId -> StaticMapMetadata(CantlinId, "Cantlin",              MapType.Town,    MapSize(Width(30), Height(30)), Address(0x8E8),  List(EntranceMetadata(Point( CantlinId,  5, 15), Address(0xF3F9), ImportantLocationType.TOWN))),
    RimuldarId -> StaticMapMetadata(RimuldarId, "Rimuldar",             MapType.Town,    MapSize(Width(30), Height(30)), Address(0xB72),  List(EntranceMetadata(Point( RimuldarId, 29, 14), Address(0xF3F3), ImportantLocationType.TOWN))),
    TantegelBasementId -> StaticMapMetadata(TantegelBasementId, "Tantegel Basement",    MapType.Other,   MapSize(Width(10), Height(10)), Address(0xD34),  List(EntranceMetadata(Point( TantegelBasementId,  0,  4), Address(0xF40B), ImportantLocationType.CAVE))),
    NorthernShrineId -> StaticMapMetadata(NorthernShrineId, "Northern Shrine",      MapType.Other,   MapSize(Width(10), Height(10)), Address(0xD66),  List(EntranceMetadata(Point( NorthernShrineId,  4,  9), Address(0xF3DB), ImportantLocationType.CAVE))),
    NorthernShrineId -> StaticMapMetadata(NorthernShrineId, "Northern Shrine",      MapType.Other,   MapSize(Width(10), Height(10)), Address(0xD66),  List(EntranceMetadata(Point( NorthernShrineId,  4,  9), Address(0xF3DB), ImportantLocationType.CAVE))),
    SouthernShrineId -> StaticMapMetadata(SouthernShrineId, "Southern Shrine",      MapType.Other,   MapSize(Width(10), Height(10)), Address(0xD98),  List(EntranceMetadata(Point( SouthernShrineId,  0,  4), Address(0xF3FC), ImportantLocationType.CAVE))),
    CharlockCaveLv1Id -> StaticMapMetadata(CharlockCaveLv1Id, "Charlock Cave Lv 1",   MapType.Dungeon, MapSize(Width(20), Height(20)), Address(0xDCA), List()),
    CharlockCaveLv2Id -> StaticMapMetadata(CharlockCaveLv2Id, "Charlock Cave Lv 2",   MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0xE92), List()),
    CharlockCaveLv3Id -> StaticMapMetadata(CharlockCaveLv3Id, "Charlock Cave Lv 3",   MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0xEC4), List()),
    CharlockCaveLv4Id -> StaticMapMetadata(CharlockCaveLv4Id, "Charlock Cave Lv 4",   MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0xEF6), List()),
    CharlockCaveLv5Id -> StaticMapMetadata(CharlockCaveLv5Id, "Charlock Cave Lv 5",   MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0xF28), List()),
    CharlockCaveLv6Id -> StaticMapMetadata(CharlockCaveLv6Id, "Charlock Cave Lv 6",   MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0xF5A), List()),
    SwampCaveId -> StaticMapMetadata(SwampCaveId, "Swamp Cave",           MapType.Dungeon, MapSize(Width( 6), Height(30)), Address(0xF8C),  List(EntranceMetadata(Point( SwampCaveId,  0,  0), Address(0xF3E7), ImportantLocationType.CAVE), EntranceMetadata(Point(SwampCaveId, 0, 29), Address(0xF3ED), ImportantLocationType.CAVE))),
    MountainCaveLv1Id -> StaticMapMetadata(MountainCaveLv1Id, "Mountain Cave",        MapType.Dungeon, MapSize(Width(14), Height(14)), Address(0xFE6),  List(EntranceMetadata(Point( MountainCaveLv1Id,  0,  7), Address(0xF3F0), ImportantLocationType.CAVE))),
    MountainCaveLv2Id -> StaticMapMetadata(MountainCaveLv2Id, "Mountain Cave Lv 2",   MapType.Dungeon, MapSize(Width(14), Height(14)), Address(0x1048), List()),
    GarinsGraveLv1Id -> StaticMapMetadata(GarinsGraveLv1Id, "Garin's Grave Lv 1",   MapType.Dungeon, MapSize(Width(20), Height(20)), Address(0x10AA), List(EntranceMetadata(Point( GarinsGraveLv1Id,  6, 11), Address(0xF411), ImportantLocationType.CAVE))),
    GarinsGraveLv2Id -> StaticMapMetadata(GarinsGraveLv2Id, "Garin's Grave Lv 2",   MapType.Dungeon, MapSize(Width(14), Height(12)), Address(0x126C), List()),
    GarinsGraveLv3Id -> StaticMapMetadata(GarinsGraveLv3Id, "Garin's Grave Lv 3",   MapType.Dungeon, MapSize(Width(20), Height(20)), Address(0x1172), List()),
    GarinsGraveLv4Id -> StaticMapMetadata(GarinsGraveLv4Id, "Garin's Grave Lv 4",   MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0x123A), List()),
    ErdricksCaveLv1Id -> StaticMapMetadata(ErdricksCaveLv1Id, "Erdrick's Cave",       MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0x12C0), List(EntranceMetadata(Point( ErdricksCaveLv1Id,  0,  0), Address(0xF3FF), ImportantLocationType.CAVE))),
    ErdricksCaveLv2Id -> StaticMapMetadata(ErdricksCaveLv2Id, "Erdrick's Cave Lv 2",  MapType.Dungeon, MapSize(Width(10), Height(10)), Address(0x12F2), List())
  )

  /*
  require 'controller'
  require 'helpers'
  require 'locations'


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


  -- TODO: at this point... is an Entrance any different from a Warp?
  -- could we get rid of Entrance and just use Warp?
  -- NOTE: 'from' is the "overworld"
  -- (""s because the entrance could be to a basement and so from might actually be tantegel or garinham)
  Entrance = class(function(a, from, to, entranceType)
    a.to = to
    a.from = from
    a.entranceType = entranceType
  end)

  function Entrance:__tostring()
    return "<Entrance from:" .. tostring(self.from) ..
                   ", to:" .. tostring(self.to) ..
                   ", entranceType:" .. tostring(self.entranceType) .. ">"
  end

  function Entrance:equals(e)
    return self.from:equals(e.from) and self.to:equals(e.to)
  end

  function EntranceMetadata:convertToEntrance(memory)
    local from = Point(memory:readROM(self.warpRomAddr), memory:readROM(self.warpRomAddr+1), memory:readROM(self.warpRomAddr+2))
    local res = Entrance(from, self.to, self.entranceType)
    return res
  end

  StaticMapMetadata = class(function(a, mapId, name, mapType, size, mapLayoutRomAddr, entrances)
    a.mapId = mapId
    a.name = name
    a.mapType = mapType
    a.size = size
    a.mapLayoutRomAddr = mapLayoutRomAddr
    -- this is a list because swamp cave has two entrances
    -- nil means it doesn't have an overworld location (or a warp location to tantegel or garinham anyway)
    a.entrances = entrances
  end)

  function StaticMapMetadata:__tostring()
    return "StaticMapMetadata(name: " .. self.name ..
                           ", size: " .. tostring(self.size) ..
                           ", mapId: " .. self.mapId ..
                           ", mapLayoutRomAddr: " .. self.mapLayoutRomAddr ..
                           ", entrances: " .. tostring(self.entrances) .. ")"
  end

  MapType = enum.new("Map Type", { "Town", "Dungeon", "Both", "Other" })

  function StaticMapMetadata:readEntranceCoordinates(memory)
    if self.entrances == nil then return nil end
    local res = list.map(self.entrances, function(e) return e:convertToEntrance(memory) end)
    return res
  end

  -- ok the idea is this:
  -- we return a table 2-29 that has all the entrances in the values
  -- then from LeaveOnFoot or whatever, we simply Goto(entrances)
  function getAllEntranceCoordinates(memory)
    local res = {}
    for i, meta in pairs(STATIC_MAP_METADATA) do
      res[i] = meta:readEntranceCoordinates(memory)
    end
    return res
  end

  mockEntranceCoordinates = {
     [2]= {Entrance(Point( 2, 19, 10), Point(1, 54,  87), ImportantLocationType.CHARLOCK)},
     [3]= {Entrance(Point( 3, 10,  0), Point(1, 29, 112), ImportantLocationType.Town)},
     [4]= {Entrance(Point( 4, 29, 11), Point(1, 85,  90), ImportantLocationType.TANTEGEL)},
     [7]= {Entrance(Point( 7, 23, 19), Point(1, 55,  67), ImportantLocationType.Town)},
     [8]= {Entrance(Point( 8, 15,  0), Point(1, 98,  98), ImportantLocationType.Town)},
     [9]= {Entrance(Point( 9, 14,  0), Point(1, 74, 108), ImportantLocationType.Town)},
     [10]={Entrance(Point(10, 15,  5), Point(1, 36,  44), ImportantLocationType.Town)},
     [11]={Entrance(Point(11, 14, 29), Point(1, 74, 110), ImportantLocationType.Town)},
     [12]={Entrance(Point(12,  4,  0), Point(4, 29,  29), ImportantLocationType.CAVE)},
     [13]={Entrance(Point(13,  9,  4), Point(1, 58, 106), ImportantLocationType.CAVE)},
     [14]={Entrance(Point(14,  4,  0), Point(1, 82,   8), ImportantLocationType.CAVE)},
     [21]={Entrance(Point(21,  0,  0), Point(1, 90,  78), ImportantLocationType.CAVE), Entrance(Point(21, 29, 0), Point(1, 93, 63), ImportantLocationType.CAVE)},
     [22]={Entrance(Point(22,  7,  0), Point(1, 96,  99), ImportantLocationType.CAVE)},
     [24]={Entrance(Point(24, 11,  6), Point(9, 19,   0), ImportantLocationType.CAVE)},
     [28]={Entrance(Point(28,  0,  0), Point(1, 86,  84), ImportantLocationType.CAVE)}
    }

  StaticMapTile = class(function(a,tileId,name,walkable,walkableWithKeys)
    a.id = tileId
    a.tileId = tileId
    a.name = name
    a.walkable = walkable
    a.walkableWithKeys = walkableWithKeys and true or false
    -- i think 1 here is ok. if its not walkable it wont end up in the graph at all
    -- the only small problem is charlock has some swamp and desert, but... they aren't really
    -- avoidable anyway, and so... it should just be fine to always use 1.
    a.weight = 1
  end)

  function StaticMapTile:__tostring()
    local w = self.walkable and "true" or "false"
    -- ok this is weird and might expose a hole in the whole program.
    -- but then again maybe not
    local wwk = self.walkableWithKeys and "true" or "true"
    return "{ tileId: " .. self.tileId .. ", name: " .. self.name ..
           ", walkable: " .. w .. ", walkableWithKeys: " .. wwk .. "}"
  end

  NON_DUNGEON_TILES = {
    [0]   = StaticMapTile(0,  "Grass" , true),
    [1]   = StaticMapTile(1,  "Sand"  , true),
    [2]   = StaticMapTile(2,  "Water" , false),
    [3]   = StaticMapTile(3,  "Chest" , true),
    [4]   = StaticMapTile(4,  "Stone" , false),
    [5]   = StaticMapTile(5,  "Up"    , true),
    [6]   = StaticMapTile(6,  "Brick" , true),
    [7]   = StaticMapTile(7,  "Down"  , true),
    [8]   = StaticMapTile(8,  "Trees" , true),
    [9]   = StaticMapTile(9,  "Swamp" , true),
    [0xA] = StaticMapTile(10, "Field" , true),
    [0xB] = StaticMapTile(11, "Door"  , false, true), -- walkableWithKeys
    [0xC] = StaticMapTile(12, "Weapon", false),
    [0xD] = StaticMapTile(13, "Inn"   , false),
    [0xE] = StaticMapTile(14, "Bridge", true),
    [0xF] = StaticMapTile(15, "Tile"  , false),
  }

  DUNGEON_TILES = {
    [0]   = StaticMapTile(0, "Stone", false),
    [1]   = StaticMapTile(1, "Up"   , true),
    [2]   = StaticMapTile(2, "Brick", true),
    [3]   = StaticMapTile(3, "Down" , true),
    [4]   = StaticMapTile(4, "Chest", true),
    [5]   = StaticMapTile(5, "Door" , false, true), -- walkableWithKeys
    -- in swamp cave, we get id six where the princess is. its the only 6 we get in any dungeon.
    [6]   = StaticMapTile(6, "Brick", true),
  }

  IMMOBILE_NPCS = {
    [Charlock]           = {},
    [Hauksness]          = {},
    [Tantegel]           = {{2,8}, {8,6}, {8,8}, {27,5}, {26,15}, {9,27}, {12,27}, {15, 20}},
    [TantegelThroneRoom] = {{3,6}, {5,6}},
    [CharlockThroneRoom] = {},
    [Kol]                = {},
    [Brecconary]         = {{1,13}, {4,7}, {10,26}, {20,23}, {28,1}},
    [Garinham]           = {{2,17}, {9,6}, {14,1}},
    [Cantlin]            = {{0,0}},
    [Rimuldar]           = {{2,4}, {27,0}},
    [TantegelBasement]   = {},
    [NorthernShrine]     = {},
    [SouthernShrine]     = {},
    [CharlockCaveLv1]    = {},
    [CharlockCaveLv2]    = {},
    [CharlockCaveLv3]    = {},
    [CharlockCaveLv4]    = {},
    [CharlockCaveLv5]    = {},
    [CharlockCaveLv6]    = {},
    [SwampCave]          = {},
    [MountainCaveLv1]    = {},
    [MountainCaveLv2]    = {},
    [GarinsGraveLv1]     = {},
    [GarinsGraveLv2]     = {},
    [GarinsGraveLv3]     = {},
    [GarinsGraveLv4]     = {},
    [ErdricksCaveLv1]    = {},
    [ErdricksCaveLv2]    = {},
  }

  function getImmobileNPCsForMap(mapId)
    if IMMOBILE_NPCS[mapId] == nil then return {} end
    return list.map(IMMOBILE_NPCS[mapId], function(xy) return Point(mapId, xy[1], xy[2]) end)
  end


  function StaticMap:resetWarps (allWarps)
    self.warps = getWarpsForMap(self.mapId, allWarps)
  end

  function StaticMap:getTileSet ()
    return self.mapId < 15 and NON_DUNGEON_TILES or DUNGEON_TILES
  end

  function StaticMap:getTileAt(x, y)
    return self:getTileSet()[self.rows[y][x]]
  end

  function StaticMap:setTileAt(x, y, newTileId)
    self.rows[y][x] = newTileId
  end

  function StaticMap:childrenIds()
    if     self.mapId ==  2 then return {6,15,16,17,18,19,20}
    elseif self.mapId ==  4 then return {5}
    elseif self.mapId ==  5 then return {4}
    elseif self.mapId == 22 then return {23}
    elseif self.mapId == 24 then return {25,26,27}
    elseif self.mapId == 28 then return {29}
    else return {}
    end
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

  case class StaticMap(mapId: MapId, mapName: String, mapType, entrances, width, height, rows, allWarps)
      a.mapId = mapId
      a.mapName = mapName
      a.mapType = mapType
      a.entrances = entrances
      a.width = width
      a.height = height
      a.rows = rows
      a.warps = getWarpsForMap(mapId, allWarps)
      a.immobileScps = getImmobileNPCsForMap(mapId)
      a.seenByPlayer = false
    end)



  def readStaticMapFromRom(memory: Memory, mapMetadata: StaticMapMetadata, allWarps: List[Warps.Warp]) = {
    // returns the tile id for the given (x,y) for the current map
    def readTileIdAt(x: Int, y: Int) = {
      val offset = (y*mapMetadata.size.width) + x
      val addr = mapMetadata.mapLayoutRomAddr + math.floor(offset/2)
      val value = memory.readROM(addr)
      val tile = isEven(offset) and hiNibble(value) or loNibble(value)
      // TODO: i tried to use 0x111 but it went insane... so just using 7 instead.
      return mapId < 12 and tile or bitwise_and(tile, 7)
    }

    // returns a two dimensional grid of tile ids for the current map
    def readTileIds () = {
      val res = {}
      for y = 0, mapMetadata.size.height-1 do
        res[y] = {}
        for x = 0, mapMetadata.size.width-1 do
          res[y][x]=readTileIdAt(x,y)
        end
      end
      return res
    }

    val entrances = nil
    if mapMetadata.entrances ~= nil then
      entrances = list.map(mapData.entrances, function(e) return e:convertToEntrance(memory) end)
    end

    return StaticMap(mapId, mapMetadata.name, mapMetadata.mapType, mapMetadata:readEntranceCoordinates(memory),
      mapMetadata.size.width, mapMetadata.size.height, readTileIds(), allWarps)
  }


}