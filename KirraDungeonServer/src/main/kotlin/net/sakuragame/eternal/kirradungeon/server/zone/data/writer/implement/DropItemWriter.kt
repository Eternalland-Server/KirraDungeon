package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneDropData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object DropItemWriter : WriteHelper {

    fun setDrop(zone: Zone, mobId: String, itemId: String, chance: Double, amountRange: IntRange) {
        KirraDungeonServer.data["${zone.id}.drops.$mobId"] = "$itemId; ${chance.coerceAtMost(1.0)}; $amountRange"
        reload()
    }

    fun read(id: String): MutableMap<String, MutableList<ZoneDropData>> {
        val toReturn = mutableMapOf<String, MutableList<ZoneDropData>>()
        val section = KirraDungeonServer.data.getConfigurationSection("$id.drops") ?: return mutableMapOf()
        section.getKeys(false).forEach {
            val split = data.getString("$id.drops.$it")?.splitWithNoSpace(";") ?: return@forEach
            val itemId = split[0]
            val chance = split[1].toDouble()
            val amountRange = split[2].parseIntRange()!!
            if (toReturn[it] == null) {
                toReturn[it] = mutableListOf(ZoneDropData(itemId, chance, amountRange))
            } else {
                toReturn[it]!!.add(ZoneDropData(itemId, chance, amountRange))
            }
        }
        return toReturn
    }
}