package net.sakuragame.eternal.kirradungeon.server.zone

import com.dscalzi.skychanger.core.api.SkyPacket
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer.data
import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneModelData
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneMonsterData
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneSkyData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneBossData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.ZoneMobData
import net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave.ZoneWaveData
import net.sakuragame.eternal.kirradungeon.server.zone.sync.ZoneCondition
import org.bukkit.Location

object FunctionZone {

    // 增加副本出生点怪物
    fun addMob(zone: Zone, loc: ZoneLocation, id: String, amount: Int) {
        val mobList = arrayListOf<String>().also {
            it.addAll(data.getStringList("${zone.id}.mobs"))
        }
        mobList.add("$loc; $id; $amount")
        data["${zone.id}.mobs"] = mobList
        Zone.i()
    }

    // 增加波次副本怪物
    fun addWaveMob(wave: Int, zone: Zone, monsterId: String, amount: Int, health: Double) {
        val id = zone.id
        val mobList = arrayListOf<String>().also {
            val strList = data.getStringList("${zone.id}.wave.$wave.monsters")
            if (strList.isNotEmpty()) {
                it.addAll(data.getStringList("${zone.id}.wave.$wave.monsters"))
            }
        }
        mobList += "$monsterId@$amount@$health"
        data["$id.wave.$wave.monsters"] = mobList
        Zone.i()
    }

    // 增加波次副本随机刷新点
    fun addWaveLoc(zone: Zone, loc: Location) {
        val id = zone.id
        val locList = arrayListOf<String>().also {
            val strList = data.getStringList("$id.wave-locs")
            if (strList.isNotEmpty()) {
                it.addAll(data.getStringList("$id.wave-locs"))
            }
        }
        locList.add(ZoneLocation.parseToZoneLocation(loc).toString())
        data["$id.wave-locs"] = locList
        Zone.i()
    }

    // 设置波次副本首领
    fun setWaveBoss(wave: Int, zone: Zone, bossId: String, health: Double) {
        val id = zone.id
        data["$id.wave.$wave.boss"] = "$bossId@$health"
        Zone.i()
    }

    // 设置副本模型
    fun setModel(zone: Zone, id: String, modelId: String, loc: ZoneLocation) {
        data["${zone.id}.model.$id"] = "$modelId@$loc"
        Zone.i()
    }

    // 移除副本模型
    fun removeModel(zone: Zone, id: String) {
        data["${zone.id}.model.$id"] = null
        Zone.i()
    }

    // 设置副本怪物首领
    fun setBoss(zone: Zone, loc: ZoneLocation, id: String) {
        data["${zone.id}.boss.id"] = id
        data["${zone.id}.boss.loc"] = loc.toString()
        Zone.i()
    }

    // 设置副本出生点坐标
    fun setLoc(zone: Zone, loc: ZoneLocation) {
        data["${zone.id}.spawn-loc"] = loc.toString()
        Zone.i()
    }

    // 设置副本天空颜色
    fun setSkyColor(zone: Zone, skyData: ZoneSkyData) {
        data["${zone.id}.change-sky-color.enabled"] = true
        data["${zone.id}.change-sky-color.value"] = skyData.toString()
        Zone.i()
    }

    // 向配置文件写入默认数据, 需要在后面自行更改
    fun writeDefaultDataToConf(id: String, name: String) {
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
        // 进入条件
        data["$id.conditions.default.daily-count"] = 10
        data["$id.conditions.default.fee"] = mutableListOf<String>().also {
            it.add("coins; 100")
        }
        data["$id.conditions.default.items"] = mutableListOf<String>().also {
            it.add("test-item; 1")
        }
        // 坐标
        data["$id.spawn-loc"] = ZoneLocation(0.0, 60.0, 0.0, -90.0f, 0.0f).toString()
    }

    // 从配置文件读取副本类型
    fun readType(id: String): ZoneType {
        return ZoneType.values().find { it.name == data.getString("$id.type")!! } ?: ZoneType.DEFAULT
    }

    // 从配置文件读取模型数据
    fun readModels(id: String): List<ZoneModelData> {
        val models = mutableListOf<ZoneModelData>()
        val sections = data.getConfigurationSection("$id.model")?.getKeys(false) ?: return emptyList()
        sections.forEach {
            val split = data.getString("$id.model.$it")?.split("@") ?: return@forEach
            val modelId = split[0]
            val loc = ZoneLocation.parseToZoneLocation(split[1]) ?: return@forEach
            Zone.editingModelIds += it
            models += ZoneModelData(it, modelId, loc)
        }
        return models
    }

    // 从配置文件读取攻克副本时间
    fun readMaxLastTime(id: String) = data.getInt("$id.max-last-time")

