package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveData
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

class WaveZone(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IZone {

    init {
        runOverTimeCheck()
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

    private val waveLocs by lazy {
        zone.data.waveSpawnLocs!!
    }

    var currentWave: ZoneWaveData? = null

    var waveCounts = 1

    var isBossSpawned = false

    override fun onPlayerJoin() {
        showResurgenceTitle()
        submit(delay = 80) {
            waveStart()
        }
    }

    override fun canClear(): Boolean {
        return waveList.isEmpty()
    }

    private fun waveStart() {
        showWavePreStartTitle()
        val nextWave = waveList.removeFirst()
        currentWave = nextWave
        submit(delay = 40) {
            spawnWaveMonsters(nextWave)
            showWaveStartTitle()
            startCountdown()
        }
    }

    private fun waveEnd() {
        waveCountsPlus()
        if (canClear()) {
            clear()
            return
        }
        showWaveEndTitle()
        submit(delay = 100) {
            waveStart()
        }
    }

    fun handleMonsterRemove() {
        monsterUUIDList.removeIf { Bukkit.getEntity(it) == null }
        if (shouldSpawnBoss()) {
            spawnWaveBoss(currentWave!!)
            return
        }
        if (isWaveEnd()) {
            waveEnd()
        }
    }

    private fun isWaveEnd(): Boolean {
        return monsterUUIDList.isEmpty() && isBossSpawned
    }

    private fun shouldSpawnBoss(): Boolean {
        return monsterUUIDList.isEmpty() && !isBossSpawned
    }

    private fun showWavePreStartTitle() {
        sendTitle("&4&l第 $waveCounts 波", "&c怪物即将来临, 请做好准备!", 5, 60, 5)
        repeat(3) {
            submit(delay = it * 7L) {
                sendSound(Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, 5.0f + it, 1.0f)
            }
        }
    }

    private fun showWaveStartTitle() {
        sendTitle("&4&l第 $waveCounts 波", "&c它们来了...", 0, 40, 5)
        sendSound(Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 10.0f, 1.0f)
    }

    private fun showWaveBossSpawnedTitle() {
        sendSound(Sound.ENTITY_ENDERMEN_SCREAM, 10.0f, 1.5f)
        sendTitle("&4&l< ⚠ >", "&c&kooOooOooooOooOoo.", 0, 40, 0)
        submit(delay = 15) {
            sendSound(Sound.ITEM_TOTEM_USE, 5.0f, 1.5f)
            sendTitle("&4&l< ⚠ >", "&c怪物首领已被召唤.", 0, 35, 0)
        }
    }

    private fun showWaveEndTitle() {
        sendTitle("&6&l已攻克", "&e下一波怪物将在 &f5&e 秒后来袭.", 5, 40, 5)
        sendSound(Sound.ENTITY_ENDERMEN_DEATH, 10.0f, 1.5f)
    }

    private fun spawnWaveMonsters(wave: ZoneWaveData) {
        wave.monsterData.forEach {
            repeat(it.amount) { _ ->
                val loc = waveLocs.random().toBukkitLocation(dungeonWorld.bukkitWorld).add(getRandomDouble(), 0.0, getRandomDouble())
                val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(it.monsterId, loc) as LivingEntity
                entity.isGlowing = true
                entity.maxHealth = it.health
                entity.health = it.health
                monsterUUIDList += entity.uniqueId
            }
        }
    }

    private fun spawnWaveBoss(wave: ZoneWaveData) {
        if (isBossSpawned) {
            return
        }
        showWaveBossSpawnedTitle()
        isBossSpawned = true
        wave.bossData.apply {
            val loc = waveLocs.random().toBukkitLocation(dungeonWorld.bukkitWorld)
            val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(bossId, loc) as LivingEntity
            entity.isGlowing = true
            entity.maxHealth = health
            entity.health = health
            monsterUUIDList += entity.uniqueId
        }
        updateBossBar(init = true)
    }

    private fun waveCountsPlus() {
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

    private fun getRandomDouble(): Double {
        return kotlin.random.Random.nextDouble(1.0, 3.0)
    }
}