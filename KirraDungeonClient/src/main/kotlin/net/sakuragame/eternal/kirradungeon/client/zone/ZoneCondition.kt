package net.sakuragame.eternal.kirradungeon.client.zone

import com.google.gson.Gson
import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.eternal.gemseconomy.api.GemsEconomyAPI
import net.sakuragame.eternal.justmessage.api.common.NotifyBox
import net.sakuragame.eternal.kirradungeon.client.printDebug
import net.sakuragame.eternal.kirradungeon.client.zone.util.getFeeJoinCounts
import net.sakuragame.eternal.kirradungeon.client.zone.util.getZoneFee
import net.sakuragame.eternal.kirradungeon.client.zone.util.getZoneItems
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.platform.util.asLangTextList
import taboolib.platform.util.hasItem
import taboolib.platform.util.isAir
import taboolib.platform.util.takeItem

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
    val number: Int,
) {

    companion object {

        const val ECONOMY_NOT_ENOUGH_KEY_BOX = "DUNGEON_ECONOMY_NOT_ENOUGH"

        const val COUNT_NOT_ENOUGH_KEY_BOX = "DUNGEON_COUNT_NOT_ENOUGH"

        const val ITEM_NOT_ENOUGH_KEY_BOX = "ITEM_NOT_ENOUGH"

        private val gson = Gson()

        fun stringToZoneCondition(string: String): ZoneCondition {
            string.printDebug()
            return gson.fromJson(string, ZoneCondition::class.java)!!
        }

        fun List<Player>.checkFee(zone: Zone): Boolean {
            forEach {
                val feeMap = it.getZoneFee(zone)
                if (feeMap.isEmpty()) return true
                feeMap.forEach mapForeach@{ mapFee ->
                    val playerBal = GemsEconomyAPI.getBalance(it.uniqueId, mapFee.key)
                    if (playerBal >= mapFee.value) {
                        if (ZoneWithDraw.gemsMap.containsKey(it.uniqueId)) {
                            ZoneWithDraw.gemsMap[it.uniqueId]!![mapFee.key] = mapFee.value
                            return@mapForeach
                        }
                        ZoneWithDraw.gemsMap[it.uniqueId] = mutableMapOf(Pair(mapFee.key, mapFee.value))
                    } else {
                        NotifyBox(ECONOMY_NOT_ENOUGH_KEY_BOX, "&6&l副本".colored(), it.asLangTextList("message-dungeon-economy-not-enough", it.displayName, mapFee.key))
                            .open(it, false)
                        return false
                    }
                }
            }
            return true
        }

        fun List<Player>.checkCounts(zone: Zone): Boolean {
            forEach {
                if (it.getFeeJoinCounts(zone) <= 0 && !it.isOp) {
                    NotifyBox(COUNT_NOT_ENOUGH_KEY_BOX, "&6&l副本".colored(), it.asLangTextList("message-dungeon-count-not-enough", it.displayName))
                        .open(it, false)
                    return false
                }
            }
            return true
        }

        fun List<Player>.checkItems(zone: Zone): Boolean {
            forEach { player ->
                val itemMap = player.getZoneItems(zone)
                if (itemMap.isEmpty()) return true
                itemMap.forEach mapForeach@{ mapItem ->
                    if (!ZaphkielAPI.registeredItem.containsKey(mapItem.key)) {
                        return@mapForeach
                    }
                    val item = ZaphkielAPI.registeredItem[mapItem.key]!!.buildItemStack(player)
                    if (item.isAir()) return@mapForeach
                    if (!player.inventory.hasItem { it.itemMeta.displayName == item.itemMeta.displayName }) {
                        NotifyBox(ITEM_NOT_ENOUGH_KEY_BOX,
                            "&6&l副本".colored(),
                            player.asLangTextList("message-dungeon-item-not-enough", player.displayName, mapItem.key))
                            .open(player, false)
                        return false
                    } else {
                        if (ZoneWithDraw.itemsMap.containsKey(player.uniqueId)) {
                            ZoneWithDraw.itemsMap[player.uniqueId]!![mapItem.key] = mapItem.value
                            return@mapForeach
                        }
                        ZoneWithDraw.itemsMap[player.uniqueId] = mutableMapOf(Pair(mapItem.key, mapItem.value))
                    }
                }
            }
            return true
        }

        fun List<Player>.withDraw() {
            forEach { player ->
                if (ZoneWithDraw.itemsMap.containsKey(player.uniqueId)) {
                    val itemMapList = ZoneWithDraw.itemsMap[player.uniqueId]!!
                    itemMapList.forEach { itemMap ->
                        player.inventory.takeItem(itemMap.value) { it.itemMeta.displayName == ZaphkielAPI.registeredItem[itemMap.key]!!.buildItemStack(player).itemMeta.displayName }
                    }
                }
                if (ZoneWithDraw.gemsMap.containsKey(player.uniqueId)) {
                    val gemPairList = ZoneWithDraw.gemsMap[player.uniqueId]!!
                    gemPairList.forEach {
                        GemsEconomyAPI.withdraw(player.uniqueId, it.value, it.key, "副本花费")
                    }
                }
                ZoneWithDraw.recycleVars(player)
            }
        }
    }
}