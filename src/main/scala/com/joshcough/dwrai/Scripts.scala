package com.joshcough.dwrai

import com.joshcough.dwrai.Button._


object Scripts {
  sealed trait Script

  sealed trait Condition

  case class Not(c: Condition) extends Condition

  sealed trait BinaryOp                     extends Condition { val l: Condition; val r: Condition }
  case class Eq(l: Condition, r: Condition) extends BinaryOp
  // case class DotEq(l: Condition, r: Condition) extends BinaryOp
  case class NotEq(l: Condition, r: Condition) extends BinaryOp
  case class Add(l: Condition, r: Condition)   extends BinaryOp
  case class Sub(l: Condition, r: Condition)   extends BinaryOp
  case class Mult(l: Condition, r: Condition)  extends BinaryOp
  case class Div(l: Condition, r: Condition)   extends BinaryOp
  case class Max(l: Condition, r: Condition)   extends BinaryOp
  case class Min(l: Condition, r: Condition)   extends BinaryOp
  case class Lt(l: Condition, r: Condition)    extends BinaryOp
  case class LtEq(l: Condition, r: Condition)  extends BinaryOp
  case class Gt(l: Condition, r: Condition)    extends BinaryOp
  case class GtEq(l: Condition, r: Condition)  extends BinaryOp

  case class Exists(cs: List[Condition])  extends Condition
  case class All(cs: List[Condition])  extends Condition

  def And(l: Condition, r: Condition): Condition = All(List(l, r))
  def Or(l: Condition, r: Condition): Condition = Exists(List(l, r))


  case class HoldButtonScript(button: Button, nrFrames: Int)             extends Script
  case class HoldButtonUntilScript(button: Button, condition: Condition) extends Script
  case class Consecutive(name: String, scripts: List[Script])            extends Script
  case class WaitUntil(condition: Condition)

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
