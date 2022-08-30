package net.sakuragame.eternal.kirradungeon.server.zone.data.sub

import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation

data class ZoneStagedMobData(val loc: ZoneLocation, val monsterId: String, val amount: Int, val multiplier: Double)