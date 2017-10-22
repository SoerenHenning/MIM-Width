import com.google.common.graph.Graph

data class TreeDecomposition<T>(val tree: Graph<Set<T>>, val mimValue: Int)