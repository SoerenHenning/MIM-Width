import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import org.jgrapht.Graph as JGraphTGraph

object JGraphTImporter  {

    fun <V,E> importGraph(jGraphTGraph: org.jgrapht.Graph<V,E>, builder: GraphBuilder<Any?>) : MutableGraph<V> {
        val guavaGraph = builder.build<V>()
        jGraphTGraph.vertexSet().forEach { v -> guavaGraph.addNode(v) }
        for(edge in jGraphTGraph.edgeSet()) {
            val source = jGraphTGraph.getEdgeSource(edge)
            val target = jGraphTGraph.getEdgeTarget(edge)
            guavaGraph.putEdge(source, target)
        }
        return guavaGraph
    }

    fun <V,E> importGraph(jGraphTGraph: org.jgrapht.Graph<V, E>) : MutableGraph<V> {
        val vertexCount = jGraphTGraph.vertexSet().size
        val graphBuilder = GraphBuilder.undirected().expectedNodeCount(vertexCount)
        return importGraph(jGraphTGraph, graphBuilder)
    }

}