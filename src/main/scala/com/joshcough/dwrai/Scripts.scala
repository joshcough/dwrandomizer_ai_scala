package com.joshcough.dwrai

import com.joshcough.dwrai.Button._

object Scripts {
  sealed trait Script

  sealed trait Literal
  case class BoolLit(b: Boolean)    extends Literal
  case class IntLit(int: Int)       extends Literal
  case class MapIdLit(mapId: MapId) extends Literal

  sealed trait Expr

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

  case object GetMapId extends Expr

  case class HoldButtonScript(button: Button, nrFrames: Int)        extends Script
  case class HoldButtonUntilScript(button: Button, condition: Expr) extends Script
  case class Consecutive(name: String, scripts: List[Script])       extends Script
  case class WaitUntil(condition: Expr)                             extends Script
  case class DebugScript(msg: String)                               extends Script

  /*
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

  val GameStartMenuScript: Consecutive = Consecutive(
    "Game start menu",
    List(
      Start.holdFor(30),
      Start.holdFor(30),
      A.holdFor(30),
      A.holdFor(30),
      Down.holdFor(10),
      Down.holdFor(10),
      Right.holdFor(10),
      Right.holdFor(10),
      Right.holdFor(10),
      A.holdFor(30),
      Down.holdFor(10),
      Down.holdFor(10),
      Down.holdFor(10),
      Right.holdFor(10),
      Right.holdFor(10),
      Right.holdFor(10),
      Right.holdFor(10),
      A.holdFor(30),
      Up.holdFor(30),
      A.holdFor(30)
    )
  )

  implicit class RichButton(b: Button) {
    def holdFor(nrFrames: Int): HoldButtonScript = HoldButtonScript(b, nrFrames)
  }
}
