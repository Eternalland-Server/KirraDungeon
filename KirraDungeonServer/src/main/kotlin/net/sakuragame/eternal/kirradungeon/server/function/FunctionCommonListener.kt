package net.sakuragame.eternal.kirradungeon.server.function

import net.sakuragame.dungeonsystem.server.api.event.DungeonLoadedEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.player.PlayerZone
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
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
        e.dungeonWorld.bukkitWorld.isAutoSave = false
        // 初始化.
        PlayerZone.create(copyZone, e.dungeonWorld)
    }

    @SubscribeEvent
    fun e(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.SPECTATOR) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        val profile = player.profile()
        val playerZone = PlayerZone.getByPlayer(player.uniqueId)
        if (playerZone == null || !profile.isChallenging) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: DungeonClearEvent) {
        val zone = Zone.getByID(e.dungeonId) ?: return
        val profile = e.player.profile()
        if (profile.number.get() <= zone.data.number) {
            profile.number.set(zone.data.number + 1)
        }
    }
}