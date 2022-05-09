package net.sakuragame.eternal.kirradungeon.server.zone.impl

import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import taboolib.common.platform.function.submit
import taboolib.platform.util.asLangText

fun IDungeon.startCountdown() {
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
            BossBar.setTime(it, lastTime--)
        }
    }
}

fun IDungeon.showResurgenceTitle() {
    submit(async = true, delay = 100L) {
        getPlayers().forEach {
            it.sendTitle("", it.asLangText("message-player-can-resurgence"), 5, 25, 0)
        }
    }
}

fun IDungeon.runOverTimeCheck() {
    submit(async = true, delay = 1000) {
        if (Zone.editingDungeonWorld != null) {
            return@submit
        }
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