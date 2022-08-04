package net.sakuragame.eternal.kirradungeon.client.zone

import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.dungeonsystem.common.exception.DungeonServerRunOutException
import net.sakuragame.dungeonsystem.common.exception.UnknownDungeonException
import net.sakuragame.dungeonsystem.common.handler.MapRequestHandler
import net.sakuragame.eternal.justmessage.api.common.NotifyBox
import net.sakuragame.eternal.kirracore.bukkit.KirraCoreBukkitAPI
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.function.FunctionListener
import net.sakuragame.eternal.kirraparty.bukkit.party.PartyAPI
import net.sakuragame.eternal.kirraparty.bukkit.party.PartyPosition
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.platform.util.sendLang
import java.util.*

data class Zone(val id: String, val neededFatigue: Int, val num: Int) {

    companion object {

        val zones = mutableListOf<Zone>()

        fun getByID(name: String) = zones.find { it.id == name }

        @Awake(LifeCycle.ENABLE)
        fun load() {
            clear()
            val section = KirraDungeonClient.dungeons.getConfigurationSection("dungeons") ?: return
            section.getKeys(false).forEach {
                val id = KirraDungeonClient.dungeons.getString("dungeons.$it.id") ?: return@forEach
                val fatigue = KirraDungeonClient.dungeons.getInt("dungeons.$it.fatigue")
                val num = KirraDungeonClient.dungeons.getInt("dungeons.$it.num")
                zones += Zone(id, fatigue, num)
            }
        }

        private fun clear() = zones.clear()

        fun preJoin(player: Player, name: String, isTeam: Boolean) {
            if (name == "nergigante_dragon" && !player.hasPermission("admin")) {
                player.sendLang("command-cant-join-story-zone")
                return
            }
            val zone = getByID(name)
            if (zone == null) {
                player.sendLang("command-not-found-zone")
                return
            }
            val party = PartyAPI.getParty(player)
            when {
                // 当进入单人副本当前却仍在任一队伍
                party != null && !isTeam -> {
                    NotifyBox(FunctionListener.AUTO_CLOSE_NOTIFY_BOX_KEY, "&6&l副本", listOf("您目前仍在一个队伍里", "请解散或退出队伍后重试.")).open(player, false)
                    return
                }
                // 当进入组队副本当前却不在任何队伍
                party == null && isTeam -> {
                    NotifyBox(FunctionListener.AUTO_CLOSE_NOTIFY_BOX_KEY, "&6&l副本", listOf("您不在任何一个队伍里", "请创建或进入队伍后重试.")).open(player, false)
                    return
                }
            }
            // 正常流程
            if (party == null) {
                zone.join(listOf(player))
                return
            } else if (party.getPosition(player.uniqueId) != PartyPosition.LEADER) {
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
        players.forEach {
            val profile = it.profile() ?: return@forEach
            if (profile.fatigue < neededFatigue) {
                players.forEach { player -> player.sendLang("message-dungeon-fatigue-not-enough", profile.player.name) }
                return
            }
        }
        // 进行传送操作, 之后转交由 Server 端处理.
        try {
            val serverId = DungeonClientAPI.getClientManager().queryServer("rpg-dungeon")
            if (serverId == null) {
                players.forEach {
                    it.sendLang("message-dungeon-server-ran-out-exception")
                }
                return
            }
            val playerSet = LinkedHashSet<Player>().apply { addAll(players) }
            players.forEach {
                KirraCoreBukkitAPI.showLoadingAnimation(it, "&6&l➱ &e正在前往: $id &7@", false)
            }
            DungeonClientAPI.getClientManager().queryDungeon(id, serverId, playerSet, object : MapRequestHandler() {

                override fun onTimeout(serverId: String) {
                    players.forEach {
                        KirraCoreBukkitAPI.cancelLoadingAnimation(it)
                        it.sendLang("message-dungeon-create-timed-out", serverId)
                    }
                }

                override fun onTeleportTimeout(serverID: String) {
                    players.forEach {
                        KirraCoreBukkitAPI.cancelLoadingAnimation(it)
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
                is UnknownDungeonException -> players.forEach {
                    it.sendLang("message-unknown-dungeon-exception")
                }

                is DungeonServerRunOutException -> players.forEach {
                    it.sendLang("message-dungeon-server-ran-out-exception")
                }
            }
        }
    }

    fun Player.teleportToAnotherServer(serverId: String) {
        KirraCoreBukkitAPI.teleportPlayerToAnotherServer(serverId, null, null, uniqueId)
    }
}

