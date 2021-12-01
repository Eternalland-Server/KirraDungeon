package net.sakuragame.eternal.kirradungeon.server.zone.sync

import com.google.gson.Gson
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer


/**
 * KirraZones
 * net.sakuragame.kirrazones.server.zone.sync.ZoneCondition
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
                KirraDungeonServer.redisConn.async().lpush("KirraZoneNames", id)
                zoneConditionList.forEach { zoneCondition ->
                    KirraDungeonServer.redisConn.async().lpush("KirraZoneConditions:$id", Gson().toJson(zoneCondition))
                }
            }
        }

        private fun doDataRecycle() {
            KirraDungeonServer.redisConn.async().del("KirraZoneNames")
            conditionMap.keys.forEach {
                KirraDungeonServer.redisConn.async().del("KirraZoneConditions:$it")
            }
        }
    }
}