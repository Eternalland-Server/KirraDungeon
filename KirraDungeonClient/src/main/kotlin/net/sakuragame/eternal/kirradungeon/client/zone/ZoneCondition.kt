package net.sakuragame.eternal.kirradungeon.client.zone

import com.google.gson.Gson

/**
 * KirraZones
 * net.sakuragame.kirrazones.client.ZoneFrequency
 *
 * @author kirraObj
 * @since 2021/11/4 14:11
 */
data class ZoneCondition(
    val permissionName: String = "default",
    val dailyCounts: Int,
    val feeToTypeMap: MutableMap<String, Double>,
    val itemIDToAmountMap: MutableMap<String, Int>,
) {

    companion object {

        private val gson = Gson()

        fun zoneConditionToString(condition: ZoneCondition) = gson.toJson(condition)!!

        fun stringToZoneCondition(string: String) = gson.fromJson(string, ZoneCondition::class.java)!!
    }
}