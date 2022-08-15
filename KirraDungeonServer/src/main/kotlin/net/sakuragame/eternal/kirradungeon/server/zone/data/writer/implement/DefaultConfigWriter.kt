package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object DefaultConfigWriter : WriteHelper {

    fun set(id: String, name: String) {
        val file = getFile(id)
        // 类型
        file["$id.type"] = "DEFAULT"
        // 副本时间
        file["$id.max-last-time"] = "300"
        // 天空数据
        file["$id.change-sky-color.enabled"] = true
        file["$id.change-sky-color.value"] = "7, 4"
        // 名字
        file["$id.name"] = name
        // 怪物
        file["$id.mobs"] = mutableListOf<String>()
        // 怪物首领
        file["$id.boss.id"] = ""
        file["$id.boss.loc"] = ZoneLocation(0.0, 60.0, 0.0, -90.0f, 0.0f).toString()
        // 坐标
        file["$id.spawn-loc"] = ZoneLocation(0.0, 60.0, 0.0, -90.0f, 0.0f).toString()
    }
}