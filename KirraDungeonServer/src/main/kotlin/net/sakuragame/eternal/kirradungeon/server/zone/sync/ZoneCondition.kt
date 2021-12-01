package net.sakuragame.eternal.kirradungeon.server.zone.sync

import com.google.gson.Gson
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer


/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.server.zone.sync.ZoneCondition
 *
 * @author kirraObj
 * @since 2021/11/8 4:39
 */
data class ZoneCondition(
    val permissionName: String = "default",
    val dailyCounts: Int,
    val feeToTypeMap: MutableMap<String, Double>,
    val itemIDToAmountMap: MutableMap<String, Int>,
) {

    companion object {

        val conditionMap = mutableMapOf<String, List<ZoneCondition>>()

        fun getConditionByName(zoneId: String): List<ZoneCondition>? {
            if (!conditionMap.containsKey(zoneId)) {
                return null
            }
            return conditionMap[zoneId]
        }

        fun syncToRedis() {
            doDataRecycle()
            conditionMap.forEach { (id, zoneConditionList) ->
                KirraDungeonServer.redisConn.async().lpush("KirraDungeonNames", id)
                zoneConditionList.forEach { zoneCondition ->
                    KirraDungeonServer.redisConn.async().lpush("KirraDungeonConditions:$id", Gson().toJson(zoneCondition))
                }
            }
        }

        private fun doDataRecycle() {
            KirraDungeonServer.redisConn.async().del("KirraDungeonNames")
            conditionMap.keys.forEach {
                KirraDungeonServer.redisConn.async().del("KirraDungeonConditions:$it")
            }
        }
    }
}