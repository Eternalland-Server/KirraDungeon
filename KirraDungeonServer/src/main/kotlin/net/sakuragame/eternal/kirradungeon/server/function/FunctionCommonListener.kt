package net.sakuragame.eternal.kirradungeon.server.function

import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenOpenEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonLoadedEvent
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.common.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.isSpectator
import net.sakuragame.eternal.kirradungeon.server.playDeathAnimation
import net.sakuragame.eternal.kirradungeon.server.turnToSpectator
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType.*
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.UnlimitedDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.WaveDungeon
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang

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
            DEFAULT -> DefaultDungeon.create(copyZone, e.dungeonWorld)
            SPECIAL -> SpecialDungeon.create(copyZone, e.dungeonWorld)
            UNLIMITED -> UnlimitedDungeon.create(copyZone, e.dungeonWorld)
            WAVE -> WaveDungeon.create(copyZone, e.dungeonWorld)
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
        if (!profile.isChallenging) {
            e.isCancelled = true
            return
        }
        if (e.cause == EntityDamageEvent.DamageCause.VOID) {
            e.isCancelled = true
            val dungeon = profile.getIDungeon() ?: return
            player.teleport(dungeon.zone.data.spawnLoc.toBukkitLocation(player.world))
            player.sendLang("message-player-lifted-from-void")
        }
    }

    @SubscribeEvent
    fun e(e: PlayerDeathEvent) {
        val player = e.entity
        if (!player.profile().isChallenging) return
        val zone = when (player.profile().zoneType) {
            DEFAULT -> DefaultDungeon.getByPlayer(e.entity.uniqueId) ?: return
            SPECIAL -> SpecialDungeon.getByPlayer(e.entity.uniqueId) ?: return
            UNLIMITED -> UnlimitedDungeon.getByPlayer(e.entity.uniqueId) ?: return
            WAVE -> WaveDungeon.getByPlayer(e.entity.uniqueId) ?: return
        }
        player.apply {
            playDeathAnimation()
            health = maxHealth
            turnToSpectator()
            sendTitle("&c&l菜".colored(), "", 0, 40, 10)
        }
        submit(async = false, delay = 10L) {
            if (zone.isAllPlayersDead() && zone.failThread == null) {
                zone.startFailThread()
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        if (!e.player.profile().isChallenging) return
        if (e.player.gameMode != GameMode.CREATIVE) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: DungeonClearEvent) {
        val zone = Zone.getByID(e.dungeonId) ?: return
        e.players.forEach {
            val profile = it.profile()
            if (profile.number.get() <= zone.data.number) {
                profile.number.set(zone.data.number + 1)
            }
        }
    }

    @SubscribeEvent
    fun e(e: UIFScreenOpenEvent) {
        val player = e.player
        val zone = when (player.profile().zoneType) {
            DEFAULT -> DefaultDungeon.getByPlayer(player.uniqueId) ?: return
            SPECIAL -> return
            UNLIMITED -> UnlimitedDungeon.getByPlayer(player.uniqueId) ?: return
            WAVE -> return
        }
        submit(delay = 40L) {
            zone.updateBossBar(init = true)
        }
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val player = e.player
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.failHudID, DragonCoreCompat.failHudYaml)
    }
}