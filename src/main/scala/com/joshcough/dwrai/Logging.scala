package com.joshcough.dwrai

object Logging {

  def log(a: Any): Unit = {
    import scala.tools.nsc.io.File
    val s = a.toString
    File("/Users/joshuacough/work/dwrandomizer_ai_scala/ai.log").appendAll(s, "\n")
  }

}
