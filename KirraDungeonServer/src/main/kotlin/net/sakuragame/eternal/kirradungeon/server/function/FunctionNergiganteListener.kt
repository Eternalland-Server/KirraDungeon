package net.sakuragame.eternal.kirradungeon.server.function

import com.sakuragame.eternal.justattribute.api.event.JARoleAttackEvent
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.getMobMaxHealth
import net.sakuragame.eternal.kirradungeon.server.sendBrokenTitleAnimation
import net.sakuragame.eternal.kirradungeon.server.zone.PlayerZone
import net.sakuragame.eternal.kirrazones.server.compat.NergiganteScriptCompat
import net.sakuragame.eternal.script.api.NergiganteAPI
import net.sakuragame.eternal.script.api.event.NSConversationEndEvent
import net.sakuragame.eternal.script.api.event.NSConversationOptionEvent
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored

/**
 * KirraZones
 * net.sakuragame.eternal.kirradungeon.server.function.FunctionNergiganteListener
 *
 * @author kirraObj
 * @since 2021/11/28 18:14
 */
@Suppress("SpellCheckingInspection")
object FunctionNergiganteListener {

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        val player = e.player
        if (NergiganteScriptCompat.uuidToEntityList.containsKey(player.uniqueId)) {
            NergiganteScriptCompat.dataRecycle(player, true)
            player.removeMetadata("NergiganteHalfHealth", KirraDungeonServer.plugin)
            player.removeMetadata("NergiganteClear", KirraDungeonServer.plugin)
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
    fun e(e: NSConversationEndEvent) {
        val player = e.player
        val playerZone = PlayerZone.getByPlayer(player.uniqueId) ?: return
        when (e.convID) {
            0 -> {
                NergiganteScriptCompat.endBound(player)
                NergiganteScriptCompat.dataRecycle(player)
                NergiganteScriptCompat.spawnEntity(player, playerZone, "nergigante_dragon")
                playerZone.showJoinHud(player, "&c&l战胜灭尽龙")
                player.gameMode = GameMode.ADVENTURE
            }
            1 -> {
                NergiganteScriptCompat.endBound(player)
                NergiganteScriptCompat.dataRecycle(player)
                NergiganteScriptCompat.spawnEntity(player, playerZone, "nergigante_dragon").also {
                    it.health = (getMobMaxHealth("nergigante_dragon") / 2)
                }
            }
            2 -> {
                submit(delay = 10) {
                    player.setMetadata("NergiganteClear", FixedMetadataValue(KirraDungeonServer.plugin, ""))
                    player.sendTitle("", "", 0, 10, 0)
                    player.removePotionEffect(PotionEffectType.BLINDNESS)
                    player.removePotionEffect(PotionEffectType.CONFUSION)
                    NergiganteAPI.startEnd(e.player)
                    PacketSender.sendStopSound(player, NergiganteScriptCompat.battleThemeBgmId)
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
            player.setMetadata("NergiganteHalfHealth", FixedMetadataValue(KirraDungeonServer.plugin, ""))
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

    private fun playDome(player: Player) {
        val playerZone = PlayerZone.getByPlayer(player.uniqueId)!!
        NergiganteScriptCompat.dataRecycle(player)
        NergiganteScriptCompat.forceBoundToEntity(player, NergiganteScriptCompat.getSpawnLoc(player))
        NergiganteScriptCompat.spawnEntity(player, playerZone, "nergigante_dragon_dome")
    }
}