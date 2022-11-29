package com.joshcough.dwrai

import com.joshcough.dwrai.Items.{Armor, ItemInventory, Shield, Weapon}

case class Equipment(weapon: Weapon, armor: Armor, shield: Shield)

// TODO: the levels should probably be in the Game, and just the current level should be here, right?
case class PlayerData(location: Point,
                      stats: Stats,
                      equipment: Equipment,
                      spells: Set[Spell],
                      items: ItemInventory,
                      statuses: Set[Status]
) {
  /*
  def xpToNextLevel = self.levels[self.stats.level + 1] - self.stats.xp
  def totalXpToNextLevel(startLevel) = self.levels[startLevel + 1] - self.levels[startLevel]
  def totalXpToNextLevelFromCurrentLevel = {
    val startLevel = self.stats.level
    self.levels[startLevel + 1] - self.levels[startLevel]
  }
   */
}

object SpellId {
  // 0xce | Spells unlocked   | 0x80=repel, 0x40=return, 0x20=outside, 0x10=stopspell,
  //      |                   | 0x8=radiant, 0x4=sleep, 0x2=hurt, 0x1=heal
  // 0xcf | Spells/Quest Prog | 0x2=hurtmore, 0x1=healmore
  val CE_BYTE: Int = 0xce
  val CF_BYTE: Int = 0xcf

  // CE_BYTE
  val HealId: SpellId      = SpellId(CE_BYTE, 0x1)  // 00000001
  val HurtId: SpellId      = SpellId(CE_BYTE, 0x2)  // 00000010
  val SleepId: SpellId     = SpellId(CE_BYTE, 0x4)  // 00000100
  val RadiantId: SpellId   = SpellId(CE_BYTE, 0x8)  // 00001000
  val StopspellId: SpellId = SpellId(CE_BYTE, 0x10) // 00010000
  val OutsideId: SpellId   = SpellId(CE_BYTE, 0x20) // 00100000
  val ReturnId: SpellId    = SpellId(CE_BYTE, 0x40) // 01000000
  val RepelId: SpellId     = SpellId(CE_BYTE, 0x80) // 10000000
  // CF_BYTE
  val HealmoreId: SpellId = SpellId(CF_BYTE, 0x1) // 00000001
  val HurtmoreId: SpellId = SpellId(CF_BYTE, 0x2) // 00000010

}

case class SpellId(byte: Int, spellId: Int)

case class Mp(value: Int)
case class Spell(id: SpellId, name: String, cost: Mp)

object Spell {
  import SpellId._
  val Heal: Spell      = Spell(HealId, "Heal", Mp(3))
  val Hurt: Spell      = Spell(HurtId, "Hurt", Mp(2))
  val Sleep: Spell     = Spell(SleepId, "Sleep", Mp(2))
  val Radiant: Spell   = Spell(RadiantId, "Radiant", Mp(2))
  val Stopspell: Spell = Spell(StopspellId, "Stopspell", Mp(2))
  val Outside: Spell   = Spell(OutsideId, "Outside", Mp(6))
  val Return: Spell    = Spell(ReturnId, "Return", Mp(8))
  val Repel: Spell     = Spell(RepelId, "Repel", Mp(2))
  val Healmore: Spell  = Spell(HealmoreId, "Healmore", Mp(8))
  val Hurtmore: Spell  = Spell(HurtmoreId, "Hurtmore", Mp(5))

  val spells: Map[SpellId, Spell] = List(
    Heal,
    Hurt,
    Sleep,
    Radiant,
    Stopspell,
    Outside,
    Return,
    Repel,
    Healmore,
    Hurtmore
  ).map(s => (s.id, s)).toMap

  def fromBytes(ceByte: Int, cfByte: Int): Set[Spell] = {
    def inBytes(spell: Spell): Boolean = {
      val byte: Int = if (spell.id.byte == CE_BYTE) ceByte else cfByte
      (byte & spell.id.spellId) > 0
    }
    spells.values.toList.filter(inBytes).toSet
  }

  /*
def spellIndex(spell) = list.indexOf(self.order, spell, function(s1, s2) return s1:equals(s2) end)
def contains(spell) = self:spellIndex(spell) ~= nil
   */

}

sealed trait Status { val byte: Int; val statusId: Int }

/*
 0xdf | 0x80=Hero asleep, 0x40=Enemy asleep, 0x20=Enemy's spell stopped,
      | 0x10=Hero's spell stopped, 0x8=You have left throne room,
      | 0x4=Death necklace obtained, 0x2=Returned Gwaelin, 0x1=Carrying Gwaelin

 0xcf | Spells/Quest Prog | 0x80=death necklace equipped, 0x40=cursed belt equipped,
      |                   | 0x20=fighters ring equipped, 0x10=dragon's scale equipped,
      |                   | 0x8=rainbow bridge, 0x4=stairs in charlock found
 */
object Status {
  val CF_BYTE: Int = 0xcf
  val DF_BYTE: Int = 0xdf

  case object StairsInCharlock      extends Status { val byte: Int = CF_BYTE; val statusId = 0x4  }
  case object RainbowBridge         extends Status { val byte: Int = CF_BYTE; val statusId = 0x8  }
  case object DragonScaleEquipped   extends Status { val byte: Int = CF_BYTE; val statusId = 0x10 }
  case object FightersRingEquipped  extends Status { val byte: Int = CF_BYTE; val statusId = 0x20 }
  case object CursedBeltEquipped    extends Status { val byte: Int = CF_BYTE; val statusId = 0x40 }
  case object DeathNecklaceEquipped extends Status { val byte: Int = CF_BYTE; val statusId = 0x80 }

  case object CarryingGwaelin       extends Status { val byte: Int = DF_BYTE; val statusId = 0x1  }
  case object ReturnedGwaelin       extends Status { val byte: Int = DF_BYTE; val statusId = 0x2  }
  case object DeathNecklaceObtained extends Status { val byte: Int = DF_BYTE; val statusId = 0x4  }
  case object LeftThroneRoom        extends Status { val byte: Int = DF_BYTE; val statusId = 0x8  }
  case object HeroSpellStopped      extends Status { val byte: Int = DF_BYTE; val statusId = 0x10 }
  case object EnemySpellStopped     extends Status { val byte: Int = DF_BYTE; val statusId = 0x20 }
  case object EnemyAsleep           extends Status { val byte: Int = DF_BYTE; val statusId = 0x40 }
  case object HeroAsleep            extends Status { val byte: Int = DF_BYTE; val statusId = 0x80 }

  val allStatuses = List(
    StairsInCharlock,
    RainbowBridge,
    DragonScaleEquipped,
    FightersRingEquipped,
    CursedBeltEquipped,
    DeathNecklaceEquipped,
    CarryingGwaelin,
    ReturnedGwaelin,
    DeathNecklaceObtained,
    LeftThroneRoom,
    HeroSpellStopped,
    EnemySpellStopped,
    EnemyAsleep,
    HeroAsleep
  )

  def getStatuses(cfByte: Int, dfByte: Int): Set[Status] =
    allStatuses
      .filter(s =>
        (s.byte == CF_BYTE) && ((cfByte & s.statusId) > 0) ||
          (s.byte == DF_BYTE) && ((dfByte & s.statusId) > 0)
      )
      .toSet
}
