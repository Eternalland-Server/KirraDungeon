package net.sakuragame.eternal.kirradungeon.server

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import com.google.gson.Gson
import io.lumine.xikage.mythicmobs.MythicMobs
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.configuration.createLocal
import taboolib.platform.BukkitPlugin
import java.util.concurrent.TimeUnit

@Suppress("SpellCheckingInspection")
object KirraDungeonServer : Plugin() {

    @Config("config.yml")
    lateinit var conf: Configuration
        private set

    val data by lazy {
        createLocal("data.json", 200, Type.JSON)
    }

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val gson by lazy {
        Gson()
    }

    val mythicmobsAPI by lazy {
        MythicMobs.inst().apiHelper!!
    }

    private val redisManager by lazy {
        ClientManagerAPI.clientPlugin.redisManager
    }

    val redisConn by lazy {
        redisManager.standaloneConn.also {
            it.setTimeout(200, TimeUnit.MILLISECONDS)
        }!!
    }

    val skyAPI by lazy {
        SkyChanger.getAPI()!!
    }
}