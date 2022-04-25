package net.sakuragame.eternal.kirradungeon.common.event

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class DungeonJoinEvent(val player: Player, val dungeonId: String) : BukkitProxyEvent()