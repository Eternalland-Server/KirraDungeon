package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object MaxLastTimeWriter : WriteHelper {

    fun set(zone: Zone, time: Int) {
        val file = getFile(zone.id)
        file["max-last-time"] = time
    }

    fun read(id: String): Int {
        val file = getFile(id)
        return file.getInt("max-last-time")
    }
}