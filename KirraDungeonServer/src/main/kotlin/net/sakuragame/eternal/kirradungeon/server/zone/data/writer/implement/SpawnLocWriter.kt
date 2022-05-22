package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object SpawnLocWriter : WriteHelper {

    fun set(zone: Zone, loc: ZoneLocation) {
        data["${zone.id}.spawn-loc"] = loc.toString()
        reload()
    }

    fun read(id: String) = ZoneLocation.parseToZoneLocation(data.getString("$id.spawn-loc")!!)
}