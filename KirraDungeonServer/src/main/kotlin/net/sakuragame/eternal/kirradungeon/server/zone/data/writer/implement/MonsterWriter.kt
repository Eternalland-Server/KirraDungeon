package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneMonsterData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBossData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneMobData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object MonsterWriter : WriteHelper {

    fun setMob(zone: Zone, loc: ZoneLocation, id: String, amount: Int, levelRange: IntRange) {
        val mobs = arrayListOf<String>().apply {
            addAll(data.getStringList("${zone.id}.mobs"))
        }
        mobs.add("$loc; $id; $amount; $levelRange")
        KirraDungeonServer.data["${zone.id}.mobs"] = mobs
        reload()
    }

    fun setBoss(zone: Zone, loc: ZoneLocation, id: String, levelRange: IntRange) {
        data["${zone.id}.boss.id"] = id
        data["${zone.id}.boss.loc"] = loc.toString()
        data["${zone.id}.boss.level-range"] = levelRange.toString()
        reload()
    }

    fun read(id: String): ZoneMonsterData {
        val mobData = mutableListOf<ZoneMobData>().also { list ->
            data.getStringList("$id.mobs").forEach string@{ string ->
                val split = string.splitWithNoSpace(";")
                if (split.size < 4) return@string
                val loc = ZoneLocation.parseToZoneLocation(split[0])!!
                val monsterId = split[1]
                val amount = split[2].toInt()
                val levelRange = split[3].parseIntRange() ?: return@string
                list += ZoneMobData(loc, monsterId, amount, levelRange)
            }
        }
        val bossData = ZoneBossData(
            ZoneLocation.parseToZoneLocation(KirraDungeonServer.data.getString("$id.boss.loc")!!)!!,
            KirraDungeonServer.data.getString("$id.boss.id")!!,
            KirraDungeonServer.data.getString("$id.boss.level-range")?.parseIntRange() ?: IntRange(1, 1)
        )
        return ZoneMonsterData(bossData, mobData)
    }
}