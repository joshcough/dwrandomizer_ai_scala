package com.joshcough.dwrai

case class Stats(level: LevelId,
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
