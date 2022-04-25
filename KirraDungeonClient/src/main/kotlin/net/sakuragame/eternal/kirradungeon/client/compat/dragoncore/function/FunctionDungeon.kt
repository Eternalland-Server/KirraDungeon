package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.taylorswiftcn.megumi.uifactory.generate.function.Statements
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamNumData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.*
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit

object FunctionDungeon {

    private val initStatements by lazy {
        Statements()
            .add("global.dungeon_category = 1;")
            .add("global.dungeon_sub_category = 1;")
            .add("global.dungeon_current_selected = 1;")
            .add("global.dungeon_page = 1;")
            .build()!!
    }

    fun sendScreen(player: Player, screen: DungeonScreen, subScreen: DungeonSubScreen) {
        player.also {
            DungeonCard.send(it, screen, subScreen)
            DungeonCategory.send(it, screen, subScreen)
            DungeonRegion.send(it, screen, subScreen)
            DungeonRoom.send(it, screen, subScreen)
            Dungeon.send(it, screen, subScreen)
        }
    }

    fun openGUI(player: Player, init: Boolean = true) {
        submit(async = true, delay = 3L) {
            if (init) {
                PacketSender.sendRunFunction(player, "default", initStatements, false)
                sendScreen(player, DungeonAPI.getDefaultScreen(), DungeonAPI.getDefaultSubScreen(null))
            }
            PacketSender.sendOpenGui(player, Dungeon.screenId)
        }
    }

    fun openAssignGUI(player: Player, numData: ParamNumData, paramData: ParamData? = null) {
        val statements = Statements()
            .add("global.dungeon_category = ${numData.param1};")
            .add("global.dungeon_sub_category =  ${numData.param2};")
            .add("global.dungeon_current_selected = ${numData.param3};")
            .build()!!
        PacketSender.sendRunFunction(player, "default", statements, false)
        val category = DungeonAPI.getDungeonCategory(numData.param1)
        val screen = DungeonAPI.getDungeonScreen(category, numData.param2 - 1) ?: return
        val subScreen = screen.dungeonSubScreens[numData.param3 - 1] ?: DungeonAPI.getDefaultSubScreen(screen)
        submit(async = true, delay = 3L) {
            sendScreen(player, screen, subScreen)
            PacketSender.sendOpenGui(player, Dungeon.screenId)
            if (paramData != null) {
                FunctionDungeonListener.doPage(player, paramData)
            }
        }
    }
}