package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.taylorswiftcn.megumi.uifactory.generate.function.Statements
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.*
import org.bukkit.entity.Player

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

    fun openMainGUI(player: Player, needInitStatements: Boolean = true) {
        if (needInitStatements) {
            PacketSender.sendRunFunction(player, "default", initStatements, false)
        }
        PacketSender.sendOpenGui(player, Dungeon.screenId)
    }
}