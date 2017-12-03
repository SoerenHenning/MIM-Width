import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import java.time.Instant

class ExactMimCalculator<T>(private val graph: Graph<T>, private val treeDecomposition: TreeDecomposition<T>) {

    fun compute(): TreeDecomposition<T> {
        val tree = treeDecomposition.tree
        val cutMimValues = HashMap<Set<T>, Int>(treeDecomposition.cutMimValues.size)
        for(edge in tree.edges()) {
            val child = edge.asSequence().minBy { it.size } ?: emptySet()
            val cut = graph.createCut(child)
            val exactMim = computeMim(cut)
            cutMimValues[child] = exactMim
        }
        return TreeDecomposition(tree, cutMimValues)
    }

    private fun computeMim(graph: Graph<T>): Int {
        return when {
            isMimEqualsZero(graph) -> 0
            !isMimGreaterOne(graph) -> 1
            !isMimGreaterTwo(graph) -> 2
            else -> Int.MAX_VALUE
        }
    }

    private fun isMimEqualsZero(graph: Graph<T>): Boolean {
        // MIM == 0 <=> No edge exists
        return graph.edges().isEmpty()
    }

    private fun isMimGreaterOne(graph: Graph<T>): Boolean {
        // Search for an induced matching of 2 => MIM is greater or equal than 2 => greater than 1
        for (firstEdgeStart in graph.nodes()) {
            for (firstEdgeEnd in graph.adjacentNodes(firstEdgeStart)) {
                val firstEdgeAdjacentNodes = graph.adjacentNodes(firstEdgeStart) + graph.adjacentNodes(firstEdgeEnd)
                for (secondEdgeStart in graph.nodes() - firstEdgeAdjacentNodes) {
                    for (secondEdgeEnd in graph.adjacentNodes(secondEdgeStart) - firstEdgeAdjacentNodes) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isMimGreaterTwo(graph: Graph<T>): Boolean {
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
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }

}