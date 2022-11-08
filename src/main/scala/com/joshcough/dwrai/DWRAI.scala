package com.joshcough.dwrai

import com.joshcough.dwrai.StaticMaps.{STATIC_MAP_METADATA, StaticMap}
import nintaco.api.ApiSource

object DWRAI {
  def main(args:Array[String]): Unit = {
    val api = ApiSource.getAPI
    val mem = Memory(api)

    api.addActivateListener(() =>
      //println(Overworld.readOverworldFromROM(mem).map(_.mkString(" | ")).mkString("\n"))
      STATIC_MAP_METADATA.values.toList.sortWith{ case (l,r) => l.id.value < r.id.value }
        .foreach(x => println(StaticMap.readStaticMapFromRom(mem, x, List()).quickPrint))
    )

    api.run()
  }
}

//    val printLocationListener: AccessPointListener = (_: Int, _: Int, _: Int) => {
//      println(mem.all)
//      -1
//    }
//    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3a)
//    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3b)
