package net.sakuragame.eternal.kirradungeon.client

import com.lambdaworks.redis.api.StatefulRedisConnection
import net.sakuragame.serversystems.manage.api.redis.RedisManager
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import java.util.concurrent.TimeUnit

object KirraDungeonClient : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val redisManager: RedisManager by lazy {
        ClientManagerAPI.clientPlugin.redisManager
    }

    val redisConn: StatefulRedisConnection<String, String> by lazy {
        redisManager.standaloneConn.also {
            it.setTimeout(200, TimeUnit.MILLISECONDS)
        }
    }
}