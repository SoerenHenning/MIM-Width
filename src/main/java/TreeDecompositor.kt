import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.Graphs
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

        //val initialSize = allVertices.size //TODO

        while(allVertices.size > 1) {
            //println("%.2f".format(((initialSize - allVertices.size.toDouble()) / initialSize) * 100) + "%") //TODO
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
    // Choose S in V' s.t. max{mim(S), mim(V'-S}} is small
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
            val remainingVertices = firstTieBreaker(vertices).toSet()
            if (remainingVertices.size == 1) {
                remainingVertices.first()
            } else {
                secondTieBreaker(remainingVertices)
            }
        }
    }

    // Graph should be a cut
    private fun computeMimHeuristic(graph: Graph<T>) : Set<EndpointPair<T>> {
        var maximumInducedMatching = emptySet<EndpointPair<T>>()
        for (i in 1..iterations) {
            val remainingGraph = Graphs.copyOf(graph)
            val maximumInducedMatchingTemp = HashSet<EndpointPair<T>>()
            while (remainingGraph.edges().isNotEmpty()) {
                val edgesWithLowestDegrees = ArrayList<EndpointPair<T>>()
                var lowestDegree = Int.MAX_VALUE
                for (edge in remainingGraph.edges()) {
                    val degree = edge.asSequence().map { x -> remainingGraph.degree(x) }.sum()
                    if (degree < lowestDegree) {
                        lowestDegree = degree
                        edgesWithLowestDegrees.clear()
                        edgesWithLowestDegrees.add(edge)
                    } else if (degree == lowestDegree) {
                        edgesWithLowestDegrees.add(edge)
                    }
                }
                val selectedEdge = if (edgesWithLowestDegrees.size == 1) {
                    edgesWithLowestDegrees.first()
                } else {
                    breakTieRandomly(edgesWithLowestDegrees)
                }

                remainingGraph.removeEdge(selectedEdge.nodeU(), selectedEdge.nodeV())
                for (node in selectedEdge) {
                    for (adjacentNode in remainingGraph.adjacentNodes(node).toList()) {
                        remainingGraph.removeEdge(node, adjacentNode)
                        for (adjacentNode2 in remainingGraph.adjacentNodes(adjacentNode).toList()) {
                            remainingGraph.removeEdge(adjacentNode, adjacentNode2)
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

