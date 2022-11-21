package com.joshcough.dwrai

import com.joshcough.dwrai.MapId.TantegelThroneRoomId
import com.joshcough.dwrai.Scripts.ThroneRoomOpeningGame
import com.joshcough.dwrai.StaticMapMetadata.STATIC_MAP_METADATA
import nintaco.api.{API, ApiSource}

object DWRAI {

  def main(args: Array[String]): Unit = {
    val api: API = ApiSource.getAPI
    addDebuggingListener(api)
    api.run()
    println("time: " + System.currentTimeMillis())

    val interpreter = Interpreter.run(
      api,
      Scripts.Consecutive(
        "Main",
        List(
          Scripts.DebugScript("starting interpreter"),
          Scripts.GameStartMenuScript,
          Scripts.WaitUntil(Scripts.OnMap(TantegelThroneRoomId)),
          ThroneRoomOpeningGame
//          Scripts.DebugScript("We should now be in front of the king!"),
//          Scripts.talkToKing,
//          Scripts.Goto(Point(TantegelThroneRoomId, 1, 1))
        )
      )
    )

  }

  def addDebuggingListener(api: API): Unit = {
    api.addActivateListener { () =>
      // memory.debug.foreach(Logging.log)
      // Logging.log(Overworld.readOverworldFromROM(memory).map(_.mkString(" | ")).mkString("\n"))
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
