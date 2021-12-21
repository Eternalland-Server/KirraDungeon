package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.manager.MainScreenManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * Dungeon
 *
 * 内部拥有两个龙核变量, 分别为 dungeon_category, dungeon_sub_category
 *
 * dungeon_category 为左侧选择大栏, 分为 "常规", "团本", "活动", "特殊" 四种.
 * 每一个大栏最多拥有 5 个小栏.
 *
 * dungeon_sub_category 为中下角选择小栏, 每个小栏里最多存在 5 个副本 / 功能设施.
 * 例: 亘古主城就在常规大栏下的废弃之地小栏里.
 * 这个小栏一共有 5 个功能设施:
 * "亘古主城", "钓鱼场", "泡点专区", "泡点专区 (会员), "???"
 *
 * 概括一下, 就是一个大栏里面最多存在 25 个副本.
 */
object Dungeon {

    const val screenId = "dungeon"

    private lateinit var defaultUI: ScreenUI

    private lateinit var defaultUIYaml: YamlConfiguration

    @Awake(LifeCycle.ENABLE)
    fun init() {
        defaultUI = MainScreenManager.getNormal()
        defaultUIYaml = defaultUI.build(null)
    }

    /**
     * 向玩家客户端发送 DungeonRoom 的龙核 Yaml.
     *
     * @param player 玩家.
     * @param category 大栏分类.
     */
    fun sendScreen(player: Player, category: DungeonCategory = DungeonCategory.NORMAL) {
        if (category == DungeonCategory.NORMAL) {
            PacketSender.sendYaml(player, FolderType.Gui, screenId, defaultUIYaml)
            return
        }
        val screenUIYaml = getScreenByCategory(category).build(player)
        PacketSender.sendYaml(player, FolderType.Gui, screenId, screenUIYaml)
    }

    private fun getScreenByCategory(category: DungeonCategory): ScreenUI {
        return when (category) {
            DungeonCategory.NORMAL -> MainScreenManager.get(DungeonCategory.NORMAL)
            else -> MainScreenManager.get(DungeonCategory.NORMAL)
        }
    }
}