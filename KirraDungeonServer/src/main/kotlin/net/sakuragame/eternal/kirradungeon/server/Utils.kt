package net.sakuragame.eternal.kirradungeon.server

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.platform.util.asLangTextList
import kotlin.math.floor

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

fun Location.toCenter(offset: Double): Location {
    return Location(world, blockX + offset, blockY + offset, blockZ + offset)
}