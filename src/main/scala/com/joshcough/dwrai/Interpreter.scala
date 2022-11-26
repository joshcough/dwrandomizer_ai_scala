package com.joshcough.dwrai

import cats.implicits._
import cats.data.StateT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.joshcough.dwrai.Button.{A, B, Down, Left, Right, Select, Start, Up}
import com.joshcough.dwrai.MapId.TantegelThroneRoomId
import nintaco.api.API

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

object Interpreter {
  def runMain(api: API): IO[Unit] = {
    val mem     = Memory(api)
    val machine = Machine(api)
    for {
      staticMaps <- StaticMap.readAllStaticMapsFromRom(mem)
      overworld  <- Overworld.readOverworldFromROM(mem)
      gameMaps    = GameMaps(staticMaps, overworld)
      scripts     = Scripts(gameMaps)
      interpreter = Interpreter(machine, scripts)
      _ <- IO {
        import interpreter.scripts._
        val script: Script = Consecutive(
          "DWR AI",
          List(
            DebugScript("starting interpreter"),
            GameStartMenuScript,
            WaitUntil(OnMap(TantegelThroneRoomId)),
            ThroneRoomOpeningGame
          )
        )
        new Thread(new Runnable() {
          def run(): Unit =
            (for {
              loc <- machine.getLocation
              game = Game(gameMaps, Graph.mkGraph(gameMaps), loc, pressedButton = None)
              _ <- interpreter.interpret(script).run(game)
              _ <- Logging.log(s"all done interpreting")
            } yield ()).unsafeRunSync()
        }).start()
      }
    } yield ()
  }
}

case class Machine(private val api: API) {

  val frameQueue: BlockingQueue[Int] = new ArrayBlockingQueue[Int](10000)
  api.addFrameListener(() => frameQueue.put(api.getFrameCount))

  val memory: Memory         = Memory(api)
  val controller: Controller = Controller(api)
  def getFrameCount: IO[Int] = IO(api.getFrameCount)
  def getLocation: IO[Point] = memory.getLocation

  def printGamePad(): IO[Unit] = {
    val buttons = List(A, B, Up, Down, Left, Right, Select, Start)
    Logging.log(buttons.zip(buttons.map { b => api.readGamepad(0, b.underlying) }))
  }

  def advanceOneFrame: StateT[IO, Game, Int] = StateT.liftF(IO(frameQueue.take()))

}

case class Game(maps: GameMaps, graph: Graph, currentLoc: Point, pressedButton: Option[Button]) {
  def press(b: Button): Game = this.copy(pressedButton = Some(b))
  def releaseButton: Game    = this.copy(pressedButton = None)
}

case class GameMaps(staticMaps: Map[MapId, StaticMap], overworld: Overworld)

