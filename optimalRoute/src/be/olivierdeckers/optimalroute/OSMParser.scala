package be.olivierdeckers.optimalroute

import java.io.InputStream
import java.util.UUID

import org.graphstream.graph.Edge
import org.graphstream.graph.{Node => GraphNode}
import org.graphstream.graph.implementations.SingleGraph

import scala.collection.mutable
import scala.util.Try
import scala.xml.XML

object OSMParser {

  def parseOSM(input: InputStream)(implicit graph: SingleGraph): Map[String, Node] = {
    val map = XML.load(getClass.getClassLoader.getResourceAsStream("map.osm"))

    val nodes = (map \ "node")
      .map(node => node \@ "id" -> Node((node \@ "lat").toDouble, (node \@ "lon").toDouble))
      .toMap

    val nodesAdded: mutable.Set[String] = mutable.Set()
    (map \ "way")
      .filter(way => (way \ "tag").exists(tag => {
        val key = tag \@ "k"
        key == "highway" || key == "junction"
      }))
      .foreach(way => {
        val nodeIds = (way \ "nd").map(_ \@ "ref")

        nodeIds.foreach(id => {
          if (!nodesAdded.contains(id)) {
            val node = nodes(id)
            val graphNode: GraphNode = graph.addNode(id)
            graphNode.addAttribute("xy", node.lon.asInstanceOf[Object], node.lat.asInstanceOf[Object])
            graphNode.addAttribute("layout.frozen")
            nodesAdded.add(id)
          }
        })

        nodeIds.sliding(2).foreach { case Seq(a, b) =>
          // Edges might be added twice to nodes
          Try {
            graph.addEdge[Edge](UUID.randomUUID().toString, a, b)
          }
        }
      })

    val activeNodes = nodes.filter(pair => nodesAdded.contains(pair._1))
    activeNodes
  }

}
