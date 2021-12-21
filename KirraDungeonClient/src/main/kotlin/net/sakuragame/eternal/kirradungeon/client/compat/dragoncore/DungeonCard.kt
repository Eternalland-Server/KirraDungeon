package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.generate.function.Statements
import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.LabelComp

import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp

import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake


/**
 * KirraDungeon
 * net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonCoreCard
 *
 * @author kirraObj
 * @since 2021/12/16 11:18
 */
object DungeonCard {

    const val screenId = "dungeon_card"

    private lateinit var ui: ScreenUI

    private lateinit var uiYaml: YamlConfiguration

    fun sendScreen(player: Player) = PacketSender.sendYaml(player, FolderType.Gui, screenId, uiYaml)

    @Awake(LifeCycle.ENABLE)
    fun init() {
        ui = ScreenUI(screenId)
            .addComponent(TextureComp("card_title")
                .setText("&f&l城墙边缘")
                .setExtend("desc_title")
            )
            .addComponent(LabelComp("card_tip", "&7宁静的高坡隐藏着不为人知的秘密")
                .setExtend("desc_tip")
            )
            .addComponent(LabelComp("card_contents", Statements()
                .add("&f&l副本难度: &e✮✩✩✩✩")
                .add("&f&l推荐战斗力: &63000")
                .add("&f&l入场费用: &a1000金币")
                .add("")
                .add("&7&l副本BOSS: &c蜗牛王")
                .add("&7&l副本怪物: &f绿蜗牛、红蜗牛")
                .build()
            )
                .setExtend("desc_contents")
            )
            .addComponent(TextureComp("single_button", "ui/dungeon/button/single.png")
                .setXY("body.x + 479", "body.y + 279")
                .setWidth("80")
                .setHeight("44")
                .addAction(ActionType.Left_Click, "single_button.texture = 'ui/dungeon/button/single_press.png';")
                .addAction(ActionType.Left_Release, "single_button.texture = 'ui/dungeon/button/single.png';")
            )
            .addComponent(TextureComp("team_button", "ui/dungeon/button/team.png")
                .setXY("body.x + 397", "body.y + 279")
                .setWidth("80")
                .setHeight("44")
                .addAction(ActionType.Left_Click, "team_button.texture = 'ui/dungeon/button/team_press.png';")
                .addAction(ActionType.Left_Release, "team_button.texture = 'ui/dungeon/button/team.png';")
            )

        uiYaml = ui.build(null)
    }
}