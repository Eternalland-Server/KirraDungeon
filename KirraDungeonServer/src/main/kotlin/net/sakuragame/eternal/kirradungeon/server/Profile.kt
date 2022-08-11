package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority.HIGHEST
import taboolib.common.platform.event.EventPriority.LOWEST
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.expansion.getDataContainer
import taboolib.expansion.releaseDataContainer
import taboolib.expansion.setupDataContainer
import java.util.*

class Profile(val player: Player) {

    private val uuidStr: String
        get() = player.uniqueId.toString()

    private val dataContainer by lazy {
        player.setupDataContainer()
        player.getDataContainer()
    }

    val isEditing: Boolean
        get() {
            val editingWorld = Zone.editingDungeonWorld ?: return false
            return editingWorld.bukkitWorld.players.find { it.uniqueId == player.uniqueId } != null
        }

    var number = 1
        set(value) {
            field = value
            save()
        }

    var isChallenging = false

    var isQuitting = false

    lateinit var zoneType: ZoneType
    lateinit var zoneUUID: UUID

    companion object {

        private val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.firstOrNull { it.player.uniqueId == uniqueId }

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
            player.profile()?.apply {
                save()
                drop()
            }
        }
    }

    fun drop() {
        if (isChallenging) {
            submit(delay = 3, async = true) {
                val dungeon = getIDungeon() ?: return@submit
                dungeon.playerUUIDList.remove(player.uniqueId)
                if (dungeon.canDel()) {
                    dungeon.del()
                }
            }
            BossBar.close(player)
        }
        player.releaseDataContainer()
        profiles.remove(player.uniqueId.toString())
    }

    fun read() {
        submit(async = true) {
            dataContainer.database.apply {
                number = get(uuidStr, "number")?.toIntOrNull() ?: number
            }
        }
    }

    fun save() {
        submit(async = true) {
            dataContainer.database.apply {
                set(uuidStr, "number", "$number")
            }
            if (player.hasPermission("admin") && isEditing) {
                player.performCommand("dungeon save")
            }
        }
    }

    fun getIDungeon() = KirraDungeonServerAPI.getDungeonByPlayer(player)
}