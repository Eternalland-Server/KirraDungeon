package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.Bukkit
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

class UnlimitedZone(override val zone: Zone, override val dungeonWorld: DungeonWorld): IZone {

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

    /**
     * 当前挑战楼层.
     * 每次挑战结束都会 + 1, 并重新生成一只属性 + 50% 的 BOSS.
     */
    var currentFloor = 1

    override fun runTimer() {
        startCountdown()
        showResurgenceTitle()
    }

    override fun canClear(): Boolean {
        return false
    }

    override fun clear() {
        error("not reachable")
    }

    fun floorPlus1() {
        currentFloor++
    }

    @Suppress("DuplicatedCode")
    companion object {

        val unlimitedZones = mutableListOf<UnlimitedZone>()

        fun getByDungeonWorldUUID(uuid: UUID) = unlimitedZones.firstOrNull { it.dungeonWorld.uuid == uuid }

        fun getByPlayer(playerUUID: UUID): UnlimitedZone? {
            unlimitedZones.forEach { unlimitedZone ->
                if (unlimitedZone.playerUUIDList.find { it == playerUUID } != null) {
                    return unlimitedZone
                }
            }
            return null
        }

        fun getByMobUUID(mobUUID: UUID): UnlimitedZone? {
            unlimitedZones.forEach { unlimitedZone ->
                if (unlimitedZone.monsterUUIDList.find { it == mobUUID } != null) {
                    return unlimitedZone
                }
                if (unlimitedZone.bossUUID == mobUUID) {
                    return unlimitedZone
                }
            }
            return null
        }

        fun create(zone: Zone, dungeonWorld: DungeonWorld) {
            unlimitedZones += UnlimitedZone(zone, dungeonWorld)
        }
    }
}