package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.LabelComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.SlotComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI.ParamType.CLOSE
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI.ParamType.PAGE
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.display
import org.bukkit.entity.Player

/**
 * 副本主界面显示实现层.
 */
object Dungeon : IScreen {

    override val screenId: String
        get() = "dungeon"

    override fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen, player: Player): ScreenUI {
        return ScreenUI(screenId)
            .addImports(listOf(DungeonCard.screenId, DungeonCategory.screenId, DungeonRegion.screenId, DungeonRoom.screenId))
            .addComponent(
                TextureComp("body", "ui/dungeon/background.png")
                    .setXY("(w-body.width)/2", "(h-body.height)/2")
                    .setWidth("564")
                    .setHeight("328")
            )
            .addComponent(
                TextureComp("title_bg", "ui/dungeon/title_bg.png")
                    .setXY("body.x + 3", "body.y + 3")
                    .setWidth("558")
                    .setHeight("33")
            )
            .addComponent(
                LabelComp("title_text", screen.category.display())
                    .setXY("title_bg.x + (title_bg.width - title_text.width)/2", "title_bg.y + (title_bg.height - title_text.height)/2")
                    .setScale(1.5)
            )
            .addComponent(
                TextureComp("close", "ui/dungeon/button/close.png")
                    .setXY("title_bg.x + 530", "title_bg.y + 4")
                    .setWidth("24")
                    .setHeight("24")
                    .addAction(ActionType.Left_Click, "close.texture = 'ui/dungeon/button/close_press.png';")
                    .addAction(ActionType.Left_Release, "close.texture = 'ui/dungeon/button/close.png';")
                    .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = CLOSE))
            )
            .addComponent(
                TextureComp("map_bg", screen.mapBgPath)
                    .setXY("body.x + 5", "body.y + 39")
                    .setWidth("388")
                    .setHeight("284")
            )
            .addComponent(
                TextureComp("map_shade", "ui/dungeon/map_shade.png")
                    .setXY("map_bg.x", "map_bg.y")
                    .setWidth("map_bg.width")
                    .setHeight("map_bg.width")
            )
            .addComponent(
                TextureComp("area", "ui/pack/frame_c.png")
                    .setXY("map_bg.x", "map_bg.y + 254")
                    .setWidth("map_bg.width")
                    .setHeight("30")
            )
            .addComponent(
                TextureComp("area_1", "0,0,0,0")
                    .setXY("area.x + 12", "area.y + 4")
                    .setWidth("64")
                    .setHeight("24")
            )
            .addComponent(
                TextureComp("area_2", "0,0,0,0")
                    .setXY("area_1.x + 75", "area_1.y")
                    .setWidth("64")
                    .setHeight("24")
            )
            .addComponent(
                TextureComp("area_3", "0,0,0,0")
                    .setXY("area_2.x + 75", "area_1.y")
                    .setWidth("64")
                    .setHeight("24")
            )
            .addComponent(
                TextureComp("area_4", "0,0,0,0")
                    .setXY("area_3.x + 75", "area_1.y")
                    .setWidth("64")
                    .setHeight("24")
            )
            .addComponent(
                TextureComp("area_5", "0,0,0,0")
                    .setXY("area_4.x + 75", "area_1.y")
                    .setWidth("64")
                    .setHeight("24")
            )
            .addComponent(
                TextureComp("dungeon_1", "ui/dungeon/default.png")
                    .setXY("body.x + 208", "body.y + 189")
                    .setWidth("74")
                    .setHeight("102")
                    .addAction(ActionType.Enter, "dungeon_1.texture = 'ui/dungeon/hover.png';")
                    .addAction(ActionType.Leave, "dungeon_1.texture = 'ui/dungeon/default.png';")
            )
            .addComponent(
                TextureComp("dungeon_2", "ui/dungeon/default.png")
                    .setXY("body.x + 100", "body.y + 171")
                    .setWidth("74")
                    .setHeight("102")
                    .addAction(ActionType.Enter, "dungeon_2.texture = 'ui/dungeon/hover.png';")
                    .addAction(ActionType.Leave, "dungeon_2.texture = 'ui/dungeon/default.png';")
            )
            .addComponent(
                TextureComp("dungeon_3", "ui/dungeon/default.png")
                    .setXY("body.x + 46", "body.y + 72")
                    .setWidth("74")
                    .setHeight("102")
                    .addAction(ActionType.Enter, "dungeon_3.texture = 'ui/dungeon/hover.png';")
                    .addAction(ActionType.Leave, "dungeon_3.texture = 'ui/dungeon/default.png';")
            )
            .addComponent(
                TextureComp("dungeon_4", "ui/dungeon/default.png")
                    .setXY("body.x + 172", "body.y + 81")
                    .setWidth("74")
                    .setHeight("102")
                    .addAction(ActionType.Enter, "dungeon_4.texture = 'ui/dungeon/hover.png';")
                    .addAction(ActionType.Leave, "dungeon_4.texture = 'ui/dungeon/default.png';")
            )
            .addComponent(
                TextureComp("dungeon_5", "ui/dungeon/default.png")
                    .setXY("body.x + 280", "body.y + 45")
                    .setWidth("74")
                    .setHeight("102")
                    .addAction(ActionType.Enter, "dungeon_5.texture = 'ui/dungeon/hover.png';")
                    .addAction(ActionType.Leave, "dungeon_5.texture = 'ui/dungeon/default.png';")
            )
            .addComponent(
                TextureComp("desc_title", "ui/dungeon/desc_title.png")
                    .setXY("body.x + 397", "body.y + 38")
                    .setWidth("162")
                    .setHeight("28")
            )
            .addComponent(
                TextureComp("desc_img", "ui/dungeon/spawn.png")
                    .setXY("desc_title.x", "desc_title.y + 25")
                    .setWidth("162")
                    .setHeight("82")
            )
            .addComponent(
                TextureComp("desc_img_shade", "ui/dungeon/desc_shade.png")
                    .setExtend("desc_img")
            )
            .addComponent(
                TextureComp("desc_bg", "ui/dungeon/desc_bg.png")
                    .setXY("desc_title.x", "desc_title.y + 111")
                    .setWidth("162")
                    .setHeight("129")
            )
            .addComponent(
                LabelComp("desc_contents", "")
                    .setXY("desc_bg.x + 10", "desc_bg.y + 10")
            )
            .addComponent(
                TextureComp("page_up", "ui/dungeon/button/page_up.png")
                    .setXY("desc_bg.x + 1.5", "desc_bg.y + 101")
                    .setWidth("10")
                    .setHeight("22")
                    .addAction(ActionType.Left_Click, "page_up.texture = 'ui/dungeon/button/page_up_press.png';")
                    .addAction(ActionType.Left_Release, "page_up.texture = 'ui/dungeon/button/page_up.png';")
                    .addAction(ActionType.Left_Click, "global.dungeon_page = global.dungeon_page - 1;")
                    .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = PAGE))
            )
            .addComponent(
                TextureComp("page_next", "ui/dungeon/button/page_next.png")
                    .setXY("drop_6_bg.x + 23", "page_up.y")
                    .setWidth("10")
                    .setHeight("22")
                    .addAction(ActionType.Left_Click, "page_next.texture = 'ui/dungeon/button/page_next_press.png';")
                    .addAction(ActionType.Left_Release, "page_next.texture = 'ui/dungeon/button/page_next.png';")
                    .addAction(ActionType.Left_Click, "global.dungeon_page = global.dungeon_page + 1;")
                    .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = PAGE))
            )
            .addComponent(
                TextureComp("drop_1_bg", "ui/dungeon/slot_bg.png")
                    .setXY("desc_bg.x + 12", "desc_bg.y + 101")
                    .setWidth("22")
                    .setHeight("22")
            )
            .addComponent(
                TextureComp("drop_2_bg", "ui/dungeon/slot_bg.png")
                    .setXY("drop_1_bg.x + 23", "drop_1_bg.y")
                    .setWidth("22")
                    .setHeight("22")
            )
            .addComponent(
                TextureComp("drop_3_bg", "ui/dungeon/slot_bg.png")
                    .setXY("drop_2_bg.x + 23", "drop_1_bg.y")
                    .setWidth("22")
                    .setHeight("22")
            )
            .addComponent(
                TextureComp("drop_4_bg", "ui/dungeon/slot_bg.png")
                    .setXY("drop_3_bg.x + 23", "drop_1_bg.y")
                    .setWidth("22")
                    .setHeight("22")
            )
            .addComponent(
                TextureComp("drop_5_bg", "ui/dungeon/slot_bg.png")
                    .setXY("drop_4_bg.x + 23", "drop_1_bg.y")
                    .setWidth("22")
                    .setHeight("22")
            )
            .addComponent(
                TextureComp("drop_6_bg", "ui/dungeon/slot_bg.png")
                    .setXY("drop_5_bg.x + 23", "drop_1_bg.y")
                    .setWidth("22")
                    .setHeight("22")
            )
            .addComponent(
                SlotComp("drop_1", "dungeon_drop_1")
                    .setDrawBackground(false)
                    .setExtend("drop_1_bg")
            )
            .addComponent(
                SlotComp("drop_2", "dungeon_drop_2")
                    .setDrawBackground(false)
                    .setExtend("drop_2_bg")
            )
            .addComponent(
                SlotComp("drop_3", "dungeon_drop_3")
                    .setDrawBackground(false)
                    .setExtend("drop_3_bg")
            )
            .addComponent(
                SlotComp("drop_4", "dungeon_drop_4")
                    .setDrawBackground(false)
                    .setExtend("drop_4_bg")
            )
            .addComponent(
                SlotComp("drop_5", "dungeon_drop_5")
                    .setDrawBackground(false)
                    .setExtend("drop_5_bg")
            )
            .addComponent(
                SlotComp("drop_6", "dungeon_drop_6")
                    .setDrawBackground(false)
                    .setExtend("drop_6_bg")
            )
    }
}