package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

class UnlimitedDungeon(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IDungeon {

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

    override val trigger = null

    override var bossUUID: UUID = UUID.randomUUID()!!

    override var failTime: Int = 60

    override var failThread: PlatformExecutor.PlatformTask? = null

    /**
     * 当前挑战楼层.
     * 每次挑战结束都会 + 1, 并重新生成一只属性 + 50% 的 BOSS.
     */
    var currentFloor = 1

    override fun onPlayerJoin() {
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
}