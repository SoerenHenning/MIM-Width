import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import java.util.*

class TreeDecompositor<T>(
        private val graph: Graph<T>,
        firstTieBreakerFactory: (Graph<T>) -> (Collection<T>) -> Iterable<T> = TieBreakers::createChooseMaxDegree,
        secondTieBreakerFactory: (Graph<T>) -> (Collection<T>) -> T = TieBreakers2::createChooseMaxNeighboursDegree,
        private val iterations: Int = 100
) {

    private val firstTieBreaker: (Collection<T>) -> Iterable<T> = firstTieBreakerFactory(graph) //TODO name
    private val secondTieBreaker: (Collection<T>) -> T = secondTieBreakerFactory(graph) //TODO name

    private val random = Random()

    fun compute(): TreeDecomposition<T> {
        val tree = GraphBuilder.undirected().build<Set<T>>()
        val allVertices = graph.nodes().toMutableSet()

        if (allVertices.isEmpty()) {
            return TreeDecomposition(tree, Int.MAX_VALUE)
        }

        var treeParent = allVertices.toSet() // Create a read-only copy
        tree.addNode(treeParent)
        var maxMim = Int.MIN_VALUE

        while(allVertices.size > 1) {
            val (vertex, mim) = chooseVertex(allVertices)
            maxMim = maxOf(mim, maxMim)
            allVertices.remove(vertex)

            // Add S and V/S as children
            tree.putEdge(treeParent, setOf(vertex))
            val remainingVertices = allVertices.toSet() // Create a read-only copy
            tree.putEdge(treeParent, remainingVertices)
            treeParent = remainingVertices
        }

        return TreeDecomposition(tree, maxMim)
    }


    // Given Graph G=(V,E) and V' <= V
    // Choose S <= V' s.t. max{mim(S), mim(V'-S}} is small
    // Random choice of S if tie => multiple runs reasonable
    // However, currently no measure for quality of tre decomposition so no way to compare single runs
    private fun chooseVertex(vertices: Set<T>): Pair<T, Int> {
        if (vertices.isEmpty()) {
            throw IllegalArgumentException("Graph must have at least one vertex")
        }
        var smallestMim = Int.MAX_VALUE
        var smallestMimVertices = mutableSetOf<T>() // Will be definitely overwritten since (|V| > 1)
        for(vertex in vertices) {
            // mim(S) = if isolated 0 else 1
            val mimS = if (graph.degree(vertex) == 0) 0 else 1
            // mim(V-S) = <use heuristic>
            val cut = createCut(graph, vertices.minus(vertex))
            val mimVminusS = computeMimHeuristic(cut).size
            val maxMim = maxOf(mimS, mimVminusS)
            if (maxMim < smallestMim) {
                smallestMim = maxMim
                smallestMimVertices = mutableSetOf(vertex)
            } else if (maxMim == smallestMim) {
                smallestMimVertices.add(vertex)
            }
        }
        return Pair(breakTie(smallestMimVertices), smallestMim)
    }

    private fun breakTie(vertices: Collection<T>): T {
        return if (vertices.size == 1) {
            vertices.first()
        } else {
            val remainingVertices = firstTieBreaker(vertices).toMutableSet()
            if (remainingVertices.size == 1) {
                remainingVertices.first()
            } else {
                secondTieBreaker(remainingVertices)
            }
        }
    }

    // Graph should be a cut
    private fun computeMimHeuristic(graph: Graph<T>) : Set<EndpointPair<T>> {
        val remainingEdges = HashSet(graph.edges())
        var maximumInducedMatching = emptySet<EndpointPair<T>>()
        //TODO only run several times if random choice was needed
        for (i in 1..iterations) {
            val maximumInducedMatchingTemp = HashSet<EndpointPair<T>>()
            while (remainingEdges.isNotEmpty()) {
                val edgesWithLowestDegrees = ArrayList<EndpointPair<T>>()
                var lowestDegree = Int.MAX_VALUE
                for (edge in remainingEdges) {
                    val degree = edge.asSequence().map { x -> graph.degree(x) }.sum()
                    if (degree < lowestDegree) {
                        lowestDegree = degree
                        edgesWithLowestDegrees.clear()
                        edgesWithLowestDegrees.add(edge)
                    } else if (degree == lowestDegree) {
                        edgesWithLowestDegrees.add(edge)
                    }
                }
                val selectedEdge = if (edgesWithLowestDegrees.size == 1) remainingEdges.first() else breakTieRandomly(edgesWithLowestDegrees)
                remainingEdges.remove(selectedEdge)

                for (node in selectedEdge) {
                    for (adjacentNode in graph.adjacentNodes(node)) {
                        val edge1 = EndpointPair.unordered(node, adjacentNode)
                        remainingEdges.remove(edge1)
                        for (adjacentNode2 in graph.adjacentNodes(adjacentNode)) {
                            val edge2 = EndpointPair.unordered(adjacentNode, adjacentNode2)
                            remainingEdges.remove(edge2)
                        }
                    }
                }
                maximumInducedMatchingTemp.add(selectedEdge)
            }
            if (maximumInducedMatchingTemp.size > maximumInducedMatching.size) {
                maximumInducedMatching = maximumInducedMatchingTemp
            }
        }
        return maximumInducedMatching
    }

    private fun <S> breakTieRandomly(edges: Collection<S>) : S {
        val x = this.random.nextInt(edges.size)
        return edges.asSequence().drop(x).first()
    }

    private fun createCut(graph: Graph<T>, oneSet: Set<T>, preserveVertices: Boolean = false) : Graph<T> {
        val cut = GraphBuilder.undirected().build<T>()
        if (preserveVertices) {
            graph.nodes().forEach { cut.addNode(it) }
        }
        graph.edges()
                .asSequence()
                .filter { (oneSet.contains(it.nodeU()) && !oneSet.contains(it.nodeV())) || (!oneSet.contains(it.nodeU()) && oneSet.contains(it.nodeV())) }
                .forEach { cut.putEdge(it.nodeU(), it.nodeV()) }
        return cut
    }

}

