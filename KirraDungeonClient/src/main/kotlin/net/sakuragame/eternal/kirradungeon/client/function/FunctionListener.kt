package net.sakuragame.eternal.kirradungeon.client.function

import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneUpdateEvent
import net.sakuragame.kirracoords.KirraCoordsAPI
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
        if (e.isLocal) {
            KirraCoordsAPI.tpCoord(player, dungeonId)
            return
        }
        val zone = Zone.getByID(e.dungeonId) ?: return
        zone.join(listOf(player))
    }
}