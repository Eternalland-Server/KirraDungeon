package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen

/**
 * 副本房间实例.
 *
 * @property name 名称.
 * @property iconPath 图标路径.
 * @property description 描述.
 * @property frameVisible 是否显示边框.
 * @property isSingle 副本类型是否为单个, 若为单个仅会渲染
 * @property forceLock 是否强制锁定.
 * @property dungeonId 与 KirraDungeon Hook 的 ID.
 */
data class DungeonSubScreen(
    val name: String,
    val iconPath: String,
    val description: ScreenDescription,
    val frameVisible: Boolean = false,
    val isSingle: Boolean = false,
    val forceLock: Boolean = false,
    val forceEmpty: Boolean = false,
    val dungeonId: String? = null,
) {

    data class ScreenDescription(val text: List<String>, val bgPath: String)
}
