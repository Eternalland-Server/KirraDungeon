package net.sakuragame.eternal.kirradungeon.common.event

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class DungeonFailEvent(val players: List<Player>, val dungeonId: String) : BukkitProxyEvent()