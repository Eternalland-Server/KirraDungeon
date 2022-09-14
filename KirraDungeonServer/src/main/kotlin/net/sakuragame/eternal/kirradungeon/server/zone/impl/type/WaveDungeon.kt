package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeAdvancement
import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeDisplay
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.gemseconomy.api.GemsEconomyAPI
import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency
import net.sakuragame.eternal.kirradungeon.server.UnitConvert
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.spawnDungeonMob
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.chat.colored
import taboolib.platform.util.title
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

    private val spawnPeriod: Long?
        get() = zone.data.metadataMap["waveSpawnPeriod"]?.toLongOrNull()

    var pickUpCoins = 0

    override fun init() {
        doWaveSpawn()
        startCoinsRunnable()
        startCountdown()
    }

    override fun onPlayerJoin(player: Player) {
        FakeAdvancement(FakeDisplay(Material.BUCKET, "&7&o愿筒子护佑你, 年轻人.".colored(), "", FakeDisplay.AdvancementFrame.GOAL, null))
            .displayToast(player)
    }

    override fun canClear(): Boolean {
        return false
    }

    private fun doWaveSpawn() {
        val period = spawnPeriod ?: return
        // 召唤副本宝箱
        submit(delay = 20L) {
            zone.data.monsterData.boss.apply {
                val boss = spawnDungeonMob(loc.toBukkitLocation(dungeonWorld.bukkitWorld), type, levelRange.random())
                monsterUUIDList += boss.uniqueId
            }
        }
        // 召唤副本小怪
        submit(period = period) {
            zone.data.monsterData.mobList.forEach { mob ->
                repeat(mob.amount) {
                    val monsterIds = getMonsterIdsFromZone()
                    val entity = spawnDungeonMob(mob.loc.toBukkitLocation(dungeonWorld.bukkitWorld), monsterIds.random(), mob.levelRange.random())
                    monsterUUIDList += entity.uniqueId
                }
            }
        }
    }

    private fun startCoinsRunnable() {
        submit(async = true, period = 5L) {
            if (canDel() || isClear || fail) {
                cancel()
                return@submit
            }
            if (isAllPlayersDead()) {
                return@submit
            }
            if (pickUpCoins == 0) {
                return@submit
            }
            getPlayers().forEach {
                it.title("", "&e当前获得了 &6&l${UnitConvert.doCommonInputInference(pickUpCoins.toDouble())}&e 金币.".colored(), 0, 30, 0)
            }
        }
    }

    override fun whenTeleportToSpawn() {
        getPlayers().forEach {
            GemsEconomyAPI.deposit(it.uniqueId, pickUpCoins.toDouble(), EternalCurrency.Coins)
        }
    }

    private fun getMonsterIdsFromZone(): MutableSet<String> {
        return zone.data.monsterDropData.keys
    }
}