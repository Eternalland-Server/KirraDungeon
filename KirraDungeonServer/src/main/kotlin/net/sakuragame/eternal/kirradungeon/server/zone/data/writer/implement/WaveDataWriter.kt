package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper
import org.bukkit.Location

object WaveDataWriter : WriteHelper {

    fun setWaveMob(wave: Int, zone: Zone, monsterId: String, amount: Int, health: Double) {
        val id = zone.id
        val mobs = arrayListOf<String>().also {
            val strList = data.getStringList("${zone.id}.wave.$wave.monsters")
            if (strList.isNotEmpty()) {
                it.addAll(data.getStringList("${zone.id}.wave.$wave.monsters"))
            }
        }
        mobs += "$monsterId@$amount@$health"
        data["$id.wave.$wave.monsters"] = mobs
        reload()
    }

    fun setWaveBoss(wave: Int, zone: Zone, bossId: String, health: Double) {
        data["${zone.id}.wave.$wave.boss"] = "$bossId@$health"
        reload()
    }

    fun addWaveLoc(zone: Zone, loc: Location) {
        val id = zone.id
        val locs = arrayListOf<String>().also {
            val strList = data.getStringList("$id.wave-locs")
            if (strList.isNotEmpty()) {
                it.addAll(data.getStringList("$id.wave-locs"))
            }
        }
        locs.add(ZoneLocation.parseToZoneLocation(loc).toString())
        data["$id.wave-locs"] = locs
        reload()
    }

    fun readLoc(id: String): List<ZoneLocation>? {
        val locList = mutableListOf<ZoneLocation>()
        data.getStringList("$id.wave-locs").forEach {
            locList.add(ZoneLocation.parseToZoneLocation(it) ?: return@forEach)
        }
        if (locList.isEmpty()) {
            return null
        }
        return locList
    }

    fun readData(id: String): List<ZoneWaveData>? {
        if (TypeWriter.read(id) != ZoneType.WAVE) {
            return null
        }
        val waveList = mutableListOf<ZoneWaveData>()
        val sections = data.getConfigurationSection("$id.wave") ?: return null
        sections.getKeys(false).forEach {
            val monsterDataList = mutableListOf<ZoneWaveData.ZoneWaveMonsterData>()
            val index = it.toInt()
            data.getStringList("$id.wave.$index.monsters").forEach { str ->
                val split = str.split("@")
                val monsterId = split[0]
                val amount = split[1].toIntOrNull() ?: 1
                val health = split[2].toDoubleOrNull() ?: 1.0
                monsterDataList += ZoneWaveData.ZoneWaveMonsterData(monsterId, amount, health)
            }
            val boss = data.getString("$id.wave.$index.boss") ?: return null
            val split = boss.split("@")
            val bossId = split[0]
            val bossHealth = split[1].toDoubleOrNull() ?: 1.0
            val bossData = ZoneWaveData.ZoneWaveBossData(bossId, bossHealth)
            waveList += ZoneWaveData(monsterDataList, bossData)
        }
        return waveList
    }
}