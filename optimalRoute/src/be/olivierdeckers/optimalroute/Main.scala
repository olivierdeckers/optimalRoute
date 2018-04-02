package be.olivierdeckers.optimalroute

import org.graphstream.graph.implementations.SingleGraph

object Main {

  import ShortestPathCalculator.{calculateShortestPath, closestNodeIds}

  implicit val graph: SingleGraph = {
    val graph = new SingleGraph("map")
    graph.addAttribute("ui.stylesheet", "node { size: 2px; }")
    graph
  }

  def main(args: Array[String]): Unit = {
    val activeNodes = OSMParser
      .parseOSM(getClass.getResourceAsStream("map.osm"))

    val start = (4.43063, 51.18866)
    val waypoint = (4.42622, 51.19524)
    val end = (4.41108, 51.21175)

    val Seq(startId, waypointId, endId) =
      closestNodeIds(activeNodes, Seq(start, waypoint, end))

    calculateShortestPath(startId, waypointId, "rgb(255,0,0)")
    calculateShortestPath(waypointId, endId, "rgb(255,0,0)")

    calculateShortestPath(startId, endId, "rgb(0,0,255)")

    graph.display(false)
  }

}
