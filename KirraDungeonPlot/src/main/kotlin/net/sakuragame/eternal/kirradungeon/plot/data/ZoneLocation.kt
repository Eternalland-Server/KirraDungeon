package net.sakuragame.eternal.kirradungeon.plot.data

import net.sakuragame.eternal.kirradungeon.plot.splitWithNoSpace
import org.bukkit.Location
import org.bukkit.World

/**
 * KirraPlotZone
 * net.sakuragame.eternal.kirraplotzone.data.ZoneLocation
 *
 * @author kirraObj
 * @since 2021/11/30 19:02
 */
data class ZoneLocation(val x: Double, val y: Double, val z: Double, val yaw: Float, val pitch: Float) {

    companion object {
        fun parseToZoneLocation(loc: Location) = ZoneLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)

        fun parseToZoneLocation(string: String): ZoneLocation? {
            val splitString = string.splitWithNoSpace(",")
            if (splitString.size < 5) return null
            return ZoneLocation(splitString[0].toDouble(),
                splitString[1].toDouble(),
                splitString[2].toDouble(),
                splitString[3].toFloat(),
                splitString[4].toFloat())
        }
    }

    fun toBukkitLocation(world: World) = Location(world, x, y, z, yaw, pitch)

    override fun toString() = "$x, $y, $z, $yaw, $pitch"
}