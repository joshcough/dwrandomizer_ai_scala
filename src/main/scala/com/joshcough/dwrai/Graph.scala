package com.joshcough.dwrai

import com.joshcough.dwrai.Overworld.OverworldId
import com.joshcough.dwrai.Warps.STATIC_WARPS

object Graph {

  def neighborsAt(staticMap: StaticMap, xIn: Int, yIn: Int): Set[Neighbor] = {
    def isWalkable(x: Int, y: Int): Boolean = staticMap.isWalkableAt(x, y)
    def isDoor(x: Int, y: Int): Boolean     = staticMap.getTileAt(x, y).isDoor

    //         x,y-1
    // x-1,y   x,y     x+1,y
    //         x,y+1
    def directNeighbors(x: Int, y: Int): List[Neighbor] =
      // if we can't walk to the node, dont bother including the node in the graph at all
      if (!isWalkable(x, y) && !isDoor(x, y)) List()
      else {
        def f(inBounds: Boolean, x_ : Int, y_ : Int, dir: Direction): List[Neighbor] = {
          // TODO: this isDoor call is just for testing
          // all the walkable shit has to get redone i think.
          if (inBounds && (isWalkable(x_, y_) || isDoor(x_, y_)))
            List(Neighbor(Point(staticMap.mapId, x_, y_), dir))
          else List()
        }

        f(x > 0, x - 1, y, West) ++
          f(x < staticMap.width.value - 1, x + 1, y, East) ++
          f(y > 0, x, y - 1, North) ++
          f(y < staticMap.height.value - 1, x, y + 1, South)
        // really useful for debugging pathing. just plug in the location you care about
        // if staticMap.mapId == 7 && x == 19 && y == 23 then log.debug("n", res) end
      }

    def warpNeighbor(x: Int, y: Int): Option[Neighbor] =
      STATIC_WARPS.get(Point(staticMap.mapId, x, y)).map(p => Neighbor(p, Warp))

    def borderNeighbors(x: Int, y: Int): List[Neighbor] = {
      // this only applies to maps where you can walk directly out onto the overworld.
      // this is just an optimization. it wouldn't matter much if this wasn't here.
      // excepppppt... this code really works on the first entrance. swamp cave has two entrances
      // so if we happened to do dungeons in here, i think things would be weird. ugh.
      val connectsToOverworld =
        staticMap.mapType == MapType.Town || staticMap.mapType == MapType.Both

      def n(condition: Boolean, dir: Direction): List[Neighbor] =
        if (condition) List(Neighbor(staticMap.entrances.head.from, dir))
        else List()

      if (isWalkable(x, y) && connectsToOverworld && staticMap.entrances.nonEmpty) {
        n(x == 0, West) ++
          n(x == staticMap.width.value - 1, East) ++
          n(y == 0, North) ++
          n(y == staticMap.height.value - 1, South)
      } else List()
    }

    def entranceNeighbors(x: Int, y: Int): List[Neighbor] =
      staticMap.entrances
        .filter(e => e.to == Point(staticMap.mapId, x, y))
        .map(e => Neighbor(e.from, Warp))

    (directNeighbors(xIn, yIn) ++
      borderNeighbors(xIn, yIn) ++
      warpNeighbor(xIn, yIn).toList ++
      entranceNeighbors(xIn, yIn)).toSet
  }

  def mkStaticMapGraph(staticMap: StaticMap): Graph =
    Graph.fromFlat(Map(staticMap.mapId -> staticMap.rows.zipWithIndex.map { case (r, y) =>
      r.zipWithIndex.map { case (_, x) =>
        val point = Point(staticMap.mapId, x, y)
        val node  = GraphNode.known(neighborsAt(staticMap, x, y), staticMap.getTileAt(x, y).isDoor)
        (point, node)
      }
    }))

  def mkOverworldGraph(overworld: Overworld): Graph = {
    def neighbors(x: Int, y: Int): Set[Neighbor] = {
      def f(inBounds: Boolean, x_ : Int, y_ : Int, dir: Direction): List[Neighbor] =
        if (inBounds) {
          if (overworld.getTileAt(x_, y_).walkable)
            List(Neighbor(Point(OverworldId, x_, y_), dir))
          else List()
        } else List()
      f(x > 0, x - 1, y, West) ++
        f(x < 118, x + 1, y, East) ++
        f(y > 0, x, y - 1, North) ++
        f(y < 118 - 1, x, y + 1, South)
    }.toSet

    Graph.fromFlat(Map(Overworld.OverworldId -> overworld.tiles.zipWithIndex.map { case (row, y) =>
      row.zipWithIndex.map { case (tile, x) =>
        (Point(Overworld.OverworldId, x, y), GraphNode.unknown(neighbors(x, y)))
      }
    }))
  }

