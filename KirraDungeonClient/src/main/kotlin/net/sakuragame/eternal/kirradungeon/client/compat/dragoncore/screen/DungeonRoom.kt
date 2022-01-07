package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

import com.google.common.util.concurrent.Atomics
import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 副本房间显示实现层.
 */
object DungeonRoom : IScreen {

    override val screenId: String
        get() = "dungeon_room"

    override fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen, player: Player): ScreenUI {
        return ScreenUI(screenId).apply {
            for (index in 0..4) {
                if (screen.dungeonSubScreens.getOrNull(index) != null) addRoom(index + 1, screen, player)
            }
        }
    }

    private fun ScreenUI.addRoom(index: Int, screen: DungeonScreen, player: Player) {
        val originIndex = index - 1
        val room = screen.dungeonSubScreens[originIndex]!!

        val name = Atomics.newReference<String>(room.name)
        val forceLock = AtomicBoolean(room.forceLock)

        if (player.profile().number.get() < getNumber(room)) {
            forceLock.set(true)
            name.set("&7&o暂未解锁".colored())
        }

        if (room.frameVisible) {
            val framePath = "(global.dungeon_current_selected == $index) ? 'ui/dungeon/selected.png' : 'ui/dungeon/open.png'"
            addComponent(TextureComp("room_${index}_frame", framePath)
                .setExtend("dungeon_${index}")
            )
            addComponent(TextureComp("room_${index}_string", "ui/dungeon/string.png")
                .setExtend("dungeon_${index}")
            )
        }

        if (forceLock.get()) {
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
                .addAction(ActionType.Left_Click, "global.dungeon_current_selected = ${index};")
                .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams())
            )
        }

        addComponent(TextureComp("room_${index}_name", "0,0,0,0")
            .setText(name.get())
            .setXY("dungeon_${index}.x", "dungeon_${index}.y + 73")
            .setWidth("dungeon_${index}.width")
            .setHeight("10")
        )
    }

    private fun getNumber(subScreen: DungeonSubScreen): Int {
        val dungeonId = subScreen.dungeonId ?: return 0
        return Zone.getByID(dungeonId)?.condition?.first()?.number ?: return 0
    }
}