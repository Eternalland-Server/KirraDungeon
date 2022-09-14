package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.MythicMobs
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justlevel.JustLevel
import net.sakuragame.eternal.justlevel.api.event.PropPickupEvent
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.WaveDungeon
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import kotlin.math.roundToInt

object FunctionWaveDungeon {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3) {
            val player = e.player
            val profile = player.profile() ?: return@submit
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

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val entity = e.entity as? LivingEntity ?: return
        if (FunctionDungeon.getByMobUUID(entity.uniqueId) !is WaveDungeon) {
            return
        }
        if (MythicMobs.inst().apiHelper.getMythicMobInstance(entity).type.internalName != "盗贼宝箱") {
            return
        }
        if (entity.hasMetadata("opening")) {
            e.isCancelled = true
            return
        }
        if (entity.health <= entity.maxHealth * 0.1) {
            entity.setMetadata("opening", FixedMetadataValue(KirraDungeonServer.plugin, ""))
            submit(delay = 20) {
                PacketSender.setModelEntityAnimation(entity, "open", 1)
            }
            submit(delay = 80) {
                entity.remove()
            }
        }
        JustLevel.getPropGenerate().itemSpray(entity.location, 2, (e.finalDamage / 3).roundToInt(), 3, 1.0)
    }

    @SubscribeEvent
    fun e(e: ItemMergeEvent) {
        val world = e.entity.world
        if (FunctionDungeon.getByBukkitWorldUUID(world.uid) !is WaveDungeon) {
            return
        }
        if (e.entity.name.contains("金币")) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PropPickupEvent) {
        val player = e.player
        val profile = player.profile() ?: return
        val dungeon = profile.getIDungeon() as? WaveDungeon ?: return
        if (e.type != 2) {
            return
        }
        val value = e.value
        dungeon.pickUpCoins += value
    }

    private fun isDungeonFromWave(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.WAVE
    }
}