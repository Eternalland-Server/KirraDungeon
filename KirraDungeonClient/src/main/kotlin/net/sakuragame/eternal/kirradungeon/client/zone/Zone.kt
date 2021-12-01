package net.sakuragame.eternal.kirradungeon.client.zone

import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.dungeonsystem.common.handler.MapRequestHandler
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.zone.util.getFeeJoinCounts
import net.sakuragame.eternal.kirradungeon.client.zone.util.getZoneFee
import net.sakuragame.eternal.kirradungeon.client.zone.util.getZoneItems
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.platform.util.*
import java.util.*

data class Zone(val name: String, val condition: List<ZoneCondition>) {

    companion object {

        val zones = mutableListOf<Zone>()

        fun getByID(name: String) = zones.firstOrNull { it.name == name }

        /**
         * 从 Redis 缓存里重新获取所有副本列表.
         */
        @Awake(LifeCycle.ENABLE)
        fun load() {
            clearAll()
            val zoneNames = KirraDungeonClient.redisConn.sync().lrange("KirraZoneNames", 0, -1)
            zoneNames.forEach { name ->
                zones += Zone(name, KirraDungeonClient.redisConn.sync().lrange("KirraZoneConditions:$name", 0, -1)
                    .map { ZoneCondition.stringToZoneCondition(it) })
            }
        }

        private fun clearAll() = zones.clear()
    }

    fun join(players: List<Player>) {
        // 检查玩家进入是否拥有足额进入次数.
        if (!players.checkCounts()) return
        // 检查玩家是否拥有目标金额.
        if (!players.checkFee()) return
        // 检查玩家是否拥有目标物品.
        if (!players.checkItems()) return
        // 进行扣除操作. (物品 & 金额)
        players.withDraw()
        // 进行传送操作, 之后转交由 Server 端处理.
        val serverId = DungeonClientAPI.getClientManager().queryServer("RPG-DUNGEON") ?: kotlin.run {
            players.sendMessage("")
            return
        }
        val linkedHashSet = LinkedHashSet<Player>().also {
            it.addAll(players)
        }
        DungeonClientAPI.getClientManager().queryDungeon(name, serverId, linkedHashSet, object : MapRequestHandler() {

            override fun onTimeout(serverId: String) =
                players.forEach {
                    it.sendLang("message-zone-create-timed-out", serverId)
                }

            override fun onTeleportTimeout(serverID: String) =
                players.forEach {
                    it.sendLang("message-zone-teleport-timed-out", serverID)
                }

            override fun handle(serverId: String, mapUUID: UUID) =
                players.forEach {
                    it.teleportToAnotherServer(serverId)
                }
        })
    }

    private fun Player.teleportToAnotherServer(serverID: String) {
        KirraCoreBukkitAPI.teleportPlayerToAnotherServer(serverID, this)
    }

    private fun Array<Player>.sendMessage(message: String) = forEach { it.sendMessage(message) }

    private fun Array<Player>.checkFee(): Boolean {
        forEach {
            it.getZoneFee(this@Zone).forEach mapForeach@{ mapFee ->
                if (KirraCoreBukkitAPI.getBalance(it, mapFee.key) >= mapFee.value) {
                    if (ZoneWithDraw.gemsMap.containsKey(it.uniqueId)) {
                        ZoneWithDraw.gemsMap[it.uniqueId]!![mapFee.key] = mapFee.value
                        return@mapForeach
                    }
                    ZoneWithDraw.gemsMap[it.uniqueId] = mutableMapOf(Pair(mapFee.key, mapFee.value))
                } else {
                    sendMessage(it.asLangText("message-zone-economy-not-enough", it.name, mapFee.key))
                    return false
                }
            }
        }
        return true
    }

    private fun Array<Player>.checkCounts(): Boolean {
        forEach {
            if (it.getFeeJoinCounts(this@Zone) < 0) {
                sendMessage(it.asLangText("message-zone-count-not-enough", it.name))
                return false
            }
        }
        return true
    }

    private fun Array<Player>.checkItems(): Boolean {
        forEach {
            it.getZoneItems(this@Zone).forEach mapForeach@{ mapItem ->
                if (!ZaphkielAPI.registeredItem.containsKey(mapItem.key)) {
                    return@mapForeach
                }
                val item = ZaphkielAPI.registeredItem[mapItem.key]!!.buildItemStack(it)
                if (item.isAir()) return@mapForeach
                if (!it.checkItem(item, mapItem.value, remove = false)) {
                    sendMessage(it.asLangText(it.asLangText("message-item-count-not-enough", it.name, item.itemMeta.displayName)))
                    return false
                } else {
                    if (ZoneWithDraw.itemsMap.containsKey(it.uniqueId)) {
                        ZoneWithDraw.itemsMap[it.uniqueId]!![mapItem.key] = mapItem.value
                        return@mapForeach
                    }
                    ZoneWithDraw.itemsMap[it.uniqueId] = mutableMapOf(Pair(mapItem.key, mapItem.value))
                }
            }
        }
        return true
    }

    private fun Array<Player>.withDraw() {
        forEach { player ->
            if (ZoneWithDraw.itemsMap.containsKey(player.uniqueId)) {
                val itemMapList = ZoneWithDraw.itemsMap[player.uniqueId]!!
                itemMapList.forEach { itemMap ->
                    player.inventory.takeItem(itemMap.value) { it.isSimilar(ZaphkielAPI.registeredItem[itemMap.key]!!.buildItemStack(player)) }
                }
            }
            if (ZoneWithDraw.gemsMap.containsKey(player.uniqueId)) {
                val gemPairList = ZoneWithDraw.gemsMap[player.uniqueId]!!
                gemPairList.forEach {
                    KirraCoreBukkitAPI.withDraw(player, it.value, it.key)
                }
            }
            ZoneWithDraw.recycleVars(player)
        }
    }

    override fun toString() = "Zone($name, $condition)"
}

