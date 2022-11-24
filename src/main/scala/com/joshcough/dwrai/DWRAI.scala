package com.joshcough.dwrai

import com.joshcough.dwrai.StaticMapMetadata.STATIC_MAP_METADATA
import nintaco.api.{API, ApiSource}

object DWRAI {

  def main(args: Array[String]): Unit = {
    val api: API = ApiSource.getAPI
    addDebuggingListener(api)
    api.run()
    println("time: " + System.currentTimeMillis())
    Interpreter.runMain(api)
  }

  def addDebuggingListener(api: API): Unit = {
    api.addActivateListener { () =>
      // memory.debug.foreach(Logging.log)
      // Logging.log(Overworld.readOverworldFromROM(memory).map(_.mkString(" | ")).mkString("\n"))
//      StaticMap.readAllStaticMapsFromRom(Memory(api)).foreach{ case (id, map) =>
//        Logging.log((id, map.entrances))
//      }
    }
  }

  def printMapStuff(memory: Memory, mapId: MapId): Graph = {
    val meta: StaticMapMetadata = STATIC_MAP_METADATA(mapId)
    val map: StaticMap          = StaticMap.readStaticMapFromRom(memory, meta)
    val graph: Graph            = Graph.mkStaticMapGraph(map)
    Logging.log(map.quickPrint)
    Logging.log(graph.quickPrint)
    graph
  }
}
// just some memory/debugging stuff i might want to use some day
//    val printLocationListener: AccessPointListener = (_: Int, _: Int, _: Int) => {
//      Logging.log(mem.debug)
//      -1
//    }
//    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3a)
//    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3b)
