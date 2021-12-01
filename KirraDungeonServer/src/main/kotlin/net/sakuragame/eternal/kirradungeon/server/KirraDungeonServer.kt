package net.sakuragame.eternal.kirradungeon.server

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import com.lambdaworks.redis.api.StatefulRedisConnection
import io.lumine.xikage.mythicmobs.MythicMobs
import net.sakuragame.serversystems.manage.api.redis.RedisManager
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import java.util.concurrent.TimeUnit

object KirraDungeonServer : Plugin() {

    @Config("config.yml", autoReload = true)
    lateinit var conf: Configuration
        private set

    @Config("data.yml")
    lateinit var data: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val mythicmobsAPI by lazy {
        MythicMobs.inst().apiHelper!!
    }

    val redisManager: RedisManager by lazy {
        ClientManagerAPI.clientPlugin.redisManager
    }

    val redisConn: StatefulRedisConnection<String, String> by lazy {
        redisManager.standaloneConn.also {
            it.setTimeout(200, TimeUnit.MILLISECONDS)
        }
    }

    val skyAPI by lazy {
        SkyChanger.getAPI()!!
    }
}