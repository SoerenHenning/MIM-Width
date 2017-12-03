import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder

fun <T> Graph<T>.createCut(oneSet: Set<T>, preserveVertices: Boolean = false) : Graph<T> {
    val cut = GraphBuilder.from(this).build<T>()
    if (preserveVertices) {
        this.nodes().forEach { cut.addNode(it) }
    }
    this.edges()
            .asSequence()
            .filter { (oneSet.contains(it.nodeU()) && !oneSet.contains(it.nodeV())) || (!oneSet.contains(it.nodeU()) && oneSet.contains(it.nodeV())) }
            .forEach { cut.putEdge(it.nodeU(), it.nodeV()) }
    return cut
}