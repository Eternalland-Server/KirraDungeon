package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.isBelongDungeon
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent

object FunctionDungeonListener {

    @SubscribeEvent
    fun e(e: UIFCompSubmitEvent) {
        if (!e.params.isBelongDungeon()) return
        execCompSubmit(e.player, e.compID, e.params)
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val defaultScreen = DungeonAPI.getDefaultScreen()
        val defaultSubScreen = DungeonAPI.getDefaultSubScreen()
        FunctionDungeon.sendScreen(e.player, defaultScreen, defaultSubScreen)
    }

    private fun execCompSubmit(player: Player, compId: String, params: SubmitParams) {
        when (DungeonAPI.ParamType.valueOf(params.getParam(1))) {
            DungeonAPI.ParamType.UPDATE -> doUpdate(player, params)
            DungeonAPI.ParamType.JOIN -> doJoin(player, compId, params)
        }
    }

    private fun doJoin(player: Player, compId: String, params: SubmitParams) {
        Bukkit.broadcastMessage("reached doJoin.")
    }

    private fun doUpdate(player: Player, params: SubmitParams) {
        val category = DungeonAPI.getDungeonCategory(params.getParamI(2))
        val dungeonScreen = DungeonAPI.getDungeonScreen(category, params.getParamI(3)) ?: kotlin.run {
            Bukkit.broadcastMessage("dungeonScreen is null.")
            return
        }
        val dungeonSubScreen = dungeonScreen.dungeonSubScreens[params.getParamI(4) - 1] ?: kotlin.run {
            Bukkit.broadcastMessage("dungeonSubScreen is null.")
            return
        }
        FunctionDungeon.sendScreen(player, dungeonScreen, dungeonSubScreen)
        FunctionDungeon.openMainGUI(player, false)
    }
}