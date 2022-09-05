package net.sakuragame.eternal.kirradungeon.server.zone.impl.type

import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeAdvancement
import com.gmail.berndivader.mythicmobsext.volatilecode.v1_12_R1.advancement.FakeDisplay
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.justlevel.api.JustLevelAPI
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.IDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.runOverTimeCheck
import net.sakuragame.eternal.kirradungeon.server.zone.impl.spawnDungeonMob
import net.sakuragame.eternal.kirradungeon.server.zone.impl.startCountdown
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.chat.colored
import java.util.*

class SpecialDungeon(override val zone: Zone, override val dungeonWorld: DungeonWorld) : IDungeon {

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

    override fun init() {
        startCountdown()
    }

    override fun onPlayerJoin(player: Player) {
        FakeAdvancement(FakeDisplay(Material.BUCKET, "&7&o愿筒子护佑你, 年轻人.".colored(), "", FakeDisplay.AdvancementFrame.GOAL, null))
            .displayToast(player)
        val multiplier = zone.data.stagedMultiplier ?: return
        val playerStage = JustLevelAPI.getTotalStage(player)
        val monsterIds = getMonsterIdsFromZone()
        zone.data.monsterData.mobList.forEach { mob ->
            repeat(mob.amount) {
                val level = multiplier.random() * playerStage
                val entity = spawnDungeonMob(mob.loc.toBukkitLocation(player.world), monsterIds.random(), level)
                monsterUUIDList += entity.uniqueId
            }
        }
    }

    override fun canClear(): Boolean {
        return false
    }

    override fun clear() {
        error("not reachable")
    }

    private fun getMonsterIdsFromZone(): MutableSet<String> {
        return zone.data.monsterDropData.keys
    }
}