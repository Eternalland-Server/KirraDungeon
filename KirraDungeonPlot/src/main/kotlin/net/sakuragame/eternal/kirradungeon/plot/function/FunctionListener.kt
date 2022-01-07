package net.sakuragame.eternal.kirradungeon.plot.function

import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.plot.KirraDungeonPlot
import net.sakuragame.eternal.kirradungeon.plot.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.plot.addNoobiePoints
import net.sakuragame.eternal.kirradungeon.plot.function.FunctionPlot.playDome
import net.sakuragame.eternal.kirradungeon.plot.getMobMaxHealth
import net.sakuragame.eternal.kirradungeon.plot.sendBrokenTitleAnimation
import net.sakuragame.eternal.script.api.NergiganteAPI
import net.sakuragame.eternal.script.api.event.NSConversationEndEvent
import net.sakuragame.eternal.script.api.event.NSConversationOptionEvent
import net.sakuragame.eternal.script.api.event.NSFilmEndEvent
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * KirraPlotZone
 * net.sakuragame.eternal.kirraplotzone.function.FunctionListener
 *
 * @author kirraObj
 * @since 2021/11/30 19:00
 */
@Suppress("SpellCheckingInspection")
object FunctionListener {

    val countDownMap = ConcurrentHashMap<UUID, Int>()

    @Awake(LifeCycle.ACTIVE)
    fun init() {
        submit(async = true, period = 20) {
            if (countDownMap.isEmpty()) {
                return@submit
            }
            countDownMap.forEach { (uuid, int) ->
                val player = Bukkit.getPlayer(uuid)
                if (player == null) {
                    countDownMap.remove(uuid)
                    return@forEach
                }
                if (player.profile()?.dungeonWorld == null) {
                    return@forEach
                }
                BossBar.setTime(player, int - 1)
                countDownMap[uuid] = int - 1
            }
        }
    }

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3L) {
            doJoinTask(e.player, e.dungeonWorld)
        }
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val player = e.player
        submit(delay = 10L) {
            if (player.profile()?.dungeonWorld != null) {
                BossBar.open(player, "&6&l位面吞噬者", "", "34", 1.0, 900)
                countDownMap[player.uniqueId] = 900
            }
        }
    }

    @SubscribeEvent
    fun e(e: EntityCombustEvent) {
        if (e.entity.name == "&6&l位面吞噬者".colored()) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: NSConversationOptionEvent) {
        val player = e.player
        if (e.convID == 2 && e.optionID == 6) {
            player.playSound(player.location, Sound.BLOCK_PORTAL_TRIGGER, 1f, 1.5f)
            player.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 99999999, 100))
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 99999999, 100))
            sendBrokenTitleAnimation(player)
        }
    }

    @SubscribeEvent
    fun e(e: NSFilmEndEvent) {
        val player = e.player
        player.addNoobiePoints(1)
        KirraCoreBukkitAPI.teleportToSpawnServer(player)
    }

    @SubscribeEvent
    fun e(e: NSConversationEndEvent) {
        val player = e.player
        when (e.convID) {
            0 -> {
                FunctionPlot.endBound(player)
                FunctionPlot.dataRecycle(player)
                FunctionPlot.spawnEntity(player, "dragon")
                FunctionPlot.showJoinHud(player, "&c&l战胜位面吞噬者")
            }
            1 -> {
                FunctionPlot.endBound(player)
                FunctionPlot.dataRecycle(player)
                FunctionPlot.spawnEntity(player, "dragon").also {
                    it?.health = (getMobMaxHealth("dragon") / 2)
                }
            }
            2 -> {
                submit(delay = 10) {
                    player.setMetadata("NergiganteClear", FixedMetadataValue(KirraDungeonPlot.plugin, ""))
                    player.removePotionEffect(PotionEffectType.BLINDNESS)
                    player.removePotionEffect(PotionEffectType.CONFUSION)
                    NergiganteAPI.startEnd(e.player)
                    player.resetTitle()
                    BossBar.close(player)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun e(e: EntityDamageByEntityEvent) {
        val entity = e.entity as? LivingEntity ?: return
        val player = e.damager as? Player ?: return
        val entityMaxHealth = getMobMaxHealth(entity)
        val entityHalfMaxHealth = entityMaxHealth / 2
        if (entity.name != "&6&l位面吞噬者".colored()) return
        if (player.hasMetadata("NergiganteHalfHealth") && e.damage + 1000 >= entity.health) {
            e.isCancelled = true
            playDome(player)
            submit(delay = 30) {
                PacketSender.sendStopSound(player, FunctionPlot.battleThemeBgmId)
                NergiganteAPI.startConversation(player, 2)
            }
            return
        }
        BossBar.setHealth(player, "&c&l${entity.health.roundToInt()} / $entityMaxHealth".colored(), entity.health / entityMaxHealth)
        if (!player.hasMetadata("NergiganteHalfHealth") && entity.health < entityHalfMaxHealth) {
            player.setMetadata("NergiganteHalfHealth", FixedMetadataValue(KirraDungeonPlot.plugin, ""))
            playDome(player)
            submit(delay = 30) {
                NergiganteAPI.startConversation(player, 1)
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerToggleSneakEvent) {
        if (e.player.spectatorTarget != null) e.isCancelled = true
    }

    @SubscribeEvent
    fun e(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.SPECTATOR) e.isCancelled = true
    }

    // 濒死保护机制.
    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        if (e.damage >= player.health) {
            e.isCancelled = true
        }
    }

    private fun doJoinTask(player: Player, dungeonWorld: DungeonWorld) {
        val profile = player.profile() ?: return
        profile.dungeonWorld = dungeonWorld
        FunctionPlot.start(player)
    }
}