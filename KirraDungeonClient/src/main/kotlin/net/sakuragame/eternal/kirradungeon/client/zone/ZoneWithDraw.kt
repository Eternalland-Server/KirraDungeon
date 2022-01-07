package net.sakuragame.eternal.kirradungeon.client.zone

import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.function.submit
import java.util.*

class ZoneWithDraw {

    companion object {

        val gemsMap = mutableMapOf<UUID, MutableMap<EternalCurrency, Double>>()

        val itemsMap = mutableMapOf<UUID, MutableMap<String, Int>>()

        @EventHandler
        fun e(e: PlayerQuitEvent) =
            submit(async = true) {
                recycleVars(e.player)
            }

        @EventHandler
        fun e(e: PlayerKickEvent) =
            submit(async = true) {
                recycleVars(e.player)
            }

        fun recycleVars(player: Player) {
            if (gemsMap.containsKey(player.uniqueId)) gemsMap.remove(player.uniqueId)
            if (itemsMap.containsKey(player.uniqueId)) itemsMap.remove(player.uniqueId)
        }
    }
}