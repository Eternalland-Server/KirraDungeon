package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake


object DungeonRegion {

    const val screenId = "dungeon_region"

    private lateinit var ui: ScreenUI

    private lateinit var uiYaml: YamlConfiguration

    fun sendScreen(player: Player) = PacketSender.sendYaml(player, FolderType.Gui, screenId, uiYaml)

    @Awake(LifeCycle.ENABLE)
    fun init() {
        ui = ScreenUI(screenId)
            .addComponent(TextureComp("region_1", "ui/common/button_normal_a.png")
                .setExtend("area_1")
                .addAction(ActionType.Left_Click, "region_1.texture = 'ui/common/button_normal_a_press.png';")
                .addAction(ActionType.Left_Release, "region_1.texture = 'ui/common/button_normal_a.png';")
                .addAction(ActionType.Left_Click, "global.dungeon_sub_category = 1;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("region_2", "ui/common/button_normal_g.png")
                .setExtend("area_2")
                .addAction(ActionType.Left_Click, "region_2.texture = 'ui/common/button_normal_g_press.png';")
                .addAction(ActionType.Left_Release, "region_2.texture = 'ui/common/button_normal_g.png';")
                .addAction(ActionType.Left_Click, "global.dungeon_sub_category = 2;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("region_3", "ui/common/button_normal_g.png")
                .setExtend("area_3")
                .addAction(ActionType.Left_Click, "region_3.texture = 'ui/common/button_normal_g_press.png';")
                .addAction(ActionType.Left_Release, "region_3.texture = 'ui/common/button_normal_g.png';")
                .addAction(ActionType.Left_Click, "global.dungeon_sub_category = 3;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("region_4", "ui/common/button_normal_f.png")
                .setExtend("area_4")
                .addAction(ActionType.Left_Click, "global.dungeon_sub_category = 4;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("region_4_lock", "ui/common/lock_shade.png")
                .setExtend("area_4")
            )
            .addComponent(TextureComp("region_5", "ui/common/button_normal_f.png")
                .setExtend("area_5")
                .addAction(ActionType.Left_Click, "global.dungeon_sub_category = 5;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("region_5_lock", "ui/common/lock_shade.png")
                .setExtend("area_5")
            )

        uiYaml = ui.build(null)
    }
}