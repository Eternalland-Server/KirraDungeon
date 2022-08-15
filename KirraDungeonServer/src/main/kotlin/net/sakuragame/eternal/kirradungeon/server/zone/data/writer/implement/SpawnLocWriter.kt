package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object SpawnLocWriter : WriteHelper {

    fun set(zone: Zone, loc: ZoneLocation) {
        val file = getFile(zone.id)
        file["spawn-loc"] = loc.toString()
        reload()
    }

    fun read(id: String): ZoneLocation? {
        val file = getFile(id)
        return ZoneLocation.parseToZoneLocation(file.getString("$id.spawn-loc")!!)
    }
}