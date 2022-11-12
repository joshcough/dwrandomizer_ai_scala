package com.joshcough.dwrai

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
}

object Controller {}

case class Controller(api: API, memory: Memory) {

  import Button._

  val allButtons = List(A, B, Up, Down, Left, Right, Select, Start)

  val emptyInputs: Map[Button, Boolean] = allButtons.map((_, false)).toMap

}
/*

function controller.waitFrames (n)
  for _ = 1,n do emu.frameadvance() end
end

-- waits either maxFrames, or until f yields true
function controller.waitUntil (f, maxFrames, msg)
  log.debug("Waiting until: " .. msg .. " for up to " .. maxFrames .. " frames.")
  local nrFramesWaited = 0
  for _ = 1,maxFrames do
    if f() then
      log.debug("Waited until: " .. msg .. " waited exactly " .. nrFramesWaited .. " frames, and condition is: " .. tostring(f()))
      return
    end
    emu.frameadvance()
    nrFramesWaited = nrFramesWaited + 1
  end
  log.debug("Waited until: " .. msg .. " waited exactly " .. nrFramesWaited .. " frames, and condition is: " .. tostring(f()))
end

function clearController()
  joypad.write(1, emptyInputs)
end

function controller.pressButton (button, wait)
  log.debug("Pressing " .. tostring(button) .. " and waiting .. " .. tostring(wait))
  e = table.shallow_copy(emptyInputs)
  e[convertButton(button)] = true
  joypad.write(1, e)
  controller.waitFrames(wait)
  clearController()
  controller.waitFrames(1)
end

function controller.holdButton (button, frames)
  local nrFrames = 0
  controller.holdButtonUntil(button, "frame count is: " .. frames, function()
    nrFrames = nrFrames + 1
    return nrFrames >= frames
  end)
end

function controller.holdButtonUntil(button, msg, conditionFunction)
  log.debug("Holding " .. tostring(button) .. " until " .. msg)
  e = table.shallow_copy(emptyInputs)
  e[convertButton(button)] = true
  while not conditionFunction() do
    joypad.write(1, e)
    emu.frameadvance()
  end
  log.debug("Done holding " .. tostring(button) .. " until " .. msg)
  clearController()
  emu.frameadvance()
end

function controller.holdButtonUntilOrMaxFrames(button, msg, conditionFunction, maxFrames)
  if maxFrames == nil then return controller.holdButtonUntil(button, msg, conditionFunction)
  else
    local nrFrames = 0
    controller.holdButtonUntil(button, msg .. " or frame count is: " .. maxFrames, function()
      nrFrames = nrFrames + 1
      return (nrFrames >= maxFrames) or conditionFunction()
    end)
  end
end

function controller.pressStart (wait) controller.pressButton(Button.START, wait) end
function controller.pressSelect (wait) controller.pressButton(Button.SELECT, wait) end
function controller.pressA (wait) controller.pressButton(Button.A, wait) end
function controller.pressB (wait) controller.pressButton(Button.B, wait) end
function controller.pressLeft (wait) controller.pressButton(Button.LEFT, wait) end
function controller.pressRight (wait) controller.pressButton(Button.RIGHT, wait) end
function controller.pressUp (wait) controller.pressButton(Button.UP, wait) end
function controller.pressDown (wait) controller.pressButton(Button.DOWN, wait) end

function controller.holdStart (frames) controller.holdButton(Button.START, frames) end
function controller.holdStartUntil (f, msg, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.START, msg, f, maxFrames) end
function controller.holdSelect (frames) controller.holdButton(Button.SELECT, frames) end
function controller.holdSelectUntil (f, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.SELECT, msg, f, maxFrames) end
function controller.holdA (frames) controller.holdButton(Button.A, frames) end
function controller.holdAUntil (f, msg, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.A, msg, f, maxFrames) end
function controller.holdB (frames) controller.holdButton(Button.B, frames) end
function controller.holdBUntil (f, msg, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.B, msg, f, maxFrames) end
function controller.holdLeft (frames) controller.holdButton(Button.LEFT, frames) end
function controller.holdLeftUntil (f, msg, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.LEFT, msg, f, maxFrames) end
function controller.holdRight (frames) controller.holdButton(Button.RIGHT, frames) end
function controller.holdRightUntil (f, msg, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.RIGHT, msg, f, maxFrames) end
function controller.holdUp (frames) controller.holdButton(Button.UP, frames) end
function controller.holdUpUntil (f, msg, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.UP, msg, f, maxFrames) end
function controller.holdDown (frames) controller.holdButton(Button.DOWN, frames) end
function controller.holdDownUntil (f, msg, maxFrames) controller.holdButtonUntilOrMaxFrames(Button.DOWN, msg, f, maxFrames) end

 */
