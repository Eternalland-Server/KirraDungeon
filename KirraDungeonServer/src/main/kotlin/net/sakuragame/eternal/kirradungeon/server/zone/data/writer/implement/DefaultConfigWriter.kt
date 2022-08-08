package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper

object DefaultConfigWriter : WriteHelper {

    fun set(id: String, name: String) {
        // 类型
        data["$id.type"] = "DEFAULT"
        // 副本时间
        data["$id.max-last-time"] = "300"
        // 天空数据
        data["$id.change-sky-color.enabled"] = true
        data["$id.change-sky-color.value"] = "7, 4"
        // 名字
        data["$id.name"] = name
        // 怪物
        data["$id.mobs"] = mutableListOf<String>()
        // 怪物首领
        data["$id.boss.id"] = ""
        data["$id.boss.loc"] = ZoneLocation(0.0, 60.0, 0.0, -90.0f, 0.0f).toString()
        // 坐标
        data["$id.spawn-loc"] = ZoneLocation(0.0, 60.0, 0.0, -90.0f, 0.0f).toString()
    }
}