package net.sakuragame.eternal.kirradungeon.client.zone.util

import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency
import net.sakuragame.eternal.kirradungeon.client.getTodayTimeUnix
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import java.util.concurrent.atomic.AtomicInteger

fun getSymbolByIndex(index: Int) =
    when (index) {
        0 -> "|"
        1 -> "/"
        2 -> "-"
        3 -> "\\"
        else -> ""
    }

fun Player.getFeeMaxJoinCounts(zone: Zone, player: Player): Int {
    if (isOp) return -1
    return zone.condition.firstOrNull { player.hasPermission(it.permissionName) }?.dailyCounts ?: 0
}

fun Player.getFeeJoinCounts(zone: Zone): Int {
    val maxJoinCounts = getFeeMaxJoinCounts(zone, this)
    if (maxJoinCounts == -1) return 1
    val joinCounts = AtomicInteger(0)
    submit(async = true) {
        val userLogs = DungeonClientAPI.getLogManager().getUserLogs(uniqueId, zone.name, false, getTodayTimeUnix(), -1) ?: return@submit
        joinCounts.set(maxJoinCounts - userLogs.size)
    }
    return joinCounts.get()
}

fun Player.getZoneFee(zone: Zone): MutableMap<EternalCurrency, Double> {
    val emptyFeeData = mutableMapOf<EternalCurrency, Double>()
    val condition = zone.condition.firstOrNull { player.hasPermission(it.permissionName) } ?: return emptyFeeData
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
    return zone.condition.firstOrNull { player.hasPermission(it.permissionName) }?.itemIDToAmountMap ?: emptyItemsData
}