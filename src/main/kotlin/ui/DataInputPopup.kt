package deus.ui

import deus.config.ConfigHandler
import deus.github.Downloader
import java.awt.BorderLayout
import java.io.IOException
import javax.swing.*

import javax.swing.SwingWorker
object DataInputPopup {

    @JvmStatic
    fun initModpackConfig() {
        // Configurar un JFrame para el formulario
        val frame = JFrame("Configuración del Modpack")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 300)
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)

        // Crear los campos de entrada y etiquetas
        val countField = JTextField(20)

        frame.add(createLabeledPanel("Cantidad de mods:", countField))

        // Botón para mostrar los campos de mod
        val showModsButton = JButton("Mostrar Campos de Mods")
        showModsButton.addActionListener {
            val countStr = countField.text.trim()
            val count = countStr.toIntOrNull()

            if (count == null || count <= 0) {
                JOptionPane.showMessageDialog(
                    frame,
                    "La cantidad de mods debe ser un número positivo.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            val mods = mutableListOf<Pair<String, String>>()
            val modNamesSet = mutableSetOf<String>()
            val modUrlsSet = mutableSetOf<String>()

            for (i in 1..count) {
                val modUrl = JOptionPane.showInputDialog(frame, "Introduce la URL del mod #$i:")?.trim()
                if (modUrl.isNullOrEmpty()) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "La URL del mod no puede estar vacía.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    return@addActionListener
                }

                val modName = extractModName(modUrl)

                if (modNamesSet.contains(modName)) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "El nombre del mod '$modName' ya ha sido introducido.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    return@addActionListener
                }

                if (modUrlsSet.contains(modUrl)) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "La URL del mod '$modUrl' ya ha sido introducida.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    return@addActionListener
                }

                modNamesSet.add(modName)
                modUrlsSet.add(modUrl)
                mods.add(modName to modUrl)
            }

            // Construir la cadena de configuración para el modpack
            val modpackConfigString = StringBuilder()
            mods.forEach { (modName, modUrl) ->
                modpackConfigString.append("$modName=$modUrl\n")
            }

            // Guardar el archivo de configuración del modpack
            val modpackConfigPath = "C:/Users/masit/Videos/AAAA/modpack.conf"
            ConfigHandler.makeConfig(modpackConfigPath, modpackConfigString.toString())

            JOptionPane.showMessageDialog(
                frame,
                "Configuración del modpack completa.\n\n$modpackConfigString",
                "Configuración",
                JOptionPane.INFORMATION_MESSAGE
            )
            frame.dispose() // Cerrar el formulario después de completar
        }
        frame.add(showModsButton)

        // Mostrar el formulario
        frame.isVisible = true
    }

    @JvmStatic
    fun initAppConfig() {
        // Configurar un JFrame para el formulario
        val frame = JFrame("Configuración de la Aplicación")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 300)
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)

        // Crear los campos de entrada y etiquetas
        val modpackUrlField = JTextField(20)
        val jarDownloadPathField = JTextField(20)

        frame.add(createLabeledPanel("URL del archivo de configuración del modpack:", modpackUrlField))
        frame.add(createLabeledPanel("Ruta para guardar los archivos .jar:", jarDownloadPathField))

        // Botón para guardar la configuración de la aplicación
        val saveConfigButton = JButton("Guardar Configuración")
        saveConfigButton.addActionListener {
            val modpackUrl = modpackUrlField.text.trim()
            val jarDownloadPath = jarDownloadPathField.text.trim()

            if (modpackUrl.isEmpty() || jarDownloadPath.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Todos los campos deben ser completados.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            // Guardar la configuración de la aplicación
            val appConfigString = "modpackConfigUrl=$modpackUrl\njarDownloadPath=$jarDownloadPath"
            val appConfigPath = "C:/Users/masit/Videos/AAAA/app.conf"
            ConfigHandler.makeConfig(appConfigPath, appConfigString)

            JOptionPane.showMessageDialog(
                frame,
                "Configuración de la aplicación completa.\n\n$appConfigString",
                "Configuración",
                JOptionPane.INFORMATION_MESSAGE
            )
            frame.dispose() // Cerrar el formulario después de completar
        }
        frame.add(saveConfigButton)

        // Mostrar el formulario
        frame.isVisible = true
    }

    private fun createLabeledPanel(label: String, textField: JTextField): JPanel {
        val panel = JPanel()
        panel.add(JLabel(label))
        panel.add(textField)
        return panel
    }

    private fun extractModName(modUrl: String): String {
        // Extraer el nombre del repositorio sin el prefijo del usuario
        val regex = Regex("""https://github\.com/[^/]+/([^/]+)""")
        val matchResult = regex.find(modUrl)
        return matchResult?.let {
            it.groupValues[1]
        } ?: "unknown"
    }

    private lateinit var progressDialog: JDialog

    @JvmStatic
    fun init() {
        val appConfigPath = "C:/Users/masit/Videos/AAAA/app.conf"
        val appConfigLines = ConfigHandler.getConfig(appConfigPath)

        if (appConfigLines.isEmpty()) {
            println("El archivo de configuración de la aplicación está vacío.")
            return
        }

        val modpackConfigUrl = appConfigLines.find { it.startsWith("modpackConfigUrl=") }?.split("=")?.get(1) ?: run {
            println("No se encontró la URL de configuración del modpack en la configuración de la aplicación.")
            return
        }

        val jarDownloadPath = appConfigLines.find { it.startsWith("jarDownloadPath=") }?.split("=")?.get(1) ?: run {
            println("No se encontró la ruta de descarga de los archivos .jar en la configuración de la aplicación.")
            return
        }

        val modpackConfigPath = "C:/Users/masit/Videos/AAAA/modpack.conf"

        // Crear y mostrar el diálogo de progreso
        showProgressDialog()

        try {
            Downloader.downloadFile(modpackConfigUrl, modpackConfigPath)
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
                    println("Descargando archivo desde: $fileUrl a $outputPath")
                    Downloader.downloadFile(fileUrl, outputPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                println("Línea de configuración inválida: $line")
            }
        }

        // Cerrar el diálogo de progreso cuando la descarga esté completa
        closeProgressDialog()

        // Mostrar mensaje final
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(
                null,
                "Descarga de mods completada.",
                "Completado",
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }

    private fun showProgressDialog() {
        SwingUtilities.invokeLater {
            val frame = JFrame()
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.isUndecorated = true
            frame.setSize(0, 0) // Ocultar el JFrame
            frame.isVisible = false

            progressDialog = JDialog(frame, "Progreso", true)
            progressDialog.setSize(300, 100)
            progressDialog.layout = BorderLayout()
            progressDialog.add(JLabel("Se están descargando los mods. Por favor, espere...", SwingConstants.CENTER), BorderLayout.CENTER)
            progressDialog.setLocationRelativeTo(null)
            progressDialog.isVisible = true
        }
    }

    private fun closeProgressDialog() {
        SwingUtilities.invokeLater {
            if (::progressDialog.isInitialized && progressDialog.isVisible) {
                progressDialog.dispose()
            }
        }
    }
}


