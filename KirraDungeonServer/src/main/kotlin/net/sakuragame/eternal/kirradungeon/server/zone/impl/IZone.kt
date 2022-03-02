package net.sakuragame.eternal.kirradungeon.server.zone.impl

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.*
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.event.DungeonJoinEvent
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType.*
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FailType.*
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.UnlimitedZone
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.chat.colored
import taboolib.platform.util.asLangText
import taboolib.platform.util.sendLang
import taboolib.platform.util.takeItem
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random

@JvmDefaultWithoutCompatibility
interface IZone {

    /**
     * 副本 UUID.
     */
    val uuid: UUID
        get() = UUID.randomUUID()

    /**
     * 副本创建时间.
     */
    val createdTime: Long

    /**
     * 原副本信息.
     */
    val zone: Zone

    /**
     * 副本世界实例.
     */
    val dungeonWorld: DungeonWorld

    /**
     * 是否通关.
     */
    var isClear: Boolean

    /**
     * 是否失败.
     */
    var isFail: Boolean

    /**
     * 副本剩余时间.
     */
    var lastTime: Int

    /**
     * 玩家 UUID 列表.
     */
    val playerUUIDList: MutableList<UUID>

    /**
     * 副本小怪 UUID 列表.
     */
    val monsterUUIDList: MutableList<UUID>

    /**
     * 副本首领 UUID.
     */
    var bossUUID: UUID

    /**
     * 失败时间, 与 failThread 挂钩.
     */
    var failTime: Int

    /**
     * 失败线程.
     * 它会在全部玩家死亡后触发, 若所有玩家没在规定时间内复活, 则副本挑战失败.
     * 判断死亡的依据为玩家是否为 SPECTATOR 模式.
     */
    var failThread: PlatformExecutor.PlatformTask?

    /**
     * 根据 UUID 获取当前所有副本玩家.
     */
    fun getPlayers() = playerUUIDList.mapNotNull { Bukkit.getPlayer(it) }

    /**
     * 根据 UUID 获取单个玩家.
     */
    fun getSinglePlayer() = Bukkit.getPlayer(playerUUIDList.first())!!

    /**
     * 根据 UUID 获取当前所有副本怪物.
     * @param containsBoss 是否包含怪物首领
     */
    fun getMonsters(containsBoss: Boolean): List<LivingEntity> {
        return mutableListOf<LivingEntity>().apply {
            addAll(monsterUUIDList.mapNotNull { Bukkit.getEntity(it) as? LivingEntity })
            if (containsBoss) add(Bukkit.getEntity(bossUUID) as? LivingEntity ?: return@apply)
        }
    }

    /**
     * 是否全部玩家阵亡.
     */
    fun isAllPlayersDead() = getPlayers().find { !it.isSpectator() } == null

    /**
     * 设置所有副本怪物的血量为满.
     * @param containsBoss 是否包含怪物首领.
     */
    fun setAllMonsterHealth2Max(containsBoss: Boolean) {
        getMonsters(containsBoss).forEach {
            it.health = getMobMaxHealth(it)
        }
    }

    /**
     * 移除所有怪物.
     */
    fun removeAllMonsters(containBoss: Boolean) {
        getMonsters(containBoss).forEach {
            it.remove()
        }
    }

    /**
     * 获取副本怪物首领.
     */
    fun getBoss() = Bukkit.getEntity(bossUUID) as? LivingEntity

    /**
     * 开启一些需要的调度器.
     */
    fun runTimer()

    /**
     * 处理玩家进入.
     */
    fun handleJoin(player: Player, spawnBoss: Boolean, spawnMob: Boolean) {
        val data = zone.data
        val loc = data.spawnLoc.toBukkitLocation(dungeonWorld.bukkitWorld)
        player.teleport(loc)
        player.profile().isChallenging = true
        player.reset()
        if (data.isCustomSkyEnabled()) {
            data.zoneSkyData!!.apply {
                val skyChangerPlayer = SkyChanger.wrapPlayer(player)
                KirraDungeonServer.skyAPI.changeSky(skyChangerPlayer, packetType, value)
            }
        }
        submit(delay = 40) {
            showJoinMessage(player, zone.name)
            spawnEntities(spawnBoss, spawnMob)
            runTimer()
            DungeonJoinEvent(player, zone.id, this@IZone).call()
        }
    }

    /**
     * 生成副本怪物.
     */
    fun spawnEntities(spawnBoss: Boolean, spawnEntity: Boolean, bossLevel: Int = 1) {
        val monsterData = zone.data.monsterData
        val mobData = monsterData.mobList
        val bossData = monsterData.boss
        if (spawnEntity) {
            mobData.forEach { monster ->
                // 将 ZoneLocation 转换为 BukkitLocation.
                val bukkitLoc = monster.loc.toBukkitLocation(dungeonWorld.bukkitWorld).add(0.0, 1.0, 0.0)
                // 调用 MythicmobsAPI, 将怪物生成到世界坐标.
                repeat(monster.amount) {
                    val randomLoc = bukkitLoc.add(Random.nextDouble(0.1, 0.3), 0.0, Random.nextDouble(0.1, 0.3))
                    val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(monster.type, randomLoc) as? LivingEntity ?: return@repeat
                    entity.isGlowing = true
                    monsterUUIDList.add(entity.uniqueId)
                }
            }
        }
        if (spawnBoss) {
            val bossEntity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(bossData.type, bossData.loc.toBukkitLocation(dungeonWorld.bukkitWorld), bossLevel) as LivingEntity
            bossEntity.isGlowing = true
            bossUUID = bossEntity.uniqueId
        }
    }

