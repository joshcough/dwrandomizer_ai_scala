package com.joshcough.dwrai

import com.joshcough.dwrai.Scripts._
import nintaco.api.API
import scala.annotation.tailrec

case class Interpreter(api: API, memory: Memory, controller: Controller) {

  import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

  val frameQueue: BlockingQueue[Int] = new ArrayBlockingQueue[Int](10000)
  api.addFrameListener(() => frameQueue.put(api.getFrameCount))

  def interpret(s: Scripts.Script): Unit = {
    println(s"interpreting: $s")
    s match {
      case HoldButtonScript(button, nrFrames) =>
        api.writeGamepad(0, button.underlying, true)
        waitForNFrames(nrFrames)
      case HoldButtonUntilScript(button, condition) => () // zzz todo
      case Consecutive(_, scripts)                  => scripts.foreach(interpret)
    }
  }

  sealed trait ConditionResult
  case class BoolRes(b: Boolean) extends ConditionResult
  case class IntRes(i: Int)      extends ConditionResult

  def eval(c: Condition): ConditionResult = c match {
    case Not(c)      => BoolRes(!evalToBool(c).b)
    case Eq(l, r)    => BoolRes(eval(l) == eval(r))
    case NotEq(l, r) => BoolRes(eval(l) != eval(r))
    case Add(l, r)   => IntRes(evalToInt(l).i + evalToInt(r).i)
    case Sub(l, r)   => IntRes(evalToInt(l).i - evalToInt(r).i)
    case Mult(l, r)  => IntRes(evalToInt(l).i * evalToInt(r).i)
    case Div(l, r)   => IntRes(evalToInt(l).i / evalToInt(r).i)
    case Min(l, r)   => IntRes(math.min(evalToInt(l).i, evalToInt(r).i))
    case Max(l, r)   => IntRes(math.max(evalToInt(l).i, evalToInt(r).i))
    case Lt(l, r)    => BoolRes(evalToInt(l).i < evalToInt(r).i)
    case LtEq(l, r)  => BoolRes(evalToInt(l).i <= evalToInt(r).i)
    case Gt(l, r)    => BoolRes(evalToInt(l).i > evalToInt(r).i)
    case GtEq(l, r)  => BoolRes(evalToInt(l).i >= evalToInt(r).i)
    case Exists(cs)  => BoolRes(cs.map(evalToBool).exists(_.b))
    case All(cs)  => BoolRes(cs.map(evalToBool).forall(_.b))
  }

  def evalToBool(c: Condition): BoolRes = eval(c) match {
    case b: BoolRes => b
    case _: IntRes =>
      throw new RuntimeException(s"Expected Bool, but condition $c resulted in Bool")
  }

  def evalToInt(c: Condition): IntRes = eval(c) match {
    case _: BoolRes =>
      throw new RuntimeException(s"Expected Int, but condition $c resulted in Bool")
    case i: IntRes => i
  }

  private final def waitForNFrames(nrFrames: Int): Unit = waitUntilFrame(
    api.getFrameCount + nrFrames
  )

  @tailrec private final def waitUntilFrame(frameToWaitUntil: Int): Unit = {
    // println(s"Waiting until $frameToWaitUntil, api.getFrameCount is: ${api.getFrameCount}")
    val currentFrame = frameQueue.take()
    // println(s"currentFrame is: $currentFrame")
    if (currentFrame < frameToWaitUntil) waitUntilFrame(frameToWaitUntil) else ()
  }
}
