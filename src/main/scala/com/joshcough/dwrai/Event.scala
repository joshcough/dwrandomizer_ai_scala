package com.joshcough.dwrai

sealed trait Event

object Event {
  case class BattleStarted(enemy: Enemy) extends Event
  case object EnemyRun                   extends Event
  case object PlayerRunSuccess           extends Event
  case object PlayerRunFailed            extends Event
  case class MapChange(newMapId: Int)    extends Event
  case object EndRepelTimer              extends Event
  case object LevelUp                    extends Event
  case object DoneLevelingUp             extends Event
  case object DeathBySwamp               extends Event
  case object EnemyDefeated              extends Event
  case object PlayerDefeated             extends Event
  case object OpenCmdWindow              extends Event
  case object CloseCmdWindow             extends Event
  case class WindowXCursor(x: Int)       extends Event
  case class WindowYCursor(y: Int)       extends Event
  case object FightEnded                 extends Event
  case object WindowOpened               extends Event
  case object WindowRemoved              extends Event
}
