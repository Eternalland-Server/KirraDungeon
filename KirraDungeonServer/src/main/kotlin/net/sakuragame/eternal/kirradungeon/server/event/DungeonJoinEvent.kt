package net.sakuragame.eternal.kirradungeon.server.event

import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * KirraDungeon
 * net.sakuragame.eternal.kirradungeon.server.event.DungeonJoinEvent
 *
 * @author kirraObj
 * @since 2021/12/9 17:57
 */
class DungeonJoinEvent(val player: Player, val dungeonId: String, val zone: IDungeon) : BukkitProxyEvent()