package net.sakuragame.eternal.kirradungeon.client.function

import net.sakuragame.eternal.justmessage.api.event.notify.NotifyBoxCancelEvent
import net.sakuragame.eternal.justmessage.api.event.notify.NotifyBoxConfirmEvent
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
import taboolib.common.platform.event.SubscribeEvent

object FunctionListener {

    const val AUTO_CLOSE_NOTIFY_BOX_KEY = "AUTO_CLOSE_BOX"

    @SubscribeEvent
    fun e(e: ZoneJoinEvent) {
        val player = e.player
        val dungeonId = e.dungeonId
        if (dungeonId == "null") return
        Zone.preJoin(player, dungeonId, e.isTeam)
    }

    @SubscribeEvent
    fun e(e: NotifyBoxConfirmEvent) {
        when (e.key) {
            AUTO_CLOSE_NOTIFY_BOX_KEY -> {
                e.player.closeInventory()
            }
        }
    }

    @SubscribeEvent
    fun e(e: NotifyBoxCancelEvent) {
        when (e.key) {
            AUTO_CLOSE_NOTIFY_BOX_KEY -> {
                e.player.closeInventory()
            }
        }
    }
}