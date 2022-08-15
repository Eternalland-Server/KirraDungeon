package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneTriggerData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBlockData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper
import org.bukkit.Bukkit

object TriggerWriter : WriteHelper {

    fun setTrigger(zone: Zone, triggerLoc: ZoneLocation) {
        val file = getFile(zone.id)
        file["trigger.loc"] = triggerLoc.toString()
        reload()
    }

    fun setBlock(zone: Zone, blocks: List<ZoneBlockData>) {
        val file = getFile(zone.id)
        val size = file.getConfigurationSection("trigger.blocks")?.getKeys(false)?.size ?: 0
        file["trigger.blocks.${size + 1}"] = blocks.map { it.toString() }
        reload()
    }

    fun read(id: String): ZoneTriggerData {
        val file = getFile(id)
        val trigger = file.getString("trigger.loc")?.let { ZoneLocation.parseToZoneLocation(it) } ?: ZoneLocation.parseToZoneLocation(Bukkit.getWorld("world").spawnLocation)
        val blocks = readBlocks(id)
        return ZoneTriggerData(trigger, blocks)
    }

    private fun readBlocks(id: String): MutableList<List<ZoneBlockData>> {
        val toReturn = mutableListOf<List<ZoneBlockData>>()
        val file = getFile(id)
        val section = file.getConfigurationSection("trigger.blocks")?.getKeys(false) ?: return toReturn
        section.map { it.toInt() }.sorted().forEach { num ->
            val blocks = file.getStringList("trigger.blocks.$num").map { ZoneBlockData.parseFromString(it) ?: return@forEach }
            toReturn += blocks
        }
        return toReturn
    }
}