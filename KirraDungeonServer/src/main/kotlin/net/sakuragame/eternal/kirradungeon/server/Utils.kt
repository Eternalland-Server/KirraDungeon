package net.sakuragame.eternal.kirradungeon.server

import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder
import net.minecraft.server.v1_12_R1.WorldBorder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.platform.util.asLangTextList
import kotlin.math.floor


fun Player.reset() {
    gameMode = GameMode.ADVENTURE
}

fun Player.sendRedHud() {
    val craftPlayer = this as CraftPlayer
    val border = WorldBorder()
    border.size = 1.0
    border.setCenter(craftPlayer.location.x + 10000, craftPlayer.location.z + 10000)
    craftPlayer.handle.playerConnection.sendPacket(PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE))
}

fun Player.removeRedHud() {
    val craftPlayer = this as CraftPlayer
    val border = WorldBorder()
    border.size = 30000000.0
    border.setCenter(craftPlayer.location.x, craftPlayer.location.z)
    craftPlayer.handle.playerConnection.sendPacket(PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE))
}

fun debug(any: Any) = Bukkit.getLogger().info("[DEBUG] $any")

fun Player.playDeathAnimation() = world.strikeLightningEffect(location)!!

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

fun getMobMaxHealth(entity: LivingEntity) = KirraDungeonServer.mythicmobsAPI.getMythicMobInstance(entity).type.health.get()

fun formatSeconds(timeInSeconds: Int): String {
    val secondsLeft = timeInSeconds % 3600 % 60
    val minutes = floor((timeInSeconds % 3600 / 60).toDouble()).toInt()
    val mM = (if (minutes < 10) "0" else "") + minutes
    val sS = (if (secondsLeft < 10) "0" else "") + secondsLeft
    return "${mM}分${sS}秒."
}