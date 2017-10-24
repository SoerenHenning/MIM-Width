import com.google.common.graph.Graph
import java.io.InputStreamReader
import java.io.BufferedReader

fun main(args: Array<String>) {

    // Different Graphs
    // Different number of iterations
    // Different first tie-breaking algorithm
    // Different second tie-breaking algorithm

    val dimacsImporter = DimacsImporter

    val dimacsGraphFiles = listOf(
            "1awd.dgf",
            "BN_28.dgf",
            "miles1500.dgf",
            "mulsol.i.5.dgf",
            "queen8_12.col",
            "zeroin.i.1.col"
    )
    val corruptedDimacsGraphFiles = listOf(
            "eil51.tsp.dgf",
            "celar06-wpp.dgf"
    )

    val dimacsGraphs = dimacsGraphFiles.asSequence().map{ Pair(it, DimacsImporter.importGraph(getClasspathFileReader("example-graphs/" + it))) }
    val corruptedDimacsGraphs = corruptedDimacsGraphFiles.asSequence().map{ Pair(it, DimacsImporter.importGraph(getClasspathFileReader("example-graphs/" + it),nodeIds = true)) }
    val graphs = dimacsGraphs + corruptedDimacsGraphs
    val iterations = listOf(10, 50, 100)
    val firstTieBreakers = mapOf<String, (Graph<Int>) -> (Collection<Int>) -> Iterable<Int>>(
            "First" to TieBreakers::createChooseFirst,
            "Max Degree" to TieBreakers::createChooseMaxDegree,
            "Min Degree" to TieBreakers::createChooseMinDegree
    )
    val secondTieBreakers = mapOf<String, (Graph<Int>) -> (Collection<Int>) -> Int>(
            "First" to TieBreakers2::createChooseFirst,
            "Max Neighbours Degree" to TieBreakers2::createChooseMaxNeighboursDegree,
            "Min Neighbours Degree" to TieBreakers2::createChooseMinNeighboursDegree,
            "Max Edges between Neighbours" to TieBreakers2::createChooseMaxNeighboursEdges,
            "Min Edges between Neighbours" to TieBreakers2::createChooseMinNeighboursEdges
    )

    println("'Graph','Vertices','Edges','Edge Density','Iterations','First Tie Breaker','Second Tie Breaker','MIM'")
    for ((graphName, graph) in graphs) {
        val numberOfVertices = graph.nodes().size
        val numberOfEdges = graph.edges().size
        val edgeDensity = numberOfEdges.toDouble() / (numberOfVertices * (numberOfVertices - 1))
        for (iteration in iterations) {
            for ((firstTieBreakerName, firstTieBreaker) in firstTieBreakers) {
                for ((secondTieBreakerName, secondTieBreaker) in secondTieBreakers) {
                    val treeDecomposition = TreeDecompositor(graph, firstTieBreaker, secondTieBreaker, iteration).compute()
                    println("'$graphName','$numberOfVertices','$numberOfEdges','$edgeDensity','$iteration','$firstTieBreakerName','$secondTieBreakerName','${treeDecomposition.mimValue}'")
                }
            }
        }
    }

}

fun getClasspathFileReader(file: String) = BufferedReader(InputStreamReader(DimacsImporter::class.java.getResourceAsStream(file)))