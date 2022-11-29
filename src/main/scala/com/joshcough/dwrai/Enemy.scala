package com.joshcough.dwrai

object Enemy {
  val SlimeId: EnemyId         = EnemyId(0)
  val RedSlimeId: EnemyId      = EnemyId(1)
  val DrakeeId: EnemyId        = EnemyId(2)
  val GhostId: EnemyId         = EnemyId(3)
  val MagicianId: EnemyId      = EnemyId(4)
  val MagidrakeeId: EnemyId    = EnemyId(5)
  val ScorpionId: EnemyId      = EnemyId(6)
  val DruinId: EnemyId         = EnemyId(7)
  val PoltergeistId: EnemyId   = EnemyId(8)
  val DrollId: EnemyId         = EnemyId(9)
  val DrakeemaId: EnemyId      = EnemyId(10)
  val SkeletonId: EnemyId      = EnemyId(11)
  val WarlockId: EnemyId       = EnemyId(12)
  val MetalScorpionId: EnemyId = EnemyId(13)
  val WolfId: EnemyId          = EnemyId(14)
  val WraithId: EnemyId        = EnemyId(15)
  val MetalSlimeId: EnemyId    = EnemyId(16)
  val SpecterId: EnemyId       = EnemyId(17)
  val WolflordId: EnemyId      = EnemyId(18)
  val DruinlordId: EnemyId     = EnemyId(19)
  val DrollmagiId: EnemyId     = EnemyId(20)
  val WyvernId: EnemyId        = EnemyId(21)
  val RogueScorpionId: EnemyId = EnemyId(22)
  val WraithKnightId: EnemyId  = EnemyId(23)
  val GolemId: EnemyId         = EnemyId(24)
  val GoldmanId: EnemyId       = EnemyId(25)
  val KnightId: EnemyId        = EnemyId(26)
  val MagiwyvernId: EnemyId    = EnemyId(27)
  val DemonKnightId: EnemyId   = EnemyId(28)
  val WerewolfId: EnemyId      = EnemyId(29)
  val GreenDragonId: EnemyId   = EnemyId(30)
  val StarwyvernId: EnemyId    = EnemyId(31)
  val WizardId: EnemyId        = EnemyId(32)
  val AxeKnightId: EnemyId     = EnemyId(33)
  val BlueDragonId: EnemyId    = EnemyId(34)
  val StonemanId: EnemyId      = EnemyId(35)
  val ArmoredKnightId: EnemyId = EnemyId(36)
  val RedDragonId: EnemyId     = EnemyId(37)
  val Dragonlord1Id: EnemyId   = EnemyId(38)
  val Dragonlord2Id: EnemyId   = EnemyId(39)