  // this makes the entire graph, overworld and all static maps
  def mkGraph(gameMaps: GameMaps): Graph = {
    val staticMapGraphs: Iterable[Graph] = gameMaps.staticMaps.values.map(mkStaticMapGraph)
    val overworldGraph                   = mkOverworldGraph(gameMaps.overworld)
    (Seq(overworldGraph) ++ staticMapGraphs).foldLeft(Graph(Map())) { _ + _ }
  }

  def fromFlat(nodes: Map[MapId, Seq[Seq[(Point, GraphNode)]]]): Graph =
    Graph(nodes.toList.flatMap { case (_, arr) => arr.flatten }.toMap)
}

sealed trait GraphNodeType
case object Unknown extends GraphNodeType // tiles on the overworld that we haven't discovered yet.
case object Known
    extends GraphNodeType // tiles on the overworld that we have discovered, or tiles on static maps.

// invariant: neighbors must be empty if nodeType == UNKNOWN.
// it _might_ be empty if we have discovered it
// but only if its not walkable, or there's literally no possible way to get to it.
// like a grass node surrounded by mountains. there would not be a path to it.
case class GraphNode(nodeType: GraphNodeType, neighbors: Set[Neighbor], isDoor: Boolean = false) {
  def mkKnown: GraphNode = this.copy(nodeType = Known)
  def isKnown            = nodeType == Known
}

object GraphNode {
  def unknown(neighbors: Set[Neighbor]): GraphNode = GraphNode(Unknown, neighbors)
  def known(neighbors: Set[Neighbor], isDoor: Boolean): GraphNode =
    GraphNode(Known, neighbors, isDoor)
}

sealed trait Direction { def oppositeDir: Direction }
case object West  extends Direction { def oppositeDir: Direction = East  }
case object East  extends Direction { def oppositeDir: Direction = West  }
case object North extends Direction { def oppositeDir: Direction = North }
case object South extends Direction { def oppositeDir: Direction = South }
// ok this is a little weird. But I guess it kinda is a direction you can go in.
case object Warp extends Direction { def oppositeDir: Direction = Warp }

case class Neighbor(point: Point, dir: Direction) {
  def isAt(p: Point): Boolean = p == point
}

