package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen

import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory

/**
 * DungeonScreen
 *
 * @property defaultSelectScreen 默认选择的 DungeonSubScreen.
 * @property category 分类.
 * @property priority 优先级.
 * @property name 显示名称.
 * @property mapBgPath 地图路径.
 * @property dungeonSubScreens 子界面表.
 */
data class DungeonScreen(
    val defaultSelectScreen: Int = 1,
    val category: DungeonCategory,
    val priority: Int,
    val name: String,
    val mapBgPath: String,
    val dungeonSubScreens: MutableMap<Int, DungeonSubScreen?> = mutableMapOf(),
)