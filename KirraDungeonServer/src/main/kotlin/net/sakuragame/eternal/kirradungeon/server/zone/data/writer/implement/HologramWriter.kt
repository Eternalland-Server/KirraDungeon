package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import ink.ptms.adyeshach.api.Hologram
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneHologramData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object HologramWriter : WriteHelper {

    val editingHolograms = mutableMapOf<String, Hologram<*>>()

    fun set(zone: Zone, id: String, message: List<String>, loc: ZoneLocation) {
        val file = getFile(zone.id)
        file["holograms.$id.loc"] = loc.toString()
        file["holograms.$id.contents"] = message
        reload()
    }

    fun remove(zone: Zone, id: String) {
        val file = getFile(zone.id)
        file["holograms.$id"] = null
        reload()
    }

    fun read(id: String): List<ZoneHologramData> {
        val toReturn = mutableListOf<ZoneHologramData>()
        val file = getFile(id)
        val sections = file.getConfigurationSection("holograms")?.getKeys(false) ?: return emptyList()
        sections.forEach {
            val loc = ZoneLocation.parseToZoneLocation(file.getString("holograms.$it.loc") ?: return@forEach) ?: return@forEach
            val contents = file.getStringList("holograms.$it.contents")
            toReturn += ZoneHologramData(it, loc, contents)
        }
        return toReturn
    }
}