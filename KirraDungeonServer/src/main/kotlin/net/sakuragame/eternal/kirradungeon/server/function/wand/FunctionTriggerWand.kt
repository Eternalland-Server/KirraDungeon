package net.sakuragame.eternal.kirradungeon.server.function.wand

import net.sakuragame.eternal.kirradungeon.server.getEditingZone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBlockData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.TriggerWriter
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored
import taboolib.platform.util.buildItem
import java.util.*

object FunctionTriggerWand : IWand {

    val triggerWand by lazy {
        buildItem(Material.STICK) {
            name = "&e&l触发器魔杖".colored()
            lore += ""
            lore += "&7左键方块设置触发器".colored()
            lore += ""
            shiny()
        }
    }

    val currentBlocks = mutableListOf<ZoneBlockData>()

    val editingBlockPlayers = mutableListOf<UUID>()

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val item = e.item ?: return
        val block = e.clickedBlock ?: return
        val player = e.player
        if (!item.isSimilar(triggerWand)) {
            return
        }
        when (e.action) {
            Action.LEFT_CLICK_BLOCK -> {
                e.isCancelled = true
                val zone = getEditingZone(player) ?: return
                TriggerWriter.setTrigger(zone, ZoneLocation.parseToZoneLocation(block.location))
                player.sendMessage("&a设置成功.".colored())
            }
            else -> return
        }
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        val player = e.player
        if (editingBlockPlayers.contains(player.uniqueId)) {
            editingBlockPlayers -= player.uniqueId
        }
    }

    @SubscribeEvent
    fun e(e: BlockPlaceEvent) {
        val player = e.player
        val block = e.block
        if (editingBlockPlayers.contains(player.uniqueId)) {
            val type = when (block.type) {
                Material.BARRIER -> {
                    block.world.playEffect(block.location, Effect.HEART, 3)
                    Material.AIR
                }
                else -> {
                    block.world.playEffect(block.location, Effect.CLOUD, 3)
                    block.type
                }
            }
            currentBlocks += ZoneBlockData(ZoneLocation.parseToZoneLocation(block.location), type)
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        val player = e.player
        val block = e.block
        if (editingBlockPlayers.contains(player.uniqueId)) {
            currentBlocks.removeIf { it.loc.toBukkitLocation(block.world) == block.location }
        }
    }
}