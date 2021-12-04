package net.sakuragame.eternal.kirradungeon.plot

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import com.lambdaworks.redis.api.StatefulRedisConnection
import io.lumine.xikage.mythicmobs.MythicMobs
import net.luckperms.api.LuckPerms
import net.sakuragame.serversystems.manage.api.redis.RedisManager
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import taboolib.platform.BukkitPlugin
import java.util.concurrent.TimeUnit

object KirraDungeonPlot : Plugin() {

    @Config
    lateinit var conf: SecuredFile
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val skyAPI by lazy {
        SkyChanger.getAPI()!!
    }

    val luckPermsAPI by lazy {
        Bukkit.getServicesManager().getRegistration(LuckPerms::class.java).provider!!
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
}