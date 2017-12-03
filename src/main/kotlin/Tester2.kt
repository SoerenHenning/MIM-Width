import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import java.time.Instant
import java.util.*

fun main(args: Array<String>) {

    //val graphFile = "example-graphs/celar06-wpp.dgf"
    //val graphFile = "example-graphs/zeroin.i.1.col"
    val graphFile = "example-graphs/miles1500.dgf"
    val graph = DimacsImporter.importGraph(getClasspathFileReader(graphFile), nodeIds = false)
    val iterations = 10
    val firstTieBreaker: (Graph<Int>, Collection<Int>) -> Iterable<Int> = ReducingTieBreakers::chooseMinDegree
    val secondTieBreaker: (Graph<Int>, Collection<Int>) -> Int  = FinalTieBreakers::chooseFirst

    val treeDecomposition = TreeDecompositor(graph, firstTieBreaker, secondTieBreaker, iterations, Random(42)).compute()

    println(treeDecomposition)
    checkMimWidth(graph, treeDecomposition)

}

fun <T> checkMimWidth(graph: Graph<T>, treeDecomposition: TreeDecomposition<T>) {
    val tree = treeDecomposition.tree
    for((i, edge) in tree.edges().withIndex()) {
        val child = edge.asSequence().minBy { it.size } ?: emptySet()
        val cut = graph.createCut(child)
        val approximatedMim = treeDecomposition.cutMimValues[child]
        println("${Instant.now()} [Start computeMim()] Cut Size: ${cut.edges().size} Child: $child Cut: $cut")
        val exactMim = computeMim(cut)
        println("${Instant.now()} [Finished computeMim()] ")
        if (approximatedMim != exactMim) {
            println("Error!   $approximatedMim | $exactMim | $child")
        }
        println("${Instant.now()} [Finished] $i/${tree.edges().size} ${i.toDouble() / tree.edges().size}")
    }
}

private fun <T> computeMim(graph: Graph<T>): Int {
    return when {
        isMimEqualsZero(graph) -> 0
        isMimEqualsOne(graph) -> 1
        isMimEqualsTwo(graph) -> 2
        else -> Int.MAX_VALUE
    }
}

private fun <T> isMimEqualsZero(graph: Graph<T>): Boolean {
    // MIM == 0 <=> No edge exists
    return graph.edges().isEmpty()
}

private fun <T> isMimGreaterOne(graph: Graph<T>): Boolean {
    // Search for an induced matching of 2 => MIM is greater or equal than 2 => greater than 1
    for (firstEdge in graph.edges()) {
        for (secondEdge in graph.edges()) {

            // if e1 and e2 not joined by an edge or facing each other => return true
            //if (firstSource != secondSource && firstTarget != secondSource && firstSource != secondTarget && firstTarget != secondTarget) {
            if (!graph.checkIfEdgesAreAdjacent(firstEdge, secondEdge)) {
                if (!graph.containsEdgeBetween(firstEdge, secondEdge)) {
                    return true
                }

            }
        }
    }
    return false
}

private fun <T> isMimEqualsOne(graph: Graph<T>): Boolean {
    return !isMimEqualsZero(graph) && !isMimGreaterOne(graph)
}

// O(|E|^3 * 3 * |E|) = O(|E|^4)
private fun <T> isMimGreaterTwo(graph: Graph<T>): Boolean {
    // Search for an induced matching of 3 => MIM is greater or equal than 3 => greater than 2
    for (firstEdge in graph.edges()) {
        for (secondEdge in graph.edges()) {
            for (thirdEdge in graph.edges()) {
                // if e1 and e2 and e3 not joined by an edge or facing each other => return true
                if (!graph.checkIfEdgesAreAdjacent(firstEdge, secondEdge) && !graph.checkIfEdgesAreAdjacent(firstEdge, thirdEdge) && !graph.checkIfEdgesAreAdjacent(secondEdge, thirdEdge)) {
                    if (!graph.containsEdgeBetween(firstEdge, secondEdge) && !graph.containsEdgeBetween(firstEdge, thirdEdge) && !graph.containsEdgeBetween(secondEdge, thirdEdge)) {
                        return true
                    }
                }
            }
        }
    }
    return false
}

// O(|E|^3 * 3 * |E|) = O(|E|^4)
private fun <T> isMimGreaterTwo2(graph: Graph<T>): Boolean {
    // Search for an induced matching of 3 => MIM is greater or equal than 3 => greater than 2
    for (firstEdgeStart in graph.nodes()) {
        for (firstEdgeEnd in graph.adjacentNodes(firstEdgeStart)) {
            val firstEdgeAdjacentNodes = graph.adjacentNodes(firstEdgeStart) + graph.adjacentNodes(firstEdgeEnd)
            for (secondEdgeStart in graph.nodes() - firstEdgeAdjacentNodes) {
                for (secondEdgeEnd in graph.adjacentNodes(secondEdgeStart) - firstEdgeAdjacentNodes) {
                    val secondEdgeAdjacentNodes = graph.adjacentNodes(secondEdgeStart) + graph.adjacentNodes(secondEdgeEnd)
                    val firstAndSecondEdgeAdjacentNodes = firstEdgeAdjacentNodes + secondEdgeAdjacentNodes
                    for (thirdEdgeStart in graph.nodes()- firstAndSecondEdgeAdjacentNodes) {
                        for (thirdEdgeEnd in graph.adjacentNodes(thirdEdgeStart) - firstAndSecondEdgeAdjacentNodes) {
                            //return true
                            val x = graph.containsEdgeBetween(EndpointPair.unordered(firstEdgeStart, firstEdgeEnd), EndpointPair.unordered(secondEdgeStart, secondEdgeEnd))
                            println("Between 1. and 2.: " + x)
                            val y = graph.containsEdgeBetween(EndpointPair.unordered(firstEdgeStart, firstEdgeEnd), EndpointPair.unordered(thirdEdgeStart, thirdEdgeEnd))
                            println("Between 1. and 3.: " + y)
                            val z = graph.containsEdgeBetween(EndpointPair.unordered(secondEdgeStart, secondEdgeEnd), EndpointPair.unordered(thirdEdgeStart, thirdEdgeEnd))
                            println("Between 2. and 2.: " + z)

                            return true
                            /*
                            val allVertices = listOf(firstEdgeStart, firstEdgeEnd, secondEdgeStart, secondEdgeEnd, thirdEdgeStart, thirdEdgeEnd)
                            if (allVertices.size == allVertices.distinct().size) {
                                return true
                            }
                            */
                        }
                    }
                }
            }
        }
    }
    return false
}

private fun <T> isMimEqualsTwo(graph: Graph<T>): Boolean {
    return isMimGreaterOne(graph) && !isMimGreaterTwo2(graph)
}

// O(|E|)
private fun <T> Graph<T>.containsEdgeBetween(firstEdge: EndpointPair<T>, secondEdge: EndpointPair<T>): Boolean {
    return firstEdge.flatMap { this.adjacentNodes(it) }.intersect(secondEdge).isNotEmpty()
}

// O(1)
private fun <T> Graph<T>.checkIfEdgesAreAdjacent(firstEdge: EndpointPair<T>, secondEdge: EndpointPair<T>): Boolean {
    return firstEdge.intersect(secondEdge).isNotEmpty()
}