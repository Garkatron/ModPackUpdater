package deus.github
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

object Downloader {

    @JvmStatic
    fun downloadGitHubRelease(owner: String, repo: String, tagName: String, assetName: String, outputPath: String) {
        val url = "https://github.com/$owner/$repo/releases/download/$tagName/$assetName"
        val client = OkHttpClient()

        val request = Request.Builder().url(url).build()

        val response: Response = client.newCall(request).execute()

        if (!response.isSuccessful) throw IOException("Failed to download file: ${response.message}")

        val file = File(outputPath)
        file.sink().buffer().use { sink ->
            response.body?.source()?.let { sink.writeAll(it) }
        }

        println("Downloaded $assetName to $outputPath")
    }

    /**
     * Descarga un archivo desde la URL proporcionada y lo guarda en el camino especificado.
     *
     * @param fileUrl La URL completa del archivo a descargar.
     * @param outputPath La ruta donde se guardará el archivo descargado.
     * @throws IOException Si ocurre un error durante la descarga o el guardado del archivo.
     */
    @JvmStatic
    fun downloadFile(fileUrl: String, outputPath: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()

        val response: Response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("Failed to download file: ${response.message}")
        }

        val file = File(outputPath)
        file.sink().buffer().use { sink ->
            response.body?.source()?.let { sink.writeAll(it) }
        }

        println("Downloaded file from $fileUrl to $outputPath")
    }
}