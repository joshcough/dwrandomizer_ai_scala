package com.joshcough.dwrai

import com.joshcough.dwrai.Overworld.OverworldId
import scala.util.Random

case class Battle(enemy: Enemy,
                  playerIsDead: Boolean = false,
                  enemyIsDead: Boolean = false,
                  battleScriptStarted: Boolean = false
)

case class GameMaps(staticMaps: Map[MapId, StaticMap], overworld: Overworld)

case class Game(maps: GameMaps,
                graph: Graph,
                playerData: PlayerData,
                levels: Seq[Level],
                pressedButton: Option[Button] = None,
                destination: Option[Point] = None,
                battle: Option[Battle] = None,
                unlockedDoors: Set[Point] = Set(),
                openChests: Set[Point] = Set(),
                enemyLocs: Map[EnemyId, Set[Point]] = Map(),
                levelingUp: Boolean = false,
                windowDepth: Int = 0
) {
  def currentLoc: Point      = playerData.location
  def press(b: Button): Game = this.copy(pressedButton = Some(b))
  def releaseButton: Game    = this.copy(pressedButton = None)
  def onOverworld: Boolean   = currentLoc.mapId == OverworldId

  def battleStarted: Boolean        = battle.exists(_.battleScriptStarted)
  def battleScriptRequired: Boolean = battle.isDefined && !battleStarted
  def startBattle: Game             = copy(battle = battle.map(b => b.copy(battleScriptStarted = true)))

  def discoverOverworldNodes: (Graph, Seq[Point]) =
    if (onOverworld)
      graph.discover(maps.overworld.getVisibleOverworldGrid(currentLoc).flatten.map(_._1))
    else (graph, Seq())

  def shortestPaths(to: Point): List[Path] = graph.shortestPath(currentLoc, List(to), 0, _ => 1)

  def encounter(enemy: Enemy): Game = {
    val newEnemyLocs = enemyLocs.get(enemy.id).map(_ + currentLoc).getOrElse(Set(currentLoc))
    copy(battle = Some(Battle(enemy)), enemyLocs = enemyLocs + (enemy.id -> newEnemyLocs))
  }

  def playerDefeated: Game = copy(battle = Some(battle.get.copy(playerIsDead = true)))
  def enemyDefeated: Game  = copy(battle = Some(battle.get.copy(enemyIsDead = true)))

  def unlockDoor(at: Point): Game = copy(unlockedDoors = unlockedDoors + at)
  def openChest(at: Point): Game  = copy(openChests = openChests + at)

  def setDestination(p: Point): Game = copy(destination = Some(p))
  def clearDestination: Game         = copy(destination = None)
  def pickRandomDestination: Game =
    copy(destination = if (onOverworld && destination.isEmpty) {
      val borderTiles: List[Point] = graph.knownWorldBorder.toList.map(_._1)
      Logging.logUnsafe(("borderTiles", borderTiles))
      val newDest = borderTiles(new Random().nextInt(borderTiles.size))
      Logging.logUnsafe(("New Destination", newDest))
      Some(newDest)
    } else destination)

  def visibleGrid: Seq[IndexedSeq[(Point, Overworld.Tile)]] =
    maps.overworld.getVisibleOverworldGrid(currentLoc).toList
}
