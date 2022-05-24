package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.Bukkit
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

/**
 * 默认副本, 副本实现类其中之一.
 */
class DefaultDungeon(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IDungeon {

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
        // 移除未经过 MythicMobDeathEvent 死亡的实体。
        submit(async = true, delay = 0L, period = 20L) {
            if (canDel() || isClear || fail) {
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

    override fun canClear() = getMonsters(containsBoss = true).isEmpty() && !isClear && !fail
}
