package net.sakuragame.eternal.kirradungeon.server.zone.data

import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBlockData

data class ZoneTriggerData(val blockMap: MutableMap<Int, List<ZoneBlockData>>)