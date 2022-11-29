package com.joshcough.dwrai

import cats.Monad
import cats.data.StateT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import com.joshcough.dwrai.Event._
import com.joshcough.dwrai.MapId.TantegelThroneRoomId
import com.joshcough.dwrai.Overworld.OverworldId
import nintaco.api.API

import scala.util.Random

case class Battle(enemy: Enemy,
                  playerIsDead: Boolean = false,
                  enemyIsDead: Boolean = false,
                  battleScriptStarted: Boolean = false
)

case class Game(maps: GameMaps,
                graph: Graph,
                playerData: PlayerData,
                levels: Seq[Level],
                pressedButton: Option[Button] = None,
                destination: Option[Point] = None,
                battle: Option[Battle] = None,
                unlockedDoors: Set[Point] = Set(),
                openChests: Set[Point] = Set(),
                enemyLocs: Map[EnemyId, Set[Point]] = Map(),
                levelingUp: Boolean = false,
                windowDepth: Int = 0
) {
  def currentLoc: Point      = playerData.location
  def press(b: Button): Game = this.copy(pressedButton = Some(b))
  def releaseButton: Game    = this.copy(pressedButton = None)
  def onOverworld: Boolean   = currentLoc.mapId == OverworldId

  def battleStarted: Boolean = battle.exists(_.battleScriptStarted)
  def battleScriptRequired   = battle.isDefined && !battleStarted
  def startBattle: Game =
    this.copy(battle = this.battle.map(b => b.copy(battleScriptStarted = true)))

  def discoverOverworldNodes: (Graph, Seq[Point]) =
    if (onOverworld)
      graph.discover(maps.overworld.getVisibleOverworldGrid(currentLoc).flatten.map(_._1))
    else (graph, Seq())
}

case class GameMaps(staticMaps: Map[MapId, StaticMap], overworld: Overworld)

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
            ThroneRoomOpeningGame,
            While(True, Consecutive("...", List(SetRandomDestination, GotoDestination)))
          )
        )

        val runAi: IO[Unit] = for {
          playerData <- machine.getPlayerData
          levels     <- machine.getLevels
          _          <- Logging.log(("player data", playerData))
          newGame = Game(gameMaps, Graph.mkGraph(gameMaps), playerData, levels)
          _ <- interpreter.interpret(script).runS(newGame)
        } yield ()

        val aiThread = new Thread(new Runnable() {
          def run(): Unit = runAi.unsafeRunSync()
        })
        aiThread.start()
        api.addDeactivateListener(() => aiThread.interrupt())
      }
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

