package net.sakuragame.eternal.kirradungeon.client.zone.event

import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.serversystems.manage.api.redis.RedisMessageListener
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.platform.type.BukkitProxyEvent

/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.client.data.event.ZoneUpdateEvent
 *
 * @author kirraObj
 * @since 2021/11/4 14:11
 */
class ZoneUpdateEvent : BukkitProxyEvent() {

    companion object {

        @Awake(LifeCycle.ENABLE)
        private fun init() {
            KirraDungeonClient.redisManager.subscribe("KirraDungeon")
            KirraDungeonClient.redisManager.registerListener(RedisListener())
        }

        private class RedisListener : RedisMessageListener(false, "KirraDungeon") {

            override fun onMessage(serviceName: String, sourceServer: String, channel: String, messages: Array<String>) {
                when (messages[0]) {
                    "update" -> ZoneUpdateEvent().call()
                }
            }
        }
    }
}