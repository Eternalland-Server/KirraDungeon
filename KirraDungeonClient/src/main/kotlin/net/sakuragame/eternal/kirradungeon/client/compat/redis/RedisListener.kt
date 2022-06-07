package net.sakuragame.eternal.kirradungeon.client.compat.redis

import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirraparty.bukkit.KirraPartyBukkit
import net.sakuragame.eternal.kirraparty.bukkit.compat.redis.RedisListener
import net.sakuragame.serversystems.manage.api.redis.RedisMessageListener
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object RedisListener : RedisMessageListener(false, "KirraDungeon") {

    @Awake(LifeCycle.ACTIVE)
    fun i() {
        KirraPartyBukkit.redisManager.subscribe("KirraParty")
        KirraPartyBukkit.redisManager.registerListener(RedisListener)
    }

    override fun onMessage(serviceName: String, sourceServer: String, channel: String, messages: Array<String>) {
        when (messages[0]) {
            "update-condition" -> Zone.load()
        }
    }
}