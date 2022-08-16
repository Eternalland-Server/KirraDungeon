package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

object Loader {

    private val folder by lazy(LazyThreadSafetyMode.NONE) {
        File(KirraDungeonServer.plugin.dataFolder, "zones")
    }

    val files = CopyOnWriteArrayList<ConfigFile>()

    @Awake(LifeCycle.LOAD)
    fun i() {
        files += folder.listFiles()!!.map { parseWithId(it) }
    }

    fun reload() {
        files.clear()
        i()
        Zone.i()
    }

    private fun parseWithId(file: File): ConfigFile {
        return Configuration.loadFromFile(file, Type.JSON).also {
            it["id"] = file.nameWithoutExtension
        }
    }
}