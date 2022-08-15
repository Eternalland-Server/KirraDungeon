package net.sakuragame.eternal.kirradungeon.server

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import io.lumine.xikage.mythicmobs.MythicMobs
import taboolib.common.platform.Plugin
import taboolib.expansion.setupPlayerDatabase
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.configuration.createLocal
import taboolib.platform.BukkitPlugin

@Suppress("SpellCheckingInspection")
object KirraDungeonServer : Plugin() {

    @Config("config.yml")
    lateinit var conf: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val mythicmobsAPI by lazy {
        MythicMobs.inst().apiHelper!!
    }

    val skyAPI by lazy {
        SkyChanger.getAPI()!!
    }

    override fun onEnable() {
        conf.getConfigurationSection("settings.database")?.let { setupPlayerDatabase(it, "kirradungeon_player") }
    }
}