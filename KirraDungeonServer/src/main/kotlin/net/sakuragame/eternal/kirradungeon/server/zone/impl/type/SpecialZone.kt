package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.showResurgenceTitle
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import taboolib.common.platform.service.PlatformExecutor
import java.util.*

class SpecialZone(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IZone {

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

    @Suppress("DuplicatedCode")
    companion object {

        val specialZones = mutableListOf<SpecialZone>()

        fun getByDungeonWorldUUID(uuid: UUID) = specialZones.firstOrNull { it.dungeonWorld.uuid == uuid }

        fun getByPlayer(playerUUID: UUID): SpecialZone? {
            specialZones.forEach { specialZone ->
                if (specialZone.playerUUIDList.find { it == playerUUID } != null) {
                    return specialZone
                }
            }
            return null
        }

        fun getByMobUUID(mobUUID: UUID): SpecialZone? {
            specialZones.forEach { specialZone ->
                if (specialZone.monsterUUIDList.find { it == mobUUID } != null) {
                    return specialZone
                }
                if (specialZone.bossUUID == mobUUID) {
                    return specialZone
                }
            }
            return null
        }

        fun create(zone: Zone, dungeonWorld: DungeonWorld) {
            specialZones += SpecialZone(zone, dungeonWorld)
        }
    }
}