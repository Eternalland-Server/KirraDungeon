package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import ink.ptms.adyeshach.api.Hologram
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneHologramData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object HologramWriter : WriteHelper {

    val editingHolograms = mutableMapOf<String, Hologram<*>>()

    fun set(zone: Zone, id: String, message: List<String>, loc: ZoneLocation) {
        data["${zone.id}.holograms.$id.loc"] = loc.toString()
        data["${zone.id}.holograms.$id.contents"] = message
        reload()
    }

    fun remove(zone: Zone, id: String) {
        data["${zone.id}.holograms.$id"] = null
        reload()
    }

    fun read(id: String): List<ZoneHologramData> {
        val toReturn = mutableListOf<ZoneHologramData>()
        val sections = data.getConfigurationSection("$id.holograms")?.getKeys(false) ?: return emptyList()
        sections.forEach {
            val loc = ZoneLocation.parseToZoneLocation(data.getString("$id.holograms.$it.loc") ?: return@forEach) ?: return@forEach
            val contents = data.getStringList("$id.holograms.$it.contents")
            toReturn += ZoneHologramData(loc, contents)
        }
        return toReturn
    }
}