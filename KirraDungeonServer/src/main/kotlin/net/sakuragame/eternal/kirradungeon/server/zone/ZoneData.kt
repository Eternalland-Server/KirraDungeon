package net.sakuragame.eternal.kirradungeon.server.zone

import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneMonsterData
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneSkyData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveData

/**
 * 副本数据.
 *
 * @property type 副本类型.
 * @property maxLastTime 最多攻克副本的时间. (秒数)
 * @property monsterData 怪物数据.
 * @property spawnLoc 玩家出生坐标.
 * @property zoneSkyData 天空数据.
 * @property number      内部编号.
 * @property iconNumber  图标编号.
 * @property resurgenceTime 复活时间.
 * @property waveData      波次数据。
 *
 */
data class ZoneData(
    val type: ZoneType,
    val maxLastTime: Int,
    val monsterData: ZoneMonsterData,
    val spawnLoc: ZoneLocation,
    val zoneSkyData: ZoneSkyData? = null,
    val number: Int,
    val iconNumber: Int,
    val resurgenceTime: Int,
    val waveData: List<ZoneWaveData>? = null,
) {

    fun isCustomSkyEnabled() = this.zoneSkyData != null
}
