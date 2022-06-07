package net.sakuragame.eternal.kirradungeon.client

import com.lambdaworks.redis.api.sync.RedisCommands
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin

@Suppress("SpellCheckingInspection")
object KirraDungeonClient : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val redisManager by lazy {
        ClientManagerAPI.clientPlugin.redisManager!!
    }

    val redisConn by lazy {
        redisManager.pooledConn!!
    }
}