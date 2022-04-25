package net.sakuragame.eternal.kirradungeon.common.event

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * KirraDungeon
 * net.sakuragame.eternal.kirradungeon.common.event.DungeonClearEvent
 *
 * @author kirraObj
 * @since 2021/12/8 2:24
 */
class DungeonClearEvent(val players: List<Player>, val dungeonId: String) : BukkitProxyEvent()