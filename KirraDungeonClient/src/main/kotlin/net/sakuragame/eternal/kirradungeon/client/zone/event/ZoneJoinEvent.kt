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
class ZoneJoinEvent : BukkitProxyEvent {
    val player: Player
    val dungeonId: String
    val isTeam: Boolean
    val isLocal: Boolean

    constructor(player: Player, dungeonId: String, isTeam: Boolean, isLocal: Boolean) : super() {
        this.player = player
        this.dungeonId = dungeonId
        this.isTeam = isTeam
        this.isLocal = isLocal
    }
}