package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object DungeonAPI {

    const val triggerKey = "K"

    enum class ParamType {
        UPDATE, JOIN, CLOSE
    }

    fun getDefaultScreen() = DungeonLoader.normalParentScreen[0]

    fun getDefaultSubScreen(screen: DungeonScreen?): DungeonSubScreen {
        if (screen == null) return getDefaultScreen().dungeonSubScreens[0]!!
        return screen.dungeonSubScreens[screen.defaultIndex]!!
    }

    fun getPluginParams(type: ParamType = ParamType.UPDATE) = SubmitParams().apply {
        addValue(KirraDungeonClient.plugin.name)
        addValue(type.name)
        add("global.dungeon_category")
        add("global.dungeon_sub_category")
        add("global.dungeon_current_selected")
    }

    @Suppress("UNREACHABLE_CODE")
    fun getDungeonCategory(index: Int) = when (index) {
        1 -> DungeonCategory.NORMAL
        2 -> DungeonCategory.TEAM
        3 -> DungeonCategory.ACTIVITY
        4 -> DungeonCategory.SPECIAL
        else -> throw error("out of dungeon category index, max is 4.")
    }

    fun getDungeonScreen(category: DungeonCategory, index: Int): DungeonScreen? {
        val parent = category.getParentScreen()
        return parent.getOrNull(index - 1)
    }

    fun getDropItemsByDungeonId(player: Player, dungeonId: String): HashMap<String, ItemStack> {
        val originList = ZaphkielAPI.registeredItem.values
            .filter { it.group?.name == dungeonId }
            .map { it.buildItemStack(player) }
        return HashMap<String, ItemStack>().also {
            for (index in 1..6) {
                it["dungeon_drop_${index}"] = originList.getOrNull(index - 1) ?: ItemStack(Material.AIR)
            }
        }
    }
}