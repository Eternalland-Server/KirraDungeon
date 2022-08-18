package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneParkourLocationData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object ParkourLocationWriter : WriteHelper {

    fun set(zone: Zone, loc: ZoneLocation) {
        val file = ParkourDropWriter.getFile(zone.id)
        val parkourDrops = mutableListOf<String>().apply {
            addAll(file.getStringList("parkour-locations"))
        }
        parkourDrops += loc.toString()
        file["parkour-locations"] = parkourDrops
        reload()
    }

    fun read(id: String): ZoneParkourLocationData? {
        val file = getFile(id)
        val strList = file.getStringList("parkour-locations")
        val toReturn = mutableListOf<ZoneLocation>()
        if (strList.isEmpty()) {
            return null
        }
        strList.forEach {
            toReturn += ZoneLocation.parseToZoneLocation(it) ?: return@forEach
        }
        return ZoneParkourLocationData(toReturn)
    }
}