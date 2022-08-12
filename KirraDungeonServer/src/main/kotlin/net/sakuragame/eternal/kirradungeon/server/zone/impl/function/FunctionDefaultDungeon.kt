package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import ink.ptms.zaphkiel.ZaphkielAPI
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.random

object FunctionDefaultDungeon {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3) {
            val player = e.player
            val profile = player.profile() ?: return@submit
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
            dungeon.handleJoin(player, spawnBoss = false, spawnMob = false, showTimeBar = false)
        }
    }

    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val entity = e.entity
        val dungeon = FunctionDungeon.getByMobUUID(entity.uniqueId) as? DefaultDungeon ?: return
        dungeon.removeMonsterUUID(entity.uniqueId)
        if (dungeon.getMonsters(containsBoss = true).isEmpty()) {
            when {
                dungeon.mobs.isEmpty() && !dungeon.naturalSpawnBoss -> return
                dungeon.mobs.size > 1 -> dungeon.doSpawn()
                else -> repeat(2) {
                    dungeon.doSpawn()
                }
            }
        }
        e.drops.clear()
        val drops = dungeon.zone.data.monsterDropData[e.mobType.internalName] ?: return
        drops.forEach {
            val random = random(0.0, 1.0)
            val dropAmount = it.dropRange.random()
            if (it.chance < random || dropAmount <= 0) {
                return@forEach
            }
            val item = ZaphkielAPI.getItemStack(it.itemId) ?: return@forEach
            item.amount = dropAmount
            entity.world.dropItemNaturally(entity.location, item)
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
    fun e(e: PlayerInteractEvent) {
        val player = e.player
        val dungeon = FunctionDungeon.getByPlayer(player.uniqueId) as? DefaultDungeon ?: return
        val block = e.clickedBlock ?: return
        val trigger = dungeon.triggerData ?: return
        if (block.type == Material.AIR || dungeon.triggered) {
            return
        }
        if (block.location == trigger.triggerLoc.toBukkitLocation(block.world)) {
            dungeon.triggered = true
            player.playSound(player.location, Sound.BLOCK_PISTON_CONTRACT, 1f, 1.5f)
            submit(delay = 5L) {
                player.playSound(player.location, Sound.BLOCK_PISTON_EXTEND, 1f, 1.5f)
            }
            submit(delay = 10L) {
                val delayTime = dungeon.handleTrigger() ?: return@submit
                submit(async = false, delay = delayTime) {
                    dungeon.doTrigger()
                }
            }
        }
    }

    private fun isDungeonFromDefault(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.DEFAULT
    }
}