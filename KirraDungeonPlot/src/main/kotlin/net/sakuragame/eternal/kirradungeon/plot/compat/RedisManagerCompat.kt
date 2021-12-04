package net.sakuragame.eternal.kirradungeon.plot.compat

import net.sakuragame.eternal.kirradungeon.plot.KirraDungeonPlot
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object RedisManagerCompat {

    @Awake(LifeCycle.ENABLE)
    fun init() {
        KirraDungeonPlot.redisConn.async().lpush("KirraDungeonNames", "nergigante_dragon")
    }
}