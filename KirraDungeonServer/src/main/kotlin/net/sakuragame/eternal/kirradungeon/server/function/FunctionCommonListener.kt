package net.sakuragame.eternal.kirradungeon.server.function

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
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.UnlimitedDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.WaveDungeon
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirramodel.KirraModelAPI
import org.bukkit.GameMode
import org.bukkit.WorldType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.world.WorldUnloadEvent
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

    // 保险措施
    // 将世界卸载后, 若不卸载区块区块则不会自动回收
    @SubscribeEvent
    fun e(e: WorldUnloadEvent) {
        val world = e.world
        world.entities.forEach {
            it.remove()
        }
        world.loadedChunks.forEach {
            it.unload(false)
        }
    }

    @SubscribeEvent
    fun e(e: DungeonLoadedEvent) {
        val dungeonWorld = e.dungeonWorld
        if (Zone.getByID(dungeonWorld.worldIdentifier) == null) {
            return
        }
        val copyZoneData = Zone.getByID(dungeonWorld.worldIdentifier)!!.data.copy()
        val copyZone = Zone.getByID(dungeonWorld.worldIdentifier)!!.copy(data = copyZoneData)
        // 将模型数据生成在世界里.
        copyZone.data.models.forEach {
            val model = KirraModelAPI.models[it.model] ?: return@forEach
            KirraModelAPI.createTempModel(it.loc.toBukkitLocation(dungeonWorld.bukkitWorld), model, it.id)
        }
        // 将矿物数据生成在世界里.
        copyZone.data.ores.forEach {
            val ore = KirraMinerAPI.ores[it.ore] ?: return@forEach
            KirraMinerAPI.createTempOre(it.id, ore, it.loc.toBukkitLocation(dungeonWorld.bukkitWorld))
        }
        dungeonWorld.bukkitWorld.isAutoSave = false
        dungeonWorld.properties.apply {
            isPVPEnabled = false
            type = WorldType.CUSTOMIZED
            addGameRule("announceAdvancements", "false")
            addGameRule("keepInventory", "true")
            addGameRule("doDaylightCycle", "false")
            addGameRule("showDeathMessages", "false")
            addGameRule("doFireTick", "false")
        }
        // 初始化.
        when (copyZoneData.type) {
            DEFAULT -> FunctionDungeon.create(DefaultDungeon(copyZone, dungeonWorld))
            SPECIAL -> FunctionDungeon.create(SpecialDungeon(copyZone, dungeonWorld))
            UNLIMITED -> FunctionDungeon.create(UnlimitedDungeon(copyZone, dungeonWorld))
            WAVE -> FunctionDungeon.create(WaveDungeon(copyZone, dungeonWorld))
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
        if (profile.isEditing) {
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
        val dungeon = FunctionDungeon.getByPlayer(player.uniqueId) ?: return
        player.apply {
            playDeathAnimation()
            health = maxHealth
            turnToSpectator()
            sendTitle("&c&l菜".colored(), "", 0, 40, 10)
        }
        submit(async = false, delay = 10L) {
            if (dungeon.isAllPlayersDead() && dungeon.failThread == null) {
                dungeon.startFailThread()
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
            if (profile.number <= zone.data.number) {
                profile.number = zone.data.number + 1
            }
        }
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val player = e.player
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.failHudID, DragonCoreCompat.failHudYaml)
        DragonCoreCompat.closeFailHud(player)
    }
}