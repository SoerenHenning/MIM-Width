import com.google.common.graph.Graph

data class TreeDecomposition<T>(
        val tree: Graph<Set<T>>,
        val cutMimValues: Map<Set<T>, Int>
) {

    val mimValue: Int by lazy {
        cutMimValues.values.max() ?: 0
    }

}