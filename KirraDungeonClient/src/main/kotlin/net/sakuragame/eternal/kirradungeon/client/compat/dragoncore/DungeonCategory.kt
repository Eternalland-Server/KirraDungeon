package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object DungeonCategory {

    const val screenId = "dungeon_category"

    private lateinit var ui: ScreenUI

    private lateinit var uiYaml: YamlConfiguration

    fun sendScreen(player: Player) = PacketSender.sendYaml(player, FolderType.Gui, screenId, uiYaml)

    @Awake(LifeCycle.ENABLE)
    fun init() {
        ui = ScreenUI(screenId)
            .addComponent(TextureComp("category_normal_bg",
                "(global.dungeon_category == 1) ? 'ui/dungeon/category/selected.png' : 'ui/dungeon/category/unselected.png'")
                .setXY("body.x - category_normal_bg.width", "body.y + 44")
                .setWidth("(global.dungeon_category == 1) ? '79' : '61'")
                .setHeight("46")
                .addAction(ActionType.Left_Click, "global.dungeon_category = 1;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("category_normal_text", "ui/dungeon/category/normal_text.png")
                .setXY("category_normal_bg.x + 32", "category_normal_bg.y + 15")
                .setWidth("44")
                .setHeight("17")
                .setVisible("global.dungeon_category == 1")
                .setExtend("category_normal_bg")
            )
            .addComponent(TextureComp("category_normal_icon", "ui/dungeon/category/normal_icon.png")
                .setXY("category_normal_bg.x + (global.dungeon_category == 1 ? 7 : 16)", "category_normal_bg.y + 7")
                .setWidth("32")
                .setHeight("32")
                .setExtend("category_normal_bg")
            )

            .addComponent(TextureComp("category_team_bg",
                "(global.dungeon_category == 2) ? 'ui/dungeon/category/selected.png' : 'ui/dungeon/category/unselected.png'")
                .setXY("body.x - category_team_bg.width", "category_normal_bg.y + 55")
                .setWidth("(global.dungeon_category == 2) ? '79' : '61'")
                .setHeight("46")
                .addAction(ActionType.Left_Click, "global.dungeon_category = 2;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("category_team_text", "ui/dungeon/category/team_text.png")
                .setXY("category_team_bg.x + 32", "category_team_bg.y + 15")
                .setWidth("44")
                .setHeight("17")
                .setVisible("global.dungeon_category == 2")
                .setExtend("category_team_bg")
            )
            .addComponent(TextureComp("category_team_icon", "ui/dungeon/category/team_icon.png")
                .setXY("category_team_bg.x + (global.dungeon_category == 2 ? 7 : 16)", "category_team_bg.y + 7")
                .setWidth("32")
                .setHeight("32")
                .setExtend("category_team_bg")
            )

            .addComponent(TextureComp("category_activity_bg",
                "(global.dungeon_category == 3) ? 'ui/dungeon/category/selected.png' : 'ui/dungeon/category/unselected.png'")
                .setXY("body.x - category_activity_bg.width", "category_team_bg.y + 55")
                .setWidth("(global.dungeon_category == 3) ? '79' : '61'")
                .setHeight("46")
                .addAction(ActionType.Left_Click, "global.dungeon_category = 3;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("category_activity_text", "ui/dungeon/category/activity_text.png")
                .setXY("category_activity_bg.x + 32", "category_activity_bg.y + 15")
                .setWidth("44")
                .setHeight("17")
                .setVisible("global.dungeon_category == 3")
                .setExtend("category_activity_bg")
            )
            .addComponent(TextureComp("category_activity_icon", "ui/dungeon/category/activity_icon.png")
                .setXY("category_activity_bg.x + (global.dungeon_category == 3 ? 7 : 16)", "category_activity_bg.y + 7")
                .setWidth("32")
                .setHeight("32")
                .setExtend("category_activity_bg")
            )

            .addComponent(TextureComp("category_special_bg",
                "(global.dungeon_category == 4) ? 'ui/dungeon/category/selected.png' : 'ui/dungeon/category/unselected.png'")
                .setXY("body.x - category_special_bg.width", "category_activity_bg.y + 55")
                .setWidth("(global.dungeon_category == 4) ? '79' : '61'")
                .setHeight("46")
                .addAction(ActionType.Left_Click, "global.dungeon_category = 4;")
                .addAction(ActionType.Left_Click, SubmitParams().also {
                    it.add("global.dungeon_category")
                    it.add("global.dungeon_sub_category")
                })
            )
            .addComponent(TextureComp("category_special_text", "ui/dungeon/category/special_text.png")
                .setXY("category_special_bg.x + 32", "category_special_bg.y + 15")
                .setWidth("44")
                .setHeight("17")
                .setVisible("global.dungeon_category == 4")
                .setExtend("category_special_bg")
            )
            .addComponent(TextureComp("category_special_icon", "ui/dungeon/category/special_icon.png")
                .setXY("category_special_bg.x + (global.dungeon_category == 4 ? 7 : 16)", "category_special_bg.y + 7")
                .setWidth("32")
                .setHeight("32")
                .setExtend("category_special_bg")
            )

        uiYaml = ui.build(null)
    }
}