package net.sakuragame.eternal.kirradungeon.server.zone.data.writer

import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Loader
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

interface WriteHelper {

    fun getFile(id: String): ConfigFile {
        val find = Loader.files.find { it.name == id }
        return when {
            find != null -> find
            else -> {
                val file = Configuration.loadFromFile(File(KirraDungeonServer.plugin.dataFolder, "zones/$id.json"), Type.JSON)
                Loader.files += file
                file
            }
        }
    }

    fun reload() {
        Zone.i()
    }
}