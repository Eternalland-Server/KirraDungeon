package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.WaveZone
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object FunctionWaveZone {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3) {
            val player = e.player
            val profile = player.profile()
            val dungeonWorld = e.dungeonWorld
            if (Zone.editingDungeonWorld != null) {
                return@submit
            }
            if (!isDungeonFromWave(e.dungeonWorld.worldIdentifier)) {
                return@submit
            }
            val waveZone = WaveZone.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.WAVE
            profile.zoneUUID = waveZone.uuid
            waveZone.addPlayerUUID(player.uniqueId)
            waveZone.handleJoin(player, spawnBoss = false, spawnMob = false)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: MythicMobDeathEvent) {
        val entity = e.entity
        val waveZone = WaveZone.getByMobUUID(entity.uniqueId) ?: return
        e.drops.clear()
        waveZone.removeMonsterUUID(entity.uniqueId)
        submit(delay = 3) {
            waveZone.handleMonsterRemove()
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val playerZone = WaveZone.getByMobUUID(e.entity.uniqueId) ?: return
        if (playerZone.bossUUID == e.entity.uniqueId) {
            playerZone.updateBossBar()
        }
    }

    private fun isDungeonFromWave(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.WAVE
    }
}