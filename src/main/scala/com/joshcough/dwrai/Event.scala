package com.joshcough.dwrai

sealed trait Event

object Event {
  case object BattleStarted extends Event {
    /*
       if (self:getMapId() > 0) then
          local enemyId = self:getEnemyId()
          local enemy = Enemies[enemyId]
          log.debug ("entering battle vs a " .. enemy.name)
          self.inBattle = true
          self.enemy = enemy
        end
     */
  }

  case object EnemyRun extends Event {
    // log.debug("the enemy is running!")
    // self.inBattle = false
  }

  case object PlayerRunSuccess extends Event {
    //log.debug("you are running!")
    //self.runSuccess = true
  }

  case object PlayerRunFailed extends Event {
    //    log.debug("you are NOT running!")
    //    self.runSuccess = false
  }

  case class MapChange(newMapId: Int) extends Event {
    //    -- log.debug("recording that the map has changed.")
    //    self.mapChanged = true // zzz we used this in a bunch of places
  }

  case object EndRepelTimer extends Event {
    //    log.debug("repel has ended")
    //    self.repelTimerWindowOpen = true
  }

  case object LevelUp extends Event {
    // log.debug("i just leveled up.")
    // self.leveledUp = true
  }

  case object DeathBySwamp extends Event {
    //    log.debug("i just died in a swamp.")
    //    self.dead = true
  }

  case object EnemyDefeated extends Event {
    //    -- log.debug("Killed an enemy.")
    //    self.enemyKilled = true
    //    self.inBattle = false
  }

  case object PlayerDefeated extends Event {
    //    log.debug("I just got killed.")
    //    self.dead = true
    //    self.inBattle = false
  }

  case object OpenCmdWindow extends Event {
    //    log.debug("Opening Non-combat command window")
    //    self.cmdWindowOpen = true
  }

  case object CloseCmdWindow extends Event {
    //    log.debug("Closing Non-combat command window")
    //    self.cmdWindowOpen = false
  }

  case class WindowXCursor(x: Int) extends Event {
    // self.windowX = self.memory:readRAM(0xD8)
  }

  case class WindowYCursor(y: Int) extends Event {
    // self.windowY = self.memory:readRAM(0xD9)
  }

}
