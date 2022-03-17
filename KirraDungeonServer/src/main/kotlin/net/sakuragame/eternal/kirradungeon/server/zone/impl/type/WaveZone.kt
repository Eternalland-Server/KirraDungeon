package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveData
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

class WaveZone(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IZone {

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

    override var failTime: Int = 60

    override var failThread: PlatformExecutor.PlatformTask? = null

    private val waveList = mutableListOf<ZoneWaveData>().also {
        it.addAll(zone.data.waveData!!)
    }

    var waveCounts = 1

    override fun onPlayerJoin() {
        startCountdown()
        showResurgenceTitle()
    }

    override fun canClear(): Boolean {
        return waveList.isEmpty()
    }

    override fun clear() {
        error("not reachable")
    }

    fun doWave() {
        showWaveStartMessage()
        val nextWave = waveList.removeFirst()
        spawnWaveMonsters(nextWave)
    }

    fun showWaveStartMessage() {
        sendTitle("&4&l&n第 $waveCounts 波", "&c怪物即将来临, 请做好准备!", 5, 40, 5)
    }

    fun spawnWaveMonsters(wave: ZoneWaveData) {
        wave.monsterData.forEach {
            repeat(it.amount) { _ ->
                val monsterLoc = zone.data.spawnLoc.toBukkitLocation(dungeonWorld.bukkitWorld).add(getRandomDouble(), 0.0, getRandomDouble())
                val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(it.monsterId, monsterLoc) as LivingEntity
                entity.health = it.health
                monsterUUIDList += entity.uniqueId
            }
        }
        wave.bossData.apply {
            val bossLoc = zone.data.spawnLoc.toBukkitLocation(dungeonWorld.bukkitWorld).add(getRandomDouble(), 0.0, getRandomDouble())
            val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(bossId, bossLoc) as LivingEntity
            entity.health = health
            monsterUUIDList += entity.uniqueId
        }
    }

    fun waveCountsPlus() {
        waveCounts++
    }

    @Suppress("DuplicatedCode")
    companion object {

        val waveZones = mutableListOf<WaveZone>()

        fun getByDungeonWorldUUID(uuid: UUID) = waveZones.firstOrNull { it.dungeonWorld.uuid == uuid }

        fun getByPlayer(playerUUID: UUID): WaveZone? {
            waveZones.forEach { waveZone ->
                if (waveZone.playerUUIDList.find { it == playerUUID } != null) {
                    return waveZone
                }
            }
            return null
        }

        fun getByMobUUID(mobUUID: UUID): WaveZone? {
            waveZones.forEach { waveZone ->
                if (waveZone.monsterUUIDList.find { it == mobUUID } != null) {
                    return waveZone
                }
                if (waveZone.bossUUID == mobUUID) {
                    return waveZone
                }
            }
            return null
        }

        fun create(zone: Zone, dungeonWorld: DungeonWorld) {
            waveZones += WaveZone(zone, dungeonWorld)
        }
    }

    fun getRandomDouble(): Double {
        return kotlin.random.Random.nextDouble(7.0, 13.0)
    }
}