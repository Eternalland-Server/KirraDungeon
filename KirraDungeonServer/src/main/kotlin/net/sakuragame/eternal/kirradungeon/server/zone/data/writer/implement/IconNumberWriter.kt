package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object IconNumberWriter : WriteHelper {

    fun set(zone: Zone, num: Int) {
        val file = getFile(zone.id)
        file["icon"] = num
        reload()
    }

    fun read(id: String): Int {
        val file = getFile(id)
        return file.getInt("icon")
    }
}