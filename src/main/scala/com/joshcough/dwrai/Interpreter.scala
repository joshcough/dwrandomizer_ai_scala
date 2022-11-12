package com.joshcough.dwrai

import com.joshcough.dwrai.MapId.TantegelThroneRoomId
import com.joshcough.dwrai.Scripts._
import nintaco.api.API

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import scala.annotation.tailrec

object Interpreter {
  def run(api: API): Unit = {
    val frameQueue: BlockingQueue[Int] = new ArrayBlockingQueue[Int](10000)
    api.addFrameListener(() => frameQueue.put(api.getFrameCount))
    val mem         = Memory(api)
    val controller  = Controller(api, mem)
    val interpreter = Interpreter(api, mem, controller, frameQueue)

    new Thread(new Runnable() {
      def run(): Unit = interpreter.interpret(
        Consecutive(
          "Main",
          List(
            Scripts.GameStartMenuScript,
            WaitUntil(Eq(GetMapId, Value(MapIdLit(TantegelThroneRoomId)))),
            DebugScript("We should now be in front of the king!")
          )
        )
      )
    }).start()
  }
}

case class Interpreter(api: API,
                       memory: Memory,
                       controller: Controller,
                       private val frameQueue: BlockingQueue[Int]
) {

  def interpret(s: Scripts.Script): Unit = {
    println(s"interpreting: $s")
    s match {
      case HoldButtonScript(button, nrFrames) =>
        api.writeGamepad(0, button.underlying, true)
        waitForNFrames(nrFrames)
      case HoldButtonUntilScript(button, condition) => () // zzz todo
      case Consecutive(_, scripts)                  => scripts.foreach(interpret)
      case WaitUntil(condition)                     => waitUntil(condition)
      case DebugScript(msg)                         => println(s"DEBUG: $msg")
    }
  }

  sealed trait ConditionResult
  case class BoolRes(b: Boolean) extends ConditionResult
  case class IntRes(i: Int)      extends ConditionResult
  case class MapIdRes(m: MapId)  extends ConditionResult

  def eval(c: Expr): ConditionResult = c match {
    case Not(c)             => BoolRes(!evalToBool(c).b)
    case Eq(l, r)           => BoolRes(eval(l) == eval(r))
    case NotEq(l, r)        => BoolRes(eval(l) != eval(r))
    case Add(l, r)          => IntRes(evalToInt(l).i + evalToInt(r).i)
    case Sub(l, r)          => IntRes(evalToInt(l).i - evalToInt(r).i)
    case Mult(l, r)         => IntRes(evalToInt(l).i * evalToInt(r).i)
    case Div(l, r)          => IntRes(evalToInt(l).i / evalToInt(r).i)
    case Min(l, r)          => IntRes(math.min(evalToInt(l).i, evalToInt(r).i))
    case Max(l, r)          => IntRes(math.max(evalToInt(l).i, evalToInt(r).i))
    case Lt(l, r)           => BoolRes(evalToInt(l).i < evalToInt(r).i)
    case LtEq(l, r)         => BoolRes(evalToInt(l).i <= evalToInt(r).i)
    case Gt(l, r)           => BoolRes(evalToInt(l).i > evalToInt(r).i)
    case GtEq(l, r)         => BoolRes(evalToInt(l).i >= evalToInt(r).i)
    case Exists(cs)         => BoolRes(cs.map(evalToBool).exists(_.b))
    case All(cs)            => BoolRes(cs.map(evalToBool).forall(_.b))
    case GetMapId           => MapIdRes(memory.getMapId)
    case Value(IntLit(v))   => IntRes(v)
    case Value(BoolLit(v))  => BoolRes(v)
    case Value(MapIdLit(v)) => MapIdRes(v)
  }

  def evalToBool(c: Expr): BoolRes = eval(c) match {
    case b: BoolRes => b
    case _          => fail(s"Expected Bool, but condition $c resulted in Bool")
  }

  def evalToInt(c: Expr): IntRes = eval(c) match {
    case i: IntRes => i
    case _         => fail(s"Expected Int, but condition $c resulted in Bool")
  }

  private def waitForNFrames(nrFrames: Int): Unit = waitUntilFrame(api.getFrameCount + nrFrames)

  @tailrec private def waitUntilFrame(frameToWaitUntil: Int): Unit = {
    // println(s"Waiting until $frameToWaitUntil, api.getFrameCount is: ${api.getFrameCount}")
    val currentFrame = frameQueue.take()
    // println(s"currentFrame is: $currentFrame")
    if (currentFrame < frameToWaitUntil) waitUntilFrame(frameToWaitUntil) else ()
  }

  @tailrec private def waitUntil(c: Expr): Unit = {
    if (!evalToBool(c).b) { frameQueue.take(); waitUntil(c) }
  }

  def fail(msg: String) = throw new RuntimeException(msg)
}
