package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneParkourDropData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper
import org.bukkit.Bukkit

object ParkourDropWriter : WriteHelper {

    fun set(zone: Zone, loc: ZoneLocation, type: Int, value: Int, amount: Int) {
        val file = getFile(zone.id)
        val parkourDrops = mutableListOf<String>().apply {
            addAll(file.getStringList("parkour-drops"))
        }
        parkourDrops += "$loc; $type; $value; $amount"
        file["parkour-drops"] = parkourDrops
        reload()
    }

    fun read(id: String): MutableList<ZoneParkourDropData> {
        val file = getFile(id)
        val strList = file.getStringList("parkour-drops")
        val toReturn = mutableListOf<ZoneParkourDropData>()
        if (strList.isEmpty()) {
            return mutableListOf()
        }
        strList.forEach {
            val split = it.splitWithNoSpace(";")
            if (split.size < 4) {
                return@forEach
            }
            val loc = ZoneLocation.parseToZoneLocation(split[0]) ?: return@forEach
            val type = split[1].toIntOrNull() ?: return@forEach
            val value = split[2].toIntOrNull() ?: return@forEach
            val amount = split[3].toIntOrNull() ?: return@forEach
            toReturn += ZoneParkourDropData(loc, type, value, amount)
        }
        return toReturn
    }
}