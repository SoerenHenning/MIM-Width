import com.google.common.graph.Graph
import java.io.InputStreamReader
import java.io.BufferedReader
import java.time.LocalDateTime
import java.util.*

fun main(args: Array<String>) {

    // Different Graphs
    // Different number of iterations
    // Different first tie-breaking algorithm
    // Different second tie-breaking algorithm

    val directory = "example-graphs"

    val dimacsGraphFiles = listOf(
            "1awd.dgf",
            "BN_28.dgf",
            "miles1500.dgf",
            "mulsol.i.5.dgf",
            "queen8_12.col",
            "zeroin.i.1.col"
            //-------
            //"BN_23.dgf",
            //"DSJR500.1c.dgf",
            //"fpsol2.i.1.dgf",
            //"inithx.i.1.dgf"
            //"le450_25c.dgf",
            //"miles1500.dgf",
            //"myciel7.dgf",
            //"queen16_16.dgf",
            //"zeroin.i.3.dgf"
            //--------
            //"1bkf.dgf",
            //"1fjl.dgf",
            //"barley.dgf",
            //"david.dgf",
            //"huck.dgf",
            //"sodoku.dgf",
            //"water.dgf",
            //"weeduk.dgf"

    )
    val corruptedDimacsGraphFiles = listOf(
            "eil51.tsp.dgf",
            "celar06-wpp.dgf"
            //"school1-pp.dgf"
    )

    val dimacsGraphs = dimacsGraphFiles.asSequence().map{ Pair(it, DimacsImporter.importGraph(getClasspathFileReader("$directory/$it"))) }
    val corruptedDimacsGraphs = corruptedDimacsGraphFiles.asSequence().map{ Pair(it, DimacsImporter.importGraph(getClasspathFileReader("$directory/$it"),nodeIds = true)) }
    //val graphs = dimacsGraphs + corruptedDimacsGraphs
    val graphs = dimacsGraphs
    //val graphs = mapOf("F}lzw" to Graph6Importer.importGraph("F}lzw"))
    //val iterations = listOf(10, 50, 100)
    val iterations = listOf(10)
    val firstTieBreakers = mapOf<String, (Graph<Int>, Collection<Int>) -> Iterable<Int>>(
            //"First" to ReducingTieBreakers::chooseFirst,
            "Max Degree" to ReducingTieBreakers::chooseMaxDegree
            //"Min Degree" to ReducingTieBreakers::chooseMinDegree
    )
    val secondTieBreakers = mapOf<String, (Graph<Int>, Collection<Int>) -> Int>(
            "First" to FinalTieBreakers::chooseFirst
            //"Max Neighbours Degree" to FinalTieBreakers::chooseMaxNeighboursDegree,
            //"Min Neighbours Degree" to FinalTieBreakers::chooseMinNeighboursDegree,
            //"Max Edges between Neighbours" to FinalTieBreakers::chooseMaxNeighboursEdges,
            //"Min Edges between Neighbours" to FinalTieBreakers::chooseMinNeighboursEdges
    )

    //val printWriter = File("result_" + LocalDateTime.now().toString().replace(':', '.') + ".txt").apply { createNewFile() }.printWriter()
    val printWriter = System.out

    printWriter.println("Start time: " + LocalDateTime.now())

    printWriter.println("'Graph','Vertices','Edges','Edge Density','Iterations','First Tie Breaker','Second Tie Breaker','MIM'")
    printWriter.flush()
    for ((graphName, graph) in graphs) {
        val numberOfVertices = graph.nodes().size
        val numberOfEdges = graph.edges().size
        val edgeDensity = (2 * numberOfEdges.toDouble()) / (numberOfVertices * (numberOfVertices - 1))
        for (iteration in iterations) {
            for ((firstTieBreakerName, firstTieBreaker) in firstTieBreakers) {
                for ((secondTieBreakerName, secondTieBreaker) in secondTieBreakers) {
                    val treeDecomposition = TreeDecompositor(graph, firstTieBreaker, secondTieBreaker, iteration, Random(42)).compute()
                    printWriter.println("'$graphName','$numberOfVertices','$numberOfEdges','$edgeDensity','$iteration','$firstTieBreakerName','$secondTieBreakerName','${treeDecomposition.mimValue}'")
                    printWriter.flush()
                }
            }
        }
    }

    printWriter.println("End time: " + LocalDateTime.now())
    printWriter.close()
}

fun getClasspathFileReader(file: String) = BufferedReader(InputStreamReader(DimacsImporter::class.java.getResourceAsStream(file)))