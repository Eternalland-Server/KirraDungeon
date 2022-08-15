package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveBossData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveMonsterData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper
import org.bukkit.Location

object WaveDataWriter : WriteHelper {

    fun setWaveMob(wave: Int, zone: Zone, monsterId: String, amount: Int, health: Double) {
        val file = getFile(zone.id)
        val mobs = arrayListOf<String>().also {
            val strList = file.getStringList("wave.$wave.monsters")
            if (strList.isNotEmpty()) {
                it.addAll(file.getStringList("wave.$wave.monsters"))
            }
        }
        mobs += "$monsterId@$amount@$health"
        file["wave.$wave.monsters"] = mobs
        reload()
    }

    fun setWaveBoss(wave: Int, zone: Zone, bossId: String, health: Double) {
        val file = getFile(zone.id)
        file["wave.$wave.boss"] = "$bossId@$health"
        reload()
    }

    fun addWaveLoc(zone: Zone, loc: Location) {
        val id = zone.id
        val file = getFile(zone.id)
        val locs = arrayListOf<String>().also {
            val strList = file.getStringList("wave-locs")
            if (strList.isNotEmpty()) {
                it.addAll(file.getStringList("wave-locs"))
            }
        }
        locs.add(ZoneLocation.parseToZoneLocation(loc).toString())
        file["wave-locs"] = locs
        reload()
    }

    fun readLoc(id: String): List<ZoneLocation>? {
        val locs = mutableListOf<ZoneLocation>()
        val file = getFile(id)
        file.getStringList("wave-locs").forEach {
            locs.add(ZoneLocation.parseToZoneLocation(it) ?: return@forEach)
        }
        if (locs.isEmpty()) {
            return null
        }
        return locs
    }

    fun readData(id: String): List<ZoneWaveData>? {
        if (TypeWriter.read(id) != ZoneType.WAVE) {
            return null
        }
        val file = getFile(id)
        val waveList = mutableListOf<ZoneWaveData>()
        val sections = file.getConfigurationSection("wave") ?: return null
        sections.getKeys(false).forEach {
            val monsterDataList = mutableListOf<ZoneWaveMonsterData>()
            val index = it.toInt()
            file.getStringList("wave.$index.monsters").forEach { str ->
                val split = str.split("@")
                val monsterId = split[0]
                val amount = split[1].toIntOrNull() ?: 1
                val health = split[2].toDoubleOrNull() ?: 1.0
                monsterDataList += ZoneWaveMonsterData(monsterId, amount, health)
            }
            val boss = file.getString("wave.$index.boss") ?: return null
            val split = boss.split("@")
            val bossId = split[0]
            val bossHealth = split[1].toDoubleOrNull() ?: 1.0
            val bossData = ZoneWaveBossData(bossId, bossHealth)
            waveList += ZoneWaveData(monsterDataList, bossData)
        }
        return waveList
    }
}