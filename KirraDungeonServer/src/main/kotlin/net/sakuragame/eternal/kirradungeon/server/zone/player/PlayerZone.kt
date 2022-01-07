package net.sakuragame.eternal.kirradungeon.server.zone.player

import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang
import java.util.*
import kotlin.random.Random

/**
 * 玩家副本, 最终副本的实现类.
 *
 */
class PlayerZone(val zone: Zone, val dungeonWorld: DungeonWorld) : IZone {

    init {
        // 超时判断. (副本创建后长时间未进入)
        submit(async = true, delay = 1000) {
            if (canDel()) {
                del()
                cancel()
                return@submit
            }
        }
    }

    override val createdTime = System.currentTimeMillis()

    override var isClear = false

    override var isFail = false

    override var lastTime = zone.data.maxLastTime

    override val playerUUIDList = mutableListOf<UUID>()
    override val monsterUUIDList = mutableListOf<UUID>()

    override var bossUUID: UUID = UUID.randomUUID()!!

    override fun runTimer() {
        // 移除未经过 EntityDeathEvent 死亡的实体。
        submit(async = true, delay = 0L, period = 20L) {
            if (canDel() || isClear || isFail) {
                cancel()
                return@submit
            }
            monsterUUIDList.removeIf { Bukkit.getEntity(it) == null }
            if (canClear()) {
                clear()
                cancel()
                return@submit
            }
        }
        // 倒计时逻辑.
        submit(async = true, delay = 60L, period = 20L) {
            if (canDel() || isClear || isFail) {
                cancel()
                return@submit
            }
            if (lastTime <= 0) {
                fail(FailType.OVERTIME)
                cancel()
                return@submit
            }
            getPlayers().forEach {
                BossBar.setTime(it, lastTime--)
            }
        }
    }

    override fun spawnEntities() {
        val monsterData = zone.data.monsterData
        val mobData = monsterData.mobList
        val bossData = monsterData.boss
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
        val bossEntity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(bossData.type, bossData.loc.toBukkitLocation(dungeonWorld.bukkitWorld)) as LivingEntity
        bossEntity.isGlowing = true
        bossUUID = bossEntity.uniqueId
    }

    override fun canClear() = getMonsters(containsBoss = true).isEmpty() && !isClear && !isFail

    override fun clear() {
        isClear = true
        val players = getPlayers()
        players.forEach {
            DungeonClearEvent(it, zone.id).call()
        }
        val delay2BackSpawnServer = KirraDungeonServer.conf.getInt("settings.delay-back-spawn-server-secs")
        sendClearMessage(players, delay2BackSpawnServer)
    }

    override fun fail(type: FailType) {
        isFail = true
        getPlayers().forEach {
            it.sendLang("message-player-lose-dungeon", zone.name)
        }
        teleportToSpawn()
    }

    override fun del() {
        DungeonServerAPI.getWorldManager().dropDungeon(dungeonWorld)
        playerZones.remove(this)
    }

    companion object {

        val playerZones = mutableListOf<PlayerZone>()

        fun getByDungeonWorldUUID(uuid: UUID) = playerZones.firstOrNull { it.dungeonWorld.uuid == uuid }

        fun getByPlayer(playerUUID: UUID): PlayerZone? {
            playerZones.forEach { playerZone ->
                if (playerZone.playerUUIDList.find { it == playerUUID } != null) {
                    return playerZone
                }
            }
            return null
        }

        fun getByMobUUID(mobUUID: UUID): PlayerZone? {
            playerZones.forEach { playerZone ->
                if (playerZone.monsterUUIDList.find { it == mobUUID } != null) {
                    return playerZone
                }
                if (playerZone.bossUUID == mobUUID) {
                    return playerZone
                }
            }
            return null
        }

        fun create(zone: Zone, dungeonWorld: DungeonWorld) {
            playerZones += PlayerZone(zone, dungeonWorld)
        }
    }
}
