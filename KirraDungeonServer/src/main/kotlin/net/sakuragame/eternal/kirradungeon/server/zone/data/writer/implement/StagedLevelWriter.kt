package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object StagedLevelWriter : WriteHelper {

    fun set(zone: Zone, multiplier: IntRange) {
        val file = getFile(zone.id)
        file["staged-multiplier"] = multiplier.toString()
        reload()
    }

    fun read(id: String): IntRange? {
        val file = getFile(id)
        val multiplier = file.getString("staged-multiplier")?.parseIntRange() ?: return null
        return multiplier
    }
}