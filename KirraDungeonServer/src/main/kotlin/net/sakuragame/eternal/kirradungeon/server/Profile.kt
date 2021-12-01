package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.kirradungeon.server.zone.PlayerZone
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority.HIGHEST
import taboolib.common.platform.event.EventPriority.LOWEST
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.server.Profile
 *
 * @author kirraObj
 * @since 2021/11/9 15:51
 */
class Profile(val player: Player) {

    var isChallenging = false

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.first { it.player.uniqueId == uniqueId }

        @SubscribeEvent(priority = HIGHEST)
        fun e(e: PlayerJoinEvent) {
            profiles[e.player.name] = Profile(e.player)
        }

        @SubscribeEvent(priority = LOWEST)
        fun e(e: PlayerKickEvent) {
            dataRecycle(e.player)
        }

        @SubscribeEvent(priority = LOWEST)
        fun e(e: PlayerQuitEvent) {
            dataRecycle(e.player)
        }

        private fun dataRecycle(player: Player) {
            player.profile().apply {
                drop()
            }
        }
    }

    fun drop() {
        if (isChallenging) {
            val playerZone = PlayerZone.getByPlayer(player.uniqueId) ?: return
            playerZone.removePlayerUUID(player.uniqueId)
            submit(delay = 3, async = true) {
                if (playerZone.uuidList.size <= 0) playerZone.delete()
            }
        }
    }
}