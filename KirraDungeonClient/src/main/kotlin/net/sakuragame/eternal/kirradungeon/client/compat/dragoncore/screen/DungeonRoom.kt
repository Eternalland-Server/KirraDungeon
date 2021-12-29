package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen

/**
 * 副本房间显示实现层.
 */
object DungeonRoom : IScreen {

    override val screenId: String
        get() = "dungeon_room"

    override fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen): ScreenUI {
        return ScreenUI(screenId).apply {
            for (index in 0..4) {
                if (screen.dungeonSubScreens.getOrNull(index) != null) addRoom(index + 1, screen)
            }
        }
    }

    private fun ScreenUI.addRoom(index: Int, screen: DungeonScreen) {
        val originIndex = index - 1
        val room = screen.dungeonSubScreens[originIndex]!!
        if (room.frameVisible) {
            val framePath = "(global.dungeon_current_selected == $index) ? 'ui/dungeon/selected.png' : 'ui/dungeon/open.png'"
            addComponent(TextureComp("room_${index}_frame", framePath)
                .setExtend("dungeon_${index}")
            )
            addComponent(TextureComp("room_${index}_string", "ui/dungeon/string.png")
                .setExtend("dungeon_${index}")
            )
        }
        if (room.forceLock) {
            addComponent(TextureComp("room_${index}_frame", "ui/dungeon/close.png")
                .setExtend("dungeon_${index}")
            )
            addComponent(TextureComp("room_${index}_icon", room.iconPath)
                .setExtend("dungeon_${index}")
            )
            addComponent(TextureComp("room_${index}_string_lock", "ui/dungeon/string_lock.png")
                .setExtend("dungeon_${index}")
            )
            addComponent(TextureComp("room_${index}_card", "ui/dungeon/card.png")
                .setExtend("dungeon_${index}")
            )
        } else {
            addComponent(TextureComp("room_${index}_icon", room.iconPath)
                .setExtend("dungeon_${index}")
            )
            addComponent(TextureComp("room_${index}_card", "ui/dungeon/card.png")
                .setExtend("dungeon_${index}")
                .addAction(ActionType.Left_Click, "dungeon_${index}.texture = 'ui/dungeon/hover.png';")
                .addAction(ActionType.Left_Release, "dungeon_${index}.texture = 'ui/dungeon/default.png';")
                .addAction(ActionType.Left_Click, "global.dungeon_current_selected = ${index};")
                .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams())
            )
        }
        addComponent(TextureComp("room_${index}_name", "0,0,0,0")
            .setText(room.name)
            .setXY("dungeon_${index}.x", "dungeon_${index}.y + 73")
            .setWidth("dungeon_${index}.width")
            .setHeight("10")
        )
    }
}