  val Slime: Enemy = Enemy(
    SlimeId,
    "Slime",
    Strength(5),
    Agility(3),
    MinHP(2),
    MaxHP(2),
    Xp(1),
    Gold(2),
    SleepRes(0),
    StopspellRes(0),
    HurtRes(0),
    Evasion(1)
  )
  val RedSlime: Enemy = Enemy(
    RedSlimeId,
    "Red Slime",
    Strength(7),
    Agility(3),
    MinHP(3),
    MaxHP(3),
    Xp(2),
    Gold(4),
    SleepRes(0),
    StopspellRes(0),
    HurtRes(0),
    Evasion(1)
  )
  val Drakee: Enemy = Enemy(
    DrakeeId,
    "Drakee",
    Strength(9),
    Agility(6),
    MinHP(4),
    MaxHP(5),
    Xp(3),
    Gold(6),
    SleepRes(0),
    StopspellRes(0),
    HurtRes(0),
    Evasion(1)
  )
  val Ghost: Enemy = Enemy(
    GhostId,
    "Ghost",
    Strength(11),
    Agility(8),
    MinHP(6),
    MaxHP(7),
    Xp(4),
    Gold(8),
    SleepRes(0),
    StopspellRes(1),
    HurtRes(0),
    Evasion(4)
  )
  val Magician: Enemy = Enemy(
    MagicianId,
    "Magician",
    Strength(11),
    Agility(12),
    MinHP(9),
    MaxHP(12),
    Xp(8),
    Gold(16),
    SleepRes(0),
    StopspellRes(1),
    HurtRes(0),
    Evasion(1)
  )
  val Magidrakee: Enemy = Enemy(
    MagidrakeeId,
    "Magidrakee",
    Strength(14),
    Agility(14),
    MinHP(10),
    MaxHP(13),
    Xp(12),
    Gold(20),
    SleepRes(0),
    StopspellRes(1),
    HurtRes(0),
    Evasion(1)
  )
  val Scorpion: Enemy = Enemy(
    ScorpionId,
    "Scorpion",
    Strength(18),
    Agility(16),
    MinHP(10),
    MaxHP(13),
    Xp(16),
    Gold(25),
    SleepRes(0),
    StopspellRes(2),
    HurtRes(0),
    Evasion(1)
  )
  val Druin: Enemy = Enemy(
    DruinId,
    "Druin",
    Strength(20),
    Agility(18),
    MinHP(17),
    MaxHP(22),
    Xp(14),
    Gold(21),
    SleepRes(0),
    StopspellRes(2),
    HurtRes(0),
    Evasion(2)
  )
  val Poltergeist: Enemy = Enemy(
    PoltergeistId,
    "Poltergeist",
    Strength(18),
    Agility(20),
    MinHP(18),
    MaxHP(23),
    Xp(15),
    Gold(19),
    SleepRes(0),
    StopspellRes(2),
    HurtRes(0),
    Evasion(6)
  )
  val Droll: Enemy = Enemy(
    DrollId,
    "Droll",
    Strength(24),
    Agility(24),
    MinHP(15),
    MaxHP(20),
    Xp(18),
    Gold(30),
    SleepRes(0),
    StopspellRes(3),
    HurtRes(0),
    Evasion(2)
  )
  val Drakeema: Enemy = Enemy(
    DrakeemaId,
    "Drakeema",
    Strength(22),
    Agility(26),
    MinHP(12),
    MaxHP(16),
    Xp(20),
    Gold(25),
    SleepRes(2),
    StopspellRes(3),
    HurtRes(0),
    Evasion(6)
  )
  val Skeleton: Enemy = Enemy(
    SkeletonId,
    "Skeleton",
    Strength(28),
    Agility(22),
    MinHP(18),
    MaxHP(24),
    Xp(25),
    Gold(42),
    SleepRes(0),
    StopspellRes(3),
    HurtRes(0),
    Evasion(4)
  )
  val Warlock: Enemy = Enemy(
    WarlockId,
    "Warlock",
    Strength(28),
    Agility(22),
    MinHP(21),
    MaxHP(28),
    Xp(28),
    Gold(50),
    SleepRes(3),
    StopspellRes(4),
    HurtRes(0),
    Evasion(2)
  )
  val MetalScorpion: Enemy = Enemy(
    MetalScorpionId,
    "Metal Scorpion",
    Strength(36),
    Agility(42),
    MinHP(14),
    MaxHP(18),
    Xp(31),
    Gold(48),
    SleepRes(0),
    StopspellRes(4),
    HurtRes(0),
    Evasion(2)
  )
  val Wolf: Enemy = Enemy(
    WolfId,
    "Wolf",
    Strength(40),
    Agility(30),
    MinHP(25),
    MaxHP(33),
    Xp(40),
    Gold(60),
    SleepRes(1),
    StopspellRes(4),
    HurtRes(0),
    Evasion(2)
  )
  val Wraith: Enemy = Enemy(
    WraithId,
    "Wraith",
    Strength(44),
    Agility(34),
    MinHP(30),
    MaxHP(39),
    Xp(42),
    Gold(62),
    SleepRes(7),
    StopspellRes(5),
    HurtRes(0),
    Evasion(4)
  )
  val MetalSlime: Enemy = Enemy(
    MetalSlimeId,
    "Metal Slime",
    Strength(10),
    Agility(255),
    MinHP(3),
    MaxHP(3),
    Xp(255),
    Gold(6),
    SleepRes(15),
    StopspellRes(5),
    HurtRes(15),
    Evasion(1)
  )
  val Specter: Enemy = Enemy(
    SpecterId,
    "Specter",
    Strength(40),
    Agility(38),
    MinHP(25),
    MaxHP(33),
    Xp(47),
    Gold(75),
    SleepRes(3),
    StopspellRes(5),
    HurtRes(0),
    Evasion(4)
  )
  val Wolflord: Enemy = Enemy(
    WolflordId,
    "Wolflord",
    Strength(50),
    Agility(36),
    MinHP(28),
    MaxHP(37),
    Xp(52),
    Gold(80),
    SleepRes(4),
    StopspellRes(6),
    HurtRes(0),
    Evasion(2)
  )
  val Druinlord: Enemy = Enemy(
    DruinlordId,
    "Druinlord",
    Strength(47),
    Agility(40),
    MinHP(27),
    MaxHP(35),
    Xp(58),
    Gold(95),
    SleepRes(15),
    StopspellRes(6),
    HurtRes(0),
    Evasion(4)
  )
  val Drollmagi: Enemy = Enemy(
    DrollmagiId,
    "Drollmagi",
    Strength(52),
    Agility(50),
    MinHP(33),
    MaxHP(44),
    Xp(58),
    Gold(110),
    SleepRes(2),
    StopspellRes(6),
    HurtRes(0),
    Evasion(1)
  )
  val Wyvern: Enemy = Enemy(
    WyvernId,
    "Wyvern",
    Strength(56),
    Agility(48),
    MinHP(28),
    MaxHP(37),
    Xp(64),
    Gold(105),
    SleepRes(4),
    StopspellRes(7),
    HurtRes(0),
    Evasion(2)
  )
  val RogueScorpion: Enemy = Enemy(
    RogueScorpionId,
    "Rogue Scorpion",
    Strength(60),
    Agility(90),
    MinHP(30),
    MaxHP(40),
    Xp(70),
    Gold(110),
    SleepRes(7),
    StopspellRes(7),
    HurtRes(0),
    Evasion(2)
  )
  val WraithKnight: Enemy = Enemy(
    WraithKnightId,
    "Wraith Knight",
    Strength(68),
    Agility(56),
    MinHP(30),
    MaxHP(40),
    Xp(72),
    Gold(120),
    SleepRes(5),
    StopspellRes(7),
    HurtRes(3),
    Evasion(4)
  )
  val Golem: Enemy = Enemy(
    GolemId,
    "Golem",
    Strength(120),
    Agility(60),
    MinHP(115),
    MaxHP(153),
    Xp(255),
    Gold(10),
    SleepRes(15),
    StopspellRes(8),
    HurtRes(15),
    Evasion(0)
  )
  val Goldman: Enemy = Enemy(
    GoldmanId,
    "Goldman",
    Strength(48),
    Agility(40),
    MinHP(27),
    MaxHP(35),
    Xp(6),
    Gold(255),
    SleepRes(13),
    StopspellRes(8),
    HurtRes(0),
    Evasion(1)
  )
  val Knight: Enemy = Enemy(
    KnightId,
    "Knight",
    Strength(76),
    Agility(78),
    MinHP(36),
    MaxHP(47),
    Xp(78),
    Gold(150),
    SleepRes(6),
    StopspellRes(8),
    HurtRes(0),
    Evasion(1)
  )
  val Magiwyvern: Enemy = Enemy(
    MagiwyvernId,
    "Magiwyvern",
    Strength(78),
    Agility(68),
    MinHP(36),
    MaxHP(48),
    Xp(83),
    Gold(135),
    SleepRes(2),
    StopspellRes(9),
    HurtRes(0),
    Evasion(2)
  )
  val DemonKnight: Enemy = Enemy(
    DemonKnightId,
    "Demon Knight",
    Strength(79),
    Agility(64),
    MinHP(29),
    MaxHP(38),
    Xp(90),
    Gold(148),
    SleepRes(15),
    StopspellRes(9),
    HurtRes(15),
    Evasion(15)
  )
  val Werewolf: Enemy = Enemy(
    WerewolfId,
    "Werewolf",
    Strength(86),
    Agility(70),
    MinHP(53),
    MaxHP(70),
    Xp(95),
    Gold(155),
    SleepRes(7),
    StopspellRes(9),
    HurtRes(0),
    Evasion(7)
  )
  val GreenDragon: Enemy = Enemy(
    GreenDragonId,
    "Green Dragon",
    Strength(88),
    Agility(74),
    MinHP(54),
    MaxHP(72),
    Xp(135),
    Gold(160),
    SleepRes(7),
    StopspellRes(10),
    HurtRes(2),
    Evasion(2)
  )
  val Starwyvern: Enemy = Enemy(
    StarwyvernId,
    "Starwyvern",
    Strength(86),
    Agility(80),
    MinHP(56),
    MaxHP(74),
    Xp(105),
    Gold(169),
    SleepRes(8),
    StopspellRes(10),
    HurtRes(1),
    Evasion(2)
  )
  val Wizard: Enemy = Enemy(
    WizardId,
    "Wizard",
    Strength(80),
    Agility(70),
    MinHP(49),
    MaxHP(65),
    Xp(120),
    Gold(185),
    SleepRes(15),
    StopspellRes(10),
    HurtRes(15),
    Evasion(2)
  )
  val AxeKnight: Enemy = Enemy(
    AxeKnightId,
    "Axe Knight",
    Strength(94),
    Agility(82),
    MinHP(51),
    MaxHP(67),
    Xp(130),
    Gold(165),
    SleepRes(15),
    StopspellRes(11),
    HurtRes(1),
    Evasion(1)
  )
  val BlueDragon: Enemy = Enemy(
    BlueDragonId,
    "Blue Dragon",
    Strength(98),
    Agility(84),
    MinHP(74),
    MaxHP(98),
    Xp(180),
    Gold(150),
    SleepRes(15),
    StopspellRes(11),
    HurtRes(7),
    Evasion(2)
  )
  val Stoneman: Enemy = Enemy(
    StonemanId,
    "Stoneman",
    Strength(100),
    Agility(40),
    MinHP(102),
    MaxHP(135),
    Xp(155),
    Gold(148),
    SleepRes(2),
    StopspellRes(11),
    HurtRes(7),
    Evasion(1)
  )
  val ArmoredKnight: Enemy = Enemy(
    ArmoredKnightId,
    "Armored Knight",
    Strength(105),
    Agility(86),
    MinHP(75),
    MaxHP(99),
    Xp(172),
    Gold(152),
    SleepRes(15),
    StopspellRes(12),
    HurtRes(1),
    Evasion(2)
  )
  val RedDragon: Enemy = Enemy(
    RedDragonId,
    "Red Dragon",
    Strength(120),
    Agility(90),
    MinHP(80),
    MaxHP(106),
    Xp(255),
    Gold(143),
    SleepRes(15),
    StopspellRes(12),
    HurtRes(15),
    Evasion(2)
  )
  val Dragonlord1: Enemy = Enemy(
    Dragonlord1Id,
    "Dragonlord1",
    Strength(90),
    Agility(75),
    MinHP(75),
    MaxHP(100),
    Xp(0),
    Gold(0),
    SleepRes(15),
    StopspellRes(15),
    HurtRes(15),
    Evasion(0)
  )
  val Dragonlord2: Enemy = Enemy(
    Dragonlord2Id,
    "Dragonlord2",
    Strength(140),
    Agility(200),
    MinHP(150),
    MaxHP(165),
    Xp(0),
    Gold(0),
    SleepRes(15),
    StopspellRes(15),
    HurtRes(15),
    Evasion(0)
  )

