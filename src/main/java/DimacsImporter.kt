import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import java.io.Reader
import java.io.BufferedReader
import java.io.StringReader
import java.util.HashMap
import java.util.Arrays

object DimacsImporter {

    fun importGraph(dimacsGraph: String) : MutableGraph<Int> {
        val graphBuilder = GraphBuilder.undirected()


        return importGraph(StringReader(dimacsGraph), { n -> n} , graphBuilder)
    }

    fun <V> importGraph(reader: Reader, vertexBuilder: (Int) -> V, builder: GraphBuilder<Any?>): MutableGraph<V> {
        val graph = builder.build<V>()

        val bufferedReader = reader as? BufferedReader ?: BufferedReader(reader)

        // add nodes
        val size = readNodeCount(bufferedReader)
        val vertices = HashMap<Int, V>(size)
        for (id in 1..size) {
            val vertex = vertexBuilder.invoke(id)
            vertices.put(id, vertex)
            graph.addNode(vertex)
        }

        // add edges
        var readLineResult = readNextLine(bufferedReader)
        while (readLineResult is ReadLineResult.Column) {
            val cols = readLineResult.content
            if (cols[0] == "e" || cols[0] == "a") {
                if (cols.size < 3) {
                    throw IllegalArgumentException("Failed to parse edge:" + Arrays.toString(cols))
                }
                val source = try {
                    Integer.parseInt(cols[1])
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Failed to parse edge source node:" + e.message, e)
                }

                val target = try {
                    Integer.parseInt(cols[2])
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Failed to parse edge target node:" + e.message, e)
                }

                val from = vertices[source] ?: throw IllegalArgumentException("Node $source does not exist")
                val to = vertices[target] ?: throw IllegalArgumentException("Node $target does not exist")
                graph.putEdge(from, to)
            }
            readLineResult = readNextLine(bufferedReader)
        }

        return graph
    }

    private fun split(src: String): Array<String> {
        return src.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private fun readNextLine(reader: BufferedReader): ReadLineResult {
        var cols: Array<String> = emptyArray()
        while (cols.isEmpty() || cols[0] == "c" || cols[0].startsWith("%")) {
            cols = split(reader.readLine() ?: break)
        }
        return if (cols.isEmpty()) ReadLineResult.EndOfStream else ReadLineResult.Column(cols)
    }

    private fun readNodeCount(reader: BufferedReader): Int {
        val readLineResult = readNextLine(reader)
        val cols = when(readLineResult) {
            is ReadLineResult.EndOfStream -> throw IllegalArgumentException("Failed to read number of vertices.")
            is ReadLineResult.Column -> readLineResult.content
        }
        if (cols[0] == "p") {
            if (cols.size < 3) {
                throw IllegalArgumentException("Failed to read number of vertices.")
            }
            val nodes = try {
                Integer.parseInt(cols[2])
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Failed to read number of vertices.")
            }

            if (nodes < 0) {
                throw IllegalArgumentException("Negative number of vertices.")
            }
            return nodes
        }
        throw IllegalArgumentException("Failed to read number of vertices.")
    }

    private sealed class ReadLineResult() {
        data class Column(val content: Array<String>): ReadLineResult() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Column

                if (!Arrays.equals(content, other.content)) return false

                return true
            }

            override fun hashCode(): Int {
                return Arrays.hashCode(content)
            }
        }

        object EndOfStream: ReadLineResult()
    }

}

fun main(args: Array<String>) {

    val s = """p edge 8 18
 n 0
 n 1
n 2
n 3
n 4
n 5
n 6
n 7
e 4 1
e 4 2
e 5 1
e 5 3
e 5 4
e 6 1
e 6 2
e 6 3
e 7 1
e 7 2
e 7 3
e 7 4
e 8 1
e 8 2
e 8 3
e 8 4
e 8 5
e 8 7
"""
    val t = "p edge 2 1\nn 1\nn 2\ne 1 2";

    val graph = DimacsImporter.importGraph(s)

    println(graph)
}