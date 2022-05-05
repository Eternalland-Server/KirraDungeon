package net.sakuragame.eternal.kirradungeon.server.compat

import com.taylorswiftcn.megumi.uifactory.generate.function.Statements
import com.taylorswiftcn.megumi.uifactory.generate.type.FunctionType
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.LabelComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.dragoncore.network.PacketSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

@Suppress("SpellCheckingInspection")
object DragonCoreCompat {

    lateinit var joinTitleHud: ScreenUI

    lateinit var joinTitleHudID: String

    lateinit var joinTitleHudYaml: YamlConfiguration

    lateinit var failHud: ScreenUI

    lateinit var failHudID: String

    lateinit var failHudYaml: YamlConfiguration

    @Awake(LifeCycle.ENABLE)
    fun init() {
        buildJoinTitleHud()
        buildFailHud()
    }

    private fun buildJoinTitleHud() {
        joinTitleHudID = "join_title_hud"
        joinTitleHud = ScreenUI(joinTitleHudID)
            .addFunctions(FunctionType.Open, Statements()
                .add("func.Function_Async_Execute('function_a');")
                .build())
            .addFunctions("function_a", Statements()
                .add("var.time = func.Time_Current;")
                .add("func.Component_Set('progress_bar', 'width', '437 * (func.Time_Current - var.time) / 2000');")
                .add("func.Delay(2000);")
                .add("func.Component_Set('progress_bar', 'width', '437');")
                .add("func.Delay(1000);")
                .add("func.Screen_Close_Hud(var.ScreenID);")
                .build())
            .addComponent(TextureComp("body", "ui/common/dungeon/title.png").also {
                it.setXY("w/2-437/2", "h/2-170/2")
                it.width = "437"
                it.height = "170"
                it.alpha = 0.05
            })
            .addComponent(TextureComp("progress_bar", "ui/common/dungeon/title.png").also {
                it.setXY("body.x", "body.y")
                it.width = "437"
                it.height = "170"
            })
            .addComponent(LabelComp("body_label", "func.PlaceholderAPI_Get('kzone_dungeon_title')").also {
                it.setXY("body.x+(body.width-body_label.width)/2+2", "body.y+(body.height-body_label.height)/2-18")
                it.width = "437"
                it.height = "170"
                it.scale = 4.0.toString()
            })
        joinTitleHudYaml = joinTitleHud.build(null)
    }

    private fun buildFailHud() {
        failHudID = "fail_hud"
        failHud = ScreenUI(failHudID).apply {
            match = "hud"
            addComponent(LabelComp("fail_hud_label", "&7&oM 键消耗复活币复活 &f&k!&r &7&oN 键退出副本回主城").also { label ->
                label.setXY("(w-body.width * body.scale)/2", "(h-body.height * body.scale)/2 + 100")
                label.setScale(2.0)
                label.visible = "func.PlaceholderAPI_Get('player_is_failed') == true"
            })
        }
        failHudYaml = failHud.build(null)
    }

    fun openFailHud(player: Player) {
        PacketSender.sendSyncPlaceholder(player, mutableMapOf("player_is_failed" to "true"))
    }

    fun closeFailHud(player: Player) {
        PacketSender.sendSyncPlaceholder(player, mutableMapOf("player_is_failed" to "false"))
    }

    fun updateDragonVars(player: Player, dungeonName: String) =
        PacketSender.sendSyncPlaceholder(player, mutableMapOf<String, String>().also {
            it["kzone_dungeon_title"] = dungeonName
        })
}