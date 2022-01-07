package net.sakuragame.eternal.kirradungeon.client.zone

import com.google.gson.Gson
import net.sakuragame.eternal.kirradungeon.client.printDebug

/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.client.ZoneFrequency
 *
 * @author kirraObj
 * @since 2021/11/4 14:11
 */
data class ZoneCondition(
    val permissionName: String = "default",
    val dailyCounts: Int,
    val feeToTypeMap: MutableMap<String, Double>,
    val itemIDToAmountMap: MutableMap<String, Int>,
    val number: Int
) {

    companion object {

        private val gson = Gson()

        fun stringToZoneCondition(string: String): ZoneCondition {
            string.printDebug()
            return gson.fromJson(string, ZoneCondition::class.java)!!
        }
    }
}