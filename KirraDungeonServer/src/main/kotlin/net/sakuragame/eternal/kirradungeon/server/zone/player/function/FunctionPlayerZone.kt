package net.sakuragame.eternal.kirradungeon.server.zone.player.function

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.event.DungeonJoinEvent
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.playDeathAnimation
import net.sakuragame.eternal.kirradungeon.server.reset
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.player.FailType
import net.sakuragame.eternal.kirradungeon.server.zone.player.PlayerZone
import net.sakuragame.eternal.kirradungeon.server.zone.player.PlayerZone.Companion.playerZones
import org.bukkit.GameMode
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import java.util.*

object FunctionPlayerZone {

    fun getByDungeonWorldUUID(uuid: UUID) = playerZones.firstOrNull { it.dungeonWorld.uuid == uuid }

    fun getByPlayer(playerUUID: UUID): PlayerZone? {
        playerZones.forEach { playerZone ->
            if (playerZone.playerUUIDList.firstOrNull { it == playerUUID } != null) {
                return playerZone
            }
        }
        return null
    }

    fun getByMobUUID(mobUUID: UUID): PlayerZone? {
        playerZones.forEach { playerZone ->
            if (playerZone.monsterUUIDList.firstOrNull { it == mobUUID } != null) {
                return playerZone
            }
        }
        return null
    }

    fun create(zone: Zone, dungeonWorld: DungeonWorld) {
        playerZones += PlayerZone(zone, dungeonWorld)
    }

    // 玩家进入.
    @SubscribeEvent
    fun e(e: DungeonPlayerJoinEvent) =
        submit(delay = 3) {
            val player = e.player
            val dungeonWorld = e.dungeonWorld
            if (Zone.editingDungeonWorld != null) {
                return@submit
            }
            val playerZone = getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return@submit
            }
            val zone = playerZone.zone
            val zoneData = zone.data
            val loc = zoneData.spawnLoc.toBukkitLocation(dungeonWorld.bukkitWorld)
            playerZone.addPlayerUUID(player.uniqueId)
            player.teleport(loc)
            player.profile().isChallenging = true
            player.reset()
            if (zoneData.isCustomSkyEnabled()) {
                zoneData.zoneSkyData!!.apply {
                    val skyChangerPlayer = SkyChanger.wrapPlayer(player)
                    KirraDungeonServer.skyAPI.changeSky(skyChangerPlayer, packetType, value)
                }
            }
            submit(delay = 40) {
                playerZone.showJoinMessage(player, zone.name)
                playerZone.spawnEntities()
                playerZone.runTimer()
                DungeonJoinEvent(player, zone.id, playerZone).call()
            }
        }

    // 玩家死亡判断.
    @SubscribeEvent
    fun e(e: PlayerDeathEvent) {
        if (!e.entity.profile().isChallenging) return
        val playerZone = getByPlayer(e.entity.uniqueId) ?: return
        val player = e.entity.also {
            it.playDeathAnimation()
            it.health = it.maxHealth
            it.gameMode = GameMode.SPECTATOR
            it.sendTitle("&c&l菜".colored(), "", 0, 40, 10)
        }
        submit(async = false, delay = 10L) {
            if (playerZone.isAllPlayersDead()) {
                playerZone.fail(FailType.ALL_DIED)
            }
        }
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        if (!e.player.profile().isChallenging) return
        if (e.player.gameMode != GameMode.CREATIVE)
            e.isCancelled = true
    }

    @SubscribeEvent
    fun e(e: EntityDamageEvent) {
        val playerZone = PlayerZone.getByMobUUID(e.entity.uniqueId) ?: return
        if (playerZone.bossUUID == e.entity.uniqueId) {
            playerZone.updateBossBar()
        }
    }

    // 副本怪物死亡判断.
    @SubscribeEvent
    fun e(e: MythicMobDeathEvent) {
        val entity = e.entity
        val playerZone = getByMobUUID(entity.uniqueId) ?: return
        playerZone.removeMonsterUUID(entity.uniqueId)
        if (playerZone.canClear()) {
            // 当副本可通关时, 执行通关操作.
            playerZone.clear()
        }
    }

    @SubscribeEvent
    fun e(e: YamlSendFinishedEvent) {
        val player = e.player
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
        submit(delay = 60L) {
            val playerZone = PlayerZone.getByPlayer(player.uniqueId) ?: return@submit
            playerZone.updateBossBar(playerZone.zone.data.iconNumber.toString(), init = true)
        }
    }
}