package net.sakuragame.eternal.kirradungeon.client

import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirraparty.bukkit.party.Party
import net.sakuragame.eternal.kirraparty.bukkit.party.Party.Companion.getParty
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang

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

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&7副本列表: ".colored())
            Zone.zones.forEach {
                sender.sendMessage("&a$it".colored())
            }
        }
    }
}