package net.sakuragame.eternal.kirradungeon.plot

import io.netty.util.internal.ConcurrentSet
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority.HIGHEST
import taboolib.common.platform.event.EventPriority.LOWEST
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

/**
 * KirraPlotZone
 * net.sakuragame.eternal.kirraplotzone.Profile
 *
 * @author kirraObj
 * @since 2021/11/30 19:09
 */
@Suppress("SpellCheckingInspection")
class Profile(val player: Player) {

    lateinit var dungeonWorld: DungeonWorld

    val entityList = ConcurrentSet<LivingEntity>()

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.firstOrNull { it.player.uniqueId == uniqueId }

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
            submit(delay = 3L) {
                val profile = player.profile() ?: return@submit
                profile.drop()
            }
        }
    }

    fun removeAllEntities() {
        entityList.forEach {
            it.remove()
        }
        entityList.clear()
    }

    fun drop() {
        if (dungeonWorld != null) {
            DungeonServerAPI.getWorldManager().dropDungeon(dungeonWorld)
        }
        player.removeMetadata("NergiganteHalfHealth", KirraDungeonPlot.plugin)
        player.removeMetadata("NergiganteClear", KirraDungeonPlot.plugin)
        profiles.remove(player.name)
    }
}