package net.sakuragame.eternal.kirradungeon.server.zone.data

import com.dscalzi.skychanger.core.api.SkyPacket

data class ZoneSkyData(val packetType: SkyPacket, val value: Float) {

    override fun toString() = "${packetType.value}, $value"
}