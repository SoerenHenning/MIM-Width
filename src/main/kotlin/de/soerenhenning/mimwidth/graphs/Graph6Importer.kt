package de.soerenhenning.mimwidth.graphs

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph

object Graph6Importer {

    fun importGraph(graph6: String) : MutableGraph<Int> {
        val graphBuilder = GraphBuilder.undirected()
        return importGraph(graph6, { n -> n }, graphBuilder)
    }

    fun <V> importGraph(graph6: String, vertexBuilder: (Int) -> V, builder: GraphBuilder<Any?>): MutableGraph<V> {
        val graph = builder.build<V>()

        val trimmed = graph6.replace("\n", "").replace("\r", "") //remove any new line characters
        val bytes = trimmed.toByteArray()
        bytes.filter { it < 63 || it > 126 }
                .forEach { throw IllegalArgumentException("Graph string seems to be corrupt. Illegal character detected: " + it) }

        Reader<V>(bytes, vertexBuilder, graph).read()

        return graph
    }

    private class Reader<V>(val bytes: ByteArray, val vertexBuilder: (Int) -> V, val graph: MutableGraph<V>) {

        private val vertices = HashMap<Int, V>()

        // pointers which index a specific byte/bit in the vector bytes
        private var byteIndex = 0
        private var bitIndex = 0

        fun read() {
            //Number of vertices n
            val n = getNumberOfVertices()

            for (i in 0 until n) {
                val vertex = vertexBuilder.invoke(i)
                vertices.put(i, vertex)
                graph.addNode(vertex)
            }

            //check whether there's enough data
            val requiredBytes = Math.ceil(vertices.size * (vertices.size - 1) / 12.0).toInt() + byteIndex
            if (bytes.size < requiredBytes) {
                throw IllegalArgumentException("Graph string seems to be corrupt. Not enough data to read graph6 graph")
            }
            //Read the lower triangle of the adjacency matrix of G
            for (i in 0 until vertices.size) {
                for (j in 0 until i) {
                    if (getBits(1) == 1) {
                        val source = vertices[i]
                        val target = vertices[j]
                        if (source != null && target != null) {
                            graph.putEdge(source, target)
                        }
                    }
                }
            }
        }

        private fun getNumberOfVertices(): Int {
            //Determine whether the number of vertices is encoded in 1, 4 or 8 bytes.
            val n: Int
            if (bytes.size > 8 && bytes[0] == 126.toByte() && bytes[1] == 126.toByte()) {
                byteIndex += 2 //Strip the first 2 garbage bytes
                n = getBits(36)
                if (n < 258048)
                    throw IllegalArgumentException("Graph string seems to be corrupt. Invalid number of vertices.")
            } else if (bytes.size > 4 && bytes[0] == 126.toByte()) {
                byteIndex++ //Strip the first garbage byte
                n = getBits(18)
                if (n < 63 || n > 258047)
                    throw IllegalArgumentException("Graph string seems to be corrupt. Invalid number of vertices.")
            } else {
                n = getBits(6)
                if (n < 0 || n > 62)
                    throw IllegalArgumentException("Graph string seems to be corrupt. Invalid number of vertices.")
            }

            return n
        }

        private fun getBits(count: Int): Int {
            var remaining = count
            var value = 0
            //Read minimum{bits we need, remaining bits in current byte}
            if (bitIndex > 0 || remaining < 6) {
                val x = Math.min(remaining, 6 - bitIndex)
                val mask = (1 shl x) - 1
                var y = bytes[byteIndex] - 63 shr 6 - bitIndex - x
                y = y and mask
                value = (value shl remaining) + y
                remaining -= x
                bitIndex += x
                if (bitIndex == 6) {
                    byteIndex++
                    bitIndex = 0
                }
            }

            //Read blocks of 6 bits at a time
            val blocks = remaining / 6
            for (j in 0 until blocks) {
                value = (value shl 6) + bytes[byteIndex] - 63
                byteIndex++
                remaining -= 6
            }

            //Read remaining bits
            if (remaining > 0) {
                var y = bytes[byteIndex] - 63
                y = y shr 6 - remaining
                value = (value shl remaining) + y
                bitIndex = remaining
            }
            return value
        }

    }

}