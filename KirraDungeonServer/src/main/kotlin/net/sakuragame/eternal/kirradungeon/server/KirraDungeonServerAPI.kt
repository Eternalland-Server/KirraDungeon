package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.UnlimitedDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.WaveDungeon
import org.bukkit.entity.Player

@Suppress("SpellCheckingInspection")
object KirraDungeonServerAPI {

    /**
     * 根据玩家获取副本实例.
     *
     * @param player 玩家
     * @return 副本实例
     */
    fun getDungeonByPlayer(player: Player): IDungeon? {
        val profile = player.profile()
        return when (profile.zoneType) {
            ZoneType.DEFAULT -> DefaultDungeon.getByPlayer(player.uniqueId) ?: return null
            ZoneType.SPECIAL -> SpecialDungeon.getByPlayer(player.uniqueId) ?: return null
            ZoneType.UNLIMITED -> UnlimitedDungeon.getByPlayer(player.uniqueId) ?: return null
            ZoneType.WAVE -> WaveDungeon.getByPlayer(player.uniqueId) ?: return null
        }
    }
}