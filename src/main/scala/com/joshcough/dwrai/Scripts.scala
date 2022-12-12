package com.joshcough.dwrai

import com.joshcough.dwrai.Button._
import com.joshcough.dwrai.MapId.{TantegelId, TantegelThroneRoomId}

case class Scripts(maps: GameMaps) {

  sealed trait Script

  sealed trait Literal
  case class BoolLit(b: Boolean)    extends Literal
  case class IntLit(int: Int)       extends Literal
  case class MapIdLit(mapId: MapId) extends Literal
  case class PositionLit(p: Point)  extends Literal

  sealed trait Expr {
    def ===(e: Expr): Expr = Eq(this, e)
    def ||(e: Expr): Expr  = Or(this, e)
    def &&(e: Expr): Expr  = And(this, e)
    def >(e: Expr): Expr   = Gt(this, e)
    def >=(e: Expr): Expr  = GtEq(this, e)
    def <(e: Expr): Expr   = Lt(this, e)
    def <=(e: Expr): Expr  = LtEq(this, e)
    def *(e: Expr): Expr   = Mult(this, e)
    def /(e: Expr): Expr   = Div(this, e)
    def +(e: Expr): Expr   = Add(this, e)
    def -(e: Expr): Expr   = Sub(this, e)
  }

  object Value {
    def apply(b: Boolean): Value = Value(BoolLit(b))
    def apply(b: Int): Value     = Value(IntLit(b))
    def apply(b: MapId): Value   = Value(MapIdLit(b))
    def apply(b: Point): Value   = Value(PositionLit(b))
  }
  case class Value(l: Literal)       extends Expr
  case class Not(c: Expr)            extends Expr
  sealed trait BinaryOp              extends Expr { val l: Expr; val r: Expr }
  case class Eq(l: Expr, r: Expr)    extends BinaryOp
  case class NotEq(l: Expr, r: Expr) extends BinaryOp
  case class Add(l: Expr, r: Expr)   extends BinaryOp
  case class Sub(l: Expr, r: Expr)   extends BinaryOp
  case class Mult(l: Expr, r: Expr)  extends BinaryOp
  case class Div(l: Expr, r: Expr)   extends BinaryOp
  case class Max(l: Expr, r: Expr)   extends BinaryOp
  case class Min(l: Expr, r: Expr)   extends BinaryOp
  case class Lt(l: Expr, r: Expr)    extends BinaryOp
  case class LtEq(l: Expr, r: Expr)  extends BinaryOp
  case class Gt(l: Expr, r: Expr)    extends BinaryOp
  case class GtEq(l: Expr, r: Expr)  extends BinaryOp
  case class Exists(cs: List[Expr])  extends Expr
  case class All(cs: List[Expr])     extends Expr
  def And(l: Expr, r: Expr): Expr = All(List(l, r))
  def Or(l: Expr, r: Expr): Expr  = Exists(List(l, r))
  val True: Expr                  = Value(true)
  val False: Expr                 = Value(false)

  case object GetMapId             extends Expr
  case object GetPosition          extends Expr
  case class IsChestOpen(p: Point) extends Expr
  case object PlayerIsDead         extends Expr
  case object EnemyIsDead          extends Expr
  case object InBattle             extends Expr
  case object CurrentDestination   extends Expr
  case object LevelingUp           extends Expr
  case object WindowDepth          extends Expr
  def AllWindowsClosed: Expr = WindowDepth === Value(0)
  def AnyWindowOpen: Expr    = WindowDepth > Value(0)

  case class HoldButtonScript(button: Button, nrFrames: Int)   extends Script
  case class HoldButtonUntilScript(button: Button, expr: Expr) extends Script
  case class Consecutive(name: String, scripts: List[Script])  extends Script
  case class While(expr: Expr, script: Script)                 extends Script
  case class WaitUntil(expr: Expr)                             extends Script
  case class WaitFor(nrFrames: Int)                            extends Script
  case class DebugScript(msg: String)                          extends Script
  case object DebugPlayerData                                  extends Script
  case object GotoDestination                                  extends Script
  case object DoNothing                                        extends Script

  case class SaveUnlockedDoor(loc: Point) extends Script
  case class OpenChest(loc: Point)        extends Script
  case object SetRandomDestination        extends Script
  case class SetDestination(p: Point)     extends Script
  case object ClearDestination            extends Script

  def gotoDestination(from: Point, to: Point)(paths: List[Path]) =
    Consecutive("Going to destination", List(goto(from, to)(paths), ClearDestination))

  def goto(from: Point, to: Point)(paths: List[Path]): Script =
    paths match {
      case Nil => DebugScript(s"WARNING: Could not find a path to $to!")
      case path :: _ =>
        val commands = path.convertPathToCommands.map {
          case OpenDoorAt(p, dir)          => OpenDoor(p, dir)
          case MoveCommand(from, to, Warp) => TakeStairs(from, to)
          case MoveCommand(from, to, dir)  => Move(from, to, dir)
        }
        Logging.logUnsafe(("Path", path))
        Logging.logUnsafe(("commands", commands))
        Consecutive(s"Goto $to from $from", commands)
    }

  def Goto(point: Point) = Consecutive(s"goto $point", List(SetDestination(point), GotoDestination))

  case class IfThen(name: String, expr: Expr, thenBranch: Script, elseBranch: Script) extends Script
  def When(name: String, expr: Expr, script: Script): Script = IfThen(name, expr, script, DoNothing)

  def OpenMenu: Script = Consecutive("Open Menu", List(A.holdFor(20), WaitFor(nrFrames = 20)))

  def OpenDoor(p: Point, dir: Direction): Script = Consecutive(
    "Open Door",
    List(
      OpenMenu,
      Down.holdFor(1),
      Down.holdFor(1),
      Right.holdFor(1),
      A.holdFor(5),
      A.holdFor(1),
      SaveUnlockedDoor(p)
    )
  )

  def TakeStairs(from: Point, to: Point): Script = Consecutive(
    "Take Stairs",
    List(
      OpenMenu,
      Down.holdFor(1),
      Down.holdFor(1),
      A.holdFor(5),
      WaitFor(60)
    )
  )

  // case class RepeatUntil(expr: Expr, script: Script) extends Script

  case class Move(from: Point, to: Point, dir: Direction) extends Script

  def OpenChestMenuing(point: Point): Script = Consecutive(
    "Menuing for opening Chest at",
    // TODO: see if we can reduce this 90 to 60 or 75. I don't remember.
    List(
      OpenMenu,
      Up.holdFor(1),
      Right.holdFor(1),
      A.holdFor(20),
      A.holdFor(1),
      OpenChest(point)
    )
  )

  def OpenChestAt(point: Point): Script = IfThen(
    name = s"Test if chest is open at $point",
    expr = IsChestOpen(point),
    thenBranch = DoNothing,
    elseBranch =
      Consecutive(s"Opening Chest at: $point", List(Goto(point), OpenChestMenuing(point)))
  )

  /* TODO: this used to be this, but i dont have the entrances in here yet
           so this needs to get done/redone somehow
           it would be nice to have all the static maps in here to to avoid reading from the rom every time.
           and probably the graphs and such too... so much to do.
  function GotoOverworld(fromMap)
    local p = entrances[fromMap][1].from
    if p.mapId ~= OverWorldId then p = entrances[p.mapId][1].from end
    return Goto(p.mapId, p.x, p.y)
  end
   */
  def GotoOverworld(from: MapId): Script =
    Goto(maps.staticMaps(from).entrances.head.from)

  val ThroneRoomOpeningGame: Script = Consecutive(
    "Tantagel Throne Room Opening Game",
    List(
      // we should already be here...but, when i monkey around its better to have this.
      Goto(Point(TantegelThroneRoomId, 3, 4)),
      Up.holdFor(10), // this makes sure we are looking at the king
      A.holdFor(10),
      A.holdFor(180), // this talks to the king
      WaitFor(5),
      A.holdFor(1),
      WaitFor(5),
      OpenChestAt(Point(TantegelThroneRoomId, 4, 4)),
      OpenChestAt(Point(TantegelThroneRoomId, 5, 4)),
      OpenChestAt(Point(TantegelThroneRoomId, 6, 1)),
      GotoOverworld(TantegelId)
      // leaveThroneRoomScript   --TODO: this would do things like use wings or cast return
    )
  )

  /*

  leaveTantegalOnFoot =
    Consecutive("Leaving Throne room via legs", {GotoOverworld(Tantegel)})

  leaveThroneRoomScript =
    IfThenScript(
      "Figure out how to leave throne room",
      HaveSpell(Return),
      Consecutive("Leaving Throne room via return", {saveWithKingScript, CastSpell(Return)}),
      IfThenScript(
        "Check to leave throne room with wings",
        HaveItem(Wings),
        Consecutive("Leaving Throne room via wings", {saveWithKingScript, UseItem(Wings)}),
        leaveTantegalOnFoot
      )
    )



    function OpenDoor(loc)
   return IfThenScript(
      "If the door closed at: " .. tostring(loc) .. ", then open it.",
      IsDoorOpen(loc),
      DoNothing,
      Consecutive("Open Door", {
        OpenMenu, PressDown(2), PressDown(2), Pres sRight(2), PressA(20), SaveUnlockedDoor(loc)
      })
    )
  end

    TakeStairs = Consecutive("Take Stairs", {
    OpenMenu,
    WaitForCmdWindowToOpen,
    RepeatUntil(WindowCursorAt(0,2), PressDown(10)),
    --PressDown(10),
    --PressDown(10),
    PressA(60),
    CloseCmdWindow,
    PressB(2),
    WaitFrames(60),
  })


   PlayerDataScript = class(Script, function(a, name, playerDataF)
      Script.init(a, name)
      a.playerDataF = playerDataF
    end)

    GetNrKeys   = PlayerDataScript("Number of magic keys player has.", function(pd) return pd.items.nrKeys end)
    GetGold     = PlayerDataScript("Amount of gold player has.", function(pd) return pd.stats.gold end)
    GetLocation = PlayerDataScript("Location of player", function(pd) return pd.loc end)
    GetMap      = PlayerDataScript("MAP", function(pd) return pd.loc.mapId end)
    GetItems    = PlayerDataScript("Player's Items", function(pd) return pd.items end)
    GetSpells   = PlayerDataScript("Player's Spells", function(pd) return pd.spells end)
    GetStatuses = PlayerDataScript("Game statuses", function(pd) return pd.statuses end)

    GetHP       = PlayerDataScript("Amount of HP the player has.", function(pd) return pd.stats.currentHP end)
    GetMP       = PlayerDataScript("Amount of MP the player has.", function(pd) return pd.stats.currentMP end)
    GetMaxHP    = PlayerDataScript("Amount of HP the player has.", function(pd) return pd.stats.maxHP end)
    GetMaxMP    = PlayerDataScript("Amount of MP the player has.", function(pd) return pd.stats.maxMP end)
   */

  /*
Contains = class(ConditionScript, function(a, container, v)
  ConditionScript.init(a, "CONTAINS: " .. tostring(v))
  a.container = container
  a.v = v
end)
   */

  def OnMap(mapId: MapId): Expr = GetMapId === Value(mapId)

  val BattleScript: Script =
    Consecutive(
      "Battle",
      List(
        A.holdUntil(EnemyIsDead || PlayerIsDead),
        WaitFor(180),
        A.holdFor(10),
        When(
          "Leveling up",
          LevelingUp,
          Consecutive("..", List(A.holdUntil(Not(LevelingUp)), A.holdFor(10), A.holdFor(10)))
        )
      )
    )

  val GameStartMenuScript: Consecutive = Consecutive(
    "Game start menu",
    List(
      Start.holdFor(20),
      Start.holdFor(20),
      A.holdFor(1),
      A.holdFor(20),
      Down.holdFor(1),
      Down.holdFor(1),
      Right.holdFor(1),
      Right.holdFor(1),
      Right.holdFor(1),
      A.holdFor(1),
      Down.holdFor(1),
      Down.holdFor(1),
      Down.holdFor(1),
      Right.holdFor(1),
      Right.holdFor(1),
      Right.holdFor(1),
      Right.holdFor(1),
      A.holdFor(30),
      Up.holdFor(30),
      A.holdFor(30)
    ).flatMap(s => List(s, WaitFor(10)))
  )

  implicit class RichButton(b: Button) {
    def holdFor(nrFrames: Int): Script =
      Consecutive(s"Hold $b for $nrFrames", List(HoldButtonScript(b, nrFrames), WaitFor(1)))
    def holdUntil(expr: Expr): Script = HoldButtonUntilScript(b, expr)
  }

  val mainScript: Script = Consecutive(
    "DWR AI",
    List(
      DebugScript("starting interpreter"),
      GameStartMenuScript,
      WaitUntil(OnMap(TantegelThroneRoomId)),
      ThroneRoomOpeningGame,
      While(True, Consecutive("...", List(SetRandomDestination, GotoDestination)))
    )
  )
}
