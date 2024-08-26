package deus.github

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import org.json.JSONArray
import org.json.JSONObject
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
    /**
     * Checks for releases in a GitHub repository and returns the latest release tag name.
     *
     * @param owner The owner of the GitHub repository.
     * @param repo The name of the GitHub repository.
     * @return The tag name of the latest release, or null if no releases are found or an error occurs.
     */
    @JvmStatic
    fun getLatestReleaseTag(owner: String, repo: String): String? {
        val url = "https://api.github.com/repos/$owner/$repo/releases"
        val client = OkHttpClient()

        val request = Request.Builder().url(url).build()

        return try {
            val response: Response = client.newCall(request).execute()

            if (!response.isSuccessful) throw IOException("Failed to get releases: ${response.message}")

            val body = response.body?.string() ?: return null
            val jsonArray = JSONArray(body)

            if (jsonArray.length() > 0) {
                val latestRelease = jsonArray.getJSONObject(0)
                latestRelease.getString("tag_name")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks for a specific asset in the latest GitHub release and downloads it.
     *
     * @param owner The owner of the GitHub repository.
     * @param repo The name of the GitHub repository.
     * @param assetName The name of the asset to download.
     * @param outputPath The file path where the downloaded asset will be saved.
     * @throws IOException If an error occurs during the download or saving of the file.
     */
    @JvmStatic
    fun downloadLatestReleaseAsset(owner: String, repo: String, assetName: String, outputPath: String) {
        val latestTag = getLatestReleaseTag(owner, repo) ?: throw IOException("No releases found or failed to get releases")

        // Build URL for the asset in the latest release
        val assetUrl = "https://github.com/$owner/$repo/releases/download/$latestTag/$assetName"
        downloadFile(assetUrl, outputPath)
    }

    @JvmStatic
    fun downloadReleaseAsset(owner: String, repo: String, latestTag: String, assetName: String, outputPath: String) {

        // Build URL for the asset in the latest release
        val assetUrl = "https://github.com/$owner/$repo/releases/download/$latestTag/$assetName"
        downloadFile(assetUrl, outputPath)
    }

    /**
     * Obtains the tag of the latest release that matches the specified prefix.
     *
     * @param owner The GitHub repository owner's name.
     * @param repo The repository name.
     * @param prefix The prefix to match in the release tag.
     * @return The tag of the latest matching release, or null if no matching release is found.
     * @throws IOException If there is an error during the request.
     */
    fun getLatestReleaseTag(owner: String, repo: String, prefix: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/releases")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Failed to get releases: ${response.message}")

        val releases = JSONArray(response.body?.string())

        val filteredReleases = mutableListOf<Pair<String, String>>()

        for (i in 0 until releases.length()) {
            val release = releases.getJSONObject(i)
            val tagName = release.getString("tag_name")

            if (tagName.startsWith(prefix)) {
                val createdAt = release.getString("created_at")
                filteredReleases.add(Pair(tagName, createdAt))
            }
        }

        // Sort by creation date (descending) and return the latest tag
        filteredReleases.sortByDescending { it.second }
        return filteredReleases.firstOrNull()?.first
    }
}
