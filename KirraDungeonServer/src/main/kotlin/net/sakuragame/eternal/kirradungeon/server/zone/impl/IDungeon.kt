package net.sakuragame.eternal.kirradungeon.server.zone.impl

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import com.sakuragame.eternal.justattribute.api.JustAttributeAPI
import com.taylorswiftcn.justwei.util.UnitConvert
import ink.ptms.adyeshach.api.AdyeshachAPI
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirracore.bukkit.KirraCoreBukkitAPI
import net.sakuragame.eternal.kirradungeon.common.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.common.event.DungeonFailEvent
import net.sakuragame.eternal.kirradungeon.common.event.DungeonJoinEvent
import net.sakuragame.eternal.kirradungeon.server.*
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneTriggerData
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FailType.*
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Sound
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
import kotlin.random.Random

@JvmDefaultWithoutCompatibility
interface IDungeon {

    /**
     * 副本 UUID
     */
    val uuid: UUID

    /**
     * 副本创建时间
     */
    val createdTime: Long

    /**
     * 原副本信息
     */
    val zone: Zone

    /**
     * 副本世界实例
     */
    val dungeonWorld: DungeonWorld

    /**
     * 副本是否初始化
     */
    var init: Boolean

    /**
     * 是否通关
     */
    var isClear: Boolean

    /**
     * 是否失败
     */
    var fail: Boolean

    /**
     * 副本剩余时间
     */
    var lastTime: Int

    /**
     * 玩家 UUID 列表
     */
    val playerUUIDList: MutableList<UUID>

    /**
     * 副本怪物 UUID 列表
     */
    val monsterUUIDList: MutableList<UUID>

    /**
     * 触发器数据
     * 玩家对触发器数据里的方块坐标操作 (例如: 拉动拉杆)
     * 触发器数据里的方块将会覆盖在地图上, 同时执行 init 方法 (召唤怪物并开始倒计时)
     *
     * 并不是所有副本都需要触发器, 所以这个变量为可空变量
     */
    val triggerData: ZoneTriggerData?

    /**
     * 副本首领 UUID
     */
    var bossUUID: UUID

    /**
     * 失败时间, 与 failThread 挂钩
     */
    var failTime: Int

    /**
     * 失败线程
     * 它会在全部玩家死亡后触发, 若所有玩家没在规定时间内复活, 则副本挑战失败
     * 判断死亡的依据为玩家是否为 SPECTATOR 模式
     */
    var failThread: PlatformExecutor.PlatformTask?

    /**
     * 根据 UUID 获取当前所有副本玩家
     */
    fun getPlayers() = playerUUIDList.mapNotNull { Bukkit.getPlayer(it) }

    /**
     * 获取第一个玩家
     */
    fun getFirst() = Bukkit.getPlayer(playerUUIDList.first())!!

    /**
     * 根据 UUID 获取当前所有副本怪物
     * @param containsBoss 是否包含怪物首领
     */
    fun getMonsters(containsBoss: Boolean): List<LivingEntity> {
        return mutableListOf<LivingEntity>().apply {
            addAll(monsterUUIDList.mapNotNull { Bukkit.getEntity(it) as? LivingEntity })
            if (containsBoss) add(Bukkit.getEntity(bossUUID) as? LivingEntity ?: return@apply)
        }
    }

    /**
     * 是否全部玩家阵亡
     */
    fun isAllPlayersDead() = getPlayers().find { !it.isSpectator() } == null

    /**
     * 移除所有怪物
     */
    fun removeAllMonsters(containBoss: Boolean) {
        getMonsters(containBoss).forEach {
            it.remove()
        }
    }

    /**
     * 获取副本怪物首领
     */
    fun getBoss() = Bukkit.getEntity(bossUUID) as? LivingEntity

    /**
     * 副本初始化 (当第一名玩家进入副本时)
     */
    fun init()

    fun whenTeleportToSpawn() {

    }

    /**
     * 当玩家进入
     */
    fun onPlayerJoin(player: Player)

