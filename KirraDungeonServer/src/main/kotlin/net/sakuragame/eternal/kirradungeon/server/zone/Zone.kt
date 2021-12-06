package net.sakuragame.eternal.kirradungeon.server.zone

import com.dscalzi.skychanger.core.api.SkyPacket
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.sync.ZoneCondition
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored

/**
 * KirraDungeons
 * net.sakuragame.KirraDungeons.server.zone.Zone
 *
 * @author kirraObj
 * @since 2021/11/8 4:39
 */
data class Zone(val id: String, val name: String, val data: ZoneData) {

    companion object {

        var editingDungeonWorld: DungeonWorld? = null

        val zones = mutableListOf<Zone>()

        fun getByName(name: String) = zones.firstOrNull { it.name == name }

        fun getByID(id: String) = zones.firstOrNull { it.id == id }

        @Awake(LifeCycle.ENABLE)
        fun init() {
            clearAll()
            KirraDungeonServer.data.getKeys(false).forEach {
                val id = it
                val name = KirraDungeonServer.data.getString("$it.name") ?: return@forEach
                updateZoneConditions(id)
                zones += Zone(id, name.colored(), ZoneData(
                    getMobMap(id),
                    getMapSpawnLoc(id)!!,
                    getMapSkyData(id)
                ))
            }
        }

        fun clearAll() {
            zones.clear()
            ZoneCondition.doDataRecycle()
        }

        fun createZone(id: String, name: String) {
            writeDefaultZoneDataToConf(id, name)
            init()
        }

        // 增加副本出生点怪物.
        fun addZoneMob(zone: Zone, loc: ZoneLocation, mobType: String, mobAmount: Int) {
            val mobList = arrayListOf<String>().also {
                it.addAll(KirraDungeonServer.data.getStringList("${zone.id}.mobs"))
            }
            mobList.add("$loc; $mobType; $mobAmount")
            KirraDungeonServer.data.set("${zone.id}.mobs", mobList)
            init()
        }

        // 设置副本出生点坐标.
        fun setZoneLoc(zone: Zone, loc: ZoneLocation) {
            KirraDungeonServer.data.set("${zone.id}.spawn-loc", loc.toString())
            init()
        }

        // 设置副本天空颜色.
        fun setZoneSkyColor(zone: Zone, zoneSkyData: ZoneData.Companion.ZoneSkyData) {
            KirraDungeonServer.data.set("${zone.id}.change-sky-color.enabled", true)
            KirraDungeonServer.data.set("${zone.id}.change-sky-color.value", zoneSkyData.toString())
            init()
        }

        // 更新副本进入要求数据, 并向 Client 端同步 Redis 数据.
        fun updateZoneConditions(id: String) {
            ZoneCondition.conditionMap.also { conditionMap ->
                conditionMap.clear()
                conditionMap.putAll(getZoneConditions(id))
            }
            ZoneCondition.syncToRedis()
        }

        // 向配置文件写入默认数据, 需要在后面自行更改.
        private fun writeDefaultZoneDataToConf(id: String, name: String) {
            // 天空数据.
            KirraDungeonServer.data["$id.change-sky-color.enabled"] = true
            KirraDungeonServer.data["$id.change-sky-color.value"] = "7, 4"
            // 名字.
            KirraDungeonServer.data["$id.name"] = name
            // 怪物.
            KirraDungeonServer.data["$id.mobs"] = mutableListOf<String>().also {
                it.add("")
            }
            // 进入条件.
            KirraDungeonServer.data["$id.conditions.default.daily-count"] = 10
            KirraDungeonServer.data["$id.conditions.default.fee"] = mutableListOf<String>().also {
                it.add("coins; 100")
            }
            KirraDungeonServer.data["$id.conditions.default.items"] = mutableListOf<String>().also {
                it.add("test-item; 1")
            }
            // 坐标.
            KirraDungeonServer.data["$id.spawn-loc"] = ZoneLocation(0.0, 60.0, 0.0, -90.0f, 0.0f).toString()
        }

        // 从配置文件读取副本进入条件数据.
        private fun getZoneConditions(id: String): MutableMap<String, MutableList<ZoneCondition>> {
            return mutableMapOf<String, MutableList<ZoneCondition>>().also { conditionMap ->
                conditionMap[id] = mutableListOf()
                KirraDungeonServer.data.getConfigurationSection("$id.conditions")!!.getKeys(false).forEach { key ->
                    val dailyCounts = KirraDungeonServer.data.getInt("$id.conditions.$key.daily-count")
                    val feeToTypeMap = mutableMapOf<String, Double>().also { feeToTypeMap ->
                        KirraDungeonServer.data.getStringList("$id.conditions.$key.fee").forEach { feeString ->
                            val splitString = feeString.splitWithNoSpace(";")
                            feeToTypeMap[splitString[0]] = splitString[1].toDouble()
                        }
                    }
                    val itemIDToAmountMap = mutableMapOf<String, Int>().also { itemIDToAmountMap ->
                        KirraDungeonServer.data.getStringList("$id.conditions.$key.items").forEach { itemString ->
                            val splitString = itemString.splitWithNoSpace(";")
                            itemIDToAmountMap[splitString[0]] = splitString[1].toInt()
                        }
                    }
                    // key = permissionName.
                    conditionMap[id]!!.add(ZoneCondition(key, dailyCounts, feeToTypeMap, itemIDToAmountMap))
                }
            }
        }

        // 从配置文件读取副本天空数据.
        private fun getMapSkyData(id: String): ZoneData.Companion.ZoneSkyData? {
            if (!KirraDungeonServer.data.getBoolean("$id.change-sky-color.enabled")) {
                return null
            }
            val split = KirraDungeonServer.data.getString("$id.change-sky-color.value")!!.splitWithNoSpace(",")
            return ZoneData.Companion.ZoneSkyData(
                if (split[0].toInt() == 7) {
                    SkyPacket.RAIN_LEVEL_CHANGE
                } else {
                    SkyPacket.THUNDER_LEVEL_CHANGE
                },
                split[1].toFloat()
            )
        }

        // 从配置文件读取副本出生点.
        private fun getMapSpawnLoc(id: String) = ZoneLocation.parseToZoneLocation(KirraDungeonServer.data.getString("$id.spawn-loc")!!)

        // 从配置文件获取副本怪物数据.
        private fun getMobMap(id: String): MutableMap<ZoneLocation, ZoneData.Companion.ZoneEntityPair> {
            return mutableMapOf<ZoneLocation, ZoneData.Companion.ZoneEntityPair>().also { mobMap ->
                KirraDungeonServer.data.getStringList("$id.mobs").forEach stringForeach@{ string ->
                    val splitString = string.splitWithNoSpace(";")
                    if (splitString.size < 3) return@stringForeach
                    val zoneLocation = ZoneLocation.parseToZoneLocation(splitString[0])!!
                    val mobType = splitString[1]
                    val mobAmount = splitString[2].toInt()
                    mobMap[zoneLocation] = ZoneData.Companion.ZoneEntityPair(mobType, mobAmount)
                }
            }
        }
    }

    override fun toString() = "Zone($id, ${name.uncolored()}, $data, ${ZoneCondition.getConditionByName(id) ?: ""})"
}