package deus.ui

import deus.config.ConfigHandler
import deus.github.Downloader
import java.awt.BorderLayout
import java.io.IOException
import javax.swing.*
import kotlin.system.exitProcess

/**
 * A utility object for handling configuration and downloading tasks related to modpacks and application settings.
 */
object DataInputPopup {

    /**
     * Initializes the modpack configuration dialog.
     * Allows the user to specify the number of mods and their corresponding URLs.
     */
    @JvmStatic
    fun initModpackConfig() {
        // Configure a JFrame for the form
        val frame = JFrame("Modpack Configuration")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 300)
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)

        // Create input fields and labels
        val countField = JTextField(20)
        frame.add(createLabeledPanel("Number of mods:", countField))

        // Button to show mod fields
        val showModsButton = JButton("Show Mod Fields")
        showModsButton.addActionListener {
            val countStr = countField.text.trim()
            val count = countStr.toIntOrNull()

            if (count == null || count <= 0) {
                JOptionPane.showMessageDialog(
                    frame,
                    "The number of mods must be a positive integer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            val mods = mutableListOf<Pair<String, String>>()
            val modNamesSet = mutableSetOf<String>()
            val modUrlsSet = mutableSetOf<String>()

            for (i in 1..count) {
                val modUrl = JOptionPane.showInputDialog(frame, "Enter the URL of mod #$i:")?.trim()
                if (modUrl.isNullOrEmpty()) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "The mod URL cannot be empty.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    return@addActionListener
                }

                val modName = extractModName(modUrl)

                if (modNamesSet.contains(modName)) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "The mod name '$modName' has already been entered.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    return@addActionListener
                }

                if (modUrlsSet.contains(modUrl)) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "The mod URL '$modUrl' has already been entered.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    return@addActionListener
                }

                modNamesSet.add(modName)
                modUrlsSet.add(modUrl)
                mods.add(modName to modUrl)
            }

            // Build the modpack configuration string
            val modpackConfigString = mods.joinToString("\n") { (modName, modUrl) -> "$modName=$modUrl" }

            // Save the modpack configuration file
            val modpackConfigPath = "./modpack.conf"
            ConfigHandler.makeConfig(modpackConfigPath, modpackConfigString)

            JOptionPane.showMessageDialog(
                frame,
                "Modpack configuration complete.\n\n$modpackConfigString",
                "Configuration",
                JOptionPane.INFORMATION_MESSAGE
            )
            frame.dispose() // Close the form after completion
        }
        frame.add(showModsButton)

        // Show the form
        frame.isVisible = true
    }

    /**
     * Initializes the application configuration dialog.
     * Allows the user to specify the URL of the modpack configuration file and the path for saving .jar files.
     */
    @JvmStatic
    fun initAppConfig() {
        // Configure a JFrame for the form
        val frame = JFrame("Application Configuration")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 300)
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)

        // Create input fields and labels
        val JrepoOwner = JTextField(20)
        val JrepoName = JTextField(20)
        val jarDownloadPathField = JTextField(20)

        frame.add(createLabeledPanel("Repo owner:", JrepoOwner))
        frame.add(createLabeledPanel("Repo name:", JrepoName))
        frame.add(createLabeledPanel("Path to save .jar files:", jarDownloadPathField))

        // Button to save application configuration
        val saveConfigButton = JButton("Save Configuration")
        saveConfigButton.addActionListener {
            val repoOwner = JrepoOwner.text.trim()
            val repoName = JrepoName.text.trim()
            val jarDownloadPath = jarDownloadPathField.text.trim()

            if (repoOwner.isEmpty() ||repoName.isEmpty() || jarDownloadPath.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "All fields must be completed.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            // Save the application configuration
            val appConfigString = "" +
                    "owner=$repoOwner\n" + "repo=$repoName" +
                    "\njarDownloadPath=$jarDownloadPath"
            val appConfigPath = "./app.conf"
            ConfigHandler.makeConfig(appConfigPath, appConfigString)

            JOptionPane.showMessageDialog(
                frame,
                "Application configuration complete.\n\n$appConfigString",
                "Configuration",
                JOptionPane.INFORMATION_MESSAGE
            )
            frame.dispose() // Close the form after completion
        }
        frame.add(saveConfigButton)

        // Show the form
        frame.isVisible = true
    }

    /**
     * Creates a JPanel with a label and a text field.
     * @param label The label text
     * @param textField The text field
     * @return A JPanel containing the label and text field
     */
    private fun createLabeledPanel(label: String, textField: JTextField): JPanel {
        val panel = JPanel()
        panel.add(JLabel(label))
        panel.add(textField)
        return panel
    }

    /**
     * Extracts the mod name from a given mod URL.
     * @param modUrl The URL of the mod
     * @return The extracted mod name, or "unknown" if the URL is not valid
     */
    private fun extractModName(modUrl: String): String {
        // Extract the repository name without the user prefix
        val regex = Regex("""https://github\.com/[^/]+/([^/]+)""")
        val matchResult = regex.find(modUrl)
        return matchResult?.groupValues?.get(1) ?: "unknown"
    }

    private lateinit var progressDialog: JDialog

    /**
     * Initializes the application by reading configuration files, downloading the modpack configuration,
     * and downloading the mod .jar files.
     */
    @JvmStatic
    fun init() {
        val appConfigPath = "./app.conf"
        val appConfigLines = ConfigHandler.getConfig(appConfigPath)

        if (appConfigLines.isEmpty()) {
            println("The application configuration file is empty.")
            return
        }

        val ownerName = appConfigLines.find { it.startsWith("owner=") }?.split("=")?.get(1) ?: run {
            println("Repo owner configuration not found in application configuration.")
            return
        }

        val repoName = appConfigLines.find { it.startsWith("repo=") }?.split("=")?.get(1) ?: run {
            println("Repo name configuration not found in application configuration.")
            return
        }

        val jarDownloadPath = appConfigLines.find { it.startsWith("jarDownloadPath=") }?.split("=")?.get(1) ?: run {
            println("Jar download path not found in application configuration.")
            return
        }

        val modpackConfigPath = "./modpack.conf"

        // Create and show the progress dialog
        showProgressDialog()

        try {
            Downloader.downloadLatestReleaseAsset(ownerName, repoName, "modpack.conf", modpackConfigPath)
        } catch (e: IOException) {
            e.printStackTrace()
            closeProgressDialog()
            return
        }

        val modpackLines = ConfigHandler.getConfig(modpackConfigPath)

        modpackLines.forEach { line ->
            val twoFragments = line.split("=")
            if (twoFragments.size == 2) {
                val modName = twoFragments[0]
                val fileUrl = twoFragments[1]
                val outputPath = "${jarDownloadPath}/${modName}.jar"

                try {
                    println("Downloading file from: $fileUrl to $outputPath")
                    Downloader.downloadFile(fileUrl, outputPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                println("Invalid configuration line: $line")
            }
        }

        // Close the progress dialog when downloads are complete
        closeProgressDialog()

        // Show final message
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(
                null,
                "Mod download completed.",
                "Completed",
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }

    /**
     * Displays a progress dialog indicating that mods are being downloaded.
     */
    private fun showProgressDialog() {
        SwingUtilities.invokeLater {
            val frame = JFrame()
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.isUndecorated = true
            frame.setSize(0, 0) // Hide the JFrame
            frame.isVisible = false

            progressDialog = JDialog(frame, "Progress", true)
            progressDialog.setSize(300, 100)
            progressDialog.layout = BorderLayout()
            progressDialog.add(
                JLabel("Downloading mods. Please wait...", SwingConstants.CENTER),
                BorderLayout.CENTER
            )
            progressDialog.setLocationRelativeTo(null)
            progressDialog.isVisible = true
        }
    }

    /**
     * Closes the progress dialog if it is currently displayed.
     */
    private fun closeProgressDialog() {
        SwingUtilities.invokeLater {
            if (::progressDialog.isInitialized && progressDialog.isVisible) {
                progressDialog.dispose()
            }
        }
        exitProcess(0)
    }
}
