package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import org.bukkit.entity.Player

/**
 * I screen
 *
 * @constructor Create empty I screen
 */
interface IScreen {

    val screenId: String

    fun send(player: Player, screen: DungeonScreen, subScreen: DungeonSubScreen) {
        val screenYaml = build2Screen(screen, subScreen, player).build(player)
        PacketSender.sendYaml(player, FolderType.Gui, screenId, screenYaml)
    }

    fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen, player: Player): ScreenUI
}