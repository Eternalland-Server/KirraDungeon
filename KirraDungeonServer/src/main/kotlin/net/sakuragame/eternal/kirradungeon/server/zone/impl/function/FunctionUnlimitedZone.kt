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
import org.bukkit.entity.Player
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
            val defaultZone = UnlimitedZone.getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            profile.zoneType = ZoneType.DEFAULT
            profile.zoneUUID = defaultZone.uuid
            defaultZone.addPlayerUUID(player.uniqueId)
            defaultZone.handleJoin(player, spawnBoss = true, spawnMob = false)
        }
    }

    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val mobType = e.mobType
        val entity = e.entity
        val unlimitedZone = UnlimitedZone.getByMobUUID(entity.uniqueId) ?: return
        unlimitedZone.removeMonsterUUID(entity.uniqueId)
        val player = unlimitedZone.getSinglePlayer()
    }

    // 血条初始化.
    @SubscribeEvent
    fun e(e: UIFScreenOpenEvent) {
        val player = e.player
        val screenId = e.screenID
        val unlimitedZone = UnlimitedZone.getByPlayer(player.uniqueId) ?: return
        if (screenId != BossBar.screenID) return
        unlimitedZone.updateBossBar(unlimitedZone.zone.data.iconNumber.toString(), init = true)
    }

    @Suppress("SpellCheckingInspection")
    private fun doSuccToNextFloor(player: Player, unlimitedZone: UnlimitedZone) {
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 9999999, 1))
        player.sendLang("")
    }

    private fun isDungeonFromUnlimited(name: String): Boolean {
        return Zone.getByID(name)?.data?.type == ZoneType.UNLIMITED
    }
}