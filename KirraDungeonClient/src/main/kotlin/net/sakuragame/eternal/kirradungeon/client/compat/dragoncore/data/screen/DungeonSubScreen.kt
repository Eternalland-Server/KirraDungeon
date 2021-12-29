package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen

/**
 * 副本房间实例.
 *
 * @property name 名称.
 * @property iconPath 图标路径.
 * @property description 描述.
 * @property frameVisible 是否显示边框.
 * @property lockedByProgress
 * @property isSingle
 * @property forceLock
 * @property dungeonId
 */
data class DungeonSubScreen(
    val name: String,
    val iconPath: String,
    val description: ScreenDescription,
    val frameVisible: Boolean = false,
    val lockedByProgress: Boolean = true,
    val isSingle: Boolean = false,
    val forceLock: Boolean = false,
    val dungeonId: String? = null,
) {

    data class ScreenDescription(val text: List<String>, val bgPath: String)
}
