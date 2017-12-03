package de.soerenhenning.mimwidth

import de.soerenhenning.mimwidth.graphs.DimacsImporter
import java.io.BufferedReader
import java.io.InputStreamReader

object ClasspathFileReader {

    fun build(file: String) = BufferedReader(InputStreamReader(DimacsImporter::class.java.getResourceAsStream(file)))

}