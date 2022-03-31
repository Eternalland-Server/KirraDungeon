package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen

import net.sakuragame.eternal.kirradungeon.client.getCurrentHour

/**
 * 副本房间实例.
 *
 * @property name 名称.
 * @property iconPath 图标路径.
 * @property description 描述.
 * @property frameVisible 是否显示边框.
 * @property isSingle 副本类型是否为单个, 若为单个仅会渲染
 * @property forceLock 是否强制锁定.
 * @property teleportType 传送类别.
 * @property teleportData 传送信息.
 * @property droppedItems 该副本所掉落的物品.
 * @property limitTime 副本限制开放时间.
 * @property limitRealm 副本限制境界.
 */
@Suppress("SpellCheckingInspection")
data class DungeonSubScreen(
    val name: String,
    val iconPath: String,
    val description: ScreenDescription,
    val frameVisible: Boolean = false,
    val isSingle: Boolean = false,
    val forceLock: Boolean = false,
    val forceEmpty: Boolean = false,
    val teleportType: ScreenTeleportType,
    val teleportData: String,
    val droppedItems: List<String> = mutableListOf(),
    val limitTime: ScreenLimitTime,
    val limitRealm: ScreenLimitRealm
) {

    enum class ScreenTeleportType {
        DUNGEON, SERVER, COORD
    }

    data class ScreenDescription(val text: List<String>, val bgPath: String)

    data class ScreenLimitTime(var from: Int, var to: Int) {

        fun isActive() = this.from != 0 && this.to != 0

        fun isInLimitTime(): Boolean {
            val currentHour = getCurrentHour()
            if (currentHour in from until to) {
                return true
            }
            return false
        }
    }

    data class ScreenLimitRealm(val realm: Int)
}
