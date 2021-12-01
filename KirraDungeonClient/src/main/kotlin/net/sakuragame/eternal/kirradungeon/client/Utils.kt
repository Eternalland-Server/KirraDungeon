package net.sakuragame.eternal.kirradungeon.client

import java.util.*

private val date by lazy {
    Date().also {
        it.hours = 4
        it.minutes = 0
    }.time
}

fun getTodayTimeUnix() = date