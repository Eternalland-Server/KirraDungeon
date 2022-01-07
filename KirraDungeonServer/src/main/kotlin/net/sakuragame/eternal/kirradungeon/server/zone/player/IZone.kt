package net.sakuragame.eternal.kirradungeon.server.zone.player

import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.formatSeconds
import net.sakuragame.eternal.kirradungeon.server.getMobMaxHealth
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

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
     * 根据 UUID 获取当前所有副本玩家.
     */

    fun getPlayers() = playerUUIDList.mapNotNull { Bukkit.getPlayer(it) }

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
    fun isAllPlayersDead() = getPlayers().find { it.gameMode != GameMode.SPECTATOR } == null

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
     * 生成副本怪物.
     */
    fun spawnEntities()

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
    fun updateBossBar(bossIcon: String = "21", init: Boolean = false) {
        submit(delay = 3L) {
            val bossEntity = getBoss() ?: return@submit
            val healthPercent = bossEntity.health / getMobMaxHealth(bossEntity)
            getPlayers().forEach {
                if (init) {
                    BossBar.open(
                        it,
                        bossEntity.name,
                        "",
                        bossIcon,
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
    fun fail(type: FailType)

    /**
     * 是否可以删除副本.
     */
    fun canDel() = playerUUIDList.size == 0

    /**
     * 执行副本删除操作.
     */
    fun del()
}
