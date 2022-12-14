package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function

import com.google.common.util.concurrent.Atomics
import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirracore.bukkit.KirraCoreBukkitAPI
import net.sakuragame.eternal.kirracore.common.packet.impl.sub.AssignType
import net.sakuragame.eternal.kirradungeon.client.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonAPI.ParamType.*
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamNumData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.sub.ScreenTeleportType
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.isBelongDungeon
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.broadcast
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

    @SubscribeEvent
    fun e(e: UIFCompSubmitEvent) {
        if (!e.isBelongDungeon()) return
        execCompSubmit(e.player, e.compID, e.params)
    }

    @SubscribeEvent
    fun onShopSubmit(e: UIFCompSubmitEvent) {
        if (e.compID == "shop") {
            val playerName = e.player.name
            val shopId = e.params.getParam(0)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shop open $playerName $shopId")
        }
    }

    @SubscribeEvent
    fun onHudSubmitCompatEvent(e: UIFCompSubmitEvent) {
        if (e.screenID != "function_hud") return
        if (e.compID != "dungeon") return
        FunctionDungeon.openGUI(e.player, init = true)
    }

    @SubscribeEvent
    fun e(e: KeyPressEvent) {
        if (e.key == DungeonAPI.triggerKey) {
            FunctionDungeon.openGUI(e.player, true)
        }
    }

    private fun execCompSubmit(player: Player, compId: String, params: SubmitParams) {
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        val paramData = getParamData(player, params)
        val profile = player.profile() ?: return
        if (profile.debugMode) {
            player.sendMessage("fromData: ${paramData.fromData}")
            player.sendMessage("toData: ${paramData.toData}")
        }
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
            ScreenTeleportType.DUNGEON -> {
                val isLocal = compId == "join_button"
                val isTeam = !isLocal && compId == "team_button"
                ZoneJoinEvent(player, paramData.subScreen.teleportData, isTeam).call()
            }

            ScreenTeleportType.SERVER -> {
                val split = paramData.subScreen.teleportData.split("@")
                if (split.size == 1) {
                    KirraCoreBukkitAPI.teleportPlayerToAnotherServer(split[0], null, null, player.uniqueId)
                    return
                }
                KirraCoreBukkitAPI.teleportPlayerToAnotherServer(split[0], AssignType.ASSIGN_WORLD, split[1], player.uniqueId)
            }

            ScreenTeleportType.COORD -> {
                val split = paramData.subScreen.teleportData.split("@")
                KirraCoreBukkitAPI.teleportPlayerToAnotherServer(split[0], AssignType.ASSIGN_COORD, split[1], player.uniqueId)
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
        FunctionDungeon.openAssignGUI(player, toData, paramData)
    }

    fun doPage(player: Player, paramData: ParamData) {
        val page = paramData.toData.param4
        val droppedItems = paramData.subScreen.droppedItems
        if (page > DungeonAPI.getMaxPage(droppedItems) || page < 1) {
            PacketSender.sendRunFunction(player, "default", PAGE_RESET_FUNCTION, false)
            DungeonAPI.sendDroppedItems(player, droppedItems, 1)
            return
        }
        DungeonAPI.sendDroppedItems(player, droppedItems, page)
    }

    private fun getParamData(player: Player, params: SubmitParams): ParamData {
        if (player.profile()?.debugMode == true) {
            printParamDataDebugMessage(player, params)
        }
        val fromNumData = getFromNumDataFromParams(params)
        val toNumData = getToNumDataFromParams(fromNumData, params)
        val category = DungeonAPI.getDungeonCategory(toNumData.param1)
        val screen = DungeonAPI.getDungeonScreen(category, toNumData.param2 - 1) ?: DungeonAPI.getDefaultScreen()
        val subScreen = screen.dungeonSubScreens[toNumData.param3 - 1] ?: DungeonAPI.getDefaultSubScreen(screen)
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