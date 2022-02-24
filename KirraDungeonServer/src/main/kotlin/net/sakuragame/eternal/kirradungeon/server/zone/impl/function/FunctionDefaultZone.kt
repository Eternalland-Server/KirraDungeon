package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenOpenEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.playDeathAnimation
import net.sakuragame.eternal.kirradungeon.server.turnToSpectator
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultZone
import org.bukkit.GameMode
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored

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

    // 玩家死亡判断.
    @SubscribeEvent
    fun e(e: PlayerDeathEvent) {
        if (!e.entity.profile().isChallenging) return
        val playerZone = DefaultZone.getByPlayer(e.entity.uniqueId) ?: return
        e.entity.apply {
            playDeathAnimation()
            health = maxHealth
            turnToSpectator()
            sendTitle("&c&l菜".colored(), "", 0, 40, 10)
        }
        submit(async = false, delay = 10L) {
            if (playerZone.isAllPlayersDead() && playerZone.failThread == null) {
                playerZone.startFailThread()
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        if (!e.player.profile().isChallenging) return
        if (e.player.gameMode != GameMode.CREATIVE) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val playerZone = DefaultZone.getByMobUUID(e.entity.uniqueId) ?: return
        if (playerZone.bossUUID == e.entity.uniqueId) {
            playerZone.updateBossBar()
        }
    }

    // 副本怪物死亡判断.
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

    // 血条初始化.
    @SubscribeEvent
    fun e(e: UIFScreenOpenEvent) {
        val player = e.player
        val screenId = e.screenID
        val playerZone = DefaultZone.getByPlayer(player.uniqueId) ?: return
        if (screenId != BossBar.screenID) return
        playerZone.updateBossBar(playerZone.zone.data.iconNumber.toString(), init = true)
    }

    private fun isDungeonFromDefault(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.DEFAULT
    }
}