    // 从配置文件读取波次数据
    fun readWaveData(id: String): List<ZoneWaveData>? {
        if (readType(id) != ZoneType.WAVE) {
            return null
        }
        val waveList = mutableListOf<ZoneWaveData>()
        val sections = data.getConfigurationSection("$id.wave") ?: return null
        sections.getKeys(false).forEach {
            val monsterDataList = mutableListOf<ZoneWaveData.ZoneWaveMonsterData>()
            val index = it.toInt()
            data.getStringList("$id.wave.$index.monsters").forEach { str ->
                val split = str.split("@")
                val monsterId = split[0]
                val amount = split[1].toIntOrNull() ?: 1
                val health = split[2].toDoubleOrNull() ?: 1.0
                monsterDataList += ZoneWaveData.ZoneWaveMonsterData(monsterId, amount, health)
            }
            val boss = data.getString("$id.wave.$index.boss") ?: return null
            val split = boss.split("@")
            val bossId = split[0]
            val bossHealth = split[1].toDoubleOrNull() ?: 1.0
            val bossData = ZoneWaveData.ZoneWaveBossData(bossId, bossHealth)
            waveList += ZoneWaveData(monsterDataList, bossData)
        }
        return waveList
    }

    // 获取波次坐标
    fun readWaveLocs(id: String): List<ZoneLocation>? {
        val locList = mutableListOf<ZoneLocation>()
        data.getStringList("$id.wave-locs").forEach {
            locList.add(ZoneLocation.parseToZoneLocation(it) ?: return@forEach)
        }
        if (locList.isEmpty()) {
            return null
        }
        return locList
    }

    // 从配置文件读取副本进入条件数据
    fun readConditions(id: String): MutableList<ZoneCondition> {
        return mutableListOf<ZoneCondition>().also conditionAlso@{ conditionList ->
            data.getConfigurationSection("$id.conditions")!!.getKeys(false).forEach sectionForeach@{ key ->
                val dailyCounts = data.getInt("$id.conditions.$key.daily-count")
                val feeToTypeMap = mutableMapOf<String, Double>().also feeToTypeAlso@{ feeToTypeMap ->
                    data.getStringList("$id.conditions.$key.fee").forEach feeToTypeForeach@{ feeString ->
                        val splitString = feeString.splitWithNoSpace(";")
                        if (splitString.size < 2) return@feeToTypeForeach
                        feeToTypeMap[splitString[0]] = splitString[1].toDouble()
                    }
                }
                val itemIDToAmountMap = mutableMapOf<String, Int>().also itemIDToAmountAlso@{ itemIDToAmountMap ->
                    data.getStringList("$id.conditions.$key.items").forEach itemIDToAmountForeach@{ itemString ->
                        val splitString = itemString.splitWithNoSpace(";")
                        if (splitString.size < 2) return@itemIDToAmountForeach
                        itemIDToAmountMap[splitString[0]] = splitString[1].toInt()
                    }
                }
                val number = readNumber(id)
                conditionList += ZoneCondition(key, dailyCounts, feeToTypeMap, itemIDToAmountMap, number)
            }
        }
    }

    // 从配置文件读取副本天空数据
    fun readSkyData(id: String): ZoneSkyData? {
        if (!data.getBoolean("$id.change-sky-color.enabled")) {
            return null
        }
        val split = data.getString("$id.change-sky-color.value")!!.splitWithNoSpace(",")
        return ZoneSkyData(
            if (split[0].toInt() == 7) {
                SkyPacket.RAIN_LEVEL_CHANGE
            } else {
                SkyPacket.THUNDER_LEVEL_CHANGE
            }, split[1].toFloat()
        )
    }

    // 从配置文件读取数字
    fun readNumber(id: String): Int {
        return data.getInt("$id.number")
    }

    // 从配置文件读取怪物图标
    fun readIcon(id: String): Int {
        return data.getInt("$id.icon")
    }

    // 从配置文件读取重生时间
    fun readResurgenceTime(id: String): Int {
        return data.getInt("$id.resurgence-time", 0)
    }

    // 从配置文件读取副本出生点
    fun readSpawnLoc(id: String) = ZoneLocation.parseToZoneLocation(data.getString("$id.spawn-loc")!!)

    // 从配置文件获取副本怪物数据
    fun readMonsterData(id: String): ZoneMonsterData {
        val mobDataList = mutableListOf<ZoneMobData>().also { mobDataList ->
            data.getStringList("$id.mobs").forEach string@{ string ->
                val splitString = string.splitWithNoSpace(";")
                if (splitString.size < 3) return@string
                val loc = ZoneLocation.parseToZoneLocation(splitString[0])!!
                val monsterId = splitString[1]
                val amount = splitString[2].toInt()
                mobDataList += ZoneMobData(loc, monsterId, amount)
            }
        }
        val bossData = ZoneBossData(
            ZoneLocation.parseToZoneLocation(data.getString("$id.boss.loc")!!)!!,
            data.getString("$id.boss.id")!!
        )
        return ZoneMonsterData(bossData, mobDataList)
    }
}