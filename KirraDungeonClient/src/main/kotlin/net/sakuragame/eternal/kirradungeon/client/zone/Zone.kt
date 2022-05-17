package net.sakuragame.eternal.kirradungeon.client.zone

import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.dungeonsystem.common.exception.DungeonServerRunOutException
import net.sakuragame.dungeonsystem.common.exception.UnknownDungeonException
import net.sakuragame.dungeonsystem.common.handler.MapRequestHandler
import net.sakuragame.eternal.kirracore.bukkit.KirraCoreBukkitAPI
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.zone.ZoneCondition.Companion.checkCounts
import net.sakuragame.eternal.kirradungeon.client.zone.ZoneCondition.Companion.checkFee
import net.sakuragame.eternal.kirradungeon.client.zone.ZoneCondition.Companion.checkItems
import net.sakuragame.eternal.kirradungeon.client.zone.ZoneCondition.Companion.withDraw
import net.sakuragame.eternal.kirraparty.bukkit.party.PartyAPI
import net.sakuragame.eternal.kirraparty.bukkit.party.PartyPosition
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.platform.util.sendLang
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
            val zoneNames = KirraDungeonClient.redisConn.sync().lrange("KirraDungeonNames", 0, -1)
            zoneNames.forEach { name ->
                zones += Zone(name, KirraDungeonClient.redisConn.sync().lrange("KirraDungeonConditions:$name", 0, -1)
                    .map { ZoneCondition.stringToZoneCondition(it) })
            }
        }

        private fun clearAll() = zones.clear()

        fun preJoin(player: Player, name: String) {
            if (name == "nergigante_dragon" && !player.hasPermission("admin")) {
                player.sendLang("command-cant-join-story-zone")
                return
            }
            val zone = getByID(name)
            if (zone == null) {
                player.sendLang("command-not-found-zone")
                return
            }
            // 与组队系统挂钩.
            val party = PartyAPI.getParty(player)
            if (party == null) {
                zone.join(listOf(player))
                return
            }
            if (party.getPosition(player.uniqueId) != PartyPosition.LEADER) {
                player.sendLang("command-member-try-join-zone")
                return
            } else {
                zone.join(party.getWholeMembers().map {
                    Bukkit.getPlayer(it) ?: kotlin.run {
                        player.sendLang("command-party-member-not-found")
                        return
                    }
                })
            }
        }
    }

    fun join(players: List<Player>) {
        // 检查玩家进入是否拥有足额进入次数.
        if (!players.checkCounts(this)) return
        // 检查玩家是否拥有目标金额.
        if (!players.checkFee(this)) return
        // 检查玩家是否拥有目标物品.
        if (!players.checkItems(this)) return
        // 进行扣除操作. (物品 & 金额)
        players.withDraw()
        // 进行传送操作, 之后转交由 Server 端处理.
        try {
            val serverId = DungeonClientAPI.getClientManager().queryServer("rpg-dungeon")
            if (serverId == null) {
                players.forEach {
                    it.sendLang("message-dungeon-server-ran-out-exception")
                }
                return
            }
            val playerSet = LinkedHashSet<Player>().also { it.addAll(players) }
            players.forEach {
                KirraCoreBukkitAPI.showLoadingTitle(it, "&6&l➱ &e正在前往: $name &7@", false)
            }
            DungeonClientAPI.getClientManager().queryDungeon(name, serverId, playerSet, object : MapRequestHandler() {

                override fun onTimeout(serverId: String) {
                    players.forEach {
                        KirraCoreBukkitAPI.cancelLoadingTitle(it)
                        it.sendLang("message-dungeon-create-timed-out", serverId)
                    }
                }

                override fun onTeleportTimeout(serverID: String) {
                    players.forEach {
                        KirraCoreBukkitAPI.cancelLoadingTitle(it)
                        it.sendLang("message-dungeon-teleport-timed-out", serverID)
                    }
                }

                override fun handle(serverId: String, mapUUID: UUID) {
                    players.forEach {
                        it.teleportToAnotherServer(serverId)
                    }
                }
            })
        } catch (e: Exception) {
            when (e) {
                is UnknownDungeonException ->
                    players.forEach {
                        it.sendLang("message-unknown-dungeon-exception")
                    }
                is DungeonServerRunOutException ->
                    players.forEach {
                        it.sendLang("message-dungeon-server-ran-out-exception")
                    }
            }
        }
    }

    fun Player.teleportToAnotherServer(serverId: String) {
        KirraCoreBukkitAPI.teleportPlayerToAnotherServer(serverId, this)
    }

    override fun toString() = "Zone($name, $condition)"
}

