package net.sakuragame.eternal.kirradungeon.client

import org.bukkit.Bukkit
import java.time.LocalDateTime

fun String.printDebug() = Bukkit.getLogger().info("[DEBUG] $this")

fun getCurrentHour() = LocalDateTime.now().hour

fun log(message: String) = Bukkit.getConsoleSender().sendMessage("[KirraDungeonClient] $message")