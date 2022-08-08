package net.sakuragame.eternal.kirradungeon.server.zone.data.sub

import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation

data class ZoneMobData(val loc: ZoneLocation, val type: String, val amount: Int, val levelRange: IntRange)