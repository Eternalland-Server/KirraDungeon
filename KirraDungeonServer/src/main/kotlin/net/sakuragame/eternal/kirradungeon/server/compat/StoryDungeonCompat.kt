package net.sakuragame.eternal.kirradungeon.server.compat

import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirradungeon.server.event.DungeonClearEvent
import org.bukkit.entity.Player
import pl.betoncraft.betonquest.BetonQuest
import taboolib.common.platform.event.SubscribeEvent

@Suppress("SpellCheckingInspection")
object StoryDungeonCompat {

    @SubscribeEvent
    fun e(e: DungeonClearEvent) {
        val player = e.player
        if (player.getNoobiePoints() == 2 && e.dungeonId == "noobie_dungeon") {
            player.addNoobiePoints(1)
            MessageAPI.sendActionTip(player, "&6&l➱ &e成功通关! 回到主城服找樱儿交付吧!")
        }
    }

    private fun Player.getNoobiePoints(): Int? {
        return BetonQuest.getInstance().getPlayerData(uniqueId).points.firstOrNull { it.category == "noobie_quest" }?.count
    }

    private fun Player.addNoobiePoints(int: Int) {
        BetonQuest.getInstance().getPlayerData(uniqueId).modifyPoints("noobie_quest", int)
    }
}