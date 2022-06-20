package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialDungeon
import org.bukkit.Bukkit
import org.bukkit.Effect
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object FunctionSpecialDungeon {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3L) {
            val player = e.player
            val profile = player.profile() ?: return@submit
            val dungeonWorld = e.dungeonWorld
            if (Zone.editingDungeonWorld != null) {
                return@submit
            }
            if (!isDungeonFromSpecial(e.dungeonWorld.worldIdentifier)) {
                return@submit
            }
            val dungeon = FunctionDungeon.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.SPECIAL
            profile.zoneUUID = dungeon.uuid
            dungeon.addPlayerUUID(player.uniqueId)
            dungeon.handleJoin(player, spawnBoss = false, spawnMob = true)
        }
    }

    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val mobType = e.mobType
        val entity = e.entity
        val dungeon = FunctionDungeon.getByMobUUID(entity.uniqueId) as? SpecialDungeon ?: return
        dungeon.removeMonsterUUID(entity.uniqueId)
        val loc = dungeon.zone.data.monsterData.mobList.map { it.loc.toBukkitLocation(entity.world) }.random()
        val resurgenceTime = dungeon.zone.data.resurgenceTime
        // 无脑刷类型, 怪物死亡之后让它在数秒后重生.
        submit(delay = resurgenceTime * 20L) {
            if (Bukkit.getWorlds().none { it.uid == loc.world.uid }) {
                return@submit
            }
            loc.world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 5)
            val mob = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(mobType, loc, 1)
            dungeon.addMonsterUUID(mob.uniqueId)
        }
    }

    private fun isDungeonFromSpecial(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.SPECIAL
    }
}