case class Interpreter(machine: Machine, scripts: Scripts) {

  import Interpreter.ConditionRes._
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
      case SaveUnlockedDoor(at: Point) =>
        modifyGame(g => g.copy(unlockedDoors = g.unlockedDoors + at))
      case OpenChest(at: Point) => modifyGame(g => g.copy(openChests = g.openChests + at))
      case Move(from, to, dir) =>
        withButton(Button.fromDir(dir))(waitUntil(Or(Eq(GetPosition, Value(to)), InBattle)))
      case GotoDestination =>
        getGame.flatMap { g =>
          interpret(
            Consecutive("", List(goto(g.destination.getOrElse(fail("boom!")))(g), ClearDestination))
          )
        }
      case SetDestination(p) => modifyGame(g => g.copy(destination = Some(p)))
      case SetRandomDestination =>
        modifyGameF(g => pickDestination(g).map(p => g.copy(destination = p)))
      case ClearDestination => modifyGame(g => g.copy(destination = None))
    }
    log(s"interpreting: $script") *> run *> syncGameState
  }

  def withButton(button: Button)(action: GameAction_): GameAction_ =
    press(button) *> action *> releaseAll

  def goto(to: Point)(game: Game): Script = {
    val from: Point       = game.currentLoc
    val paths: List[Path] = game.graph.shortestPath(from, List(to), 0, _ => 1)
    paths match {
      case Nil => DebugScript(s"WARNING: Could not find a path to $to!")
      case path :: _ =>
        Consecutive(
          s"Goto $to from $from",
          path.convertPathToCommands.map {
            case OpenDoorAt(p, dir)          => OpenDoor(p, dir)
            case MoveCommand(from, to, Warp) => TakeStairs(from, to)
            case MoveCommand(from, to, dir)  => Move(from, to, dir)
          }
        )
    }
  }

  def eval(c: Expr): GameAction[ConditionRes] = c match {
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
    case GetMapId           => getGame.map(g => MapIdRes(g.currentLoc.mapId))
    case GetPosition        => getGame.map(g => PositionRes(g.currentLoc))
    case IsChestOpen(p)     => pure(BoolRes(false)) // TODO
    case PlayerIsDead       => getGame.map(g => BoolRes(g.battle.exists(_.playerIsDead)))
    case EnemyIsDead        => getGame.map(g => BoolRes(g.battle.exists(_.enemyIsDead)))
    case InBattle           => getGame.map(g => BoolRes(g.battle.isDefined))
    case WindowDepth        => getGame.map(g => IntRes(g.windowDepth))
    case LevelingUp         => getGame.map(g => BoolRes(g.levelingUp))
    case CurrentDestination => getGame.map(g => PositionRes(g.destination.getOrElse(fail("boom"))))
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

  private def waitUntil(c: Expr): GameAction_ =
    log(("wait until", c)) *>
      evalToBool(c).flatMap(b =>
        when(!b.b)(machine.advanceOneFrame *> syncGameState *> waitUntil(c))
      )

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

    _ <- getGame.flatMap(g => log(("leveled up", g.levelingUp)))

    // TODO this is sketchy
    _ <- getGame.flatMap(g => when(g.battleScriptRequired)(executeBattle))
  } yield ()

  def executeBattle: GameAction_ = for {
    _ <- modifyGame(g => g.startBattle)
    _ <- interpret(BattleScript)
  } yield ()

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
      case BattleStarted(enemy) =>
        modifyGame { g =>
          val newEnemyLocs =
            g.enemyLocs.get(enemy.id).map(_ + g.currentLoc).getOrElse(Set(g.currentLoc))
          g.copy(battle = Some(Battle(enemy)), enemyLocs = g.enemyLocs + (enemy.id -> newEnemyLocs))
        }
      case PlayerDefeated =>
        modifyGame(g => g.copy(battle = Some(g.battle.get.copy(playerIsDead = true))))
      case EnemyDefeated =>
        modifyGame(g => g.copy(battle = Some(g.battle.get.copy(enemyIsDead = true))))
      case LevelUp             => modifyGame(_.copy(levelingUp = true))
      case DoneLevelingUp      => modifyGame(_.copy(levelingUp = false))
      case FightEnded          => modifyGame(_.copy(battle = None))
      case MapChange(newMapId) => lift(machine.cheat)
      case WindowOpened        => modifyGame(g => g.copy(windowDepth = g.windowDepth + 1))
      case WindowRemoved       => modifyGame(g => g.copy(windowDepth = g.windowDepth - 1))
      case _                   => pure(())
    }
  } yield ()

  // TODO: this _should_ be pure, not IO. only IO because we print a few things.
  def pickDestination(game: Game): IO[Option[Point]] =
    if (game.onOverworld && game.destination.isEmpty) {
      val borderTiles: List[Point] = game.graph.knownWorldBorder.toList.map(_._1)
      for {
        _ <- logIO(("borderTiles", borderTiles))
        newDest = borderTiles(new Random().nextInt(borderTiles.size))
        _ <- logIO(("New Destination", newDest))
      } yield Some(newDest)
    } else game.destination.pure[IO]

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

  def printVisibleOverworldGrid(game: Game): IO[Unit] = {
    logIO("-----Grid-----") *>
      game.maps.overworld.getVisibleOverworldGrid(game.currentLoc).toList.traverse_ { row =>
        logIO(row.map(_._2).mkString(","))
      }
  }
}
