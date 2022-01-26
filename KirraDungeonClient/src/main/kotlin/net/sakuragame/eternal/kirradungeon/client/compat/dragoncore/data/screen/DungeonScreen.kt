package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen

import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory

/**
 * DungeonScreen
 *
 * @property defaultIndex 默认排序.
 * @property category 分类.
 * @property priority 优先级.
 * @property name 显示名称.
 * @property mapBgPath 地图路径.
 * @property dungeonSubScreens 龙核子界面列表.
 */
data class DungeonScreen(
    val defaultIndex: Int = 1,
    val category: DungeonCategory,
    val priority: Int,
    val name: String,
    val mapBgPath: String,
    val dungeonSubScreens: Array<DungeonSubScreen?> = arrayOfNulls(5),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DungeonScreen) return false

        if (category != other.category) return false
        if (priority != other.priority) return false
        if (name != other.name) return false
        if (mapBgPath != other.mapBgPath) return false
        if (!dungeonSubScreens.contentEquals(other.dungeonSubScreens)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = category.hashCode()
        result = 31 * result + priority
        result = 31 * result + name.hashCode()
        result = 31 * result + mapBgPath.hashCode()
        result = 31 * result + dungeonSubScreens.contentHashCode()
        return result
    }
}