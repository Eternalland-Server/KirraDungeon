package net.sakuragame.eternal.kirradungeon.client.function

import net.sakuragame.dungeonsystem.client.api.DungeonClientAPI
import net.sakuragame.dungeonsystem.common.handler.MapRequestHandler
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.kirradungeon.client.zone.Zone
import net.sakuragame.eternal.kirradungeon.client.zone.event.ZoneUpdateEvent
import net.sakuragame.kirracore.bukkit.KirraCoreBukkitAPI
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.asLangTextList
import java.util.*

@Suppress("SpellCheckingInspection")
object FunctionListener {

    @EventHandler
    fun e(e: ZoneUpdateEvent) {
        submit(async = true) {
            Zone.load()
        }
    }

    @EventHandler
    fun e(e: PlayerJoinEvent) {
        val player = e.player
        if (!player.hasPermission("noobie-plot")) return
        DungeonClientAPI.getClientManager().queryDungeon("nergigante_dragon", player, object : MapRequestHandler() {

            override fun onTimeout(serverID: String) = handleJoinNoobieZoneTimedOut(player, serverID)

            override fun onTeleportTimeout(serverID: String) = handleJoinNoobieZoneTimedOut(player, serverID)

            override fun handle(serverID: String, mapUUID: UUID) {
                KirraCoreBukkitAPI.teleportPlayerToAnotherServer(serverID, player.uniqueId)
            }
        })
    }

    private fun handleJoinNoobieZoneTimedOut(player: Player, serverID: String) {
        player.asLangTextList("message-noobie-zone-join-timed-out", serverID).forEach {
            MessageAPI.sendActionTip(player, it)
        }
        KirraCoreBukkitAPI.teleportPlayerToAnotherServer("rpg-login-1", player)
    }
}