package net.sakuragame.eternal.kirradungeon.server.compat

import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirradungeon.server.event.DungeonClearEvent
import net.sakuragame.eternal.kirradungeon.server.event.DungeonJoinEvent
import org.bukkit.entity.Player
import pl.betoncraft.betonquest.BetonQuest
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored

@Suppress("SpellCheckingInspection")
object StoryDungeonCompat {

    @SubscribeEvent
    fun e(e: DungeonClearEvent) {
        val player = e.player
        if (player.getNoobiePoints() == 3 && e.dungeonId == "noobie_dungeon") {
            player.addNoobiePoints(1)
            val message = "&6&l➱ &e成功通关! 回到主城服找樱儿交付吧!".colored()
            MessageAPI.sendActionTip(player, message)
            player.sendMessage(message)
        }
    }

    @SubscribeEvent
    fun e(e: DungeonJoinEvent) {
        val player = e.player
        if (player.inventory.contents.isEmpty()) {
            player.inventory.addItem(ZaphkielAPI.getItem("destoryer_sword")!!.rebuildToItemStack(player))
        }
    }

    private fun Player.getNoobiePoints(): Int? {
        return BetonQuest.getInstance().getPlayerData(uniqueId).points.firstOrNull { it.category == "noobie_quest" }?.count
    }

    private fun Player.addNoobiePoints(int: Int) {
        BetonQuest.getInstance().getPlayerData(uniqueId).modifyPoints("noobie_quest", int)
    }
}