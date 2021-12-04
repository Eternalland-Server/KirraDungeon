package net.sakuragame.eternal.kirradungeon.client.function

import net.sakuragame.eternal.kirradungeon.client.zone.Zone
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
}