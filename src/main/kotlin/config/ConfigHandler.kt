package deus.config

import java.io.File
import java.io.IOException

/**
 * A utility object for handling configuration files.
 */
object ConfigHandler {

    /**
     * Reads the content of a configuration file and returns it as a list of strings.
     *
     * @param path The path to the configuration file.
     * @return A list of strings, each representing a line from the configuration file.
     */
    @JvmStatic
    fun getConfig(path: String): List<String> {
        val lineList = mutableListOf<String>()

        File(path).useLines { lines -> lines.forEach { lineList.add(it) } }

        return lineList.toList()
    }

    /**
     * Creates or overwrites a configuration file with the specified content.
     *
     * @param path The path where the configuration file will be created or overwritten.
     * @param content The content to write to the configuration file.
     * @throws IOException If an error occurs during the file writing process.
     */
    @JvmStatic
    fun makeConfig(path: String, content: String) {
        val file = File(path)
        try {
            file.writeText(content)
            println("File created and written successfully at $path")
        } catch (e: IOException) {
            println("Error writing to file: ${e.message}")
        }
    }
}