case class Interpreter(machine: Machine, scripts: Scripts) {

  import scripts._

  def interpret(s: Script): StateT[IO, Game, Unit] = {
    val run: StateT[IO, Game, Unit] = s match {
      case HoldButtonScript(button, nrFrames) =>
        for {
          _ <- press(button)
          _ <- waitForNFrames(nrFrames)
          _ <- releaseAll
        } yield ()
      case HoldButtonUntilScript(button, condition) =>
        for {
          _ <- press(button)
          _ <- waitUntil(condition)
          _ <- releaseAll
        } yield ()
      case WaitUntil(condition) => waitUntil(condition)
      case WaitFor(nrFrames)    => waitForNFrames(nrFrames)

      case DoNothing               => pure(())
      case Consecutive(_, scripts) => scripts.traverse_(interpret)
      case DebugScript(msg)        => lift(Logging.log(s"DEBUG: $msg"))
      case IfThen(name, expr, thenB, elseB) =>
        evalToBool(expr).flatMap(br => interpret(if (br.b) thenB else elseB))

      case SaveUnlockedDoor(at: Point) => pure(()) // TODO
      case OpenChest(at: Point)        => pure(()) // TODO
      case Move(from, to, dir) =>
        interpret(
          HoldButtonUntilScript(Button.fromDir(dir), Eq(GetPosition, Value(PositionLit(to))))
        )
      case Goto(to: Point) =>
        for {
          currentLoc <- getGame.map(_.currentLoc)
          res        <- goto(from = currentLoc, to = to)
        } yield res
    }

    lift(Logging.log(s"interpreting: $s")) *> run *> syncGameState
  }

  // TODO: we have to figure out a way to return an updated graph here
  //  if we've discovered new things.
  def goto(from: Point, to: Point): StateT[IO, Game, Unit] = {
    for {
      graph <- getGame.map(_.graph)
      path: Path                     = graph.shortestPath(from, List(to), 0, _ => 1).head
      commands: Seq[MovementCommand] = path.convertPathToCommands
      scripts: Seq[Script] = commands.map {
        case OpenDoorAt(p, dir)          => OpenDoor(p, dir)
        case MoveCommand(from, to, Warp) => TakeStairs(from, to)
        case MoveCommand(from, to, dir)  => Move(from, to, dir)
      }
      res <- scripts.traverse_(s => interpret(s))
    } yield res
  }

  def debugPath(path: Path, commands: Seq[MovementCommand], scripts: Seq[Script]): IO[Unit] = for {
    _ <- Logging.log("PATH ----")
    _ <- path.path.traverse(Logging.log)
    _ <- Logging.log("COMMANDS ----")
    _ <- commands.traverse(Logging.log)
    _ <- Logging.log("SCRIPTS ----")
    _ <- scripts.traverse(Logging.log)
    _ <- Logging.log("-------")
  } yield ()

  sealed trait ConditionResult
  case class BoolRes(b: Boolean) extends ConditionResult {
    def negate: BoolRes = BoolRes(!b)
  }
  case class IntRes(i: Int) extends ConditionResult {
    def +(o: IntRes): IntRes   = IntRes(i + o.i)
    def -(o: IntRes): IntRes   = IntRes(i - o.i)
    def *(o: IntRes): IntRes   = IntRes(i * o.i)
    def /(o: IntRes): IntRes   = IntRes(i / o.i)
    def <(o: IntRes): BoolRes  = BoolRes(i < o.i)
    def >(o: IntRes): BoolRes  = BoolRes(i > o.i)
    def <=(o: IntRes): BoolRes = BoolRes(i <= o.i)
    def >=(o: IntRes): BoolRes = BoolRes(i >= o.i)
  }
  object IntRes {
    def min(l: IntRes, r: IntRes): IntRes = IntRes(math.min(l.i, r.i))
    def max(l: IntRes, r: IntRes): IntRes = IntRes(math.max(l.i, r.i))
  }
  case class MapIdRes(m: MapId)    extends ConditionResult
  case class PositionRes(p: Point) extends ConditionResult

  def eval(c: Expr): StateT[IO, Game, ConditionResult] = c match {
    case Not(c)      => evalToBool(c).map(_.negate)
    case Eq(l, r)    => for { l_ <- eval(l); r_ <- eval(r) } yield BoolRes(l_ == r_)
    case NotEq(l, r) => for { l_ <- eval(l); r_ <- eval(r) } yield BoolRes(l_ != r_)
    case Add(l, r)   => intOp(l, r)(_ + _)
    case Sub(l, r)   => intOp(l, r)(_ - _)
    case Mult(l, r)  => intOp(l, r)(_ * _)
    case Div(l, r)   => intOp(l, r)(_ / _)
    case Min(l, r)   => intOp(l, r)(IntRes.min)
    case Max(l, r)   => intOp(l, r)(IntRes.max)
    case Lt(l, r)    => intOp(l, r)(_ < _)
    case LtEq(l, r)  => intOp(l, r)(_ <= _)
    case Gt(l, r)    => intOp(l, r)(_ > _)
    case GtEq(l, r)  => intOp(l, r)(_ >= _)
    case Exists(cs)  => for { bs <- cs.traverse(evalToBool) } yield BoolRes(bs.exists(_.b))
    case All(cs)     => for { bs <- cs.traverse(evalToBool) } yield BoolRes(bs.forall(_.b))

    //
    case GetMapId       => getGame.map(g => MapIdRes(g.currentLoc.mapId))
    case GetPosition    => getGame.map(g => PositionRes(g.currentLoc))
    case IsChestOpen(p) => StateT.pure(BoolRes(false)) // TODO

    //
    case Value(IntLit(v))      => StateT.pure(IntRes(v))
    case Value(BoolLit(v))     => StateT.pure(BoolRes(v))
    case Value(MapIdLit(v))    => StateT.pure(MapIdRes(v))
    case Value(PositionLit(p)) => StateT.pure(PositionRes(p))
  }

  def intOp(l: Expr, r: Expr)(
      f: (IntRes, IntRes) => ConditionResult
  ): StateT[IO, Game, ConditionResult] =
    for { l_ <- evalToInt(l); r_ <- evalToInt(r) } yield f(l_, r_)

  def evalToBool(c: Expr): StateT[IO, Game, BoolRes] = eval(c).flatMap {
    case b: BoolRes => StateT.pure(b)
    case _          => fail(s"Expected Bool, but condition $c resulted in Bool")
  }

  def evalToInt(c: Expr): StateT[IO, Game, IntRes] = eval(c).flatMap {
    case i: IntRes => StateT.pure(i)
    case _         => fail(s"Expected Int, but condition $c resulted in Bool")
  }

  private def waitForNFrames(nrFrames: Int): StateT[IO, Game, Unit] = for {
    currentFrame <- lift(machine.getFrameCount)
    res          <- waitUntilFrame(currentFrame + nrFrames)
  } yield res

  private def waitUntilFrame(frameToWaitUntil: Int): StateT[IO, Game, Unit] = for {
    // Logging.log(s"Waiting until $frameToWaitUntil, api.getFrameCount is: ${api.getFrameCount}")
    _ <- syncGameState
    // printGamePad()
    currentFrame <- machine.advanceOneFrame
    // Logging.log(s"currentFrame is: $currentFrame")
    _ <- if (currentFrame < frameToWaitUntil) waitUntilFrame(frameToWaitUntil) else unit
  } yield ()

  private def waitUntil(c: Expr): StateT[IO, Game, Unit] = {
    val run1: StateT[IO, Game, Unit] = for {
      // Logging.log("waiting for a frame")
      _ <- machine.advanceOneFrame
      _ <- syncGameState
      // printGamePad()
      // Logging.log("recurring")
      _ <- waitUntil(c)
    } yield ()

    for {
      _ <- lift(Logging.log(s"waiting until $c"))
      b <- evalToBool(c)
      _ <- lift(Logging.log(s"b $b"))
      _ <- if (!b.b) run1 else unit
    } yield ()
  }

  def fail(msg: String) = throw new RuntimeException(msg)

  def pure[A](a: A): StateT[IO, Game, A]      = StateT.pure(a)
  def lift[A](io: IO[A]): StateT[IO, Game, A] = StateT.liftF(io)
  def unit: StateT[IO, Game, Unit]            = StateT.pure(())

  def press(button: Button): StateT[IO, Game, Unit] = StateT.modify(_.press(button))
  def releaseAll: StateT[IO, Game, Unit]            = StateT.modify(_.releaseButton)

  def getGame: StateT[IO, Game, Game]             = StateT.get
  def setGame(game: Game): StateT[IO, Game, Unit] = StateT.set(game)
  def syncGameState: StateT[IO, Game, Unit] = for {
    game <- getGame

    // read stuff from machine into the game
    loc <- lift(machine.getLocation)
    _   <- lift(if (loc != game.currentLoc) Logging.log(s"current loc: $loc") else ().pure[IO])
    _   <- setGame(game.copy(currentLoc = loc))

    // set stuff from the game into the machine
    _ <- game.pressedButton.traverse(b => lift(machine.controller.press(b)))
  } yield ()
}
