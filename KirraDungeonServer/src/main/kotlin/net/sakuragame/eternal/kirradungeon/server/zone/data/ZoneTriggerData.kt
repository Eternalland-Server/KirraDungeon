package net.sakuragame.eternal.kirradungeon.server.zone.data

import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBlockData

data class ZoneTriggerData(val triggerLoc: ZoneLocation?, val blocks: MutableList<List<ZoneBlockData>>)