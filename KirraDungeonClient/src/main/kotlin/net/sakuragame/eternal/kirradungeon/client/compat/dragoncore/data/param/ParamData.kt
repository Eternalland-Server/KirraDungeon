package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param

import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen

data class ParamData(
    val fromData: ParamNumData,
    val toData: ParamNumData,
    val category: DungeonCategory,
    val screen: DungeonScreen,
    val subScreen: DungeonSubScreen,
)