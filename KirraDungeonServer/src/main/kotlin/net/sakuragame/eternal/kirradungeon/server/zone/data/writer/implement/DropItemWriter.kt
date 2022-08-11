package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneDropData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object DropItemWriter : WriteHelper {

    fun clear(zone: Zone) {
        data["${zone.id}.drops"] = mutableListOf<String>()
        reload()
    }

    fun setDrop(zone: Zone, mobId: String, itemId: String, chance: Double, amountRange: IntRange) {
        val drops = arrayListOf<String>().apply {
            addAll(data.getStringList("${zone.id}.drops"))
        }
        drops += "$mobId; $itemId; ${chance.coerceAtMost(1.0)}; $amountRange"
        KirraDungeonServer.data["${zone.id}.drops"] = drops
        reload()
    }

    fun read(id: String): MutableMap<String, MutableList<ZoneDropData>> {
        val toReturn = mutableMapOf<String, MutableList<ZoneDropData>>()
        val drops = KirraDungeonServer.data.getStringList("$id.drops")
        drops.forEach {
            val split = it.splitWithNoSpace(";")
            if (split.size < 4) {
                return@forEach
            }
            val mobId = split[0]
            val itemId = split[1]
            val chance = split[2].toDouble()
            val amountRange = split[3].parseIntRange()!!
            if (toReturn[mobId] == null) {
                toReturn[mobId] = mutableListOf(ZoneDropData(itemId, chance, amountRange))
            } else {
                toReturn[mobId]!!.add(ZoneDropData(itemId, chance, amountRange))
            }
        }
        return toReturn
    }
}