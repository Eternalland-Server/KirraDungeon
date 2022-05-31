package net.sakuragame.eternal.kirradungeon.server.zone.data.sub

import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import org.bukkit.Material

data class ZoneBlockData(val loc: ZoneLocation, val material: Material) {

    override fun toString() = "$loc @ ${material.name}"

    companion object {

        fun parseFromString(str: String): ZoneBlockData? {
            val split = str.split(" @ ")
            if (split.size != 2) {
                return null
            }
            val loc = ZoneLocation.parseToZoneLocation(split[0]) ?: return null
            val material = Material.getMaterial(split[1])
            return ZoneBlockData(loc, material)
        }
    }
}