package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneTriggerData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBlockData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

@Suppress("UNCHECKED_CAST")
object TriggerWriter : WriteHelper {

    fun setTrigger(zone: Zone, triggerLoc: ZoneLocation) {
        data["${zone.id}.trigger.loc"] = triggerLoc.toString()
        reload()
    }

    fun setBlock(zone: Zone, blocks: List<ZoneBlockData>) {
        data["${zone.id}.triggers"] = (data.getList("${zone.id}.trigger.blocks") as? MutableList<List<ZoneBlockData>> ?: mutableListOf()).also {
            it += blocks
        }
        reload()
    }

    fun read(id: String): ZoneTriggerData {
        val trigger = data.getString("$id.trigger.loc")?.let { ZoneLocation.parseToZoneLocation(it) }
        val blocks = data.getList("$id.trigger.blocks") as? MutableList<List<ZoneBlockData>> ?: mutableListOf()
        return ZoneTriggerData(trigger, blocks)
    }
}