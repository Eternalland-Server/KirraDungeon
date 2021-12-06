package net.sakuragame.eternal.kirradungeon.plot.function

import com.sakuragame.eternal.justattribute.api.event.JARoleAttackEvent
import net.luckperms.api.node.types.MetaNode
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.plot.KirraDungeonPlot
import net.sakuragame.eternal.kirradungeon.plot.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.plot.function.FunctionPlot.playDome
import net.sakuragame.eternal.kirradungeon.plot.getMobMaxHealth
import net.sakuragame.eternal.kirradungeon.plot.sendBrokenTitleAnimation
import net.sakuragame.eternal.script.api.NergiganteAPI
import net.sakuragame.eternal.script.api.event.NSConversationEndEvent
import net.sakuragame.eternal.script.api.event.NSConversationOptionEvent
import net.sakuragame.eternal.script.api.event.NSFilmEndEvent
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored

/**
 * KirraPlotZone
 * net.sakuragame.eternal.kirraplotzone.function.FunctionListener
 *
 * @author kirraObj
 * @since 2021/11/30 19:00
 */
@Suppress("SpellCheckingInspection")
object FunctionListener {

    private val firstStepNode by lazy {
        MetaNode
            .builder()
            .key("noobie_tutorial_step_1")
            .value(true)
            .build()
    }

    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) {
        submit(delay = 3L) {
            doJoinTask(e.player, e.dungeonWorld)
        }
    }

    @SubscribeEvent
    fun e(e: EntityCombustEvent) {
        if (e.entity.name == "&c&l灭尽龙".colored()) {
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
        submit(delay = 10L) {
            val player = e.player
            KirraDungeonPlot.luckPermsAPI.userManager.getUser(player.uniqueId)!!.also { user ->
                user.data().clear { data -> data.key == "noobie_tutorial_step_1" }
                user.data().add(firstStepNode)
                KirraDungeonPlot.luckPermsAPI.userManager.saveUser(user)
            }
            KirraCoreBukkitAPI.teleportToSpawnServer(player)
        }
    }

    @SubscribeEvent
    fun e(e: NSConversationEndEvent) {
        val player = e.player
        when (e.convID) {
            0 -> {
                FunctionPlot.endBound(player)
                FunctionPlot.dataRecycle(player)
                FunctionPlot.spawnEntity(player, "nergigante_dragon")
                FunctionPlot.showJoinHud(player, "&c&l战胜灭尽龙")
                player.gameMode = GameMode.ADVENTURE
            }
            1 -> {
                FunctionPlot.endBound(player)
                FunctionPlot.dataRecycle(player)
                FunctionPlot.spawnEntity(player, "nergigante_dragon").also {
                    it.health = (getMobMaxHealth("nergigante_dragon") / 2)
                }
            }
            2 -> {
                submit(delay = 10) {
                    player.setMetadata("NergiganteClear", FixedMetadataValue(KirraDungeonPlot.plugin, ""))
                    player.resetTitle()
                    player.removePotionEffect(PotionEffectType.BLINDNESS)
                    player.removePotionEffect(PotionEffectType.CONFUSION)
                    NergiganteAPI.startEnd(e.player)
                    PacketSender.sendStopSound(player, FunctionPlot.battleThemeBgmId)
                }
            }
        }
    }

    @SubscribeEvent
    fun e(e: JARoleAttackEvent) {
        val entity = e.victim ?: return
        val entityHalfMaxHealth = getMobMaxHealth(entity) / 2
        val player = e.player
        if (entity.name != "&c&l灭尽龙".colored()) return
        if (!player.hasMetadata("NergiganteHalfHealth") && entity.health < entityHalfMaxHealth) {
            player.setMetadata("NergiganteHalfHealth", FixedMetadataValue(KirraDungeonPlot.plugin, ""))
            playDome(player)
            submit(delay = 30) {
                NergiganteAPI.startConversation(player, 1)
            }
            return
        }
        if (player.hasMetadata("NergiganteHalfHealth") && e.damage >= entity.health) {
            e.isCancelled = true
            playDome(player)
            submit(delay = 30) {
                NergiganteAPI.startConversation(player, 2)
            }
            return
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
        player.profile().dungeonWorld = dungeonWorld
        FunctionPlot.start(player)
    }
}