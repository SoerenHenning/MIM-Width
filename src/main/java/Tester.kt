import com.google.common.graph.Graph
import java.io.InputStreamReader
import java.io.BufferedReader

fun main(args: Array<String>) {

    // Different Graphs
    // Different number of iterations
    // Different first tie-breaking algorithm
    // Different second tie-breaking algorithm

    val dimacsImporter = DimacsImporter

    //eil51.tsp.dgf
    //celar06-wpp.dgf
    val graphFiles = listOf(
            "1awd.dgf",
            "BN_28.dgf",
            "miles1500.dgf",
            "mulsol.i.5.dgf",
            "queen8_12.col",
            "zeroin.i.1.col"
    )

    val graphs = graphFiles.asSequence().map{ Pair(it, dimacsImporter.importGraph(getClasspathFileReader("example-graphs/" + it))) }
    val iterations = listOf(10, 100)
    val firstTieBreakers = mapOf<String, (Graph<Int>) -> (Collection<Int>) -> Iterable<Int>>(
            "Max Degree" to TieBreakers::createChooseMaxDegree,
            "Min Degree" to TieBreakers::createChooseMinDegree
    )
    val secondTieBreakers = mapOf<String, (Graph<Int>) -> (Collection<Int>) -> Int>(
            "Max Neighbours Degree" to TieBreakers2::createChooseMaxNeighboursDegree,
            "Min Neighbours Degree" to TieBreakers2::createChooseMinNeighboursDegree,
            "Max Edges between Neighbours" to TieBreakers2::createChooseMaxNeighboursEdges,
            "Min Edges between Neighbours" to TieBreakers2::createChooseMinNeighboursEdges
    )

    for ((graphName, graph) in graphs) {
        for (iteration in iterations) {
            for ((firstTieBreakerName, firstTieBreaker) in firstTieBreakers) {
                for ((secondTieBreakerName, secondTieBreaker) in secondTieBreakers) {
                    val treeDecomposition = TreeDecompositor(graph, firstTieBreaker, secondTieBreaker, iteration).compute()
                    println("$graphName,$iteration,$firstTieBreakerName,$secondTieBreakerName,${treeDecomposition.mimValue}")
                }
            }
        }
    }

}

fun getClasspathFileReader(file: String) = BufferedReader(InputStreamReader(DimacsImporter::class.java.getResourceAsStream(file)))