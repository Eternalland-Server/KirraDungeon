package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.justability.api.event.PowerCastEvent
import net.sakuragame.eternal.justlevel.JustLevel
import net.sakuragame.eternal.justlevel.api.JustLevelAPI
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.ParkourDungeon
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang

@Suppress("SpellCheckingInspection")
object FunctionParkourDungeon {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3) {
            val player = e.player
            val profile = player.profile() ?: return@submit
            val dungeonWorld = e.dungeonWorld
            if (Zone.editingDungeonWorld != null) {
                return@submit
            }
            if (!isDungeonFromParkour(e.dungeonWorld.worldIdentifier)) {
                return@submit
            }
            val dungeon = FunctionDungeon.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.PARKOUR
            profile.zoneUUID = dungeon.uuid
            dungeon.addPlayerUUID(player.uniqueId)
            dungeon.handleJoin(player, spawnBoss = false, spawnMob = false, showTimeBar = true)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val player = e.player
        if (e.action != Action.RIGHT_CLICK_BLOCK || e.clickedBlock.type != Material.LEVER) {
            return
        }
        val loc = player.location
        val dungeon = FunctionDungeon.getByPlayer(player.uniqueId) as? ParkourDungeon ?: return
        dungeon.locationRecorder[player.uniqueId] = loc
        player.sendLang("message-parkour-dungeon-location-record")
    }

    @SubscribeEvent
    fun onInteractPressurePlate(e: PlayerInteractEvent) {
        val player = e.player
        if (e.action != Action.PHYSICAL) {
            return
        }
        val dungeon = FunctionDungeon.getByPlayer(player.uniqueId) as? ParkourDungeon ?: return
        val block = e.clickedBlock
        if (block.type == Material.IRON_PLATE && !dungeon.isClear) {
            dungeon.clear()
        }
    }

    @SubscribeEvent
    fun e(e: PowerCastEvent.Pre) {
        val player = e.player
        if (FunctionDungeon.getByPlayer(player.uniqueId) is ParkourDungeon) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun onEditingMerge(e: ItemMergeEvent) {
        val world = e.entity.world
        val zoneWorld = Zone.editingDungeonWorld?.bukkitWorld ?: return
        if (world.uid == zoneWorld.uid) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: ItemMergeEvent) {
        val world = e.entity.world
        if (FunctionDungeon.getByBukkitWorldUUID(world.uid) is ParkourDungeon) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        val player = e.player
        if (e.from.x == e.to.x && e.from.y == e.to.y && e.from.z == e.to.z) {
            return
        }
        val dungeon = FunctionDungeon.getByPlayer(player.uniqueId) as? ParkourDungeon ?: return
        val to = e.to ?: return
        val pullBackYCoord = KirraDungeonServer.conf.getInt("settings.pull-back-y-coord")
        val loc = dungeon.locationRecorder[player.uniqueId] ?: dungeon.zone.data.spawnLoc.toBukkitLocation(player.world)
        if (to.y < pullBackYCoord) {
            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            player.teleport(loc)
            player.sendLang("message-player-lifted-from-void")
        }
    }

    private fun isDungeonFromParkour(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.PARKOUR
    }
}