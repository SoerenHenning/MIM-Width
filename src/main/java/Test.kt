import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.Graph as JGraphTGraph

fun main(args: Array<String>) {
    val jGraphTGraph1 = buildMimEqualsOneGraph()
    val graph1 = JGraphTImporter.importGraph(jGraphTGraph1)
    println(graph1)

    val jGraphTGraph2 = buildMimEqualsTwoGraph()
    val graph2 = JGraphTImporter.importGraph(jGraphTGraph2)
    println(graph2)

    val jGraphTGraph3 = buildMimEqualsThreeGraph()
    val graph3 = JGraphTImporter.importGraph(jGraphTGraph3)
    println(graph3)
}

private fun buildMimEqualsOneGraph(): JGraphTGraph<Int, DefaultEdge> {
    val graph = SimpleGraph<Int, DefaultEdge>(DefaultEdge::class.java)
    graph.addVertex(1)
    graph.addVertex(2)
    graph.addVertex(3)
    graph.addVertex(4)
    graph.addEdge(1, 2)
    graph.addEdge(2, 3)
    graph.addEdge(3, 4)
    return graph
}

private fun buildMimEqualsTwoGraph(): JGraphTGraph<Int, DefaultEdge> {
    val graph = SimpleGraph<Int, DefaultEdge>(DefaultEdge::class.java)
    graph.addVertex(1)
    graph.addVertex(2)
    graph.addVertex(3)
    graph.addVertex(4)
    graph.addVertex(5)
    graph.addEdge(1, 2)
    graph.addEdge(2, 3)
    graph.addEdge(3, 4)
    graph.addEdge(4, 5)
    return graph
}

private fun buildMimEqualsThreeGraph(): JGraphTGraph<Int, DefaultEdge> {
    val graph = SimpleGraph<Int, DefaultEdge>(DefaultEdge::class.java)
    graph.addVertex(1)
    graph.addVertex(2)
    graph.addVertex(3)
    graph.addVertex(4)
    graph.addVertex(5)
    graph.addVertex(6)
    graph.addVertex(7)
    graph.addEdge(1, 2)
    graph.addEdge(2, 3)
    graph.addEdge(3, 4)
    graph.addEdge(4, 5)
    graph.addEdge(3, 6)
    graph.addEdge(6, 7)
    return graph
}