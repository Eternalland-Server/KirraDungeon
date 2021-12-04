package net.sakuragame.eternal.kirradungeon.server.zone

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.kickPlayerByNotFoundData
import net.sakuragame.eternal.kirradungeon.server.playDeathAnimation
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang
import java.util.*

data class PlayerZone(val zone: Zone, val dungeonWorld: DungeonWorld) {

    val uuidList = mutableListOf<UUID>()

    val need2KillEntityList = mutableListOf<LivingEntity>()

    companion object {

        private val playerZones = mutableListOf<PlayerZone>()

        fun getByDungeonWorldUUID(uuid: UUID) = playerZones.firstOrNull { it.dungeonWorld.uuid == uuid }

        fun getByPlayer(playerUUID: UUID): PlayerZone? {
            playerZones.forEach { playerZone ->
                if (playerZone.uuidList.firstOrNull { it == playerUUID } != null) {
                    return playerZone
                }
            }
            return null
        }

        fun getByMobUUID(mobUUID: UUID): PlayerZone? {
            playerZones.forEach { playerZone ->
                if (playerZone.need2KillEntityList.firstOrNull { it.uniqueId == mobUUID } != null) {
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
                doJoinTask(e.player, e.dungeonWorld)
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
                it.sendTitle("&c&l菜", "", 0, 40, 10)
                it.sendLang("message-player-dead-dungeon", playerZone.zone.name)
            }
            // 以下开始执行一些神奇的操作... (例: 数秒后可复活, 复活币判断...)
            // 如果队伍全灭.
            if (playerZone.isAllDead()) {
                playerZone.allEntityRegen2MaxHealth()
            }
        }

        // 副本怪物死亡判断.
        @SubscribeEvent
        fun e(e: EntityDeathEvent) {
            val entity = e.entity
            val playerZone = getByMobUUID(entity.uniqueId) ?: return
            playerZone.removeEntity(entity)
            if (playerZone.canClear()) {
                // 当副本可通关时, 执行通关操作.
                playerZone.clear()
            }
        }

        private fun doJoinTask(player: Player, dungeonWorld: DungeonWorld) {
            // 如果在编辑模式, 则不进行操作.
            if (Zone.editingDungeonWorld != null) {
                return
            }
            val playerZone = getByDungeonWorldUUID(dungeonWorld.uuid) ?: kotlin.run {
                kickPlayerByNotFoundData(player)
                return
            }
            val zoneData = playerZone.zone.data
            val loc = zoneData.spawnLoc.toBukkitLocation(dungeonWorld.bukkitWorld)
            playerZone.uuidList += player.uniqueId
            player.profile().isChallenging = true
            player.teleport(loc)
            if (zoneData.isCustomSkyEnabled())
                zoneData.skyData!!.apply {
                    val skyChangerPlayer = SkyChanger.wrapPlayer(player)
                    KirraDungeonServer.skyAPI.changeSky(skyChangerPlayer, packetType, value)
                }
            playerZone.showJoinMessage(player)
        }
    }

    // 展示进入信息.
    fun showJoinMessage(player: Player) {
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
        DragonCoreCompat.updateDragonVars(player, zone.name)
        PacketSender.sendOpenHud(player, DragonCoreCompat.joinTitleHudID)
        player.sendLang("message-player-join-dungeon", zone.name)
    }

    // 移除一名玩家的 UUID.
    fun removePlayerUUID(uuid: UUID) = uuidList.remove(uuid)

    // 队伍玩家全部死亡.
    fun isAllDead() = uuidList.map { Bukkit.getPlayer(it) }.firstOrNull { it?.gameMode != GameMode.SPECTATOR } == null

    // 使目前所有的怪物都恢复至满血.
    fun allEntityRegen2MaxHealth() {
        need2KillEntityList.forEach {
            it.health = it.maxHealth
        }
    }

    // 移除怪物.
    fun removeEntity(entity: Entity) = need2KillEntityList.removeIf { it.uniqueId == entity.uniqueId }

    // 是否可以通关.
    fun canClear() = need2KillEntityList.isEmpty()

    // 执行通关操作.
    fun clear() {
        val delay2BackSpawnServer = KirraDungeonServer.conf.getLong("delay-back-spawn-server-secs")
        uuidList.map { Bukkit.getPlayer(it) }.forEach {
            if (it == null) return@forEach
            it.sendLang("message-player-clear-dungeon", delay2BackSpawnServer)
        }
        submit(delay = delay2BackSpawnServer * 20, async = true) {
            KirraCoreBukkitAPI.teleportToSpawnServer(*uuidList.toTypedArray())
        }
    }

    // 是否可以删除该副本. (无人游玩状态)
    fun canDelete() = uuidList.size <= 0

    // 删除副本.
    fun delete() {
        DungeonServerAPI.getWorldManager().dropDungeon(dungeonWorld)
        playerZones.remove(this)
    }

    init {
        // 生成怪物步骤.
        zone.data.entityMap.forEach { (zoneLoc, mobData) ->
            // 将 ZoneLocation 转换为 BukkitLocation.
            val bukkitLoc = zoneLoc.toBukkitLocation(dungeonWorld.bukkitWorld)
            // 调用 MythicmobsAPI, 将怪物生成到世界坐标.
            repeat(mobData.amount) {
                need2KillEntityList.add(KirraDungeonServer.mythicmobsAPI.spawnMythicMob(mobData.mobType, bukkitLoc) as? LivingEntity ?: return@repeat)
            }
        }
        // 超时判断. (副本创建后长时间未进入)
        submit(async = true, delay = 1000) {
            if (Zone.editingDungeonWorld != null) return@submit
            if (canDelete()) delete()
        }
    }
}