package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.platform.util.asLangTextList
import kotlin.math.floor

fun getEditingZone(player: Player): Zone? {
    val world = Zone.editingDungeonWorld ?: run {
        player.sendMessage("&c无效编辑, 你并没有在配置副本".colored())
        return null
    }
    return Zone.getByID(world.worldIdentifier) ?: run {
        player.sendMessage("&c错误, 副本不存在".colored())
        return null
    }
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
    PacketSender.sendPlaySound(
        this,
        "sounds/a/$sound.ogg",
        0.33f, 1f,
        false,
        0f, 0f, 0f
    );
}

fun Location.toCenter(offset: Double): Location {
    return Location(world, blockX + offset, blockY.toDouble(), blockZ + offset)
}