package com.joshcough.dwrai

import nintaco.api.{API, ApiSource}

object DWRAI {
  def main(args: Array[String]): Unit = {
    val api: API    = ApiSource.getAPI
    val mem: Memory = Memory(api)
    val interpreter = Interpreter(api, mem, Controller(api, mem))

    api.run()

    //debugSomeStuff(api, mem)
    new Thread(new Runnable() {
      def run(): Unit = interpreter.interpret(Scripts.GameStartMenuScript)
    }).start()
  }

  def debugSomeStuff(api: API, memory: Memory): Unit = {
    import com.joshcough.dwrai.StaticMaps.{STATIC_MAP_METADATA, StaticMap}
    api.addActivateListener { () =>
      memory.debug.foreach(println)
      println(Overworld.readOverworldFromROM(memory).map(_.mkString(" | ")).mkString("\n"))
      STATIC_MAP_METADATA.values.toList
        .sortWith { case (l, r) => l.id.value < r.id.value }
        .foreach(x => println(StaticMap.readStaticMapFromRom(memory, x, List()).quickPrint))
    }
  }
}

//    val printLocationListener: AccessPointListener = (_: Int, _: Int, _: Int) => {
//      println(mem.all)
//      -1
//    }
//    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3a)
//    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3b)
