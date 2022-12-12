package com.joshcough.dwrai

import cats.Monad
import cats.data.StateT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import com.joshcough.dwrai.Event._
import nintaco.api.API

object Interpreter {
  def runMain(api: API): IO[Unit] = IO {
    val aiThread = new Thread(new Runnable() {
      def run(): Unit = runNewGame(api).unsafeRunSync()
    })
    aiThread.start()
    api.addDeactivateListener(() => aiThread.interrupt())
  }

  def runNewGame(api: API): IO[Unit] = {
    val machine = Machine(api)
    for {
      newGame <- machine.newGame
      interpreter = Interpreter(machine, newGame.maps)
      _ <- interpreter.interpret(interpreter.scripts.mainScript).runS(newGame)
    } yield ()
  }

  object ConditionRes {
    sealed trait ConditionRes
    case class BoolRes(b: Boolean) extends ConditionRes {
      def negate: BoolRes = BoolRes(!b)
    }
    case class IntRes(i: Int) extends ConditionRes {
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
    case class MapIdRes(m: MapId)    extends ConditionRes
    case class PositionRes(p: Point) extends ConditionRes
  }
}

case class Interpreter(machine: Machine, gameMaps: GameMaps) {

  import Interpreter.ConditionRes._

  val scripts: Scripts = Scripts(gameMaps)
  import scripts._

  type GameAction[T] = StateT[IO, Game, T]
  type GameAction_   = GameAction[Unit]

  def interpret(script: Script): GameAction_ = {
    val run: GameAction_ = script match {
      // Lower level (language level) scripts
      case HoldButtonScript(button, nrFrames)       => withButton(button)(waitForNFrames(nrFrames))
      case HoldButtonUntilScript(button, condition) => withButton(button)(waitUntil(condition))
      case WaitUntil(condition)                     => waitUntil(condition)
      case WaitFor(nrFrames)                        => waitForNFrames(nrFrames)
      case DoNothing                                => pure(())
      case Consecutive(_, scripts)                  => scripts.traverse_(interpret)
      case DebugScript(msg)                         => log(s"DEBUG: $msg")
      case DebugPlayerData                          => log()
      case IfThen(name, expr, thenB, elseB) =>
        evalToBool(expr).flatMap(br => interpret(if (br.b) thenB else elseB))
      case While(expr, s) =>
        def loop: GameAction_ =
          log(("in while loop", expr, s)) *> whenE(expr)(interpret(s) *> syncGameState *> loop)
        loop
      // These are higher level scripts that require the game
      // which leads me to believe there should be a separate GameScript constructor or something.
      case SaveUnlockedDoor(at: Point) => modifyGame(_.unlockDoor(at))
      case OpenChest(at: Point)        => modifyGame(_.openChest(at))
      case Move(from, to, dir) =>
        withButton(Button.fromDir(dir))(waitUntil(Or(Eq(GetPosition, Value(to)), InBattle)))
      case GotoDestination =>
        getGame.flatMap { g =>
          val to: Point = g.destination.getOrElse(fail("boom!"))
          interpret(scripts.gotoDestination(g.currentLoc, to)(g.shortestPaths(to)))
        }
      case SetDestination(p)    => modifyGame(g => g.copy(destination = Some(p)))
      case SetRandomDestination => modifyGame(_.pickRandomDestination)
      case ClearDestination     => modifyGame(g => g.copy(destination = None))
    }
    log(s"interpreting: $script") *> run *> syncGameState
  }

  def withButton(button: Button)(action: GameAction_): GameAction_ =
    press(button) *> action *> releaseAll

  def goto(to: Point)(game: Game): Script =
    scripts.goto(game.currentLoc, to)(game.shortestPaths(to))

  def eval(expr: Expr): GameAction[ConditionRes] = {
    val go: GameAction[ConditionRes] = expr match {
      // Literals
      case Value(IntLit(v))      => pure(IntRes(v))
      case Value(BoolLit(v))     => pure(BoolRes(v))
      case Value(MapIdLit(v))    => pure(MapIdRes(v))
      case Value(PositionLit(p)) => pure(PositionRes(p))

      // Language level Exprs
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
      case Exists(cs)  => cs.traverse(evalToBool).map(bs => BoolRes(bs.exists(_.b)))
      case All(cs)     => cs.traverse(evalToBool).map(bs => BoolRes(bs.forall(_.b)))

      // Game Level Exprs
      case GetMapId       => getGame.map(g => MapIdRes(g.currentLoc.mapId))
      case GetPosition    => getGame.map(g => PositionRes(g.currentLoc))
      case IsChestOpen(p) => pure(BoolRes(false)) // TODO
      case PlayerIsDead   => getGame.map(g => BoolRes(g.battle.exists(_.playerIsDead)))
      case EnemyIsDead    => getGame.map(g => BoolRes(g.battle.exists(_.enemyIsDead)))
      case InBattle       => getGame.map(g => BoolRes(g.battle.isDefined))
      case WindowDepth    => getGame.map(g => IntRes(g.windowDepth))
      case LevelingUp     => getGame.map(g => BoolRes(g.levelingUp))
      case CurrentDestination =>
        getGame.map(g => PositionRes(g.destination.getOrElse(fail("boom"))))
    }

    for {
      res <- go
      _   <- log((expr, res))
    } yield res
  }

  def intOp(l: Expr, r: Expr)(f: (IntRes, IntRes) => ConditionRes): GameAction[ConditionRes] =
    for { l_ <- evalToInt(l); r_ <- evalToInt(r) } yield f(l_, r_)

  def evalToBool(c: Expr): GameAction[BoolRes] = eval(c).flatMap {
    case b: BoolRes => pure(b)
    case _          => fail(s"Expected Bool, but condition $c resulted in Bool")
  }

  def evalToInt(c: Expr): GameAction[IntRes] = eval(c).flatMap {
    case i: IntRes => pure(i)
    case _         => fail(s"Expected Int, but condition $c resulted in Bool")
  }

  private def waitForNFrames(nrFrames: Int): GameAction_ =
    lift(machine.getFrameCount).flatMap(f => waitUntilFrame(f + nrFrames))

  private def waitUntilFrame(frameToWaitUntil: Int): GameAction_ = for {
    _            <- syncGameState
    currentFrame <- machine.advanceOneFrame
    _            <- when(currentFrame < frameToWaitUntil)(waitUntilFrame(frameToWaitUntil))
  } yield ()

  private def waitUntil(e: Expr): GameAction_ =
    whenE(Not(e))(machine.advanceOneFrame *> syncGameState *> waitUntil(e))

  def fail(msg: String)                                          = throw new RuntimeException(msg)
  def pure[A](a: A): GameAction[A]                               = StateT.pure(a)
  def lift[A](io: IO[A]): GameAction[A]                          = StateT.liftF(io)
  def unit: GameAction_                                          = StateT.pure(())
  def when[M[_]: Monad](b: Boolean)(f: => M[Unit]): M[Unit]      = if (b) f else ().pure[M]
  def whenM[M[_]: Monad](fb: M[Boolean])(f: => M[Unit]): M[Unit] = fb.flatMap(when(_)(f))
  def whenE(e: Expr)(f: => GameAction_): GameAction_             = whenM(evalToBool(e).map(_.b))(f)
  def log(a: Any): GameAction_                                   = lift(Logging.log(a))
  def logIO(a: Any): IO[Unit]                                    = Logging.log(a)
  def press(button: Button): GameAction_                         = StateT.modify(_.press(button))
  def releaseAll: GameAction_                                    = StateT.modify(_.releaseButton)
  def getGame: GameAction[Game]                                  = StateT.get
  def setGame(game: Game): GameAction_                           = StateT.set(game)
  def modifyGame: (Game => Game) => StateT[IO, Game, Unit]       = StateT.modify[IO, Game]
  def modifyGameF: (Game => IO[Game]) => StateT[IO, Game, Unit]  = StateT.modifyF[IO, Game]
  def logG[A](msg: String)(f: Game => A): GameAction_            = getGame.flatMap(g => log((msg, f(g))))

  def syncGameState: GameAction_ = for {
    // read stuff from machine into the game
    _ <- updatePlayerData

    // TODO: do these two lines really count as syncing the game state?
    // they are doing a lot of logic
    // seems like it should come in a phase after this. like, sync, and then act.
    // update the graph with any newly discovered nodes
    _ <- discoverOverworldNodesM

    // set stuff from the game into the machine
    _ <- getGame.flatMap(_.pressedButton.traverse(b => lift(machine.controller.press(b))))

    // handle events (TODO: should we do this immediately instead of last?)
    _ <- machine.pollEvent.traverse(handleEvent)

    // if a battle has started in the game, we handle that here.
    _ <- getGame.flatMap(g => when(g.battleScriptRequired)(executeBattle))
  } yield ()

  def executeBattle: GameAction_ = modifyGame(_.startBattle) *> interpret(BattleScript)

  def updatePlayerData: StateT[IO, Game, Unit] = modifyGameF { g =>
    for {
      pd <- machine.getPlayerData
      _  <- when(pd != g.playerData)(logPlayerData)
    } yield g.copy(playerData = pd)
  }

  def logPlayerData: IO[Unit] =
    machine.getPlayerData.flatMap(pd => logIO(("==== Player Data ====", pd)))

  // get any events from the machine and update the game state
  def handleEvent(event: Event): GameAction_ = for {
    _ <- log(("== EVENT ==", event))
    _ <- event match {
      case BattleStarted(enemy) => modifyGame(_.encounter(enemy))
      case PlayerDefeated       => modifyGame(_.playerDefeated)
      case EnemyDefeated        => modifyGame(_.enemyDefeated)
      case LevelUp              => modifyGame(_.copy(levelingUp = true))
      case DoneLevelingUp       => modifyGame(_.copy(levelingUp = false))
      case FightEnded           => modifyGame(_.copy(battle = None))
      case WindowOpened         => modifyGame(g => g.copy(windowDepth = g.windowDepth + 1))
      case WindowRemoved        => modifyGame(g => g.copy(windowDepth = g.windowDepth - 1))
      case MapChange(newMapId)  => lift(machine.cheat)
      case _                    => pure(())
    }
  } yield ()

  def discoverOverworldNodesM: GameAction_ =
    for {
      game <- getGame
      (updatedGraph, newlyDiscoveredPoints) = game.discoverOverworldNodes
      _ <- lift(
        newlyDiscoveredPoints.traverse(p =>
          logIO(("discovered", p, game.maps.overworld.getTileAt(p.x, p.y)))
        )
      )
      _ <- setGame(game.copy(graph = updatedGraph))
    } yield ()

  def printVisibleOverworldGrid(game: Game): IO[Unit] = for {
    _ <- logIO("-----Grid-----")
    _ <- game.visibleGrid.traverse_ { row => logIO(row.map(_._2).mkString(",")) }
  } yield ()
}
