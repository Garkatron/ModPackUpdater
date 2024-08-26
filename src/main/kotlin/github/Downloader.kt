package deus.github

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

/**
 * A utility object for downloading files from GitHub releases or any other URL.
 */
object Downloader {

    /**
     * Downloads an asset from a GitHub release and saves it to the specified path.
     *
     * @param owner The owner of the GitHub repository.
     * @param repo The name of the GitHub repository.
     * @param tagName The release tag name.
     * @param assetName The name of the asset to download.
     * @param outputPath The file path where the downloaded asset will be saved.
     * @throws IOException If an error occurs during the download or saving of the file.
     */
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
     * Downloads a file from the provided URL and saves it to the specified path.
     *
     * @param fileUrl The full URL of the file to download.
     * @param outputPath The path where the downloaded file will be saved.
     * @throws IOException If an error occurs during the download or saving of the file.
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
