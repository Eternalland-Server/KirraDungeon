package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeAdvancement
import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeDisplay
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveData
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.chat.colored
import java.util.*

class WaveDungeon(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IDungeon {

    init {
        runOverTimeCheck()
    }

    override val uuid = UUID.randomUUID()!!

    override val createdTime = System.currentTimeMillis()

    override var init = false

    override var isClear = false

    override var fail = false

    override var lastTime = zone.data.maxLastTime

    override val playerUUIDList = mutableListOf<UUID>()

    override val monsterUUIDList = mutableListOf<UUID>()

    override val triggerData = null

    override var bossUUID: UUID = UUID.randomUUID()!!

    override var failTime: Int = 60

    override var failThread: PlatformExecutor.PlatformTask? = null

    var waveCounts = 1

    private var isBossSpawned = false

    private val waveList = mutableListOf<ZoneWaveData>().also {
        it.addAll(zone.data.waveData!!)
    }

    private val waveLocs by lazy {
        zone.data.waveSpawnLocs!!
    }

    private var currentWave: ZoneWaveData? = null

    override fun init() {
        submit(delay = 80) {
            waveStart()
        }
    }

    override fun onPlayerJoin(player: Player) {
        FakeAdvancement(FakeDisplay(Material.BUCKET, "&7&o愿筒子护佑你, 年轻人.".colored(), "", FakeDisplay.AdvancementFrame.GOAL, null))
            .displayToast(player)
    }

    override fun canClear(): Boolean {
        return false
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
            entity.maxHealth = health
            entity.health = health
            monsterUUIDList += entity.uniqueId
        }
        updateBossBar(init = true)
    }

    private fun waveCountsPlus() {
        waveCounts++
    }

    private fun getRandomDouble(): Double {
        return kotlin.random.Random.nextDouble(1.0, 3.0)
    }
}