package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen

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
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirradungeon.client.zone.util.getFeeJoinCounts
import net.sakuragame.eternal.kirradungeon.client.zone.util.getFeeMaxJoinCounts
import org.bukkit.entity.Player
import taboolib.module.chat.colored


/**
 * 副本右侧介绍栏显示实现层.
 */
object DungeonCard : IScreen {

    override val screenId: String
        get() = "dungeon_card"

    override fun build2Screen(screen: DungeonScreen, subScreen: DungeonSubScreen, player: Player): ScreenUI {
        return ScreenUI(screenId).apply {
            addComponent(TextureComp("card_title")
                .setText(subScreen.name)
                .setExtend("desc_title")
            )
            addComponent(TextureComp("card_img", subScreen.description.bgPath)
                .setExtend("desc_img")
            )
            val newList = arrayListOf<String>().also {
                it.addAll(subScreen.description.text)
                it.add("")
                it.add(getJoinCountsData(player, subScreen.teleportData) ?: "")
            }
            addComponent(LabelComp("card_contents", newList)
                .setExtend("desc_contents")
            )
            if (!subScreen.isSingle) {
                addComponent(TextureComp("single_button", "ui/dungeon/button/single.png")
                    .setXY("body.x + 479", "body.y + 279")
                    .setWidth("80")
                    .setHeight("44")
                    .addAction(ActionType.Left_Click, "single_button.texture = 'ui/dungeon/button/single_press.png';")
                    .addAction(ActionType.Left_Release, "single_button.texture = 'ui/dungeon/button/single.png';")
                    .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = JOIN))
                )
                addComponent(TextureComp("team_button", "ui/dungeon/button/team.png")
                    .setXY("body.x + 397", "body.y + 279")
                    .setWidth("80")
                    .setHeight("44")
                    .addAction(ActionType.Left_Click, "team_button.texture = 'ui/dungeon/button/team_press.png';")
                    .addAction(ActionType.Left_Release, " team_button.texture = 'ui/dungeon/button/team.png';")
                    .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = JOIN))
                )
            } else {
                addComponent(TextureComp("join_button", "ui/dungeon/button/join.png")
                    .setXY("body.x + 397", "body.y + 279")
                    .setWidth("163")
                    .setHeight("44")
                    .addAction(ActionType.Left_Click, "join_button.texture = 'ui/dungeon/button/join_press.png';")
                    .addAction(ActionType.Left_Release, "join_button.texture = 'ui/dungeon/button/join.png';")
                    .addAction(ActionType.Left_Click, DungeonAPI.getPluginParams(type = JOIN))
                )
            }
            if (player.hasPermission("admin")) {
                return@apply
            }
            if (subScreen.limitTime.isActive() && !subScreen.limitTime.isInLimitTime()) {
                addLockComponent()
            }
            if (player.profile().number.get() < DungeonAPI.getNumber(subScreen)) {
                addLockComponent()
            }
            if (player.getRealm() < subScreen.limitRealm.realm) {
                addLockComponent()
            }
        }
    }

    private fun ScreenUI.addLockComponent() {
        addComponent(TextureComp("join_lock", "ui/common/lock_shade.png")
            .setXY("body.x + 397", "body.y + 279")
            .setWidth("163")
            .setHeight("44")
        )
    }

    private fun getJoinCountsData(player: Player, dungeonId: String): String? {
        val zone = Zone.getByID(dungeonId) ?: return null
        val maxCounts = player.getFeeMaxJoinCounts(zone)
        val currentCounts = player.getFeeJoinCounts(zone)
        // 无限副本次数.
        if (currentCounts >= 999 || currentCounts == -1) {
            return "&6&l副本次数: &a无限/无限".colored()
        }
        // 次数为零.
        if (currentCounts <= 0) {
            return "&6&l副本次数: &c$currentCounts/0".colored()
        }
        // 次数等于最大进入次数.
        if (currentCounts == maxCounts) {
            return "&6&l副本次数: &a$currentCounts/$maxCounts".colored()
        }
        // 次数不满.
        if (currentCounts < maxCounts) {
            return "&6&l副本次数: &e$currentCounts/$maxCounts".colored()
        }
        return ""
    }
}