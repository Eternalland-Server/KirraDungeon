package net.sakuragame.eternal.kirradungeon.client

import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.dungeonsystem.common.handler.MapRequestHandler
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirraparty.bukkit.party.Party
import net.sakuragame.eternal.kirraparty.bukkit.party.Party.Companion.getParty
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.spigotmc.SpigotConfig
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.asLangTextList
import taboolib.platform.util.sendLang
import java.util.*

@Suppress("SpellCheckingInspection")
@CommandHeader(name = "KirraDungeonClient", aliases = ["dungeon"], permissionDefault = PermissionDefault.NOT_OP)
object Commands {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val join = subCommand {
        dynamic {
            execute<Player> { player, _, argument ->
                if (argument == "nergigante_dragon" && !player.hasPermission("admin")) {
                    player.sendLang("command-cant-join-story-zone")
                    return@execute
                }
                val zone = Zone.getByID(argument)
                if (zone == null) {
                    player.sendLang("command-not-found-zone")
                    return@execute
                }
                // 与组队系统挂钩.
                val party = player.getParty()
                if (party == null) {
                    zone.join(listOf(player))
                    return@execute
                }
                if (party.getTeamPosition(player.uniqueId) != Party.Position.LEADER) {
                    player.sendLang("command-member-try-join-zone")
                    return@execute
                } else {
                    zone.join(party.getWholeMembers().map {
                        Bukkit.getPlayer(it) ?: kotlin.run {
                            player.sendLang("command-party-member-not-found")
                            return@execute
                        }
                    })
                }
            }
        }
    }

    @CommandBody(permission = "admin")
    val joinTutorialDungeon = subCommand {
        execute<Player> { player, _, _ ->
            if (player.hasPermission("admin")) {
                DungeonClientAPI.getClientManager().queryDungeon("nergigante_dragon", player, object : MapRequestHandler() {

                    override fun onTimeout(serverID: String) = player.asLangTextList("message-noobie-dungeon-join-timed-out", serverID).forEach {
                        MessageAPI.sendActionTip(player, it)
                    }

                    override fun onTeleportTimeout(serverID: String) = player.asLangTextList("message-noobie-dungeon-join-timed-out", serverID).forEach {
                        MessageAPI.sendActionTip(player, it)
                    }

                    override fun handle(serverID: String, mapUUID: UUID) {
                        KirraCoreBukkitAPI.teleportPlayerToAnotherServer(serverID, player.uniqueId)
                    }
                })
            }
            player.sendMessage(SpigotConfig.unknownCommandMessage)
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&7副本列表: ".colored())
            Zone.zones.forEach {
                sender.sendMessage("&a$it".colored())
            }
        }
    }

    @CommandBody(permission = "admin")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            Zone.load()
            sender.sendMessage("&7已重载.".colored())
            return@execute
        }
    }
}