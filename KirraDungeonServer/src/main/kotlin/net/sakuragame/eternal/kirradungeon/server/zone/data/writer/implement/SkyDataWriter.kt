package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import com.dscalzi.skychanger.core.api.SkyPacket
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneSkyData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object SkyDataWriter : WriteHelper {

    fun set(zone: Zone, skyData: ZoneSkyData) {
        data["${zone.id}.change-sky-color.enabled"] = true
        data["${zone.id}.change-sky-color.value"] = skyData.toString()
        reload()
    }

    fun read(id: String): ZoneSkyData? {
        if (!data.getBoolean("$id.change-sky-color.enabled")) {
            return null
        }
        val split = data.getString("$id.change-sky-color.value")!!.splitWithNoSpace(",")
        return ZoneSkyData(
            when (split[0].toInt()) {
                7 -> SkyPacket.RAIN_LEVEL_CHANGE
                else -> SkyPacket.THUNDER_LEVEL_CHANGE
            }, split[1].toFloat()
        )
    }
}