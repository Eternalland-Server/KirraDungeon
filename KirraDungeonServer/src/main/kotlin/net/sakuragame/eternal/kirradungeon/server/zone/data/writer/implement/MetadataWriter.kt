package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object MetadataWriter : WriteHelper {

    fun set(zone: Zone, key: String, value: String) {
        data["${zone.id}.metadata.$key"] = value
        reload()
    }

    fun remove(zone: Zone, key: String) {
        data["${zone.id}.medata.$key"] = null
        reload()
    }

    fun read(id: String): MutableMap<String, String> {
        val toReturn = mutableMapOf<String, String>()
        val section = data.getConfigurationSection("$id.metadata")?.getKeys(false) ?: return toReturn
        section.forEach {
            toReturn[it] = data.getString("$id.metadata.$it") ?: return@forEach
        }
        return toReturn
    }
}