package net.sakuragame.eternal.kirradungeon.server.zone.data.writer

import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import taboolib.module.configuration.Configuration

interface WriteHelper {

    val data: Configuration
        get() = KirraDungeonServer.data

    fun reload() {
        Zone.i()
    }
}