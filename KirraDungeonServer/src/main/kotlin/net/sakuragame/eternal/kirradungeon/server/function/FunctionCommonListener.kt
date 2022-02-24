package net.sakuragame.eternal.kirradungeon.server.function

import net.sakuragame.dungeonsystem.server.api.event.DungeonLoadedEvent
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.isSpectator
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType.*
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialZone
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
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
        when (copyZoneData.type) {
            DEFAULT -> DefaultZone.create(copyZone, e.dungeonWorld)
            SPECIAL -> SpecialZone.create(copyZone, e.dungeonWorld)
            UNLIMITED -> {
                // TODO: fill it
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractAtEntityEvent) {
        if (e.player.isSpectator()) e.isCancelled = true
    }

    @SubscribeEvent
    fun e(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        if (player.isSpectator()) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        val profile = player.profile()
        val playerZone = DefaultZone.getByPlayer(player.uniqueId)
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

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val player = e.player
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
    }
}