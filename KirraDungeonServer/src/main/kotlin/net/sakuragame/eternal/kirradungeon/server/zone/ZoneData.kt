package net.sakuragame.eternal.kirradungeon.server.zone

import com.dscalzi.skychanger.core.api.SkyPacket

/**
 * KirraZones
 * net.sakuragame.kirrazones.server.zone.ZoneData
 *
 * @author kirraObj
 * @since 2021/11/8 9:26
 */
data class ZoneData(
    val entityMap: MutableMap<ZoneLocation, ZoneEntityPair>,
    val spawnLoc: ZoneLocation,
    val skyData: ZoneSkyData? = null,
) {

    fun isCustomSkyEnabled() = this.skyData != null

    companion object {

        data class ZoneEntityPair(val mobType: String, val amount: Int)

        data class ZoneSkyData(val packetType: SkyPacket, val value: Float) {

            override fun toString() = "${packetType.value}, $value"
        }
    }
}

