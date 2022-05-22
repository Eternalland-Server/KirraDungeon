package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object TypeWriter : WriteHelper {

    fun set(zone: Zone, type: ZoneType) {
        data["${zone.id}.type"] = type.name
        reload()
    }

    fun read(id: String): ZoneType = ZoneType.values().find { it.name == data.getString("$id.type") } ?: ZoneType.DEFAULT
}