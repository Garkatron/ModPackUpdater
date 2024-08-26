package deus;

import deus.config.ConfigHandler
import deus.github.Downloader
import deus.ui.DataInputPopup
import java.io.File
import javax.swing.JOptionPane
import javax.swing.JOptionPane.showInputDialog

fun main() {

    if (!File("C:/Users/masit/Videos/AAAA/app.conf").exists()) {DataInputPopup.initAppConfig()}

    if (!File("C:/Users/masit/Videos/AAAA/modpack.conf").exists()) {DataInputPopup.initModpackConfig()}

    DataInputPopup.init()

}