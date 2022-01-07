package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.zone.player.PlayerZone
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority.HIGHEST
import taboolib.common.platform.event.EventPriority.LOWEST
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import java.util.concurrent.atomic.AtomicInteger

/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.server.Profile
 *
 * @author kirraObj
 * @since 2021/11/9 15:51
 */
class Profile(val player: Player) {

    val number = AtomicInteger(1)

    var isChallenging = false

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.first { it.player.uniqueId == uniqueId }

        @SubscribeEvent(priority = HIGHEST)
        fun e(e: PlayerJoinEvent) {
            profiles[e.player.name] = Profile(e.player).apply {
                read()
            }
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
                save()
                drop()
            }
        }
    }

    fun drop() {
        if (isChallenging) {
            val playerZone = PlayerZone.getByPlayer(player.uniqueId) ?: return
            playerZone.removePlayerUUID(player.uniqueId)
            submit(delay = 3, async = true) {
                if (playerZone.playerUUIDList.size <= 0) playerZone.del()
            }
            BossBar.close(player)
        }
    }

    fun read() {
        submit(async = true) {
            val num = Database.getNumber(player) ?: return@submit
            number.set(num)
        }
    }

    fun save() {
        submit(async = true) {
            Database.setNumber(player, number.get())
        }
    }
}