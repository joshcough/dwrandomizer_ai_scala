package com.joshcough.dwrai

import cats.effect.IO
import scala.tools.nsc.io.File

object Logging {

  def log(a: Any): IO[Unit] = IO(
    File("/Users/joshuacough/work/dwrandomizer_ai_scala/ai.log").appendAll(a.toString, "\n")
  )

}
