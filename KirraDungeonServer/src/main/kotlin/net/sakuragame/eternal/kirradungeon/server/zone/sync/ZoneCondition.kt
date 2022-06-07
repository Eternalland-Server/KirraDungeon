package net.sakuragame.eternal.kirradungeon.server.zone.sync

import com.google.gson.Gson
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.debug
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.ConditionWriter
import taboolib.common.platform.function.submit


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
    val number: Int
) {

    companion object {

        private val conditionMap = mutableMapOf<String, List<ZoneCondition>>()

        fun getConditionByName(zoneId: String): List<ZoneCondition>? {
            if (!conditionMap.containsKey(zoneId)) {
                return null
            }
            return conditionMap[zoneId]
        }

        fun sync2Redis() {
            initConditionsMap()
            doRedisRecycle()
            submit(async = true, delay = 2L) {
                debug(conditionMap)
                conditionMap.forEach { (id, zoneConditionList) ->
                    KirraDungeonServer.redisConn.lpush("KirraDungeonNames", id)
                    zoneConditionList.forEach { zoneCondition ->
                        KirraDungeonServer.redisConn.lpush("KirraDungeonConditions:$id", Gson().toJson(zoneCondition))
                    }
                }
                KirraDungeonServer.redisManager.publish("KirraDungeon", "main", "update-condition")
            }
        }

        fun initConditionsMap() {
            Zone.zones.forEach { key ->
                val conditions = ConditionWriter.read(key.id)
                conditionMap[key.id] = conditions
            }
        }

        fun doRedisRecycle() {
            KirraDungeonServer.redisConn.del("KirraDungeonNames")
            conditionMap.keys.forEach {
                KirraDungeonServer.redisConn.del("KirraDungeonConditions:$it")
            }
        }
    }
}