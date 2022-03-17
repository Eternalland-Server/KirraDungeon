package net.sakuragame.eternal.kirradungeon.client

import org.bukkit.Bukkit
import java.time.LocalDateTime
import java.util.*

fun String.printDebug() = Bukkit.getLogger().info("[DEBUG] $this")

val date by lazy {
    Date().also {
        it.hours = 4
        it.minutes = 0
    }.time
}

fun getCurrentHour() = LocalDateTime.now().hour

fun getTodayTimeUnix() = date

fun log(message: String) = Bukkit.getConsoleSender().sendMessage("[KirraDungeonClient] $message")