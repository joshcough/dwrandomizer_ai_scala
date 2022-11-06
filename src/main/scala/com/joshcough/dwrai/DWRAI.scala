package com.joshcough.dwrai

import nintaco.api.ApiSource

object DWRAI {
  def main(args:Array[String]): Unit = {
    val api = ApiSource.getAPI
    val mem = Memory(api)

    api.addActivateListener(() =>
      //println(Overworld.readOverworldFromROM(mem).map(_.mkString(" | ")).mkString("\n"))
      ()
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
