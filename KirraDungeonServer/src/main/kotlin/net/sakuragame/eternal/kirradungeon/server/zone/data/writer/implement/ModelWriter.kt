package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneModelData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object ModelWriter : WriteHelper {

    fun set(zone: Zone, id: String, modelId: String, loc: ZoneLocation) {
        data["${zone.id}.model.$id"] = "$modelId@$loc"
        reload()
    }

    fun remove(zone: Zone, id: String) {
        data["${zone.id}.model.$id"] = null
        reload()
    }

    fun read(id: String): List<ZoneModelData> {
        val models = mutableListOf<ZoneModelData>()
        val sections = data.getConfigurationSection("$id.model")?.getKeys(false) ?: return emptyList()
        sections.forEach {
            val split = data.getString("$id.model.$it")?.split("@") ?: return@forEach
            val modelId = split[0]
            val loc = ZoneLocation.parseToZoneLocation(split[1]) ?: return@forEach
            models += ZoneModelData(it, modelId, loc)
        }
        return models
    }
}