package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object ResurgenceTimeWriter : WriteHelper {

    fun set(zone: Zone, num: Int) {
        data["${zone.id}.resurgence-time"] = num
        reload()
    }

    fun read(id: String) = data.getInt("$id.resurgence-time")
}