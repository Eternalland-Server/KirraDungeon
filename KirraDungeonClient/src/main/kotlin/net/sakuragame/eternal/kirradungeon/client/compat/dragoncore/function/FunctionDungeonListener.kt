package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.*
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory.NORMAL
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent

object FunctionDungeonListener {

    @SubscribeEvent
    fun e(e: UIFCompSubmitEvent) {
//        execCompSubmit(e.player, e.compID, e.params)
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        e.player.apply {
            Dungeon.sendScreen(this)
            DungeonCard.sendScreen(this)
            DungeonCategory.sendScreen(this)
            DungeonRegion.sendScreen(this)
            DungeonRoom.sendScreen(this)
        }
    }

    private fun execCompSubmit(player: Player, compId: String, params: SubmitParams) {
        Bukkit.broadcastMessage("$compId, ${params.getParam(0) ?: "null"}, ${params.getParam(1) ?: "null"}")
        when (compId) {
            "category_normal_bg" -> {
                DungeonRoom.sendScreen(player, NORMAL)
                FunctionDungeon.openMainGUI(player)
            }
            "category_team_bg" -> {
            }
            "category_activity_bg" -> {
            }
            "category_special_bg" -> {
            }
        }
    }
}