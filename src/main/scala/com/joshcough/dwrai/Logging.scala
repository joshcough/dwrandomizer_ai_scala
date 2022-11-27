package com.joshcough.dwrai

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import scala.tools.nsc.io.File

object Logging {

  def log(a: Any): IO[Unit] = IO(
    File("/Users/joshuacough/work/dwrandomizer_ai_scala/ai.log").appendAll(a.toString, "\n")
  )

  def logUnsafe(a: Any): Unit = log(a).unsafeRunSync()
}
