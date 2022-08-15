package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneDropData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper
import taboolib.module.configuration.ConfigFile

object DropItemWriter : WriteHelper {

    fun clear(zone: Zone) {
        val file = getFile(zone.id)
        file["drops"] = mutableListOf<String>()
        reload()
    }

    fun setDrop(zone: Zone, mobId: String, itemId: String, chance: Double, amountRange: IntRange) {
        val file = getFile(zone.id)
        val drops = arrayListOf<String>().apply {
            addAll(file.getStringList("drops"))
        }
        drops += "$mobId; $itemId; ${chance.coerceAtMost(1.0)}; $amountRange"
        file["drops"] = drops
        reload()
    }

    fun read(id: String): MutableMap<String, MutableList<ZoneDropData>> {
        val file = getFile(id)
        val toReturn = mutableMapOf<String, MutableList<ZoneDropData>>()
        val drops = file.getStringList("drops")
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