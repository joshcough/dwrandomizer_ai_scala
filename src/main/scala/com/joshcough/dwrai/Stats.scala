package com.joshcough.dwrai

object Stats {
  case class CurrentHP(value: Int) extends BoxedInt
  case class MaxHP(value: Int) extends BoxedInt
  case class CurrentMP(value: Int) extends BoxedInt
  case class MaxMP(value: Int) extends BoxedInt
  case class Xp(value: Int) extends BoxedInt
  case class Gold(value: Int) extends BoxedInt
  case class Level(value: Int) extends BoxedInt
  case class Strength(value: Int) extends BoxedInt
  case class Agility(value: Int) extends BoxedInt
  case class AttackPower(value: Int) extends BoxedInt
  case class DefensePower(value: Int) extends BoxedInt
}

import Stats._

case class Stats(level: Level,
                 currentHP: CurrentHP, maxHP: MaxHP, currentMP: CurrentMP, maxMP: MaxMP,
                 gold: Gold, xp: Xp,
                 strength: Strength, agility: Agility, attackPower: AttackPower, defensePower: DefensePower)