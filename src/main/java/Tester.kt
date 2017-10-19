import java.io.FileReader
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


    graphFiles.asSequence()
            .map { dimacsImporter.importGraph(getClasspathFileReader("example-graphs/" + it)) }
            .map { TreeDecompositor(it).compute() }
            .forEach { println(it) }

}

fun getClasspathFileReader(file: String) =  BufferedReader(InputStreamReader(DimacsImporter::class.java.getResourceAsStream(file)))