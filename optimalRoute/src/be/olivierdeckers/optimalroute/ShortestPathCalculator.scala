package be.olivierdeckers.optimalroute

import org.graphstream.algorithm.AStar
import org.graphstream.algorithm.AStar.DistanceCosts
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.{Path, Node => GraphNode}

import scala.collection.JavaConverters._

object ShortestPathCalculator {

  /** (Lon, Lag) pair */
  type Position = (Double, Double)

  val AVERAGE_RADIUS_OF_EARTH_M = 6371000

  /**
    * Haversine distance between two (lon, lat) pairs
    */
  def calculateDistanceInMeter(pos1: Position, pos2: Position): Double = {
    val latDistance = Math.toRadians(pos1._2 - pos2._2)
    val lngDistance = Math.toRadians(pos1._1 - pos2._1)
    val sinLat = Math.sin(latDistance / 2)
    val sinLng = Math.sin(lngDistance / 2)
    val a = sinLat * sinLat +
      (Math.cos(Math.toRadians(pos1._2))
        * Math.cos(Math.toRadians(pos2._2))
        * sinLng * sinLng)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    AVERAGE_RADIUS_OF_EARTH_M * c
  }

  /**
    * Calculate shortest path using A* and visualise it on the graph.
    * @return The length of the path in meters
    */
  def calculateShortestPath(startNodeId: String, endNodeId: String, color: String)(implicit graph: SingleGraph): Path = {
    graph.getNode[GraphNode](startNodeId).setAttribute("ui.style", "fill-color: rgb(0,255,0);")
    graph.getNode[GraphNode](endNodeId).setAttribute("ui.style", "fill-color: rgb(255,0,0);")

    val aStar = new AStar(graph)
    aStar.setCosts(new DistanceCosts)
    aStar.compute(startNodeId, endNodeId)
    val path = aStar.getShortestPath
    path.getEdgePath.asScala.foreach(edge => {
      edge.setAttribute("ui.style", s"fill-color: $color;size:5px;z-index:999;")
    })
    path
  }

  def pathLength(path: Path): Double = {
    path.getNodeIterator[GraphNode].asScala.sliding(2).foldLeft(0d) {
      case (acc, Seq(a, b)) =>
        val pos1 = getNodePosition(a)
        val pos2 = getNodePosition(b)
        val edgeLength = calculateDistanceInMeter(pos1, pos2)
        acc + edgeLength
    }
  }

  def getNodePosition(node: GraphNode): (Double, Double) = {
    val List(x, y) = node.getAttribute[Array[Object]]("xy").toList.map(_.asInstanceOf[Double])
    (x, y)
  }

  /**
    * Calculates the ids of the closes nodes to a list of coordinates in one iteration
    * @param pos List of (Lon, Lat) coordinates
    */
  def closestNodeIds(nodes: Map[String, Node], pos: Seq[(Double, Double)]): Vector[String] = {
    case class NodeWithDistance(id: String, x: Double, y: Double, distance: Double)
    object NodeWithDistance {
      def init: NodeWithDistance = NodeWithDistance(null, 0, 0, Double.MaxValue)
    }

    nodes.foldLeft(Vector.fill(pos.length)(NodeWithDistance.init)) {
      case (nodesWithDistance, (id, node)) =>
        val distances = pos.map { case (x, y) => (x - node.lon) * (x - node.lon) + (y - node.lat) * (y - node.lat) }

        (nodesWithDistance zip distances).map {
          case (old@NodeWithDistance(_, _, _, distance), newDistance) => if (distance > newDistance) {
            NodeWithDistance(id, node.lon, node.lat, newDistance)
          } else {
            old
          }
        }
    }.map(_.id)
  }

}
