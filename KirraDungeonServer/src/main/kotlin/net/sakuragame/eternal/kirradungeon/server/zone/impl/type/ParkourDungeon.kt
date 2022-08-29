package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeAdvancement
import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeDisplay
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import net.sakuragame.eternal.waypoints.api.WaypointsAPI
import net.sakuragame.eternal.waypoints.core.IconType
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.chat.colored
import java.util.*

class ParkourDungeon(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IDungeon {

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

    override var bossUUID = UUID.randomUUID()!!

    override var failTime = 60

    override var failThread: PlatformExecutor.PlatformTask? = null

    val locationRecorder = mutableMapOf<UUID, Location>()

    private val goalLocation: ZoneLocation?
        get() {
            val str = zone.data.metadataMap["goalPoint"] ?: return null
            return ZoneLocation.parseToZoneLocation(str)
        }

    override fun init() {
        startCountdown()
    }

    override fun onPlayerJoin(player: Player) {
        FakeAdvancement(FakeDisplay(Material.BUCKET, "&7&o愿筒子护佑你, 年轻人.".colored(), "", FakeDisplay.AdvancementFrame.GOAL, null))
            .displayToast(player)
        WaypointsAPI.navPointer(player, "parkour", IconType.Normal, goalLocation?.toBukkitLocation(player.world) ?: return, 1.0, listOf("终点(%distance%)"))
    }

    override fun canClear(): Boolean {
        return false
    }
}