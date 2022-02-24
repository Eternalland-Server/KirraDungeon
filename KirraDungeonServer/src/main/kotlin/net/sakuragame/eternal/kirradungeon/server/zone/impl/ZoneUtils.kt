package net.sakuragame.eternal.kirradungeon.server.zone.impl

import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import taboolib.common.platform.function.submit
import taboolib.platform.util.asLangText

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
            BossBar.setTime(it, lastTime--)
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