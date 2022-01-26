package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.getParentScreen
import org.bukkit.entity.Player
import taboolib.module.chat.colored

/**
 * 副本区域显示实现层.
 */
object DungeonRegion : IScreen {

    override val screenId: String
        get() = "dungeon_region"

    private val notOpenText = "&f&l暂未开放".colored()

    override fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen, player: Player): ScreenUI {
        return ScreenUI(screenId).apply {
            for (index in 1..5) {
                addRegion(index, screen)
            }
        }
    }

    private fun ScreenUI.addRegion(index: Int, screen: DungeonScreen) {
        val parentScreen = screen.category.getParentScreen()
        val originIndex = index - 1
        if (parentScreen.getOrNull(originIndex) == null) {
            addComponent(TextureComp("region_${index}", "ui/common/button_normal_f.png")
                .setText(notOpenText)
                .setExtend("area_${index}")
            )
            addComponent(TextureComp("region_${index}_lock", "ui/common/lock_shade.png")
                .setExtend("area_${index}")
            )
            return
        }
        addComponent(TextureComp("region_${index}", "ui/common/button_normal_a.png")
            .setText(parentScreen[originIndex].name)
            .setExtend("area_${index}")
            .addAction(ActionType.Left_Click, "region_${index}.texture = 'ui/common/button_normal_a_press.png';")
            .addAction(ActionType.Left_Release, "region_${index}.texture = 'ui/common/button_normal_a.png';")
            .addAction(ActionType.Left_Click, "global.dungeon_sub_category = ${index};")
            .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams())
        )
    }
}