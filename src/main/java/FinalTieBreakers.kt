import com.google.common.graph.Graph

object FinalTieBreakers {

    fun <T> chooseFirst(graph: Graph<T>, vertices: Collection<T>): T = vertices.first()

    fun <T> chooseMaxNeighboursDegree(graph: Graph<T>, vertices: Collection<T>):  T
            = vertices.maxBy { graph.getNeighboursDegree(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    fun <T> chooseMinNeighboursDegree(graph: Graph<T>, vertices: Collection<T>): T
            = vertices.minBy { graph.getNeighboursDegree(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    fun <T> chooseMaxNeighboursEdges(graph: Graph<T>, vertices: Collection<T>): T
            = vertices.maxBy { graph.getNeighboursEdgesCount(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    fun <T> chooseMinNeighboursEdges(graph: Graph<T>, vertices: Collection<T>): T
            = vertices.minBy { graph.getNeighboursEdgesCount(it) } ?: throw IllegalArgumentException("Collection of vertices must not be empty")

    private fun <T> Graph<T>.getNeighboursDegree(vertex: T): Int = this.adjacentNodes(vertex).sumBy { this.degree(it)}

    private fun <T> Graph<T>.getNeighboursEdgesCount(vertex: T): Int {
        val neighbours = this.adjacentNodes(vertex)
        return this.edges().asSequence().filter { neighbours.containsAll(it.toList()) }.count()
    }
}