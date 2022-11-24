package com.joshcough.dwrai

import com.joshcough.dwrai.Button.{A, B, Down, Left, Right, Select, Start, Up}
import com.joshcough.dwrai.MapId.TantegelThroneRoomId
import com.joshcough.dwrai.StaticMapMetadata.STATIC_MAP_METADATA
import nintaco.api.API

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import scala.annotation.tailrec

object Interpreter {
  def runMain(api:API): Unit = {
    val mem         = Memory(api)
    val controller  = Controller(api, mem)
    val interpreter = Interpreter(api, mem, controller)
    import interpreter.scripts._
    interpreter.spawn(Consecutive(
      "DWR AI",
      List(
        DebugScript("starting interpreter"),
        GameStartMenuScript,
        WaitUntil(OnMap(TantegelThroneRoomId)),
        ThroneRoomOpeningGame
      )
    ))
  }
}

case class Interpreter(api: API, memory: Memory, controller: Controller) {

  val scripts = Scripts(memory)
  import scripts._

  val frameQueue: BlockingQueue[Int] = new ArrayBlockingQueue[Int](10000)
  api.addFrameListener(() => frameQueue.put(api.getFrameCount))

  val graph: Graph = Graph.mkGraph(memory)

  def spawn(script: Script): Unit = {
    new Thread(new Runnable() { def run(): Unit = interpret(script) }).start()
  }

  def interpret(s: Script): Unit = {
    Logging.log(s"interpreting: $s")
    s match {
      case HoldButtonScript(button, nrFrames) =>
        api.writeGamepad(0, button.underlying, true)
        waitForNFrames(nrFrames, eachFrame = () => api.writeGamepad(0, button.underlying, true))
        api.writeGamepad(0, button.underlying, false)
      case HoldButtonUntilScript(button, condition) =>
        api.writeGamepad(0, button.underlying, true)
        waitUntil(condition, eachFrame = () => api.writeGamepad(0, button.underlying, true))
      case Consecutive(_, scripts)          => scripts.foreach(interpret)
      case WaitUntil(condition)             => waitUntil(condition, () => ())
      case WaitFor(nrFrames)                => waitForNFrames(nrFrames, eachFrame = () => ())
      case DebugScript(msg)                 => Logging.log(s"DEBUG: $msg")
      case SaveUnlockedDoor(at: Point)      => () // TODO
      case OpenChest(at: Point)             => () // TODO
      case DoNothing                        => ()
      case IfThen(name, expr, thenB, elseB) => interpret(if (evalToBool(expr).b) thenB else elseB)

      case Move(from, to, dir) =>
        interpret(
          HoldButtonUntilScript(buttonFromDirection(dir), Eq(GetPosition, Value(PositionLit(to))))
        )

      case Goto(p: Point) =>
        val path                    = graph.shortestPath(memory.getLocation, List(p), 0, _ => 1).head
        val commands = path.convertPathToCommands
        val scripts = commands.map {
          case OpenDoorAt(p, dir)          => OpenDoor(p, dir)
          case MoveCommand(from, to, Warp) => TakeStairs(from, to)
          case MoveCommand(from, to, dir)  => Move(from, to, dir)
        }
        //Logging.log("PATH ----")
        //path.path.foreach(Logging.log)
        //Logging.log("COMMANDS ----")
        //commands.foreach(Logging.log)
        //Logging.log("SCRIPTS ----")
        //scripts.foreach(Logging.log)
        //Logging.log("-------")
        scripts.foreach(interpret)
    }

    def buttonFromDirection(dir: Direction): Button = dir match {
      case North => Button.Up
      case South => Button.Down
      case East  => Button.Right
      case West  => Button.Left
      case _     => throw new RuntimeException("no button for Warp.")
    }
  }

  sealed trait ConditionResult
  case class BoolRes(b: Boolean)   extends ConditionResult
  case class IntRes(i: Int)        extends ConditionResult
  case class MapIdRes(m: MapId)    extends ConditionResult
  case class PositionRes(p: Point) extends ConditionResult

  def eval(c: Expr): ConditionResult = c match {
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
    case All(cs)     => BoolRes(cs.map(evalToBool).forall(_.b))

    //
    case GetMapId       => MapIdRes(memory.getMapId)
    case GetPosition    => PositionRes(memory.getLocation)
    case IsChestOpen(p) => BoolRes(false)

    //
    case Value(IntLit(v))      => IntRes(v)
    case Value(BoolLit(v))     => BoolRes(v)
    case Value(MapIdLit(v))    => MapIdRes(v)
    case Value(PositionLit(p)) => PositionRes(p)
  }

  def evalToBool(c: Expr): BoolRes = eval(c) match {
    case b: BoolRes => b
    case _          => fail(s"Expected Bool, but condition $c resulted in Bool")
  }

  def evalToInt(c: Expr): IntRes = eval(c) match {
    case i: IntRes => i
    case _         => fail(s"Expected Int, but condition $c resulted in Bool")
  }

  private def waitForNFrames(nrFrames: Int, eachFrame: () => Unit): Unit =
    waitUntilFrame(api.getFrameCount + nrFrames, eachFrame)

  @tailrec private def waitUntilFrame(frameToWaitUntil: Int, eachFrame: () => Unit): Unit = {
    // Logging.log(s"Waiting until $frameToWaitUntil, api.getFrameCount is: ${api.getFrameCount}")
    eachFrame()
    printGamePad()
    val currentFrame = frameQueue.take()
    // Logging.log(s"currentFrame is: $currentFrame")
    if (currentFrame < frameToWaitUntil) waitUntilFrame(frameToWaitUntil, eachFrame)
    else ()
  }

  @tailrec private def waitUntil(c: Expr, eachFrame: () => Unit): Unit = {
    Logging.log(s"waiting until $c")
    val res = evalToBool(c)
    Logging.log(s"res $res")
    if (!res.b) {
      Logging.log("waiting for a frame")
      frameQueue.take()
      eachFrame()
      printGamePad()
      Logging.log("recurring")
      waitUntil(c, eachFrame)
    }
  }

  def fail(msg: String) = throw new RuntimeException(msg)

  def printGamePad(): Unit = {
    val buttons = List(A, B, Up, Down, Left, Right, Select, Start)
    Logging.log(buttons.zip(buttons.map { b => api.readGamepad(0, b.underlying) }))
  }
}
