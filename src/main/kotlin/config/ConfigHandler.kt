package deus.config

import java.io.File
import java.io.IOException

object ConfigHandler {

    @JvmStatic
    fun getConfig(path: String): List<String> {
        val lineList = mutableListOf<String>()

        File(path).useLines { lines -> lines.forEach { lineList.add(it) }}

        return lineList.toList();
    }

    @JvmStatic
    fun makeConfig(path: String, content: String) {
        val file = File(path)
        try {
            file.writeText(content)
            println("Archivo creado y escrito con éxito en $path")
        } catch (e: IOException) {
            println("Error al escribir en el archivo: ${e.message}")
        }
    }


}