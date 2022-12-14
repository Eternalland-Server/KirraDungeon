package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI.getNumber
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import org.bukkit.entity.Player
import taboolib.module.chat.colored

object DungeonRoom : IScreen {

    override val screenId: String
        get() = "dungeon_room"

    override fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen, player: Player): ScreenUI {
        return ScreenUI(screenId).apply {
            for (index in 0..4) {
                if (screen.dungeonSubScreens[index] != null) addRoom(index + 1, screen, player)
            }
        }
    }

    private fun ScreenUI.addRoom(index: Int, screen: DungeonScreen, player: Player) {
        val originIndex = index - 1
        val room = screen.dungeonSubScreens[originIndex]!!
        if (room.forceEmpty) {
            return
        }
        val profile = player.profile() ?: return
        var name = room.name
        var forceLock = room.forceLock
        if (profile.number < getNumber(room)) {
            forceLock = true
            name = "&7&o暂未解锁"
        }
        if (room.frameVisible) {
            val framePath =
                "(global.dungeon_current_selected == $index) ? 'ui/dungeon/selected.png' : 'ui/dungeon/open.png'"
            addComponent(
                TextureComp("room_${index}_frame", framePath)
                    .setExtend("dungeon_${index}")
            )
            addComponent(
                TextureComp("room_${index}_string", "ui/dungeon/string.png")
                    .setExtend("dungeon_${index}")
            )
        }
        if (forceLock) {
            addComponent(
                TextureComp("room_${index}_frame", "ui/dungeon/close.png")
                    .setExtend("dungeon_${index}")
            )
            addComponent(
                TextureComp("room_${index}_icon", room.iconPath)
                    .setExtend("dungeon_${index}")
            )
            addComponent(
                TextureComp("room_${index}_string_lock", "ui/dungeon/string_lock.png")
                    .setExtend("dungeon_${index}")
            )
            addComponent(
                TextureComp("room_${index}_card", "ui/dungeon/card.png")
                    .setExtend("dungeon_${index}")
            )
        } else {
            addComponent(
                TextureComp("room_${index}_icon", room.iconPath)
                    .setExtend("dungeon_${index}")
            )
            addComponent(
                TextureComp("room_${index}_card", "ui/dungeon/card.png")
                    .setExtend("dungeon_${index}")
                    .addAction(
                        ActionType.Left_Click,
                        DungeonAPI.getPluginParams(toScreenData = "current_selected = $index")
                    )
            )
        }
        addComponent(
            TextureComp("room_${index}_name", "0,0,0,0")
                .setText(name.colored())
                .setXY("dungeon_${index}.x", "dungeon_${index}.y + 73")
                .setWidth("dungeon_${index}.width")
                .setHeight("10")
        )
    }
}