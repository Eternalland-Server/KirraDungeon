package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@Suppress("SpellCheckingInspection")
@CommandHeader(name = "KirraDungeonServer", aliases = ["dungeon"], permission = "admin")
object Commands {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }


    fun getZone(player: Player, zoneId: String): Zone? {
        val zone = Zone.getByID(zoneId)
        if (zone == null) {
            player.sendMessage("&c[System] &7错误的名字或类型.".colored())
            return null
        }
        return zone
    }
}
