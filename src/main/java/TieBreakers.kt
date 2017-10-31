import com.google.common.graph.Graph

object TieBreakers {

    fun <T> chooseFirst(graph: Graph<T>, vertices: Collection<T>): Iterable<T>
            = listOf(vertices.first())
    fun <T> chooseMaxDegree(graph: Graph<T>, vertices: Collection<T>): Iterable<T>
            = vertices.maxByAll { graph.degree(it) }

    fun <T> chooseMinDegree(graph: Graph<T>, vertices: Collection<T>): Iterable<T>
            = vertices.minByAll { graph.degree(it) }

    fun <T> createChooseFirst(graph: Graph<T>): (Collection<T>) -> Iterable<T>
            = { vertices -> listOf(vertices.first()) }

    fun <T> createChooseMaxDegree(graph: Graph<T>): (Collection<T>) -> Iterable<T>
            = { vertices -> vertices.maxByAll { graph.degree(it) }}

    fun <T> createChooseMinDegree(graph: Graph<T>): (Collection<T>) -> Iterable<T>
            = { vertices -> vertices.minByAll { graph.degree(it) }}


    // Implemented in a very functional style, no idea how this decreases the performance
    private inline fun <T, R : Comparable<R>> Iterable<T>.maxByAll(selector: (T) -> R): Iterable<T> =
            fold(mutableListOf(), { list, value -> when {
                list.isEmpty() -> mutableListOf(value)
                selector(value) > selector(list.first()) -> mutableListOf(value)
                selector(value) == selector(list.first()) -> list.apply { add(value) }
                else -> list
            } })

    // Implemented in a very functional style, no idea how this decreases the performance
    private inline fun <T, R : Comparable<R>> Iterable<T>.minByAll(selector: (T) -> R): Iterable<T> =
            fold(mutableListOf(), { list, value -> when {
                list.isEmpty() -> mutableListOf(value)
                selector(value) < selector(list.first()) -> mutableListOf(value)
                selector(value) == selector(list.first()) -> list.apply { add(value) }
                else -> list
            } })

}

object TieBreakers2 {

    fun <T> chooseFirst(graph: Graph<T>, vertices: Collection<T>): T = vertices.first()

    fun <T> chooseMaxNeighboursDegree(graph: Graph<T>, vertices: Collection<T>):  T
            = vertices.maxBy { graph.getNeighboursDegree(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    fun <T> chooseMinNeighboursDegree(graph: Graph<T>, vertices: Collection<T>): T
            = vertices.minBy { graph.getNeighboursDegree(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    fun <T> chooseMaxNeighboursEdges(graph: Graph<T>, vertices: Collection<T>): T
            = vertices.maxBy { graph.getNeighboursEdgesCount(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    fun <T> chooseMinNeighboursEdges(graph: Graph<T>, vertices: Collection<T>): T
            = vertices.minBy { graph.getNeighboursEdgesCount(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    fun <T> createChooseFirst(graph: Graph<T>): (Collection<T>) -> T = { vertices -> vertices.first() }

    fun <T> createChooseMaxNeighboursDegree(graph: Graph<T>): (Collection<T>) -> T
            = { vertices -> vertices.maxBy { graph.getNeighboursDegree(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty") }

    fun <T> createChooseMinNeighboursDegree(graph: Graph<T>): (Collection<T>) -> T
            = { vertices -> vertices.minBy { graph.getNeighboursDegree(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty") }

    fun <T> createChooseMaxNeighboursEdges(graph: Graph<T>): (Collection<T>) -> T
            = { vertices -> vertices.maxBy { graph.getNeighboursEdgesCount(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty") }

    fun <T> createChooseMinNeighboursEdges(graph: Graph<T>): (Collection<T>) -> T
            = { vertices -> vertices.minBy { graph.getNeighboursEdgesCount(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty") }

    private fun <T> Graph<T>.getNeighboursDegree(vertex: T): Int = this.adjacentNodes(vertex).sumBy { this.degree(it)}

    private fun <T> Graph<T>.getNeighboursEdgesCount(vertex: T): Int {
        val neighbours = this.adjacentNodes(vertex)
        return this.edges().asSequence().filter { neighbours.containsAll(it.toList()) }.count()
    }
}