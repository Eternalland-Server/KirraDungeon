package net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement

import net.sakuragame.eternal.kirradungeon.server.splitWithNoSpace
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.WriteHelper
import net.sakuragame.eternal.kirradungeon.server.zone.sync.ZoneCondition

object ConditionWriter : WriteHelper {

    fun read(id: String): MutableList<ZoneCondition> {
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
                val number = NumberWriter.read(id)
                conditionList += ZoneCondition(key, dailyCounts, feeToTypeMap, itemIDToAmountMap, number)
            }
        }
    }
}