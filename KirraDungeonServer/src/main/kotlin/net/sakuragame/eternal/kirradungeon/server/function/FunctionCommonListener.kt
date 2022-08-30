package net.sakuragame.eternal.kirradungeon.server.function

import ink.ptms.zaphkiel.ZaphkielAPI
import net.minecraft.server.v1_12_R1.TileEntitySkull
import net.sakuragame.dungeonsystem.server.api.event.DungeonLoadedEvent
import net.sakuragame.eternal.dragoncore.api.PlayerAPI
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.common.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.*
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType.*
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.*
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirramodel.KirraModelAPI
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.world.WorldUnloadEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.groundBlock
import taboolib.platform.util.isAir
import taboolib.platform.util.sendLang

/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.server.function.FunctionListener
 *
 * @author kirraObj
 * @since 2021/11/8 3:51
 */
@Suppress("SpellCheckingInspection")
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: ItemSpawnEvent) {
        val item = e.entity.itemStack
        if (item.isAir()) {
            e.isCancelled = true
            return
        }
        val itemStream = ZaphkielAPI.read(item)
        if (itemStream.isVanilla()) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if (e.player.gameMode == GameMode.SPECTATOR) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerPickupItemEvent) {
        val player = e.player
        if (getEditingZone(player, silent = true) != null) {
            if (player.gameMode == GameMode.SURVIVAL) {
                e.item.remove()
            }
            e.isCancelled = true
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
        // 模型数据生成
        copyZone.data.models.forEach {
            val model = KirraModelAPI.models[it.model] ?: return@forEach
            KirraModelAPI.createTempModel(it.loc.toBukkitLocation(dungeonWorld.bukkitWorld), model, it.id)
        }
        // 矿物数据生成
        copyZone.data.ores.forEach {
            val ore = KirraMinerAPI.ores[it.ore] ?: return@forEach
            KirraMinerAPI.createTempOre(it.id, ore, it.loc.toBukkitLocation(dungeonWorld.bukkitWorld))
        }
        dungeonWorld.bukkitWorld.isAutoSave = false
        dungeonWorld.bukkitWorld.apply {
            pvp = false
            setGameRuleValue("doMobSpawning", "false")
            setGameRuleValue("announceAdvancements", "false")
            setGameRuleValue("keepInventory", "true")
            setGameRuleValue("doDaylightCycle", "false")
            setGameRuleValue("showDeathMessages", "false")
            setGameRuleValue("doFireTick", "false")
        }
        // 初始化.
        when (copyZoneData.type) {
            DEFAULT -> FunctionDungeon.create(DefaultDungeon(copyZone, dungeonWorld))
            SPECIAL -> FunctionDungeon.create(SpecialDungeon(copyZone, dungeonWorld))
            UNLIMITED -> FunctionDungeon.create(UnlimitedDungeon(copyZone, dungeonWorld))
            WAVE -> FunctionDungeon.create(WaveDungeon(copyZone, dungeonWorld))
            PARKOUR -> FunctionDungeon.create(ParkourDungeon(copyZone, dungeonWorld))
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
    fun e(e: PlayerMoveEvent) {
        val player = e.player
        if (e.from.x == e.to.x && e.from.y == e.to.y && e.from.z == e.to.z) {
            return
        }
        handleLift(e, player)
        handleParkourBlock(player)
    }

    @SubscribeEvent
    fun e(e: PlayerDeathEvent) {
        val player = e.entity
        val profile = player.profile() ?: return
        if (!profile.isChallenging) return
        val dungeon = FunctionDungeon.getByPlayer(player.uniqueId) ?: return
        profile.deathPlace = player.location
        player.apply {
            playDeathAnimation()
            health = maxHealth
            turnToSpectator()
            val deathNotice = KirraDungeonServer.conf.getStringList("settings.death-notices").randomOrNull() ?: "&c&l菜"
            sendTitle(deathNotice.colored(), "", 0, 40, 10)
        }
        submit(async = false, delay = 10L) {
            if (dungeon.isAllPlayersDead() && dungeon.failThread == null) {
                dungeon.startFailThread()
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        val player = e.player
        val profile = player.profile() ?: return
        if (!profile.isChallenging) return
        if (e.player.gameMode != GameMode.CREATIVE) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: DungeonClearEvent) {
        val zone = Zone.getByID(e.dungeonId) ?: return
        e.players.forEach {
            val profile = it.profile() ?: return@forEach
            if (profile.number <= zone.data.number) {
                profile.number = zone.data.number + 1
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockPhysicsEvent) {
        val world = e.block.world
        val uid = world.uid
        if (uid == Zone.editingDungeonWorld?.bukkitWorld?.uid) {
            e.isCancelled = true
            return
        }
        if (FunctionDungeon.getByDungeonWorldUUID(uid) == null) {
            return
        }
        e.isCancelled = true
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val player = e.player
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
    }

    private fun handleLift(e: PlayerMoveEvent, player: Player) {
        val profile = player.profile() ?: return
        if (profile.isEditing) {
            return
        }
        val to = e.to ?: return
        val pullBackYCoord = KirraDungeonServer.conf.getInt("settings.pull-back-y-coord")
        if (to.y < pullBackYCoord) {
            val dungeon = profile.getIDungeon() ?: return
            if (dungeon is ParkourDungeon) {
                return
            }
            player.teleport(dungeon.zone.data.spawnLoc.toBukkitLocation(player.world))
            player.sendLang("message-player-lifted-from-void")
            return
        }
    }

    private fun handleParkourBlock(player: Player) {
        if (player.profile() == null) {
            return
        }
        val block = player.groundBlock
        if (block.type != Material.SKULL) {
            return
        }
        val moveDirection = PlayerAPI.getMoveDirection(player)?.clone() ?: return
        val entity = (block.world as CraftWorld).getTileEntityAt(block.x, block.y, block.z) as? TileEntitySkull ?: return
        val gameProfile = entity.gameProfile ?: return
        val name = gameProfile.name
        val multiplyValue = gameProfile.properties.get("multiplyValue")?.firstOrNull()?.value?.toDoubleOrNull() ?: return
        val yValue = gameProfile.properties.get("yValue")?.firstOrNull()?.value?.toDoubleOrNull() ?: return
        if (multiplyValue == 0.0 && yValue == 0.0) {
            return
        }
        when (name) {
            "303" -> PacketSender.sendPlaySound(
                player, "sounds/a/1000.ogg",
                0.33f, 1f,
                false,
                0f, 0f, 0f
            )

            else -> PacketSender.sendPlaySound(
                player, "sounds/a/1001.ogg",
                0.33f, 1f,
                false,
                0f, 0f, 0f
            )
        }
        moveDirection.multiply(multiplyValue)
        moveDirection.y = yValue
        player.velocity = moveDirection
    }
}