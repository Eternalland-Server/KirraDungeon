package net.sakuragame.eternal.kirradungeon.server.zone.data.writer

import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Loader
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

interface WriteHelper {

    @Suppress("DuplicatedCode")
    fun getFile(id: String): ConfigFile {
        val find = Loader.files.find { it.name == id }
        return when {
            find != null -> find
            else -> {
                val file = File(KirraDungeonServer.plugin.dataFolder, "zones/$id.json").apply {
                    createNewFile()
                }
                val conf = Configuration.loadFromFile(file, Type.JSON)
                Loader.files += conf
                conf
            }
        }
    }

    fun reload() {
        Loader.files.forEach {
            it.saveToFile(it.file)
        }
        Zone.i()
    }
}