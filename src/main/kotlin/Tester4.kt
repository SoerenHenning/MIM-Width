import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.asSequence

fun main(args: Array<String>) {

    val directory = "graphs/twlib"

    Files.list(Paths.get("src/main/resources/graphs/twlib"))
            .asSequence()
            .map { it.fileName.toString() }
            .filter { try {
                DimacsImporter.importGraph(getClasspathFileReader("$directory/$it"), nodeIds = true)
                return@filter true;
            } catch (e: Exception) {
                return@filter false;
            } }
            .forEach {
                println("\"$it\",")
            }



}
