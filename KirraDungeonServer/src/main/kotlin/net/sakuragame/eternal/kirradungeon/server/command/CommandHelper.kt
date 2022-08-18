package net.sakuragame.eternal.kirradungeon.server.command

import net.sakuragame.eternal.kirradungeon.server.function.wand.FunctionModelWand
import net.sakuragame.eternal.kirradungeon.server.function.wand.FunctionOreWand
import net.sakuragame.eternal.kirradungeon.server.getEditingZone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.giveItem

@CommandHeader("DungeonHelper")
object CommandHelper {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val getModelWand = subCommand {
        execute<Player> { player, _, _ ->
            player.sendMessage("&a已给予模型魔杖".colored())
            player.giveItem(FunctionModelWand.modelWand)
        }
    }

    @CommandBody
    val getOreWand = subCommand {
        execute<Player> { player, _, _ ->
            player.sendMessage("&a已给予矿物魔杖".colored())
            player.giveItem(FunctionOreWand.oreWand)
        }
    }

    @CommandBody
    val checkModels = subCommand {
        execute<Player> { player, _, _ ->
            val zone = getEditingZone(player) ?: return@execute
            player.sendMessage("&a${zone.id} 的副本模型:".colored())
            zone.data.models.forEach {
                player.sendMessage("&7${it.id} - ${it.loc} - ${it.model}".colored())
            }
        }
    }

    @CommandBody
    val getZoneLoc = subCommand {
        execute<Player> { player, _, _ ->
            val loc = ZoneLocation.parseToZoneLocation(player.location).toString()
            player.sendMessage("&a坐标: &f$loc".colored())
        }
    }
}