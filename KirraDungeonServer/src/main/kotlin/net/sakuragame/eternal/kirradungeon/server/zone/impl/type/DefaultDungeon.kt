package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeAdvancement
import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeDisplay
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.justlevel.api.JustLevelAPI
import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.playDragonCoreSound
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneTriggerData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneMobData
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.spawnDungeonMob
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import net.sakuragame.eternal.waypoints.api.WaypointsAPI
import net.sakuragame.eternal.waypoints.core.IconType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.chat.colored
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class DefaultDungeon(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IDungeon {

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

    override val triggerData: ZoneTriggerData?
        get() = if (zone.data.trigger.blocks.isEmpty()) {
            null
        } else {
            zone.data.trigger.copy()
        }

    override var bossUUID = UUID.randomUUID()!!

    override var failTime = 60

    override var failThread: PlatformExecutor.PlatformTask? = null

    val mobs = mutableListOf<ZoneMobData>().apply {
        addAll(zone.data.monsterData.mobList)
    }

    private val monsterIds by lazy {
        getMonsterIdsFromZone()
    }

    val naturalSpawnBoss by lazy {
        !zone.data.metadataMap.containsKey("DISABLE_NATURAL_SPAWN")
    }

    private var bossSpawned = false

    var triggered = false

    override fun init() {
        // 移除未经过 MythicMobDeathEvent 死亡的实体
        submit(async = true, delay = 0L, period = 20L) {
            if (canDel() || isClear || fail) {
                cancel()
                return@submit
            }
            monsterUUIDList.removeIf { Bukkit.getEntity(it) == null }
            submit(async = false) {
                getMonsters(true)
                    .filter { it.location.y < 0 }
                    .forEach { entity ->
                        val str = entity.getMetadata("ORIGIN_LOC").getOrNull(0)?.asString() ?: return@forEach
                        val zoneLoc = ZoneLocation.parseToZoneLocation(str) ?: return@forEach
                        val loc = zoneLoc.toBukkitLocation(entity.world)
                            .clone()
                            .add(0.0, 1.0, 0.0)
                        entity.teleport(loc)
                    }
            }
            if (canClear()) {
                clear()
                cancel()
                return@submit
            }
        }
        submit(delay = 40) {
            if (triggerData == null) {
                triggered = true
                doTrigger()
            }
        }
    }

    override fun onPlayerJoin(player: Player) {
        if (zone.data.stagedMobs.isEmpty()) {
            return
        }
        val playerStage = JustLevelAPI.getTotalStage(player)
        zone.data.stagedMobs.forEach {
            val level = (it.multiplier * playerStage).roundToInt()
            spawnDungeonMob(it.loc.toBukkitLocation(player.world), it.monsterId, level)
        }
    }

    fun doTrigger() {
        doSpawn()
        submit(async = true, delay = 20L) {
            updateBossBar(init = true)
        }
        getPlayers().forEach {
            it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            submit(delay = 10) {
                it.playSound(it.location, Sound.ENTITY_ENDERDRAGON_GROWL, 1f, 1f)
            }
            FakeAdvancement(FakeDisplay(Material.BUCKET, "&7&o愿筒子护佑你, 年轻人.".colored(), "", FakeDisplay.AdvancementFrame.GOAL, null))
                .displayToast(it)
        }
        startCountdown()
    }

    fun doSpawn() {
        val mobData = mobs.removeFirstOrNull()
        if (mobData == null) {
            if (bossSpawned) {
                return
            }
            val bossData = zone.data.monsterData.boss
            bossSpawned = true
            if (bossData.type.isEmpty() || bossData.type.isBlank()) {
                return
            }
            val loc = bossData.loc.toBukkitLocation(dungeonWorld.bukkitWorld)
            val entity = spawnDungeonMob(loc, bossData.type, bossData.levelRange.random())
            doBossNotice(loc)
            bossUUID = entity.uniqueId
            return
        }
        val loc = mobData.loc.toBukkitLocation(dungeonWorld.bukkitWorld).add(0.0, 1.5, 0.0)
        doMobNotice(loc)
        repeat(mobData.amount) {
            val randomLoc = loc.add(Random.nextDouble(0.1, 0.3), 0.0, Random.nextDouble(0.1, 0.3))
            val entity = spawnDungeonMob(randomLoc, monsterIds.random(), mobData.levelRange.random())
            monsterUUIDList.add(entity.uniqueId)
        }
    }

    private fun doMobNotice(loc: Location) {
        val upperLocation = loc.clone().add(0.0, 2.0, 0.0)
        getPlayers().forEach {
            if (mobs.size == 0 && !naturalSpawnBoss) {
                WaypointsAPI.navPointer(it, "dungeon", IconType.Mobs, upperLocation, 1.0, listOf("尝试触发机关?"))
            } else {
                WaypointsAPI.navPointer(it, "dungeon", IconType.Mobs, upperLocation, 1.0, listOf("击杀所有怪物!"))
            }
            it.playDragonCoreSound("sounds/d/101.ogg")
        }
    }

    private fun doBossNotice(loc: Location) {
        val upperLocation = loc.clone().add(0.0, 2.0, 0.0)
        getPlayers().forEach {
            WaypointsAPI.navPointer(it, "dungeon", IconType.Boss, upperLocation, 1.0, listOf("全力击败它, 通关副本!"))
            it.playDragonCoreSound("sounds/d/102.ogg")
        }
        submit(async = true, delay = 20L) {
            getPlayers().forEach { BossBar.close(it) }
            updateBossBar(init = true)
        }
    }

    private fun getMonsterIdsFromZone(): List<String> {
        val boss = zone.data.monsterData.boss
        if (boss.type.isEmpty() || boss.type.isBlank()) {
            return zone.data.monsterDropData.keys.toMutableList()
        }
        return zone.data.monsterDropData.keys.filter { it != boss.type }
    }

    override fun canClear() = getMonsters(containsBoss = true).isEmpty() && bossSpawned && triggered && !isClear && !fail
}