  val enemiesList: List[(EnemyId, Enemy)] = List(
    SlimeId         -> Slime,
    RedSlimeId      -> RedSlime,
    DrakeeId        -> Drakee,
    GhostId         -> Ghost,
    MagicianId      -> Magician,
    MagidrakeeId    -> Magidrakee,
    ScorpionId      -> Scorpion,
    DruinId         -> Druin,
    PoltergeistId   -> Poltergeist,
    DrollId         -> Droll,
    DrakeemaId      -> Drakeema,
    SkeletonId      -> Skeleton,
    WarlockId       -> Warlock,
    MetalScorpionId -> MetalScorpion,
    WolfId          -> Wolf,
    WraithId        -> Wraith,
    MetalSlimeId    -> MetalSlime,
    SpecterId       -> Specter,
    WolflordId      -> Wolflord,
    DruinlordId     -> Druinlord,
    DrollmagiId     -> Drollmagi,
    WyvernId        -> Wyvern,
    RogueScorpionId -> RogueScorpion,
    WraithKnightId  -> WraithKnight,
    GolemId         -> Golem,
    GoldmanId       -> Goldman,
    KnightId        -> Knight,
    MagiwyvernId    -> Magiwyvern,
    DemonKnightId   -> DemonKnight,
    WerewolfId      -> Werewolf,
    GreenDragonId   -> GreenDragon,
    StarwyvernId    -> Starwyvern,
    WizardId        -> Wizard,
    AxeKnightId     -> AxeKnight,
    BlueDragonId    -> BlueDragon,
    StonemanId      -> Stoneman,
    ArmoredKnightId -> ArmoredKnight,
    RedDragonId     -> RedDragon,
    Dragonlord1Id   -> Dragonlord1,
    Dragonlord2Id   -> Dragonlord2
  )

