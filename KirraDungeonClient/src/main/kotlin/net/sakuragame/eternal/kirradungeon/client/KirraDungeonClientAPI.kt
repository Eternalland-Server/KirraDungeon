package net.sakuragame.eternal.kirradungeon.client

import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.param.ParamNumData
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.function.FunctionDungeon
import org.bukkit.entity.Player

@Suppress("SpellCheckingInspection")
object KirraDungeonClientAPI {

    fun openUI(player: Player, category: Int, subCategory: Int, currentSelected: Int) {
        val numData = ParamNumData(category, subCategory, currentSelected, 1)
        FunctionDungeon.openAssignGUI(player, numData)
    }
}