// TODO: make these indexedseq
case class Graph(nodes: Map[Point, GraphNode]) {
  // def prettyPrint: String = ...

  def +(other: Graph): Graph = Graph(nodes ++ other.nodes)

  //def getNodeAtPoint(p: Point): Option[GraphNode] = nodes.get(p)
  def isDoor(p: Point): Boolean = nodes(p).isDoor

  def staticMapDijkstra(src: Point, dests: List[Point], nrKeys: Int): List[Path] =
    dijkstra(src, dests, nrKeys, _ => 1)

  def shortestPath(src: Point,
                   dests: List[Point],
                   nrKeys: Int,
                   weightFn: Point => Int
  ): List[Path] = dijkstra(src, dests, nrKeys, weightFn)

  // Find the shortest path between the current and dest nodes
  // returns paths sorted by weight, ASC
  def dijkstra(src: Point, dests: List[Point], nrKeys: Int, weightFn: Point => Int): List[Path] = {

    // NOTE: it might be possible to get rid of this some day.
    // but its definitely encapsulated here so its not that bad.
    // i feel like we should be able to get rid of the mutable 'trail' and 'distanceTo' vals
    // by changing the while look to an unfold (still until pq.isEmpty)
    // and we can just build those up during the unfold.
    // but tbh what we have here really isnt that bad.
    import scala.collection.mutable

    case class Trail(trail: Map[Point, Neighbor], weights: Map[Point, Int])

    val trail: Trail = {
      object QueueNode {
        implicit val pointOrdering: Ordering[QueueNode] = (a: QueueNode, b: QueueNode) =>
          a.weight.compare(b.weight)
      }
      case class QueueNode(point: Point, weight: Int)

      val distanceTo: mutable.Map[Point, Int] = mutable.Map(src -> 0)
      val trail: mutable.Map[Point, Neighbor] = mutable.Map()

      def getDistanceTo(p: Point): Int = distanceTo.getOrElse(p, Int.MaxValue)

      val pq: mutable.PriorityQueue[QueueNode] = mutable.PriorityQueue[QueueNode](QueueNode(src, 0))

      while (pq.nonEmpty) {
        val current           = pq.dequeue()
        val distanceToCurrent = getDistanceTo(current.point)
        val neighbors: Set[Neighbor] = {
          val n = nodes(current.point)
          if (n.isKnown) n.neighbors else Set()
        }
        neighbors.foreach { neighbor =>
          val newWeight = distanceToCurrent + weightFn(neighbor.point)
          if (newWeight < getDistanceTo(neighbor.point)) {
            distanceTo.put(neighbor.point, newWeight)
            trail.put(neighbor.point, Neighbor(current.point, neighbor.dir))
            pq.enqueue(QueueNode(neighbor.point, newWeight))
          }
        }
      }
      Trail(trail.toMap, distanceTo.toMap)
    }

    // Create path string from table of previous nodes
    def followTrailToDest(trail: Map[Point, Neighbor])(dest: Point): List[PathNode] = {
      Iterator
        .unfold[PathNode, (Point, Option[Neighbor])]((dest, trail.get(dest))) { case (curr, op) =>
          op.map { prev =>
            if (src == prev.point)
              (PathNode(prev.point, curr, prev.dir, isDoor = false), (prev.point, None))
            else
              (
                PathNode(prev.point, curr, prev.dir, isDoor(prev.point)),
                (prev.point, trail.get(prev.point))
              )
          }
        }
        .toList
        .reverse
    }

    val pathBuilder: Point => List[PathNode] = followTrailToDest(trail.trail)(_)
    dests
      .flatMap { dest =>
        trail.weights.get(dest) match {
          case None    => List()
          case Some(_) => List(Path(src, dest, trail.weights(dest), pathBuilder(dest), 0))
        }

      }
      .sortWith(_.weight < _.weight)
  }

  /* This should only ever be called on overworld nodes
   * TODO: But I think it makes sense to also all it on the two dungeons
   * that need to be discovered. We should make their two entrances
   * "unknown" in the graph.
   */
  def discover(ps: Seq[Point]): (Graph, Seq[Point]) = {
    val undiscoveredNodes = ps.filter(p => !nodes(p).isKnown)
    val updatedGraph      = Graph(nodes ++ undiscoveredNodes.map(p => (p, nodes(p).mkKnown)).toMap)
    (updatedGraph, undiscoveredNodes)
  }

  // NOTE: if this is slow we could call it whenever discover is called and cache it
  def knownWorldBorder: Set[(Point, GraphNode)] = {
    // Logging.logUnsafe("OVERWORLD NODES!")
    // nodes.toList.filter { case (p, _) => p.mapId == OverworldId }.foreach(Logging.logUnsafe)
    nodes.filter { case (p, gn) =>
      p.mapId == OverworldId && (gn.nodeType match {
        case Unknown => false
        // this is saying: if you are discovered, one of your neighbors is undiscovered
        // then YOU are on the border. you are a border tile.
        // because you bump up against the unknown. :)
        case Known => gn.neighbors.exists(n => nodes(n.point).nodeType == Unknown)
      })
    }.toSet
  }
}

case class PathNode(from: Point, to: Point, dir: Direction, isDoor: Boolean)

// TODO: in the lua code we had pathBeforeOverworld and pathRest
case class Path(src: Point, dest: Point, weight: Int, path: List[PathNode], nrKeysRequired: Int) {
  def isEmpty: Boolean = path.isEmpty
  def convertPathToCommands: List[MovementCommand] =
    path.zip(path.drop(1).map(Option(_)) ++ List(None)).flatMap { case (node, nextNode) =>
      if (nextNode.exists(_.isDoor))
        List(OpenDoorAt(node.to, node.dir), MoveCommand(node.from, node.to, node.dir))
      else List(MoveCommand(node.from, node.to, node.dir))
    }
}

sealed trait MovementCommand
case class OpenDoorAt(p: Point, dir: Direction)                extends MovementCommand
case class MoveCommand(from: Point, to: Point, dir: Direction) extends MovementCommand

/*
  Logging.logUnsafe("--------- BAD PATH ---------")
  Logging.logUnsafe("----trail----")
  trail.trail.foreach(Logging.logUnsafe)
  Logging.logUnsafe("----weights----")
  trail.weights.foreach(Logging.logUnsafe)
  Logging.logUnsafe("----------------------------")
 */