    /**
     * 处理玩家进入
     */
    fun handleJoin(player: Player, spawnBoss: Boolean, spawnMob: Boolean, showTimeBar: Boolean = true) {
        val data = zone.data
        val loc = data.spawnLoc.toBukkitLocation(dungeonWorld.bukkitWorld)
        player.teleport(loc)
        player.profile()?.isChallenging = true
        player.reset()
        if (data.isCustomSkyEnabled()) {
            data.zoneSkyData!!.apply {
                val skyChangerPlayer = SkyChanger.wrapPlayer(player)
                KirraDungeonServer.skyAPI.changeSky(skyChangerPlayer, packetType, value)
            }
        }
        if (!init) {
            init = true
            init()
            submit(async = false, delay = 20) {
                spawnEntities(spawnBoss, spawnMob)
            }
            if (showTimeBar) {
                submit(async = false, delay = 40) {
                    updateBossBar(init = true)
                }
            }
        }
        submit(delay = 40) {
            showJoinMessage(player, zone.name)
            onPlayerJoin(player)
            // 展示全息
            data.holograms.forEach {
                AdyeshachAPI.createHologram(player, it.loc.toBukkitLocation(dungeonWorld.bukkitWorld), it.contents.colored())
            }
            DungeonJoinEvent(player, zone.id).call()
        }
    }

    /**
     * 执行触发器方法 (覆盖方块)
     */
    fun handleTrigger(): Long? {
        val currentTrigger = triggerData ?: return null
        if (currentTrigger.blocks.isEmpty()) return null
        val blocks = currentTrigger.blocks
        var delay = 0L
        blocks.forEach {
            if (canDel()) {
                return null
            }
            submit(async = false, delay = delay) {
                it.forEach {
                    val block = it.loc.toBukkitLocation(dungeonWorld.bukkitWorld).block
                    block.world.playEffect(block.location, Effect.MOBSPAWNER_FLAMES, 1)
                    block.type = it.material
                    getPlayers().forEach { player ->
                        player.playSound(block.location, Sound.BLOCK_STONE_PLACE, 1.5f, 1f)
                    }
                }
            }
            delay += 10
        }
        return delay + 1
    }

    /**
     * 生成副本怪物
     */
    fun spawnEntities(spawnBoss: Boolean, spawnEntity: Boolean, bossLevel: Int = 1) {
        val monsterData = zone.data.monsterData
        val mobData = monsterData.mobList
        val bossData = monsterData.boss
        if (spawnEntity) {
            mobData.forEach { monster ->
                // 将 ZoneLocation 转换为 BukkitLocation.
                val loc = monster.loc.toBukkitLocation(dungeonWorld.bukkitWorld).add(0.0, 1.0, 0.0)
                // 调用 MythicmobsAPI, 将怪物生成到世界坐标.
                repeat(monster.amount) {
                    val randomLoc = loc.add(Random.nextDouble(0.1, 0.3), 0.0, Random.nextDouble(0.1, 0.3))
                    val entity = spawnDungeonMob(randomLoc, monster.type)
                    monsterUUIDList.add(entity.uniqueId)
                }
            }
        }
        if (spawnBoss) {
            val loc = bossData.loc.toBukkitLocation(dungeonWorld.bukkitWorld)
            val bossEntity = spawnDungeonMob(loc, bossData.type, bossLevel)
            bossUUID = bossEntity.uniqueId
        }
    }

    /**
     * 向玩家展示副本进入信息
     */
    fun showJoinMessage(player: Player, zoneName: String) {
        DragonCoreCompat.updateDragonVars(player, zoneName)
        PacketSender.sendOpenHud(player, DragonCoreCompat.joinTitleHudID)
        player.sendLang("message-player-join-dungeon", zoneName)
    }

    /**
     * 向玩家展示通关信息
     */
    fun sendClearMessage(players: List<Player>, delaySecs: Int) {
        val passedTime = formatSeconds(TimeUnit.SECONDS.convert((System.currentTimeMillis() - createdTime), TimeUnit.MILLISECONDS).toInt())
        players.forEach {
            it.sendTitle("&a&l通关!".colored(), "&7通关时长: $passedTime".colored(), 0, 100, 0)
            it.playDragonCoreSound("sounds/d/100.ogg")
            it.sendLang("message-player-clear-dungeon", delaySecs)
        }
        teleportToSpawn()
    }

    /**
     * 向玩家展示大标题
     */
    fun sendTitle(title: String, subTitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        getPlayers().forEach {
            it.sendTitle(title.colored(), subTitle.colored(), fadeIn, stay, fadeOut)
        }
    }

    /**
     * 向玩家播放声音
     */
    fun sendSound(sound: Sound, volume: Float, pitch: Float) {
        getPlayers().forEach {
            it.playSound(it.location, sound, volume, pitch)
        }
    }

