import com.google.common.graph.Graph

object ReducingTieBreakers {

    fun <T> chooseFirst(graph: Graph<T>, vertices: Collection<T>): Iterable<T>
            = listOf(vertices.first())
    fun <T> chooseMaxDegree(graph: Graph<T>, vertices: Collection<T>): Iterable<T>
            = vertices.maxByAll { graph.degree(it) }

    fun <T> chooseMinDegree(graph: Graph<T>, vertices: Collection<T>): Iterable<T>
            = vertices.minByAll { graph.degree(it) }

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

