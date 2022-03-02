package net.sakuragame.eternal.kirradungeon.client.zone.util

import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency
import net.sakuragame.eternal.kirradungeon.client.getTodayTimeUnix
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirradungeon.client.zone.ZoneCondition
import org.bukkit.entity.Player

fun getNonDefaultZoneCondition(player: Player, zone: Zone): ZoneCondition {
    return zone.condition
        .firstOrNull { it.permissionName != "default" && player.hasPermission(it.permissionName) }
        ?: getDefaultZoneConditions(zone)
}

fun getDefaultZoneConditions(zone: Zone): ZoneCondition {
    return zone.condition.first()
}

fun Player.getFeeMaxJoinCounts(zone: Zone): Int {
    if (isOp) return -1
    return getNonDefaultZoneCondition(this, zone).dailyCounts
}

fun Player.getFeeJoinCounts(zone: Zone): Int {
    val maxJoinCounts = getFeeMaxJoinCounts(zone)
    if (maxJoinCounts == -1) return -1
    val userLogs = DungeonClientAPI.getLogManager().getUserLogs(uniqueId, zone.name, false, getTodayTimeUnix(), -1).size
    return maxJoinCounts - userLogs
}

fun Player.getZoneFee(zone: Zone): MutableMap<EternalCurrency, Double> {
    val emptyFeeData = mutableMapOf<EternalCurrency, Double>()
    val condition = getNonDefaultZoneCondition(this, zone)
    if (isOp) return emptyFeeData
    condition.feeToTypeMap.forEach { (currencyString, value) ->
        val currency = EternalCurrency.values().find { currencyString.lowercase() == it.identifier } ?: EternalCurrency.Coins
        emptyFeeData[currency] = value
    }
    return emptyFeeData
}

fun Player.getZoneItems(zone: Zone): MutableMap<String, Int> {
    val emptyItemsData = mutableMapOf<String, Int>()
    if (isOp) return emptyItemsData
    return getNonDefaultZoneCondition(this, zone).itemIDToAmountMap
}