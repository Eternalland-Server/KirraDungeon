package net.sakuragame.eternal.kirradungeon.client.zone.event

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class ZoneJoinEvent(val player: Player, val dungeonId: String, val isTeam: Boolean) : BukkitProxyEvent()