  val enemiesMap: Map[EnemyId, Enemy] = enemiesList.toMap

}

case class MinHP(value: Int)
case class SleepRes(value: Int)
case class StopspellRes(value: Int)
case class HurtRes(value: Int)
case class Evasion(value: Int)

case class EnemyId(value: Byte)

case class Enemy(id: EnemyId,
                 name: String,
                 strength: Strength,
                 agility: Agility,
                 minHP: MinHP,
                 maxHP: MaxHP,
                 xp: Xp,
                 gold: Gold,
                 sleepRes: SleepRes,
                 stopspellRes: StopspellRes,
                 hurtRes: HurtRes,
                 evasion: Evasion
                 // this has to be a Map[EnemyId, List[Point]] stored in the Game
                 // locations: List[Point]
) {

  def oneRoundDamageRange(playerData: PlayerData): (Int, Int) = {
    val z = math.max(0, playerData.stats.attackPower.value - (agility.value / 2))
    (math.floor(z / 4).toInt, math.floor(z / 2).toInt)
  }

  // true if the enemy can be defeated (on average) in 3 turns or less.
  // TODO: but why 3...?
  // shouldn't we factor in the str of the enemy and the players hp/armor/etc?
  def canBeDefeatedByPlayer(playerData: PlayerData): Boolean = {
    val (oneRoundMin, _) = oneRoundDamageRange(playerData)
    val avgDamage        = oneRoundMin * 1.5
    // TODO: why a 3 here? that means 3 turns right? why?
    // i dont think there is a good reason. this all needs revisiting.
    (minHP.value + maxHP.value) / 2 < avgDamage * 3
  }

}

