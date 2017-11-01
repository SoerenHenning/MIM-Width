import com.google.common.graph.Graph
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

fun main(args: Array<String>) {



    val directory = "graphs/twlib"

    val dimacsGraphFiles = listOf<String>(
            //"1a8o.dgf", //6
            //"1g2r.dgf", //6
            //"anna.dgf" //7
            //"huck.dgf" //3
            //"mainuk.dgf" //4
            //"mildew.dgf", //4
            //"miles250.dgf", //4
            //"myciel6.dgf", //10
            //"oesoca42.dgf", //3
            //"pathfinder.dgf", //3
            //"weeduk.dgf" //1|2
            //"1plc.dgf", //10
            //"1lkk.dgf", // 10
            //"1l9l.dgf" //5
            //"jean.dgf" //4
            //"sodoku.dgf", //8
            //"queen8_8.dgf", //9
            //"1a62.dgf" //9
    )
    val corruptedDimacsGraphFiles = listOf<String>(
            //"celar03-pp.dgf", //4
            //"celar04-pp.dgf", //7
            //"graph03.dgf" //12
            //"munin2-pp.dgf", //6
            //"pigs-pp.dgf" //6
            //"celar07-pp.dgf" //5
            //"pcb3038-pp-003.dgf", //4
            //"celar01-pp.dgf", //6
            //"graph01.dgf" //13
    )

    val dimacsGraphs = dimacsGraphFiles.asSequence().map{ Pair(it, DimacsImporter.importGraph(getClasspathFileReader("$directory/$it"))) }
    val corruptedDimacsGraphs = corruptedDimacsGraphFiles.asSequence().map{ Pair(it, DimacsImporter.importGraph(getClasspathFileReader("$directory/$it"),nodeIds = true)) }
    //val graphs = dimacsGraphs + corruptedDimacsGraph
    val graphs = ExampleGraphs.graphs.filter { it.graph.nodes().size in 60..100 }

    val iterations = 10
    val firstTieBreaker: (Graph<Int>, Collection<Int>) -> Iterable<Int> = ReducingTieBreakers::chooseMinDegree
    val secondTieBreaker: (Graph<Int>, Collection<Int>) -> Int  = FinalTieBreakers::chooseMaxNeighboursEdges

    for ((graphName, graph) in graphs) {
        val treeDecomposition = TreeDecompositor(graph, firstTieBreaker, secondTieBreaker, iterations, Random(42)).compute()
        val approximatedMim = treeDecomposition.mimValue
        if (approximatedMim <= 2) {
            val exactMim = ExactMimCalculator(graph, treeDecomposition).compute().mimValue
            if (exactMim <= 2) {
                println("[ALERT] $graphName     Upper Bound for MIM Width: $exactMim")
                println("'$graphName','$approximatedMim','$exactMim'")
            } else {
                println("'$graphName','$approximatedMim',''")
            }
        } else {
            println("'$graphName','$approximatedMim',''")
        }
    }

}
