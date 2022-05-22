package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneOreData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object OreWriter : WriteHelper {

    fun set(zone: Zone, id: String, oreId: String, loc: ZoneLocation) {
        data["${zone.id}.ore.$id"] = "$oreId@$loc"
        reload()
    }

    fun remove(zone: Zone, id: String) {
        data["${zone.id}.ore.$id"] = null
        reload()
    }

    fun read(id: String): List<ZoneOreData> {
        val models = mutableListOf<ZoneOreData>()
        val sections = data.getConfigurationSection("$id.ore")?.getKeys(false) ?: return emptyList()
        sections.forEach {
            val split = data.getString("$id.ore.$it")?.split("@") ?: return@forEach
            val modelId = split[0]
            val loc = ZoneLocation.parseToZoneLocation(split[1]) ?: return@forEach
            models += ZoneOreData(it, modelId, loc)
        }
        return models
    }
}