    /**
     * 传送玩家至主城
     */
    fun teleportToSpawn() {
        whenTeleportToSpawn()
        var secs = 10
        submit(delay = 60L, period = 20, async = true) {
            getPlayers().forEach {
                if (!it.isOnline) {
                    cancel()
                    return@submit
                }
                secs--
                it.sendTitle("", "&a将在 &f&l$secs &a秒后传送回您到主城.".colored(), 0, 25, 0)
                if (secs <= 0) {
                    KirraCoreBukkitAPI.teleportPlayerToServerByBalancing("test", it.uniqueId)
                    cancel()
                    return@submit
                }
            }
        }
    }

    /**
     * 为所有队伍玩家更新 BOSS 血条
     */
    fun updateBossBar(init: Boolean = false) {
        submit(async = false, delay = 3L) {
            val entity = getBoss()
            getPlayers().forEach {
                if (entity == null) {
                    if (init) {
                        BossBar.open(it, lastTime)
                    }
                    return@forEach
                }
                val percent = entity.health / getMobMaxHealth(entity)
                if (init) {
                    BossBar.open(it, entity.name, "", zone.data.iconNumber.toString(), percent, lastTime)
                }
                val currentHealth = UnitConvert.formatCN(UnitConvert.TenThousand, entity.health)
                val maxHealth = UnitConvert.formatCN(UnitConvert.TenThousand, getMobMaxHealth(entity))
                BossBar.setHealth(it, "&c&l$currentHealth / $maxHealth".colored(), percent)
            }
        }
    }

    /**
     * 开始失败线程运行
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
            val players = getPlayers()
            players.forEach {
                val profile = it.profile() ?: return@forEach
                if (profile.isQuitting) {
                    return@forEach
                }
                if (players.find { player -> player.gameMode != GameMode.SPECTATOR } == null) {
                    it.sendTitle("&7将在 $failTime 秒自动退出".colored(), "&7&oM 键消耗复活币复活 &f&k!&r &7&oESC 键退出副本回主城".colored(), 0, 40, 0)
                } else {
                    it.sendTitle("", "&7&oM 键消耗复活币复活 &f&k!&r &7&oESC 键退出副本回主城.".colored(), 0, 40, 0)
                }
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
     * 为副本增加一个玩家 UUID
     */
    fun addPlayerUUID(uuid: UUID) = playerUUIDList.add(uuid)

    /**
     * 移除一个副本的玩家 UUID
     */
    fun removePlayerUUID(uuid: UUID) = playerUUIDList.remove(uuid)

    /**
     * 为副本增加一个怪物 UUID
     */
    fun addMonsterUUID(uuid: UUID) = monsterUUIDList.add(uuid)

    /**
     * 移除一个副本的怪物 UUID
     */
    fun removeMonsterUUID(uuid: UUID) = monsterUUIDList.remove(uuid)

    /**
     * 是否可以通关
     */
    fun canClear(): Boolean

    /**
     * 执行通关相关操作
     */
    fun clear() {
        isClear = true
        val players = getPlayers()
        DungeonClearEvent(players, zone.id).call()
        val delay2BackSpawnServer = KirraDungeonServer.conf.getInt("settings.delay-back-spawn-server-secs")
        sendClearMessage(players, delay2BackSpawnServer)
    }

    /**
     * 玩家挑战失败
     * @param type 失败类型
     */
    fun fail(type: FailType) {
        fail = true
        DungeonFailEvent(getPlayers(), zone.id).call()
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
     * 是否可以删除副本
     */
    fun canDel() = playerUUIDList.size == 0

    /**
     * 执行副本删除操作
     */
    fun del() {
        DungeonServerAPI.getWorldManager().dropDungeon(dungeonWorld)
        FunctionDungeon.dungeons -= this
    }

    /**
     * 是否可以复活
     */
    fun canResurgence() = failThread != null

    /**
     * 执行复活操作
     */
    fun resurgence(player: Player) {
        failThread?.cancel()
        failThread = null
        failTime = 60
        val profile = player.profile() ?: return
        player.apply {
            reset()
            teleport(profile.deathPlace)
            sendTitle("&6&l复活".colored(), "&7尽全力打败怪物们!".colored(), 3, 40, 5)
            player.inventory.takeItem { it.itemMeta.displayName.contains("复活币") }
        }
        JustAttributeAPI.getRoleCharacter(player).apply {
            setHealth(maxHP * 0.4)
            setMana(maxMP * 0.4)
        }
        getPlayers().forEach {
            it.sendLang("message-player-resurgence", player.displayName)
        }
    }
}
