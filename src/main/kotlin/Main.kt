package deus

import deus.ui.DataInputPopup
import java.io.File

fun main() {

    if (!File("./app.conf").exists()) {
        DataInputPopup.initAppConfig()
    }

    if (!File("./modpack.conf").exists()) {
        DataInputPopup.initModpackConfig()
    }

    DataInputPopup.init()

}