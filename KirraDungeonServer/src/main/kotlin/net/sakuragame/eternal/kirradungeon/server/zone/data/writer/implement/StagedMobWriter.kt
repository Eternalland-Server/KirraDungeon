package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneStagedMobData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object StagedMobWriter : WriteHelper {

    fun clear(zone: Zone) {
        val file = getFile(zone.id)
        file["staged-mobs"] = mutableListOf<String>()
        reload()
    }

    fun setMob(zone: Zone, loc: ZoneLocation, type: String, amount: Int, multiplier: Double) {
        val file = getFile(zone.id)
        val mobs = arrayListOf<String>().apply {
            addAll(file.getStringList("staged-mobs"))
        }
        mobs.add("$loc; $type; $amount; $multiplier")
        file["staged-mobs"] = mobs
        reload()
    }

    fun read(id: String): List<ZoneStagedMobData> {
        val file = getFile(id)
        val toReturn = mutableListOf<ZoneStagedMobData>().also { list ->
            file.getStringList("staged-mobs").forEach strList@{ string ->
                val split = string.splitWithNoSpace(";")
                if (split.size < 4) return@strList
                val loc = ZoneLocation.parseToZoneLocation(split[0])!!
                val monsterId = split[1]
                val amount = split[2].toInt()
                val multiplier = split[3].toDouble()
                list += ZoneStagedMobData(loc, monsterId, amount, multiplier)
            }
        }
        return toReturn
    }
}