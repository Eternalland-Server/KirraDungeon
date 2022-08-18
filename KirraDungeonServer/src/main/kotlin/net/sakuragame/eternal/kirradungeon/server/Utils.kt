package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.asLangTextList
import kotlin.math.floor

fun getEditingZone(player: Player, silent: Boolean = true): Zone? {
    val world = Zone.editingDungeonWorld ?: run {
        if (!silent) {
            player.sendMessage("&c无效编辑, 你并没有在配置副本".colored())
        }
        return null
    }
    return Zone.getByID(world.worldIdentifier) ?: run {
        if (silent) {
            player.sendMessage("&c错误, 副本不存在".colored())
        }
        return null
    }
}

fun String.parseIntRange(): IntRange? {
    val split = split("..")
    val start = split.getOrNull(0)?.toIntOrNull() ?: return null
    val end = split.getOrNull(1)?.toIntOrNull() ?: return null
    return IntRange(start, end)
}

fun debug(any: Any) = Bukkit.getLogger().info("[DEBUG] $any")

fun String.splitWithNoSpace(regex: String) = this
    .replace(" ", "")
    .split(regex)

fun kickPlayerByNotFoundData(player: Player) {
    val strBuilder = StringBuilder()
    player.asLangTextList("message-player-not-found-data").forEach {
        strBuilder.append("$it\n")
    }
    player.kickPlayer(strBuilder.toString())
}

fun getMobMaxHealth(entity: LivingEntity): Double {
    return entity.maxHealth
}

fun formatSeconds(timeInSeconds: Int): String {
    val secondsLeft = timeInSeconds % 3600 % 60
    val minutes = floor((timeInSeconds % 3600 / 60).toDouble()).toInt()
    val mM = (if (minutes < 10) "0" else "") + minutes
    val sS = (if (secondsLeft < 10) "0" else "") + secondsLeft
    return "${mM}分${sS}秒."
}

fun Player.playDragonCoreSound(sound: String) {
    submit(async = true, delay = 3L) {
        PacketSender.sendPlaySound(
            this@playDragonCoreSound,
            sound,
            1f, 1f,
            false,
            0f, 0f, 0f
        );
    }
}

fun Location.toCenter(offset: Double): Location {
    return Location(world, blockX + offset, blockY.toDouble(), blockZ + offset)
}