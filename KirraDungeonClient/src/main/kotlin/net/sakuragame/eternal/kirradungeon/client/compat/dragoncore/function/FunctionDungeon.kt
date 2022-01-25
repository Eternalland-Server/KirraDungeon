package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.taylorswiftcn.megumi.uifactory.generate.function.Statements
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.*
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit

object FunctionDungeon {

    val initStatements by lazy {
        Statements()
            .add("global.dungeon_category = 1;")
            .add("global.dungeon_sub_category = 1;")
            .add("global.dungeon_current_selected = 1;")
            .build()!!
    }

    fun sendScreen(player: Player, screen: DungeonScreen, subScreen: DungeonSubScreen) {
        player.apply {
            DungeonCard.send(this, screen, subScreen)
            DungeonCategory.send(this, screen, subScreen)
            DungeonRegion.send(this, screen, subScreen)
            DungeonRoom.send(this, screen, subScreen)
            Dungeon.send(this, screen, subScreen)
        }
    }

    fun openMainGUI(player: Player, init: Boolean = true) {
        submit(async = true, delay = 3L) {
            if (init) {
                PacketSender.sendRunFunction(player, "default", initStatements, false)
                sendScreen(player, DungeonAPI.getDefaultScreen(), DungeonAPI.getDefaultSubScreen(null))
            }
            PacketSender.sendOpenGui(player, Dungeon.screenId)
        }
    }

    fun openMainGUI(player: Player, statementsTriple: Triple<Int, Int, Int>) {
        val statements = Statements()
            .add("global.dungeon_category = ${statementsTriple.first};")
            .add("global.dungeon_sub_category =  ${statementsTriple.second};")
            .add("global.dungeon_current_selected = ${statementsTriple.third};")
            .build()!!
        PacketSender.sendRunFunction(player, "default", statements, false)
        submit(async = true) {
            val category = DungeonAPI.getDungeonCategory(statementsTriple.first)
            val screen = DungeonAPI.getDungeonScreen(category, statementsTriple.second) ?: return@submit
            val subScreen = screen.dungeonSubScreens[statementsTriple.third - 1] ?: DungeonAPI.getDefaultSubScreen(screen)
            sendScreen(player, screen, subScreen)
            PacketSender.sendOpenGui(player, Dungeon.screenId)
        }
    }

    fun sendItems(player: Player, dungeonId: String) {
        submit(async = true, delay = 3L) {
            DungeonAPI.getDropItemsByDungeonId(player, dungeonId).forEach { (slotId, item) ->
                PacketSender.putClientSlotItem(player, slotId, item)
            }
        }
    }
}