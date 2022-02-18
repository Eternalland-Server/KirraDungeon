package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI.ParamType.*
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen.ScreenTeleportType.*
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.getParentScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.isBelongDungeon
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
import net.sakuragame.kirracoords.KirraCoordsAPI
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.asLangText
import java.util.concurrent.TimeUnit

object FunctionDungeonListener {

    private const val PAGE_RESET_FUNCTION = "global.dungeon_page = 1;"

    private val baffle by lazy {
        Baffle.of(3, TimeUnit.SECONDS)
    }

    private data class ParamData(
        val categoryChanged: Boolean,
        val screenChanged: Boolean,
        val category: DungeonCategory,
        val screen: DungeonScreen,
        val subScreen: DungeonSubScreen,
        val originParam1: Int,
        val originParam2: Int,
        val originParam3: Int,
        val originParam4: Int,
    )

    // 主城末地门监听.
    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        val block = e.to.block
        val player = e.player
        if (block != null && block.type == Material.END_GATEWAY) {
            if (!baffle.hasNext(player.name)) return
            baffle.next(player.name)
            FunctionDungeon.openGUI(player, init = true)
        }
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
        FunctionDungeon.openGUI(e.player, init = true)
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
            FunctionDungeon.openGUI(e.player, true)
        }
    }

    @Suppress("RemoveRedundantQualifierName")
    private fun execCompSubmit(player: Player, compId: String, params: SubmitParams) {
        val paramData = getParamData(player, params) ?: return
        when (DungeonAPI.ParamType.valueOf(params.getParam(1))) {
            UPDATE -> doUpdate(player, paramData)
            JOIN -> doJoin(player, compId, paramData)
            CLOSE -> doClose(player)
            PAGE -> doPage(player, paramData)
        }
    }

    private fun doClose(player: Player) = player.closeInventory()

    private fun doJoin(player: Player, compId: String, paramData: ParamData) {
        player.closeInventory()
        when (paramData.subScreen.teleportType) {
            DUNGEON -> {
                val isLocal = compId == "join_button"
                val isTeam = !isLocal && compId == "team_button"
                ZoneJoinEvent(player, paramData.subScreen.teleportData, isTeam).call()
            }
            SERVER -> {
                val split = paramData.subScreen.teleportData.split("@")
                if (split.size == 1) {
                    KirraCoreBukkitAPI.teleportPlayerToAnotherServer(split[0], player)
                    return
                }
                KirraCoreBukkitAPI.teleportPlayerToAnotherServer(split[0], split[1], player)
            }
            COORD -> KirraCoordsAPI.tpCoord(player, paramData.subScreen.teleportData)
        }
    }

    private fun doUpdate(player: Player, paramData: ParamData) {
        var screen = paramData.screen
        var subScreen = paramData.subScreen
        if (paramData.categoryChanged) {
            PacketSender.sendRunFunction(player, "default", "global.dungeon_sub_category = 1;", true)
            PacketSender.sendRunFunction(player, "default", "global.dungeon_current_selected = 1;", true)
            screen = paramData.category.getParentScreen()[0]
            subScreen = DungeonAPI.getDefaultSubScreen(screen)
        }
        if (paramData.screenChanged) {
            PacketSender.sendRunFunction(player, "default", "global.dungeon_current_selected = 1;", true)
            subScreen = DungeonAPI.getDefaultSubScreen(screen)
        }
        FunctionDungeon.sendScreen(player, screen, subScreen)
        FunctionDungeon.openGUI(player, false)
        doPage(player, paramData)
    }

    private fun doPage(player: Player, paramData: ParamData) {
        val page = paramData.originParam4
        val droppedItems = paramData.subScreen.droppedItems
        if (page > DungeonAPI.getMaxPage(droppedItems) || page < 1) {
            PacketSender.sendRunFunction(player, "default", PAGE_RESET_FUNCTION, false)
            DungeonAPI.sendDroppedItems(player, droppedItems, 1)
            return
        }
        DungeonAPI.sendDroppedItems(player, droppedItems, page)
    }

    private fun getParamData(player: Player, params: SubmitParams): ParamData? {
        val categoryChanged = params.getParam(2) == "true"
        val screenChanged = params.getParam(3) == "true"
        val category = DungeonAPI.getDungeonCategory(params.getParamI(4))
        val screen = DungeonAPI.getDungeonScreen(category, params.getParamI(5)) ?: kotlin.run {
            player.closeInventory()
            MessageAPI.sendActionTip(player, player.asLangText("message-screen-went-wrong-exception"))
            return null
        }
        val subScreen = screen.dungeonSubScreens[params.getParamI(6) - 1] ?: DungeonAPI.getDefaultSubScreen(screen)
        if (player.profile().debugMode.get()) {
            player.sendMessage("&c[DEBUG]")
            player.sendMessage("${params.getParamI(4)}")
            player.sendMessage("${params.getParamI(5)}")
            player.sendMessage("${params.getParamI(6)}")
            player.sendMessage("${params.getParamI(7)}")
        }
        return ParamData(
            categoryChanged,
            screenChanged,
            category,
            screen,
            subScreen,
            params.getParamI(4),
            params.getParamI(5),
            params.getParamI(6),
            params.getParamI(7)
        )
    }
}