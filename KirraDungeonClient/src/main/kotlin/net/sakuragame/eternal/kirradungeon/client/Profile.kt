package net.sakuragame.eternal.kirradungeon.client

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.expansion.getDataContainer
import taboolib.expansion.releaseDataContainer
import taboolib.expansion.setupDataContainer

/**
 * KirraDungeon
 * net.sakuragame.eternal.kirradungeon.client.Profile
 *
 * @author kirraObj
 * @since 2022/1/6 22:01
 */
class Profile(val player: Player) {

    private val uuidStr: String
        get() = player.uniqueId.toString()

    private val dataContainer by lazy {
        player.setupDataContainer()
        player.getDataContainer()
    }

    var number = 1
        set(value) {
            field = value
            save()
        }

    var fatigue = 300

    var fatigueMillis = System.currentTimeMillis()

    var debugMode = false

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        @Schedule(async = true, period = 20 * 60 * 5)
        fun s() {
            profiles.values.forEach {
                it.save()
            }
        }

        @Schedule(async = true, period = 20 * 60 * 30)
        fun refreshFatigue() {
            profiles.values.forEach {
                it.plusFatigue()
            }
        }

        fun Player.profile() = profiles.values.firstOrNull { it.player.uniqueId == uniqueId }

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
            player.profile()?.apply {
                save()
                drop()
            }
        }
    }

    fun read() {
        submit(async = true) {
            dataContainer.database.apply {
                number = get(uuidStr, "number")?.toIntOrNull() ?: number
                fatigue = get(uuidStr, "fatigue")?.toIntOrNull() ?: fatigue
                fatigueMillis = get(uuidStr, "fatigue_millis")?.toLongOrNull() ?: fatigueMillis
                plusFatigue()
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun plusFatigue() {
        val offset = (System.currentTimeMillis() - fatigueMillis).toInt() / 1000
        if (fatigue + offset > KirraDungeonClientAPI.fatigueMaxValue) {
            fatigue = KirraDungeonClientAPI.fatigueMaxValue
        } else {
            fatigue += KirraDungeonClientAPI.getRecoverFatigueBySeconds(offset)
        }
        fatigueMillis = System.currentTimeMillis()
    }

    fun save() {
        submit(async = true) {
            dataContainer.database.apply {
                set(uuidStr, "number", "$number")
                set(uuidStr, "fatigue", "$fatigue")
                set(uuidStr, "fatigue_millis", "$fatigueMillis")
            }
        }
    }

    fun drop() {
        player.releaseDataContainer()
        profiles.remove(player.name)
    }
}