package net.sakuragame.eternal.kirradungeon.server.zone

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.event.DungeonPlayerJoinEvent
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.api.event.YamlSendFinishedEvent
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.*
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.event.DungeonJoinEvent
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class PlayerZone(val zone: Zone, val dungeonWorld: DungeonWorld) {

    var isClear = false

    val createdTime = System.currentTimeMillis()

    val uuidList = mutableListOf<UUID>()
    val need2KillEntityUUIDList = mutableListOf<UUID>()

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
                if (playerZone.need2KillEntityUUIDList.firstOrNull { it == mobUUID } != null) {
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
        fun e(e: MythicMobDeathEvent) {
            val entity = e.entity
            val playerZone = getByMobUUID(entity.uniqueId) ?: return
            playerZone.removeEntity(entity.uniqueId)
            if (playerZone.canClear()) {
                // 当副本可通关时, 执行通关操作.
                playerZone.clear()
            }
        }

        @SubscribeEvent
        fun e(e: YamlSendFinishedEvent) {
            val player = e.player
            PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
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
            submit(delay = 40) {
                playerZone.showJoinMessage(player)
                playerZone.spawnEntities()
            }
            DungeonJoinEvent(player, playerZone.zone.id, playerZone).call()
        }
    }

    // 展示进入信息.
    fun showJoinMessage(player: Player) {
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
        need2KillEntityUUIDList.forEach {
            val entity = Bukkit.getEntity(it) as? LivingEntity ?: return@forEach
            entity.health = getMobMaxHealth(entity)
        }
    }

    // 移除怪物.
    fun removeEntity(entityUUID: UUID) = need2KillEntityUUIDList.removeIf { it == entityUUID }

    // 是否可以通关.
    fun canClear() = need2KillEntityUUIDList.isEmpty() && !isClear

    // 执行通关操作.
    fun clear() {
        isClear = true
        val players = mutableListOf<Player>()
        uuidList.forEach {
            val player = Bukkit.getPlayer(it) ?: return@forEach
            players.add(player)
            DungeonClearEvent(player, zone.id).call()
        }
        val delay2BackSpawnServer = KirraDungeonServer.conf.getInt("settings.delay-back-spawn-server-secs")
        sendClearMessage(players, delay2BackSpawnServer)
    }

    fun sendClearMessage(players: List<Player>, delaySecs: Int) {
        val passedTime = formatSeconds(TimeUnit.SECONDS.convert((System.currentTimeMillis() - createdTime), TimeUnit.MILLISECONDS).toInt())
        players.forEach {
            it.sendTitle("&a&l通关!".colored(), "&7通关时长: $passedTime".colored(), 20, 100, 20)
            it.sendLang("message-player-clear-dungeon", delaySecs)
            var secs = delaySecs
            submit(delay = 60L, period = 20, async = true) {
                if (!it.isOnline) {
                    cancel()
                    return@submit
                }
                secs--
                it.sendTitle("", "&a将在 &f&l$secs &a秒后传送回您到主城.".colored(), 10, 20, 0)
                if (secs <= 0) {
                    it.sendTitle("", "&e&l正在传送.".colored(), 10, 200, 0)
                    KirraCoreBukkitAPI.teleportToSpawnServer(it)
                    cancel()
                    return@submit
                }
            }
        }
    }

    // 是否可以删除该副本. (无人游玩状态)
    fun canDelete() = uuidList.size <= 0

    // 删除副本.
    fun delete() {
        DungeonServerAPI.getWorldManager().dropDungeon(dungeonWorld)
        playerZones.remove(this)
    }

    // 生成怪物.
    fun spawnEntities() {
        // 生成怪物步骤.
        zone.data.entityMap.forEach { (zoneLoc, mobData) ->
            // 将 ZoneLocation 转换为 BukkitLocation.
            val bukkitLoc = zoneLoc.toBukkitLocation(dungeonWorld.bukkitWorld).add(0.0, 1.0, 0.0)
            // 调用 MythicmobsAPI, 将怪物生成到世界坐标.
            repeat(mobData.amount) {
                val randomLoc = bukkitLoc.add(Random.nextDouble(0.1, 1.0), 0.0, Random.nextDouble(0.1, 1.0))
                val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(mobData.mobType, randomLoc) as? LivingEntity ?: return@repeat
                need2KillEntityUUIDList.add(entity.uniqueId)
            }
        }
        // 检测是否有意外死亡并未计入事件的怪物.
        submit(async = true, delay = 20L, period = 20L) {
            need2KillEntityUUIDList.removeIf { Bukkit.getEntity(it) == null }
            if (canClear()) {
                clear()
                cancel()
                return@submit
            }
        }
    }

    init {
        // 超时判断. (副本创建后长时间未进入)
        submit(async = true, delay = 1000) {
            if (canDelete()) {
                delete()
                cancel()
                return@submit
            }
        }
    }
}