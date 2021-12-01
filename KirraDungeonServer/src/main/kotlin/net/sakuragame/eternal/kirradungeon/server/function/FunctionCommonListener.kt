package net.sakuragame.eternal.kirradungeon.server.function

import net.sakuragame.dungeonsystem.server.api.event.DungeonLoadedEvent
import net.sakuragame.eternal.kirradungeon.server.zone.PlayerZone
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import taboolib.common.platform.event.SubscribeEvent

/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.server.function.FunctionListener
 *
 * @author kirraObj
 * @since 2021/11/8 3:51
 */
object FunctionCommonListener {

    @SubscribeEvent
    fun e(e: DungeonLoadedEvent) {
        if (Zone.getByID(e.dungeonWorld.worldIdentifier) == null) {
            return
        }
        val copyZoneData = Zone.getByID(e.dungeonWorld.worldIdentifier)!!.data.copy()
        val copyZone = Zone.getByID(e.dungeonWorld.worldIdentifier)!!.copy(data = copyZoneData)
        PlayerZone.create(copyZone, e.dungeonWorld)
    }

    @SubscribeEvent
    fun e(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.SPECTATOR) {
            e.isCancelled = true
        }
    }
}