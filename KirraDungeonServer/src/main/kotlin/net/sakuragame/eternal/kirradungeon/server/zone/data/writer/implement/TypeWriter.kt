package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object TypeWriter : WriteHelper {

    fun set(zone: Zone, type: ZoneType) {
        val file = getFile(zone.id)
        file["${zone.id}.type"] = type.name
        reload()
    }

    fun read(id: String): ZoneType {
        val file = getFile(id)
        return ZoneType.values().find { it.name == file.getString("type") } ?: ZoneType.DEFAULT
    }
}