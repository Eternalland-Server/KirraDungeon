package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object IconNumberWriter : WriteHelper {

    fun set(zone: Zone, num: Int) {
        data["${zone.id}.icon"] = num
        reload()
    }

    fun read(id: String): Int = data.getInt("$id.icon")
}