    /**
     * 向玩家展示副本进入信息.
     */
    fun showJoinMessage(player: Player, zoneName: String) {
        DragonCoreCompat.updateDragonVars(player, zoneName)
        PacketSender.sendOpenHud(player, DragonCoreCompat.joinTitleHudID)
        player.sendLang("message-player-join-dungeon", zoneName)
    }

    /**
     * 向玩家展示通关信息.
     */
    fun sendClearMessage(players: List<Player>, delaySecs: Int) {
        val passedTime = formatSeconds(TimeUnit.SECONDS.convert((System.currentTimeMillis() - createdTime), TimeUnit.MILLISECONDS).toInt())
        players.forEach {
            it.sendTitle("&a&l通关!".colored(), "&7通关时长: $passedTime".colored(), 20, 100, 20)
            it.sendLang("message-player-clear-dungeon", delaySecs)
        }
        teleportToSpawn()
    }

    fun teleportToSpawn() {
        var secs = 5
        submit(delay = 60L, period = 20, async = true) {
            getPlayers().forEach {
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

    /**
     * 更新 BOSS 血条为所有队伍玩家.
     */
    fun updateBossBar(init: Boolean = false) {
        submit(delay = 3L) {
            val bossEntity = getBoss() ?: return@submit
            val healthPercent = bossEntity.health / getMobMaxHealth(bossEntity)
            getPlayers().forEach {
                if (init) {
                    BossBar.open(
                        it,
                        bossEntity.name,
                        "",
                        zone.data.iconNumber.toString(),
                        healthPercent,
                        lastTime
                    )
                    return@forEach
                }
                BossBar.setHealth(it, "&c&l${bossEntity.health.roundToInt()} / ${getMobMaxHealth(bossEntity)}".colored(), healthPercent)
            }
        }
    }

    /**
     * 开始失败线程运行.
     */
    fun startFailThread() {
        getPlayers().forEach {
            it.sendLang("message-fail-thread-started", failTime)
        }
        failThread = submit(async = true, period = 20) {
            if (!isAllPlayersDead()) {
                cancel()
                return@submit
            }
            failTime--
            getPlayers().forEach {
                val profile = it.profile()
                if (profile.isQuitting) {
                    return@forEach
                }
                it.sendTitle("&4&l全员死亡".colored(), "&7将会在 $failTime &7秒后自动退出.".colored(), 0, 40, 0)
            }
            if (failTime <= 0) {
                fail(ALL_DIED)
                failThread = null
                cancel()
                return@submit
            }
        }
    }

    /**
     * 为副本增加一个玩家 UUID.
     */
    fun addPlayerUUID(uuid: UUID) = playerUUIDList.add(uuid)

    /**
     * 移除一个副本的玩家 UUID.
     */
    fun removePlayerUUID(uuid: UUID) = playerUUIDList.remove(uuid)

    /**
     * 为副本增加一个怪物 UUID.
     */
    fun addMonsterUUID(uuid: UUID) = monsterUUIDList.add(uuid)

    /**
     * 移除一个副本的怪物 UUID.
     */
    fun removeMonsterUUID(uuid: UUID) = monsterUUIDList.remove(uuid)

    /**
     * 是否可以通关。
     */
    fun canClear(): Boolean

    /**
     * 执行通关相关操作。
     */
    fun clear()

    /**
     * 玩家挑战失败.
     * @param type 失败类型.
     */
    fun fail(type: FailType) {
        isFail = true
        val failText = when (type) {
            OVERTIME -> Bukkit.getServer().consoleSender.asLangText("message-player-over-time")
            ALL_DIED -> Bukkit.getServer().consoleSender.asLangText("message-player-over-time")
            CUSTOM -> ""
        }
        removeAllMonsters(true)
        getPlayers().forEach {
            it.sendTitle(failText, "", 3, 60, 3)
            it.turnToSpectator()
            it.sendLang("message-player-lose-dungeon", zone.name)
        }
        teleportToSpawn()
    }

    /**
     * 是否可以删除副本.
     */
    fun canDel() = playerUUIDList.size == 0

    /**
     * 执行副本删除操作.
     */
    fun del() {
        DungeonServerAPI.getWorldManager().dropDungeon(dungeonWorld)
        when (zone.data.type) {
            DEFAULT -> DefaultZone.defaultZones.remove(this)
            SPECIAL -> SpecialZone.specialZones.remove(this)
            UNLIMITED -> UnlimitedZone.unlimitedZones.remove(this)
        }
    }

    /**
     * 是否可以复活.
     */
    fun canResurgence() = failThread != null

    /**
     * 执行复活操作.
     */
    fun resurgence(player: Player) {
        failThread?.cancel()
        failThread = null
        failTime = 60
        player.teleport(zone.data.spawnLoc.toBukkitLocation(player.world))
        player.reset()
        player.sendTitle("&6&l复活".colored(), "&7尽全力打败怪物们!".colored(), 3, 40, 0)
        player.inventory.takeItem { it.itemMeta.displayName.contains("复活币") }
        player.health = player.maxHealth / 2
        setAllMonsterHealth2Max(true)
        getPlayers().forEach {
            it.sendLang("message-player-resurgence", player.displayName)
        }
    }
}