package com.joshcough.dwrai

import nintaco.api.{AccessPointListener, AccessPointType, ApiSource}

object DWRAI {
  def main(args:Array[String]): Unit = {
    val api = ApiSource.getAPI
    val mem = Memory(api)
    // api.addFrameListener(() => println(s"Frame Listener: x: ${api.readCPU(0x8e)}"))
    val printLocationListener: AccessPointListener = (_: Int, _: Int, _: Int) => {
      println(mem.all)
      -1
    }
    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3a)
    api.addAccessPointListener(printLocationListener, AccessPointType.PostWrite, 0x3b)
    api.run()
  }
}

