package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenOpenEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultZone
import org.bukkit.event.entity.EntityDamageEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object FunctionDefaultZone {

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
            val defaultZone = DefaultZone.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.DEFAULT
            profile.zoneUUID = defaultZone.uuid
            defaultZone.addPlayerUUID(player.uniqueId)
            defaultZone.handleJoin(player, spawnBoss = true, spawnMob = true)
        }
    }

    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val entity = e.entity
        val playerZone = DefaultZone.getByMobUUID(entity.uniqueId) ?: return
        playerZone.removeMonsterUUID(entity.uniqueId)
        if (playerZone.canClear()) {
            // 当副本可通关时, 执行通关操作.
            playerZone.clear()
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val playerZone = DefaultZone.getByMobUUID(e.entity.uniqueId) ?: return
        if (playerZone.bossUUID == e.entity.uniqueId) {
            playerZone.updateBossBar()
        }
    }

    // 血条初始化.
    @SubscribeEvent
    fun e(e: UIFScreenOpenEvent) {
        val player = e.player
        val screenId = e.screenID
        val playerZone = DefaultZone.getByPlayer(player.uniqueId) ?: return
        if (screenId != BossBar.screenID) return
        submit(delay = 20L) {
            playerZone.updateBossBar(init = true)
        }
    }

    private fun isDungeonFromDefault(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.DEFAULT
    }
}