package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object NumberWriter : WriteHelper {

    fun set(zone: Zone, num: Int) {
        data["${zone.id}.number"] = num
        reload()
    }

    fun read(id: String): Int = data.getInt("$id.number")
}