package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.DungeonManager
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object FunctionDefaultDungeon {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3) {
            val player = e.player
            val profile = player.profile()
            val dungeonWorld = e.dungeonWorld
            if (Zone.editingDungeonWorld != null) {
                return@submit
            }
            if (!isDungeonFromDefault(e.dungeonWorld.worldIdentifier)) {
                return@submit
            }
            val dungeon = DungeonManager.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.DEFAULT
            profile.zoneUUID = dungeon.uuid
            dungeon.addPlayerUUID(player.uniqueId)
            dungeon.handleJoin(player, spawnBoss = true, spawnMob = true)
        }
    }

    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val entity = e.entity
        val dungeon = DungeonManager.getByMobUUID(entity.uniqueId) ?: return
        dungeon.removeMonsterUUID(entity.uniqueId)
        if (dungeon.canClear()) {
            // 当副本可通关时, 执行通关操作.
            dungeon.clear()
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val playerZone = DungeonManager.getByMobUUID(e.entity.uniqueId) ?: return
        if (playerZone.bossUUID == e.entity.uniqueId) {
            playerZone.updateBossBar()
        }
    }

    private fun isDungeonFromDefault(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.DEFAULT
    }
}