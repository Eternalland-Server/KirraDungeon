package net.sakuragame.eternal.kirradungeon.client.zone.event

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * KirraDungeon
 * net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneJoinEvent
 *
 * @author kirraObj
 * @since 2021/12/28 15:27
 */
class ZoneJoinEvent(val player: Player, val dungeonId: String, val isTeam: Boolean) : BukkitProxyEvent()