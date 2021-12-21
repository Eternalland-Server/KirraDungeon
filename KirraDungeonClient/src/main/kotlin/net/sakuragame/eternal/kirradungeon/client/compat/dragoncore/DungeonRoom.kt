package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonSubCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.manager.normal.NormalRoomManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake


object DungeonRoom {

    const val screenId = "dungeon_room"

    private lateinit var defaultUI: ScreenUI

    private lateinit var defaultUIYaml: YamlConfiguration

    /**
     * 向玩家客户端发送 DungeonRoom 的龙核 Yaml.
     *
     * @param player 玩家.
     * @param category 大栏分类.
     * @param subCategory 小栏分类.
     */
    fun sendScreen(player: Player, category: DungeonCategory = DungeonCategory.NORMAL, subCategory: DungeonSubCategory = DungeonSubCategory.FIRST) {
        if (category == DungeonCategory.NORMAL) {
            PacketSender.sendYaml(player, FolderType.Gui, screenId, defaultUIYaml)
            return
        }
        val screenUIYaml = getScreenByCategory(category, subCategory).build(player)
        PacketSender.sendYaml(player, FolderType.Gui, screenId, screenUIYaml)
    }

    @Awake(LifeCycle.ENABLE)
    fun init() {
        defaultUI = getScreenByCategory(DungeonCategory.NORMAL, DungeonSubCategory.FIRST)
        defaultUIYaml = defaultUI.build(null)
    }

    private fun getScreenByCategory(category: DungeonCategory, subCategory: DungeonSubCategory): ScreenUI {
        return when (category) {
            DungeonCategory.NORMAL -> NormalRoomManager.get(subCategory)
            else -> NormalRoomManager.get(subCategory)
        }
    }
}