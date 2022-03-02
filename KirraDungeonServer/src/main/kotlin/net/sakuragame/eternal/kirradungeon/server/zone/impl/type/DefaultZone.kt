package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.Bukkit
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

/**
 * 默认副本, 副本实现类其中之一.
 */
class DefaultZone(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IZone {

    init {
        // 超时判断. (副本创建后长时间未进入)
        submit(async = true, delay = 2000) {
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

    override var failTime: Int = 60

    override var failThread: PlatformExecutor.PlatformTask? = null

    override fun runTimer() {
        // 移除未经过 MythicMobDeathEvent 死亡的实体。
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
        startCountdown()
        showResurgenceTitle()
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

    @Suppress("DuplicatedCode")
    companion object {

        val defaultZones = mutableListOf<DefaultZone>()

        fun getByDungeonWorldUUID(uuid: UUID) = defaultZones.firstOrNull { it.dungeonWorld.uuid == uuid }

        fun getByPlayer(playerUUID: UUID): DefaultZone? {
            defaultZones.forEach { defaultZone ->
                if (defaultZone.playerUUIDList.find { it == playerUUID } != null) {
                    return defaultZone
                }
            }
            return null
        }

        fun getByMobUUID(mobUUID: UUID): DefaultZone? {
            defaultZones.forEach { defaultZone ->
                if (defaultZone.monsterUUIDList.find { it == mobUUID } != null) {
                    return defaultZone
                }
                if (defaultZone.bossUUID == mobUUID) {
                    return defaultZone
                }
            }
            return null
        }

        fun create(zone: Zone, dungeonWorld: DungeonWorld) {
            defaultZones += DefaultZone(zone, dungeonWorld)
        }
    }
}
