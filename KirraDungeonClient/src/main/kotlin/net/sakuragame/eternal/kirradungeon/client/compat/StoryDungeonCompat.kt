package net.sakuragame.eternal.kirradungeon.client.compat

import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.dungeonsystem.common.handler.MapRequestHandler
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.entity.Player
import taboolib.platform.util.asLangTextList
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("SpellCheckingInspection")
object StoryDungeonCompat {

    fun join(player: Player): Boolean {
        val serverId = DungeonClientAPI.getClientManager().queryServer("rpg-story")
        if (serverId == null) {
            handleJoinFailed(player)
            return false
        }
        val isSucc = AtomicBoolean(true)
        DungeonClientAPI.getClientManager().queryDungeon("nergigante_dragon", serverId, LinkedHashSet<Player>().apply {
            add(player)
        }, object : MapRequestHandler() {

            override fun onTimeout(serverID: String) {
                handleJoinFailed(player)
                isSucc.set(false)
            }

            override fun onTeleportTimeout(serverID: String) {
                handleJoinFailed(player)
                isSucc.set(false)
            }

            override fun handle(serverID: String, mapUUID: UUID) {
                KirraCoreBukkitAPI.teleportPlayerToAnotherServer(serverID, player.uniqueId)
            }
        })
        return isSucc.get()
    }

    private fun handleJoinFailed(player: Player) {
        player.asLangTextList("message-noobie-dungeon-join-timed-out", "rpg-story-1").forEach {
            MessageAPI.sendActionTip(player, it)
        }
    }
}