package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.isBelongDungeon
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.asLangText
import java.util.concurrent.TimeUnit

object FunctionDungeonListener {

    val baffle by lazy {
        Baffle.of(3, TimeUnit.SECONDS)
    }

    private data class ParamData(val category: DungeonCategory, val screen: DungeonScreen, val subScreen: DungeonSubScreen)

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        val block = e.to.block
        val player = e.player
        if (block != null && block.type == Material.END_GATEWAY) {
            if (!baffle.hasNext(player.name)) return
            baffle.next(player.name)
            FunctionDungeon.openMainGUI(player, init = true)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        if (baffle.hasNext(e.player.name)) baffle.reset(e.player.name)
    }

    @SubscribeEvent
    fun e(e: PlayerKickEvent) {
        if (baffle.hasNext(e.player.name)) baffle.reset(e.player.name)
    }

    @SubscribeEvent
    fun e(e: UIFCompSubmitEvent) {
        if (!e.isBelongDungeon()) return
        execCompSubmit(e.player, e.compID, e.params)
    }

    @SubscribeEvent
    fun onHudSubmitCompatEvent(e: UIFCompSubmitEvent) {
        if (e.screenID != "function_hud") return
        if (e.compID != "dungeon") return
        FunctionDungeon.openMainGUI(e.player, init = true)
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val defaultScreen = DungeonAPI.getDefaultScreen()
        val defaultSubScreen = DungeonAPI.getDefaultSubScreen(defaultScreen)
        FunctionDungeon.sendScreen(e.player, defaultScreen, defaultSubScreen)
    }

    @SubscribeEvent
    fun e(e: KeyPressEvent) {
        if (e.key == DungeonAPI.triggerKey) {
            FunctionDungeon.openMainGUI(e.player, true)
        }
    }

    private fun execCompSubmit(player: Player, compId: String, params: SubmitParams) {
        val paramData = getParamData(player, params) ?: return
        when (DungeonAPI.ParamType.valueOf(params.getParam(1))) {
            DungeonAPI.ParamType.UPDATE -> doUpdate(player, paramData)
            DungeonAPI.ParamType.JOIN -> doJoin(player, compId, paramData)
            DungeonAPI.ParamType.CLOSE -> doClose(player)
        }
    }

    private fun doClose(player: Player) = player.closeInventory()

    private fun doJoin(player: Player, compId: String, paramData: ParamData) {
        val isLocal = compId == "join_button"
        val isTeam = !isLocal && compId == "team_button"
        ZoneJoinEvent(player, paramData.subScreen.dungeonId.toString(), isTeam, isLocal).call()
        player.closeInventory()
    }

    private fun doUpdate(player: Player, paramData: ParamData) {
        FunctionDungeon.sendScreen(player, paramData.screen, paramData.subScreen)
        FunctionDungeon.openMainGUI(player, false)
    }

    private fun getParamData(player: Player, params: SubmitParams): ParamData? {
        val category = DungeonAPI.getDungeonCategory(params.getParamI(2))
        val screen = DungeonAPI.getDungeonScreen(category, params.getParamI(3)) ?: kotlin.run {
            player.closeInventory()
            MessageAPI.sendActionTip(player, player.asLangText("message-screen-went-wrong-exception"))
            return null
        }
        val subScreen = screen.dungeonSubScreens[params.getParamI(4) - 1] ?: DungeonAPI.getDefaultSubScreen(screen)
        if (player.profile().debugMode.get()) {
            player.sendMessage("&c[DEBUG] ${params.getParamI(2)}, ${params.getParamI(3)}, ${params.getParamI(4)}")
        }
        return ParamData(category, screen, subScreen)
    }
}