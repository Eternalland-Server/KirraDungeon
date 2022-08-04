package net.sakuragame.eternal.kirradungeon.client

import taboolib.common.platform.Plugin
import taboolib.expansion.setupPlayerDatabase
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin

@Suppress("SpellCheckingInspection")
object KirraDungeonClient : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    @Config(value = "dungeons.yml")
    lateinit var dungeons: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    override fun onEnable() {
        conf.getConfigurationSection("settings.database")?.let { setupPlayerDatabase(it, "kirradungeon_player") }
    }
}     