package net.sakuragame.eternal.kirradungeon.server.zone

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.zone.sync.ZoneCondition
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored

/**
 * 副本类.
 *
 * @property id 副本 Id.
 * @property name 副本显示名称.
 * @property data 副本数据.
 */
data class Zone(val id: String, val name: String, val data: ZoneData) {

    override fun toString() = "Zone($id, ${name.uncolored()}, $data, ${ZoneCondition.getConditionByName(id) ?: ""})"

    companion object {

        var editingDungeonWorld: DungeonWorld? = null

        val zones = mutableListOf<Zone>()

        fun getByName(name: String) = zones.firstOrNull { it.name == name }

        fun getByID(id: String) = zones.firstOrNull { it.id == id }

        @Awake(LifeCycle.ENABLE)
        fun i() {
            clear()
            KirraDungeonServer.data.getKeys(false).forEach {
                val id = it
                val name = KirraDungeonServer.data.getString("$it.name") ?: return@forEach
                zones += Zone(
                    id, name.colored(), ZoneData(
                        type = FunctionZone.readType(id),
                        maxLastTime = FunctionZone.readMaxLastTime(id),
                        monsterData = FunctionZone.readMonsterData(id),
                        spawnLoc = FunctionZone.readSpawnLoc(id)!!,
                        zoneSkyData = FunctionZone.readSkyData(id),
                        number = FunctionZone.readNumber(id),
                        iconNumber = FunctionZone.readIcon(id),
                        resurgenceTime = FunctionZone.readResurgenceTime(id),
                        waveData = FunctionZone.readWaveData(id),
                        waveSpawnLocs = FunctionZone.readWaveLocs(id)
                    )
                )
            }
            submit(async = true, delay = 2L) {
                ZoneCondition.sync2Redis()
            }
        }

        fun clear() {
            zones.clear()
        }

        fun create(id: String, name: String) {
            FunctionZone.writeDefaultDataToConf(id, name)
            i()
        }
    }
}
