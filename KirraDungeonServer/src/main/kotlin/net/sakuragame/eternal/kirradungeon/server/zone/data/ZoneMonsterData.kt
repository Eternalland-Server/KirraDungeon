package net.sakuragame.eternal.kirradungeon.server.zone.data

import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBossData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneMobData

data class ZoneMonsterData(val boss: ZoneBossData, val mobList: MutableList<ZoneMobData>)