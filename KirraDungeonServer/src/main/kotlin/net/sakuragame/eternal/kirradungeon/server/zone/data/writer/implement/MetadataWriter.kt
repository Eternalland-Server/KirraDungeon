package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object MetadataWriter : WriteHelper {

    fun set(zone: Zone, key: String, value: String) {
        val file = getFile(zone.id)
        file["metadata.$key"] = value
        reload()
    }

    fun remove(zone: Zone, key: String) {
        val file = getFile(zone.id)
        file["metadata.$key"] = null
        reload()
    }

    fun read(id: String): MutableMap<String, String> {
        val toReturn = mutableMapOf<String, String>()
        val file = getFile(id)
        val section = file.getConfigurationSection("metadata")?.getKeys(false) ?: return toReturn
        section.forEach {
            toReturn[it] = file.getString("metadata.$it") ?: return@forEach
        }
        return toReturn
    }
}