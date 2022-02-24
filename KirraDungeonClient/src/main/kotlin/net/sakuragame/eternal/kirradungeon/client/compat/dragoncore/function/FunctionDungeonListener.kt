package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.google.common.util.concurrent.Atomics
import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI.ParamType.*
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamNumData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen.ScreenTeleportType.*
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.isBelongDungeon
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.asLangText
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

object FunctionDungeonListener {

    private const val PAGE_RESET_FUNCTION = "global.dungeon_page = 1;"

    private val REGEX_FOR_PARAMS by lazy {
        Pattern.compile("([a-zA-Z_]+) = (.*)", Pattern.CASE_INSENSITIVE)
    }

    private val baffle by lazy {
        Baffle.of(3, TimeUnit.SECONDS)
    }

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
                KirraCoreBukkitAPI.teleportPlayerToAnotherWorld(split[0], split[1], player)
            }
            COORD -> {
                val split = paramData.subScreen.teleportData.split("@")
                KirraCoreBukkitAPI.teleportPlayerToAnotherCoord(split[0], split[1], player)
            }
        }
    }

    private fun doUpdate(player: Player, paramData: ParamData) {
        if (paramData.fromData.isSame(paramData.toData)) {
            return
        }
        val toData = paramData.toData
        if (paramData.fromData.isCategoryChanged(toData)) {
            PacketSender.sendRunFunction(player, "default", "global.dungeon_sub_category = 1;", true)
            PacketSender.sendRunFunction(player, "default", "global.dungeon_current_selected = 1;", true)
            toData.param2 = 1
            toData.param3 = 1
        }
        if (paramData.fromData.isScreenChanged(toData)) {
            PacketSender.sendRunFunction(player, "default", "global.dungeon_current_selected = 1;", true)
            toData.param3 = 1
        }
        if (paramData.screen.defaultSelectScreen != 1) {
            PacketSender.sendRunFunction(player, "default", "global.dungeon_current_selected = ${paramData.screen.defaultSelectScreen};", true)
            toData.param3 = paramData.screen.defaultSelectScreen
        }
        FunctionDungeon.openAssignGUI(player, toData)
        doPage(player, paramData)
    }

    private fun doPage(player: Player, paramData: ParamData) {
        val page = paramData.toData.param4
        val droppedItems = paramData.subScreen.droppedItems
        if (page > DungeonAPI.getMaxPage(droppedItems) || page < 1) {
            PacketSender.sendRunFunction(player, "default", PAGE_RESET_FUNCTION, false)
            DungeonAPI.sendDroppedItems(player, droppedItems, 1)
            return
        }
        DungeonAPI.sendDroppedItems(player, droppedItems, page)
    }

    private fun getParamData(player: Player, params: SubmitParams): ParamData? {
        val fromNumData = getFromNumDataFromParams(params)
        val toNumData = getToNumDataFromParams(fromNumData, params)
        val category = DungeonAPI.getDungeonCategory(toNumData.param1)
        val screen = DungeonAPI.getDungeonScreen(category, toNumData.param2) ?: kotlin.run {
            player.closeInventory()
            MessageAPI.sendActionTip(player, player.asLangText("message-screen-went-wrong-exception"))
            return null
        }
        val subScreen = screen.dungeonSubScreens[toNumData.param3 - 1] ?: DungeonAPI.getDefaultSubScreen(screen)
        if (player.profile().debugMode.get()) {
            printParamDataDebugMessage(player, params)
        }
        return ParamData(fromNumData, toNumData, category, screen, subScreen)
    }

    private fun getFromNumDataFromParams(params: SubmitParams): ParamNumData {
        val category = params.getParamI(3)
        val screen = params.getParamI(4)
        val subScreen = params.getParamI(5)
        val page = params.getParamI(6)
        return ParamNumData(category, screen, subScreen, page)
    }

    private fun getToNumDataFromParams(fromData: ParamNumData, params: SubmitParams): ParamNumData {
        val input = params.getParam(2)
        if (input.isNullOrEmpty()) {
            return fromData
        }
        val type = Atomics.newReference<String>()
        val value = AtomicInteger(0)
        val matcher = REGEX_FOR_PARAMS.matcher(input)
        while (matcher.find()) {
            type.set(matcher.group(1))
            value.set(matcher.group(2).toInt())
        }
        return when (type.get()) {
            "category" ->
                ParamNumData(
                    value.get(),
                    fromData.param2,
                    fromData.param3,
                    fromData.param4
                )
            "sub_category" -> {
                ParamNumData(
                    fromData.param1,
                    value.get(),
                    fromData.param3,
                    fromData.param4
                )
            }
            "current_selected" ->
                ParamNumData(
                    fromData.param1,
                    fromData.param2,
                    value.get(),
                    fromData.param4
                )
            else -> fromData
        }
    }

    private fun printParamDataDebugMessage(player: Player, params: SubmitParams) {
        player.sendMessage("&c[DEBUG]")
        player.sendMessage("${params.getParamI(5)}")
        player.sendMessage("${params.getParamI(6)}")
        player.sendMessage("${params.getParamI(7)}")
        player.sendMessage("${params.getParamI(8)}")
    }
}