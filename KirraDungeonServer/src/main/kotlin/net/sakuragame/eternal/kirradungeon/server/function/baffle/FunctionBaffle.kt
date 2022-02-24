package net.sakuragame.eternal.kirradungeon.server.function.baffle

import taboolib.common5.Baffle
import java.util.concurrent.TimeUnit

object FunctionBaffle {

    val functionBaffle by lazy {
        Baffle.of(1, TimeUnit.SECONDS)
    }
}