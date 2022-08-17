package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import com.taylorswiftcn.megumi.uifactory.generate.type.ActionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.LabelComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI.ParamType.JOIN
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.getRealm
import org.bukkit.entity.Player

object DungeonCard : IScreen {

    override val screenId: String
        get() = "dungeon_card"

    override fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen, player: Player): ScreenUI {
        return ScreenUI(screenId).apply {
            addComponent(
                TextureComp("card_title")
                    .setText(subScreen.name)
                    .setExtend("desc_title")
            )
            addComponent(
                TextureComp("card_img", subScreen.description.bgPath)
                    .setExtend("desc_img")
            )
            addComponent(
                TextureComp("desc_img_shade", "ui/dungeon/desc_shade.png")
                    .setExtend("desc_img")
            )
            addComponent(
                LabelComp("card_tip", subScreen.description.tip)
                    .setExtend("desc_tip")
            )
            val description = arrayListOf<String>().also {
                it.addAll(subScreen.description.text)
            }
            addComponent(
                LabelComp("card_contents", description)
                    .setExtend("desc_contents")
            )
            if (!subScreen.isSingle) {
                addComponent(
                    TextureComp("single_button", "ui/dungeon/button/single.png")
                        .setXY("body.x + 479", "body.y + 279")
                        .setWidth("80")
                        .setHeight("44")
                        .addAction(ActionType.Left_Click, "single_button.texture = 'ui/dungeon/button/single_press.png';")
                        .addAction(ActionType.Left_Release, "single_button.texture = 'ui/dungeon/button/single.png';")
                        .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = JOIN))
                )
                addComponent(
                    TextureComp("team_button", "ui/dungeon/button/team.png")
                        .setXY("body.x + 397", "body.y + 279")
                        .setWidth("80")
                        .setHeight("44")
                        .addAction(ActionType.Left_Click, "team_button.texture = 'ui/dungeon/button/team_press.png';")
                        .addAction(ActionType.Left_Release, " team_button.texture = 'ui/dungeon/button/team.png';")
                        .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = JOIN))
                )
            } else {
                addComponent(
                    TextureComp("join_button", "ui/dungeon/button/join.png")
                        .setXY("body.x + 397", "body.y + 279")
                        .setWidth("163")
                        .setHeight("44")
                        .addAction(ActionType.Left_Click, "join_button.texture = 'ui/dungeon/button/join_press.png';")
                        .addAction(ActionType.Left_Release, "join_button.texture = 'ui/dungeon/button/join.png';")
                        .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = JOIN))
                )
            }
            if (subScreen.shopId != null) {
                addComponent(
                    TextureComp("shop", "ui/dungeon/shop.png")
                        .setXY("card_contents.x+126", "card_contents.y")
                        .setWidth("20")
                        .setHeight("20")
                        .addAction(ActionType.Left_Click, "func.Sound_Play();")
                        .addAction(ActionType.Left_Click, "shop.texture = 'ui/dungeon/shop_press.png';")
                        .addAction(ActionType.Left_Release, "shop.texture = 'ui/dungeon/shop.png';")
                        .addAction(ActionType.Left_Release, SubmitParams().apply {
                            addValue("${subScreen.shopId}")
                        })
                )
            }
            if (player.hasPermission("admin")) {
                return@apply
            }
            if (subScreen.limitTime.isActive() && !subScreen.limitTime.isInLimitTime()) {
                addLockComponent()
            }
            val profile = player.profile() ?: return@apply
            if (profile.number < DungeonAPI.getNumber(subScreen)) {
                addLockComponent()
            }
            if (player.getRealm() < subScreen.limitRealm.realm) {
                addLockComponent()
            }
        }
    }

    private fun ScreenUI.addLockComponent() {
        addComponent(
            TextureComp("join_lock", "ui/common/lock_shade.png")
                .setXY("body.x + 397", "body.y + 279")
                .setWidth("163")
                .setHeight("44")
        )
    }
}