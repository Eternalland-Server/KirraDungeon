package net.sakuragame.eternal.kirradungeon.client

import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonLoader
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamNumData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.getIndex
import org.bukkit.entity.Player

@Suppress("SpellCheckingInspection")
object KirraDungeonClientAPI {

    val fatigueMaxValue by lazy {
        KirraDungeonClient.conf.getInt("settings.fatigue.max-value")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val fatigueMinutes by lazy {
        KirraDungeonClient.conf.getInt("settings.fatigue.recover.period") * 60
    }

    fun getRecoverFatigueBySeconds(secs: Int): Int {
        return (secs / fatigueMinutes) * KirraDungeonClient.conf.getInt("settings.fatigue.recover.value")
    }

    /**
     * 给玩家打开指定界面.
     *
     * @param player 玩家
     * @param dungeonId 副本 id
     * @return 是否成功打开
     */
    fun openUI(player: Player, dungeonId: String): Boolean {
        val paramData = doSearch(dungeonId) ?: return false
        FunctionDungeon.openAssignGUI(player, paramData.toData, paramData)
        return true
    }

    /**
     * 根据 id 来获取内部子界面实例.
     *
     * @param dungeonId 副本 id
     * @return 内部子界面
     */
    fun getDungenSubScreenById(dungeonId: String): DungeonSubScreen? {
        val paramData = doSearch(dungeonId) ?: return null
        return paramData.subScreen
    }

    private fun doSearch(dungeonId: String): ParamData? {
        DungeonLoader.parentScreens.forEach firstForeach@{ (category, parentScreen) ->
            parentScreen.forEachIndexed secondForeach@{ secondIndex, dungeonScreen ->
                dungeonScreen.dungeonSubScreens.forEach thirdForeach@{ (thirdIndex, subScreen) ->
                    val name = subScreen?.name ?: return@thirdForeach
                    if (name.contains(dungeonId)) {
                        val numData = ParamNumData(category.getIndex(), secondIndex + 1, thirdIndex + 1, 1)
                        return ParamData(numData, numData, category, dungeonScreen, subScreen)
                    }
                }
            }
        }
        return null
    }
}
