package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneMonsterData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBossData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneMobData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object MonsterWriter : WriteHelper {

    fun clear(zone: Zone) {
        val file = getFile(zone.id)
        file["mobs"] = mutableListOf<String>()
        reload()
    }

    fun setMob(zone: Zone, loc: ZoneLocation, amount: Int, levelRange: IntRange) {
        val file = getFile(zone.id)
        val mobs = arrayListOf<String>().apply {
            addAll(file.getStringList("mobs"))
        }
        mobs.add("$loc; $amount; $levelRange")
        file["mobs"] = mobs
        reload()
    }

    fun setBoss(zone: Zone, loc: ZoneLocation, id: String, levelRange: IntRange) {
        val file = getFile(zone.id)
        file["boss.id"] = id
        file["boss.loc"] = loc.toString()
        file["boss.level-range"] = levelRange.toString()
        reload()
    }

    fun read(id: String): ZoneMonsterData {
        val file = getFile(id)
        val mobData = mutableListOf<ZoneMobData>().also { list ->
            file.getStringList("mobs").forEach strList@{ string ->
                val split = string.splitWithNoSpace(";")
                if (split.size < 3) return@strList
                val loc = ZoneLocation.parseToZoneLocation(split[0])!!
                val monsterId = getRandomMonster(id) ?: return@strList
                val amount = split[1].toInt()
                val levelRange = split[2].parseIntRange() ?: return@strList
                list += ZoneMobData(loc, monsterId, amount, levelRange)
            }
        }
        val bossData = ZoneBossData(
            ZoneLocation.parseToZoneLocation(file.getString("boss.loc")!!)!!,
            file.getString("$id.boss.id")!!,
            file.getString("$id.boss.level-range")?.parseIntRange() ?: IntRange(1, 1)
        )
        return ZoneMonsterData(bossData, mobData)
    }

    private fun getRandomMonster(id: String): String? {
        return DropItemWriter.read(id).keys.randomOrNull()
    }
}