package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.sub

import net.sakuragame.eternal.kirradungeon.client.getCurrentHour

data class ScreenLimitTime(var from: Int, var to: Int) {

    fun isActive() = this.from != 0 && this.to != 0

    fun isInLimitTime(): Boolean {
        val currentHour = getCurrentHour()
        if (currentHour in from until to) {
            return true
        }
        return false
    }
}