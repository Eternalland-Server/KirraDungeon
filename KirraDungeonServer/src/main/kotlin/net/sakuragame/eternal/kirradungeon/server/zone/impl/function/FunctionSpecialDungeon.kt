package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.DungeonManager
import org.bukkit.Bukkit
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object FunctionSpecialDungeon {

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
            val specialZone = DungeonManager.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
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
        val dungeon = DungeonManager.getByMobUUID(entity.uniqueId) ?: return
        dungeon.removeMonsterUUID(entity.uniqueId)
        val loc = dungeon.zone.data.monsterData.mobList.map { it.loc.toBukkitLocation(entity.world) }.random()
        val resurgenceTime = dungeon.zone.data.resurgenceTime
        // 无脑刷类型, 怪物死亡之后让它在数秒后重生.
        submit(delay = resurgenceTime * 20L) {
            if (Bukkit.getWorlds().none { it.uid == loc.world.uid }) {
                return@submit
            }
            val mob = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(mobType, loc, 1)
            dungeon.addMonsterUUID(mob.uniqueId)
        }
    }

    private fun isDungeonFromSpecial(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.SPECIAL
    }
}