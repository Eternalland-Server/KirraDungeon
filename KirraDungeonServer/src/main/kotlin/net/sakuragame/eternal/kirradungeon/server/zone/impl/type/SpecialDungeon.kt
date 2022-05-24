package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

class SpecialDungeon(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IDungeon {

    init {
        runOverTimeCheck()
    }

    override val createdTime = System.currentTimeMillis()

    override var init = false

    override var isClear = false

    override var fail = false

    override var lastTime = zone.data.maxLastTime

    override val playerUUIDList = mutableListOf<UUID>()

    override val monsterUUIDList = mutableListOf<UUID>()

    override var bossUUID = UUID.randomUUID()!!

    override var failTime = 60

    override var failThread: PlatformExecutor.PlatformTask? = null

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
}