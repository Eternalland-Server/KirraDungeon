package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import net.sakuragame.eternal.justability.api.event.PowerCastEvent
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.ParkourDungeon
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.sendLang

@Suppress("SpellCheckingInspection")
object FunctionParkourDungeon {

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val player = e.player
        if (e.action != Action.RIGHT_CLICK_BLOCK || e.clickedBlock.type != Material.LEVER) {
            return
        }
        e.isCancelled = true
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
        if (block.type == Material.IRON_PLATE) {
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
            player.teleport(loc)
            player.sendLang("message-player-lifted-from-void")
        }
    }
}