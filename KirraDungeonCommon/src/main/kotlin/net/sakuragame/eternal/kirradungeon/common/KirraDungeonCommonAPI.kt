package net.sakuragame.eternal.kirradungeon.common

import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClientAPI
import net.sakuragame.eternal.kirradungeon.common.KirraDungeonCommonAPI.ServerType.*
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.Bukkit

@Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")
object KirraDungeonCommonAPI {

    enum class ServerType {
        NONE, CLIENT, SERVER;
    }

    fun getRunningServerType(): ServerType {
        if (Bukkit.getPluginManager().getPlugin("KirraDungeonClient") != null) {
            return CLIENT
        }
        if (Bukkit.getPluginManager().getPlugin("KirraDungeonServer") != null) {
            return SERVER
        }
        return NONE
    }

    fun getDisplayNameById(id: String): String? {
        return when (getRunningServerType()) {
            NONE -> null
            CLIENT -> KirraDungeonClientAPI.getDungenSubScreenById(id)?.name
            SERVER -> Zone.zones.find { it.name.contains(id) }?.name
        }
    }
}