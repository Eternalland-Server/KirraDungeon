package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object MaxLastTimeWriter : WriteHelper {

    fun set(zone: Zone, time: Int) = data.set("${zone.id}.max-last-time", time)

    fun read(id: String) = data.getInt("$id.max-last-time")
}