package net.sakuragame.eternal.kirradungeon.server

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

object Loader {

    private val folder by lazy(LazyThreadSafetyMode.NONE) {
        File(KirraDungeonServer.plugin.dataFolder, "zones")
    }

    val files = mutableListOf<ConfigFile>()

    @Awake(LifeCycle.LOAD)
    fun i() {
        files += folder.listFiles()!!.map { Configuration.loadFromFile(it, Type.JSON) }
    }
}