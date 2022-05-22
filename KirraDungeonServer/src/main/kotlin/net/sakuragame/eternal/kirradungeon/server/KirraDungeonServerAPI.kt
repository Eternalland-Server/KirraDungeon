package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.kirradungeon.server.zone.impl.DungeonManager
import org.bukkit.entity.Player
import taboolib.common5.Baffle
import java.util.concurrent.TimeUnit

@Suppress("SpellCheckingInspection")
object KirraDungeonServerAPI {

    val baffle by lazy {
        Baffle.of(1, TimeUnit.SECONDS)
    }

    /**
     * 根据玩家获取副本实例.
     *
     * @param player 玩家
     * @return 副本实例
     */
    fun getDungeonByPlayer(player: Player) = DungeonManager.getByPlayer(player.uniqueId)
}