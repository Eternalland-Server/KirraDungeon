package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import org.bukkit.Bukkit
import org.bukkit.entity.Monster
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
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
            val dungeon = FunctionDungeon.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
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
        val dungeon = FunctionDungeon.getByMobUUID(entity.uniqueId) as? DefaultDungeon ?: return
        dungeon.removeMonsterUUID(entity.uniqueId)
        if (dungeon.canClear()) {
            // 当副本可通关时, 执行通关操作.
            dungeon.clear()
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val dungeon = FunctionDungeon.getByMobUUID(e.entity.uniqueId) as? DefaultDungeon ?: return
        if (dungeon.bossUUID == e.entity.uniqueId) {
            dungeon.updateBossBar()
        }
    }

    @SubscribeEvent
    fun e1(e: EntityDamageEvent) {
        val entity = e.entity as? Monster ?: return
        if (FunctionDungeon.getByMobUUID(entity.uniqueId) == null) {
            return
        }
        if (e.cause == EntityDamageEvent.DamageCause.VOID) {
            e.isCancelled = true
            val str = entity.getMetadata("ORIGIN_LOC").getOrNull(0)?.asString() ?: return
            val zoneLoc = ZoneLocation.parseToZoneLocation(str) ?: return
            val bukkitLoc = zoneLoc.toBukkitLocation(entity.world)
            entity.teleport(bukkitLoc)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val player = e.player
        val dungeon = FunctionDungeon.getByPlayer(player.uniqueId) as? DefaultDungeon ?: return
        val block = e.clickedBlock
        val trigger = dungeon.trigger ?: return
        if (block.location == trigger.triggerLoc.toBukkitLocation(block.world)) {
            val future = dungeon.handleTrigger() ?: return
        }
    }

    private fun isDungeonFromDefault(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.DEFAULT
    }
}