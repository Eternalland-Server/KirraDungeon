package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.google.common.collect.Lists
import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.eternal.dragoncore.network.PacketSender
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
        UPDATE, JOIN, CLOSE, PAGE
    }

    fun getDefaultScreen() = DungeonLoader.normalParentScreen[0]

    fun getDefaultSubScreen(screen: DungeonScreen?): DungeonSubScreen {
        if (screen == null) return getDefaultScreen().dungeonSubScreens[0]!!
        return screen.dungeonSubScreens.find { it?.forceEmpty == false } ?: getDefaultScreen().dungeonSubScreens[0]!!
    }

    fun getPluginParams(
        type: ParamType = ParamType.UPDATE,
        toScreenData: String = "",
    ) = SubmitParams().apply {
        addValue(KirraDungeonClient.plugin.name)
        addValue(type.name)
        // toScreenData
        addValue(toScreenData)
        // fromScreenData
        add("global.dungeon_category")
        add("global.dungeon_sub_category")
        add("global.dungeon_current_selected")
        add("global.dungeon_page")
    }

    fun getDungeonCategory(index: Int) = when (index) {
        1 -> DungeonCategory.NORMAL
        2 -> DungeonCategory.TEAM
        3 -> DungeonCategory.ACTIVITY
        4 -> DungeonCategory.SPECIAL
        else -> error("out of dungeon category index, max is 4.")
    }

    fun getDungeonScreen(category: DungeonCategory, index: Int): DungeonScreen? {
        val parent = category.getParentScreen()
        return parent.getOrNull(index - 1)
    }

    fun getMaxPage(droppedItems: List<String>): Int {
        return Lists.partition(droppedItems, 6).size
    }

    fun sendDroppedItems(player: Player, droppedItems: List<String>, page: Int) {
        val partition = Lists.partition(droppedItems, 6)
        val split = partition.getOrNull(page - 1) ?: partition.getOrNull(0)
        for (index in 1..6) {
            val slotId = "dungeon_drop_$index"
            val indexStr = split?.getOrNull(index - 1) ?: "null"
            val item = ZaphkielAPI.getItemStack(indexStr) ?: ItemStack(Material.AIR)
            PacketSender.putClientSlotItem(player, slotId, item)
        }
    }
}