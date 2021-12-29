package net.sakuragame.eternal.kirradungeon.client

import net.sakuragame.eternal.kirradungeon.client.compat.StoryDungeonCompat
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonLoader
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirraparty.bukkit.party.Party
import net.sakuragame.eternal.kirraparty.bukkit.party.Party.Companion.getParty
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang

@Suppress("SpellCheckingInspection")
@CommandHeader(name = "KirraDungeonClient", aliases = ["dungeon"])
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
            val isSucc = StoryDungeonCompat.join(player)
            if (!isSucc) {
                player.sendMessage("&7进入失败, 请检查服务器是否在线.".colored())
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

    @CommandBody
    val openUI = subCommand {
        execute<Player> { player, _, _ ->
            FunctionDungeon.openMainGUI(player)
        }
    }

    @CommandBody(permission = "admin")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            Zone.load()
            DungeonLoader.i()
            sender.sendMessage("&7已重载.".colored())
            return@execute
        }
    }
}