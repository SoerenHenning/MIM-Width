import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import java.io.Reader
import java.io.BufferedReader
import java.util.HashMap
import java.util.Arrays

object DimacsImporter {

    fun importGraph(
            reader: Reader,
            builder: GraphBuilder<Any?> = GraphBuilder.undirected(),
            nodeIds: Boolean = false
    ): MutableGraph<Int> {
        return importGraph(reader, builder, nodeIds, {n -> n})
    }

    fun <V> importGraph(
            reader: Reader,
            builder: GraphBuilder<Any?> = GraphBuilder.undirected(),
            nodeIds: Boolean = false,
            vertexBuilder: (Int) -> V
    ): MutableGraph<V> {
        val graph = builder.build<V>()

        val bufferedReader = reader as? BufferedReader ?: BufferedReader(reader)

        // create nodes map
        val size = readNodeCount(bufferedReader)
        val vertices = HashMap<Int, V>(size)

        var readLineResult = readNextLine(bufferedReader)

        // add nodes
        if (nodeIds) {
            while (readLineResult is ReadLineResult.Column && readLineResult[0] == "n") {
                val vertexId = try {
                    Integer.parseInt(readLineResult[1])
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Failed to parse node:" + e.message, e)
                }
                val vertex = vertexBuilder.invoke(vertexId)
                vertices.put(vertexId, vertex)
                graph.addNode(vertex)
                readLineResult = readNextLine(bufferedReader)
            }
        } else {
            for (id in 1..size) {
                val vertex = vertexBuilder.invoke(id)
                vertices.put(id, vertex)
                graph.addNode(vertex)
            }
        }

        // add edges
        while (readLineResult is ReadLineResult.Column) {
            if (readLineResult[0] == "e" || readLineResult[0] == "a") {
                if (readLineResult.size < 3) {
                    throw IllegalArgumentException("Failed to parse edge:" + readLineResult)
                }
                val source = try {
                    Integer.parseInt(readLineResult[1])
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Failed to parse edge source node:" + e.message, e)
                }

                val target = try {
                    Integer.parseInt(readLineResult[2])
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
        var columns: Array<String> = emptyArray()
        while (columns.isEmpty() || columns[0] == "c" || columns[0].startsWith("%") || columns[0].startsWith("X")) {
            columns = split(reader.readLine() ?: break)
        }
        return if (columns.isEmpty()) ReadLineResult.EndOfStream else ReadLineResult.Column(columns)
    }

    private fun readNodeCount(reader: BufferedReader): Int {
        val readLineResult = readNextLine(reader)
        when (readLineResult) {
            is ReadLineResult.EndOfStream -> throw IllegalArgumentException("Failed to read number of vertices.")
            is ReadLineResult.Column ->  {
                if (readLineResult[0] == "p") {
                    if (readLineResult.size < 3) {
                        throw IllegalArgumentException("Failed to read number of vertices.")
                    }
                    val nodes = try {
                        Integer.parseInt(readLineResult[2])
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
        }
    }

    private sealed class ReadLineResult() {
        data class Column(val content: Array<String>): ReadLineResult() {

            val size
                get() = content.size

            operator fun get(index: Int): String {
                return content[index]
            }

            override fun toString(): String {
                return Arrays.toString(content)
            }

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