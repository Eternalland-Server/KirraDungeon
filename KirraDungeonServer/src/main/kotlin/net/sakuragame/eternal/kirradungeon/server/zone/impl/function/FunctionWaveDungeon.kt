package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.WaveDungeon
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object FunctionWaveDungeon {

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
            val dungeon = FunctionDungeon.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.WAVE
            profile.zoneUUID = dungeon.uuid
            dungeon.addPlayerUUID(player.uniqueId)
            dungeon.handleJoin(player, spawnBoss = false, spawnMob = false)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: MythicMobDeathEvent) {
        val entity = e.entity
        val waveZone = FunctionDungeon.getByMobUUID(entity.uniqueId) as? WaveDungeon ?: return
        e.drops.clear()
        waveZone.removeMonsterUUID(entity.uniqueId)
        submit(delay = 3) {
            waveZone.handleMonsterRemove()
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val waveDungeon = FunctionDungeon.getByMobUUID(e.entity.uniqueId) as? WaveDungeon ?: return
        if (waveDungeon.bossUUID == e.entity.uniqueId) {
            waveDungeon.updateBossBar()
        }
    }

    private fun isDungeonFromWave(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.WAVE
    }
}