package net.sakuragame.eternal.kirradungeon.server.zone.impl

import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Profile
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.SpecialZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.UnlimitedZone
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.WaveZone
import taboolib.common.platform.function.submit
import taboolib.platform.util.asLangText

fun Profile.getIZone(): IZone? {
    return when (zoneType) {
        ZoneType.DEFAULT -> DefaultZone.getByPlayer(player.uniqueId) ?: return null
        ZoneType.SPECIAL -> SpecialZone.getByPlayer(player.uniqueId) ?: return null
        ZoneType.UNLIMITED -> UnlimitedZone.getByPlayer(player.uniqueId) ?: return null
        ZoneType.WAVE -> WaveZone.getByPlayer(player.uniqueId) ?: return null
    }
}
fun IZone.startCountdown() {
    submit(async = true, delay = 20L, period = 20L) {
        if (canDel() || isClear || isFail) {
            cancel()
            return@submit
        }
        if (isAllPlayersDead()) {
            return@submit
        }
        if (lastTime <= 0) {
            submit(async = false) {
                fail(FailType.OVERTIME)
            }
            cancel()
            return@submit
        }
        getPlayers().forEach {
            BossBar.open(it, lastTime--)
        }
    }
}

fun IZone.showResurgenceTitle() {
    submit(async = true, delay = 100L) {
        getPlayers().forEach {
            it.sendTitle("", it.asLangText("message-player-can-resurgence"), 5, 25, 0)
        }
    }
}

fun IZone.runOverTimeCheck() {
    submit(async = true, delay = 1000) {
        if (canDel()) {
            del()
            cancel()
            return@submit
        }
    }
}

fun getWaveIndex(id: String): Int? {
    val section = KirraDungeonServer.data.getConfigurationSection("$id.wave") ?: return null
    return section.getKeys(false)
        .map { it.toInt() }
        .maxOf { it } + 1
}