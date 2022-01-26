package net.sakuragame.eternal.kirradungeon.client

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * KirraDungeon
 * net.sakuragame.eternal.kirradungeon.client.Profile
 *
 * @author kirraObj
 * @since 2022/1/6 22:01
 */
class Profile(val player: Player) {

    val number = AtomicInteger(1)

    val debugMode = AtomicBoolean(false)

    val currentDungeonCategory = AtomicInteger(1)

    val currentDungeonScreen = AtomicInteger(1)

    val currentSubScreen = AtomicInteger(1)

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.first { it.player.uniqueId == uniqueId }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun e(e: PlayerJoinEvent) {
            profiles[e.player.name] = Profile(e.player).apply {
                read()
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        fun e(e: PlayerKickEvent) {
            dataRecycle(e.player)
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
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

    fun read() {
        submit(async = true, delay = 40L) {
            val num = Database.getNumber(player) ?: return@submit
            number.set(num)
        }
    }

    fun save() {
        submit(async = true) {
            Database.setNumber(player, number.get())
        }
    }

    fun drop() {
        profiles.remove(player.name)
    }
}