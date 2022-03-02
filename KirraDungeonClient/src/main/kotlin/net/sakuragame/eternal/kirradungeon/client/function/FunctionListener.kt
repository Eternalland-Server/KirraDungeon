package net.sakuragame.eternal.kirradungeon.client.function

import net.sakuragame.eternal.justmessage.api.event.notify.NotifyBoxConfirmEvent
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirradungeon.client.zone.ZoneCondition
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneUpdateEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

@Suppress("UNUSED_PARAMETER")
object FunctionListener {

    @SubscribeEvent
    fun e(e: ZoneUpdateEvent) {
        submit(async = true) {
            Zone.load()
        }
    }

    @SubscribeEvent
    fun e(e: ZoneJoinEvent) {
        val player = e.player
        val dungeonId = e.dungeonId
        if (dungeonId == "null") return
        Zone.preJoin(player, dungeonId)
    }

    @SubscribeEvent
    fun e(e: NotifyBoxConfirmEvent) {
        when (e.key) {
            ZoneCondition.COUNT_NOT_ENOUGH_KEY_BOX, ZoneCondition.ECONOMY_NOT_ENOUGH_KEY_BOX, ZoneCondition.ITEM_NOT_ENOUGH_KEY_BOX -> {
                e.player.closeInventory()
            }
        }
    }
}