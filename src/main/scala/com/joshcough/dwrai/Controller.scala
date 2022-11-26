package com.joshcough.dwrai

import cats.effect.IO
import cats.implicits._
import nintaco.api.{API, GamepadButtons}

sealed trait Button { val underlying: Int }

object Button {
  case object A      extends Button { val underlying: Int = GamepadButtons.A      }
  case object B      extends Button { val underlying: Int = GamepadButtons.B      }
  case object Select extends Button { val underlying: Int = GamepadButtons.Select }
  case object Start  extends Button { val underlying: Int = GamepadButtons.Start  }
  case object Up     extends Button { val underlying: Int = GamepadButtons.Up     }
  case object Down   extends Button { val underlying: Int = GamepadButtons.Down   }
  case object Left   extends Button { val underlying: Int = GamepadButtons.Left   }
  case object Right  extends Button { val underlying: Int = GamepadButtons.Right  }

  val allButtons: Seq[Button]                      = List(A, B, Up, Down, Left, Right, Select, Start)
  val defaultControllerState: Map[Button, Boolean] = allButtons.map((_, false)).toMap

  def fromDir(dir: Direction): Button = dir match {
    case North => Button.Up
    case South => Button.Down
    case East  => Button.Right
    case West  => Button.Left
    case _     => throw new RuntimeException("no button for Direction.")
  }
}

object Controller {}

case class Controller(api: API) {
  import Button._

  def releaseAll: IO[Unit] = allButtons.traverse_(button =>
    IO {
      api.writeGamepad(0, button.underlying, false)
    }
  )

  def press(button: Button): IO[Unit] =
    releaseAll *> IO { api.writeGamepad(0, button.underlying, true) }

  def printGamePad(): IO[Unit] =
    Logging.log(allButtons.zip(allButtons.map { b => api.readGamepad(0, b.underlying) }))
}
