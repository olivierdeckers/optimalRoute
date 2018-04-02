package be.olivierdeckers.optimalroute

import utest._

object ShortestPathCalculatorTest extends TestSuite {
  val tests = Tests{
    'closestNodeIds - {
      val nodes = Map(
        "1" -> Node(10, 10),
        "2" -> Node(0, 10),
        "3" -> Node(10, 15)
      )
      val positions = Seq[(Double, Double)](
        (5.8, 10),
        (10, 10),
        (0, 0),
        (15, 7)
      )
      val result = ShortestPathCalculator.closestNodeIds(nodes, positions)
      result ==> Vector("1", "1", "2", "3")
    }
  }
}
