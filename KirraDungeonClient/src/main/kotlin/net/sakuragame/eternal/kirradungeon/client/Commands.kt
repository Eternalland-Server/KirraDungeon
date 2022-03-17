package net.sakuragame.eternal.kirradungeon.client

import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.StoryDungeonCompat
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonLoader
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamNumData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

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
                Zone.preJoin(player, argument)
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
        dynamic(commit = "category") {
            dynamic(commit = "subCategory") {
                dynamic(commit = "currentSelected") {
                    execute<Player> { player, context, _ ->
                        val numData = ParamNumData(context.get(1).toInt(), context.get(2).toInt(), context.get(3).toInt(), 1)
                        FunctionDungeon.openAssignGUI(player, numData)
                    }
                }
            }
        }
        execute<Player> { player, _, _ ->
            FunctionDungeon.openGUI(player)
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

    @CommandBody
    val debug = subCommand {
        execute<Player> { player, _, _ ->
            player.profile().debugMode.set(!player.profile().debugMode.get())
            player.sendMessage("&c[System] &f${player.profile().debugMode.get().toString().uppercase()}".colored())
        }
    }

    @CommandBody
    val setNumber = subCommand {
        dynamic(commit = "player") {
            suggestion<CommandSender> { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            dynamic(commit = "number") {
                execute<CommandSender> { sender, context, _ ->
                    val player = Bukkit.getPlayer(context.get(1)) ?: return@execute
                    val number = context.get(2).toIntOrNull() ?: 1
                    player.profile().number.set(number)
                    player.profile().save()
                    sender.sendMessage("&c[System] &7已经把 ${player.name} 的编号设置为 ${number}".colored())
                }
            }
        }
    }
}