package net.sakuragame.eternal.kirradungeon.server.zone

import net.sakuragame.eternal.kirradungeon.server.zone.data.*
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneDropData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneHologramData

/**
 * 副本数据
 *
 * @property type 副本类型
 * @property maxLastTime 最多攻克副本的时间 (秒数)
 * @property monsterData 怪物数据
 * @property spawnLoc 玩家出生坐标
 * @property zoneSkyData 天空数据
 * @property number      内部编号
 * @property iconNumber  图标编号
 * @property resurgenceTime 复活时间
 * @property models 模型数据
 * @property ores 矿物数据
 * @property trigger 方块触发数据
 *
 */
data class ZoneData(
    val type: ZoneType,
    val maxLastTime: Int,
    val monsterData: ZoneMonsterData,
    val monsterDropData: MutableMap<String, MutableList<ZoneDropData>>,
    val spawnLoc: ZoneLocation,
    val zoneSkyData: ZoneSkyData? = null,
    val number: Int,
    val iconNumber: Int,
    val resurgenceTime: Int,
    val models: List<ZoneModelData>,
    val ores: List<ZoneOreData>,
    val trigger: ZoneTriggerData,
    val holograms: List<ZoneHologramData>,
    val metadataMap: MutableMap<String, String>,
    val stagedMultiplier: IntRange?
) {

    fun isCustomSkyEnabled() = this.zoneSkyData != null
}
