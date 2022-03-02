package net.sakuragame.eternal.kirradungeon.server.zone.impl.function

import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenOpenEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.UnlimitedZone
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang

object FunctionUnlimitedZone {

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3) {
            val player = e.player
            val profile = player.profile()
            val dungeonWorld = e.dungeonWorld
            if (Zone.editingDungeonWorld != null) {
                return@submit
            }
            if (!isDungeonFromUnlimited(e.dungeonWorld.worldIdentifier)) {
                return@submit
            }
            val unlimitedZone = UnlimitedZone.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.UNLIMITED
            profile.zoneUUID = unlimitedZone.uuid
            unlimitedZone.addPlayerUUID(player.uniqueId)
            unlimitedZone.handleJoin(player, spawnBoss = true, spawnMob = false)
        }
    }

    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val entity = e.entity
        val unlimitedZone = UnlimitedZone.getByMobUUID(entity.uniqueId) ?: return
        unlimitedZone.removeMonsterUUID(entity.uniqueId)
        // 爬塔, 在怪物首领死后执行下一层相关操作.
        doSuccToNextFloor(unlimitedZone)
    }

    // 血条初始化.
    @SubscribeEvent
    fun e(e: UIFScreenOpenEvent) {
        val player = e.player
        val screenId = e.screenID
        val unlimitedZone = UnlimitedZone.getByPlayer(player.uniqueId) ?: return
        if (screenId != BossBar.screenID) return
        submit(delay = 20L) {
            unlimitedZone.updateBossBar(init = true)
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val playerZone = UnlimitedZone.getByMobUUID(e.entity.uniqueId) ?: return
        if (playerZone.bossUUID == e.entity.uniqueId) {
            playerZone.updateBossBar()
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun doSuccToNextFloor(unlimitedZone: UnlimitedZone) {
        unlimitedZone.getPlayers().forEach {
            unlimitedZone.floorPlus1()
            val currentFloor = unlimitedZone.currentFloor
            it.sendLang("message-player-succ-to-next-floor", currentFloor)
            submit(async = false, delay = 100) {
                it.teleport(unlimitedZone.zone.data.spawnLoc.toBukkitLocation(it.world))
                it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 9999999, 10))
                it.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 9999999, 10))
            }
            submit(async = false, delay = 160) {
                it.removePotionEffect(PotionEffectType.BLINDNESS)
                it.removePotionEffect(PotionEffectType.SLOW)
                unlimitedZone.spawnEntities(spawnBoss = true, spawnEntity = false, currentFloor)
                unlimitedZone.updateBossBar()
                it.sendLang("message-player-start-to-fight")
            }
        }
    }

    private fun isDungeonFromUnlimited(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.UNLIMITED
    }
}