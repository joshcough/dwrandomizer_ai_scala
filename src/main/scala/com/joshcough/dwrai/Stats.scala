package com.joshcough.dwrai

object Stats {
  case class CurrentHP(value: Int)
  case class MaxHP(value: Int)
  case class CurrentMP(value: Int)
  case class MaxMP(value: Int)
  case class Xp(value: Int)
  case class Gold(value: Int)
  case class Level(value: Int)
  case class Strength(value: Int)
  case class Agility(value: Int)
  case class AttackPower(value: Int)
  case class DefensePower(value: Int)
}

import Stats._

case class Stats(level: Level,
                 currentHP: CurrentHP,
                 maxHP: MaxHP,
                 currentMP: CurrentMP,
                 maxMP: MaxMP,
                 gold: Gold,
                 xp: Xp,
                 strength: Strength,
                 agility: Agility,
                 attackPower: AttackPower,
                 defensePower: DefensePower
)
