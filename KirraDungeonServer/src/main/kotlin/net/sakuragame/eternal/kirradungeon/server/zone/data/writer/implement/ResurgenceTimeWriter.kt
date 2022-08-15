package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object ResurgenceTimeWriter : WriteHelper {

    fun set(zone: Zone, num: Int) {
        val file = getFile(zone.id)
        file["resurgence-time"] = num
        reload()
    }

    fun read(id: String): Int {
        val file = getFile(id)
        return file.getInt("$id.resurgence-time")
    }
}