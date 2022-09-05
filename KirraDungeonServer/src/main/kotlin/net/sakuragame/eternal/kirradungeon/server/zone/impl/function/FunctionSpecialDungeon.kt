package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
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
            dungeon.handleJoin(player, spawnBoss = false, spawnMob = false)
        }
    }

    private fun isDungeonFromSpecial(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.SPECIAL
    }
}