package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneDropData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object DropItemWriter : WriteHelper {

    fun setDrop(zone: Zone, mobId: String, itemId: Int, chance: Double, amountRange: IntRange) {
        KirraDungeonServer.data["${zone.id}.drops.$mobId"] = "$itemId; ${chance.coerceAtMost(1.0)}; $amountRange"
        reload()
    }

    fun read(id: String): MutableMap<String, ZoneDropData> {
        val toReturn = mutableMapOf<String, ZoneDropData>()
        val section = KirraDungeonServer.data.getConfigurationSection("${id}.drops") ?: return mutableMapOf()
        section.getKeys(false).forEach {
            val split = section.getString("$id.drops.$it")?.splitWithNoSpace(";") ?: return@forEach
            val itemId = split[0]
            val chance = split[1].toDouble()
            val amountRange = split[2].parseIntRange()!!
            toReturn[it] = ZoneDropData(itemId, chance, amountRange)
        }
        return toReturn
    }
}