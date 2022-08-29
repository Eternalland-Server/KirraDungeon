package net.sakuragame.eternal.kirradungeon.server.zone.impl

import net.sakuragame.eternal.justmessage.screen.hud.BossBar
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.Loader
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common.platform.function.submit
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.platform.util.asLangText
import java.io.File

fun IDungeon.startCountdown() {
    submit(async = true, delay = 20L, period = 20L) {
        if (canDel() || isClear || fail) {
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
    val file = getFile(id)
    val section = file.getConfigurationSection("wave") ?: return null
    return section.getKeys(false)
        .map { it.toInt() }
        .maxOf { it } + 1
}

fun spawnDungeonMob(loc: Location, type: String, level: Int = 1): LivingEntity {
    val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(type, loc, level) as LivingEntity
    val zoneLoc = ZoneLocation.parseToZoneLocation(loc).toString()
    entity.setMetadata("ORIGIN_LOC", FixedMetadataValue(KirraDungeonServer.plugin, zoneLoc))
    return entity
}

@Suppress("DuplicatedCode")
fun getFile(id: String): ConfigFile {
    val find = Loader.files.find { it.name == id }
    return when {
        find != null -> find
        else -> {
            val file = Configuration.loadFromFile(File(KirraDungeonServer.plugin.dataFolder, "zones/$id.json"), Type.JSON)
            Loader.files += file
            file
        }
    }
}