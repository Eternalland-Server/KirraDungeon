package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialZone
import org.bukkit.Bukkit
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object FunctionSpecialZone {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3L) {
            val player = e.player
            val profile = player.profile()
            val dungeonWorld = e.dungeonWorld
            if (Zone.editingDungeonWorld != null) {
                return@submit
            }
            if (!isDungeonFromSpecial(e.dungeonWorld.worldIdentifier)) {
                return@submit
            }
            val specialZone = SpecialZone.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.SPECIAL
            profile.zoneUUID = specialZone.uuid
            specialZone.addPlayerUUID(player.uniqueId)
            specialZone.handleJoin(player, spawnBoss = false, spawnMob = true)
        }
    }

    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val mobType = e.mobType
        val entity = e.entity
        val specialZone = SpecialZone.getByMobUUID(entity.uniqueId) ?: return
        specialZone.removeMonsterUUID(entity.uniqueId)
        val loc = specialZone.zone.data.monsterData.mobList.map { it.loc.toBukkitLocation(entity.world) }.random()
        val resurgenceTime = specialZone.zone.data.resurgenceTime
        submit(delay = resurgenceTime * 20L) {
            if (Bukkit.getWorlds().none { it.uid == loc.world.uid }) {
                return@submit
            }
            val mob = mobType.spawn(BukkitAdapter.adapt(loc), 1.0)
            specialZone.addMonsterUUID(mob.uniqueId)
        }
    }

    private fun isDungeonFromSpecial(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.SPECIAL
    }
}