/*

Grind = class(function(a, location, enemy)
  a.location = location
  a.enemy = enemy
end)

function Grind:__tostring()
  return "Grinding: at: " .. tostring(self.location) .. ", vs: " .. tostring(self.enemy.name)
end

-- have we seen any enemies that we can kill (or have killed) ?
-- does that enemy give "good" experience (where good is 10% or more of what it takes to get to the next level)
--        hmmm.... .10% of the amount remaining? or 10% of the whole?
--    if that is true, then walk to one of the locs where we've seen that enemy and just walk back and forth
--    fighting it (and others) until we get to the next level
function getGrindInfo(playerData, game)
  local bestEnemy
  local bestEnemyLocs

  -- filter out locations
  -- remove all things that aren't the overworld
  -- remove all swamps
  -- and the neighbors of the locations (because we want back and forth and dont want to grind on a swamp)
  -- we only need to worry about the neighbors if they are _all_ swamp.
  -- if one is non-swamp, then we would want to pick that one to walk back and forth on
  -- if there are no non-swamp locations, we wont grind there.
  function filterOutUngrindableLocs(locs)
    return list.filter(locs, function(l) return
      l.mapId == OverWorldId and game.overworld:getTileAt(l.x, l.y, game) ~= Swamp
    end)
  end

  for _, enemy in ipairs(Enemies) do
    local nonSwampLocations = filterOutUngrindableLocs(enemy.locations)
    --- return only the locations who are not a swamp and have at least one non-swamp neighbor
    local nonSwampLocationsWithNonSwampNeighbors = list.filter(nonSwampLocations, function(loc)
      local gn = game.graph:grindableNeighbors(game, loc.x, loc.y)
      return #gn > 0
    end)

    if enemy:canBeDefeatedByPlayer(playerData) and
      #(nonSwampLocationsWithNonSwampNeighbors) > 0 and
      (bestEnemy == nil or bestEnemy.exp < enemy.exp) and
      enemy ~= Enemies[GoldmanId] and
      enemy ~= Enemies[DemonKnightId] and
      enemy.exp > playerData:totalXpToNextLevelFromCurrentLevel() * 0.1
    then
      bestEnemy = enemy
      bestEnemyLocs = nonSwampLocationsWithNonSwampNeighbors
    end
  end
  if bestEnemy ~= nil
  then return Grind(chooseClosestTileForGrinding(playerData.loc, bestEnemyLocs), bestEnemy)
  else return nil
  end
end

function chooseClosestTileForGrinding(playerLoc, enemyLocations)
  log.debug("picking closest tile to the player for grinding")
  local d = list.min(enemyLocations, function(t)
    return math.abs(t.x - playerLoc.x) + math.abs(t.y - playerLoc.y)
  end)
  return d
end

function Enemy:executeBattle(game)
  if not list.any(self.locations, function(loc) loc:equals(game:getLocation()) end) then
    table.insert(self.locations, game:getLocation())
    -- log.debug("have now seen " .. self.name .. " at: ", tostring(self.locations))
  end

  function battleStarted() return game.inBattle end
  function battleEnded()
    -- log.debug("self.enemyKilled", self.enemyKilled, "self.dead", self.dead, "self.inBattle: ", self.inBattle)
    return game.enemyKilled  -- i killed the enemy
      or   game.dead         -- the enemy killed me
      or   not game.inBattle -- the enemy ran
  end

  controller.waitUntil(battleStarted, 120, "battle has started")

  local enemyCanBeDefeated =
    self:canBeDefeatedByPlayer(game:readPlayerData()) or self.id == MetalSlimeId

  log.debug("canBeDefeatedByPlayer", enemyCanBeDefeated, "oneRoundDamageRange", self:oneRoundDamageRange(game:readPlayerData()))

  if enemyCanBeDefeated then
    controller.holdAUntil(battleEnded, "battle has ended")
    controller.waitFrames(180)
    controller.pressA(10)
  else
    while not game.runSuccess and not game.dead do
      controller.pressDown(2)
      controller.pressA(60)
    end
  end

  log.debug("xpToNextLevel: ", game:readPlayerData():xpToNextLevel(), "self.stats.level", game:readPlayerData().stats.level)
end
 */
