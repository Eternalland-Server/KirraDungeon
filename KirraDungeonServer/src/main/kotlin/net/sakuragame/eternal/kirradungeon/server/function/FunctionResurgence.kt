package net.sakuragame.eternal.kirradungeon.server.function

import net.sakuragame.eternal.dragoncore.api.CoreAPI
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent
import net.sakuragame.eternal.justmessage.api.common.NotifyBox
import net.sakuragame.eternal.justmessage.api.event.notify.NotifyBoxConfirmEvent
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.zone.player.PlayerZone
import org.bukkit.GameMode
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.hasItem

object FunctionResurgence {

    private const val RESURGENCE_BOX_KEY = "RESURGENCE_BOX"

    @Awake(LifeCycle.ACTIVE)
    fun e() {
        CoreAPI.registerKey("M")
    }

    @SubscribeEvent
    fun e(e: KeyPressEvent) {
        if (e.key.uppercase() != "M") {
            return
        }
        val player = e.player
        val profile = e.player.profile()
        val playerZone = PlayerZone.getByPlayer(player.uniqueId) ?: return
        if (player.gameMode == GameMode.SPECTATOR && playerZone.canResurgence()) {
            if (!player.isPlayerHasResurgenceItem()) {
                return
            }
            NotifyBox(RESURGENCE_BOX_KEY, "&6&l副本", listOf("是否花费一复活币复活?")).open(player, false)
        }
    }

    @SubscribeEvent
    fun e(e: NotifyBoxConfirmEvent) {
        if (e.key != RESURGENCE_BOX_KEY) {
            return
        }
        val player = e.player
        val playerZone = PlayerZone.getByPlayer(player.uniqueId) ?: return
        player.closeInventory()
        playerZone.resurgence(player)
    }

    private fun Player.isPlayerHasResurgenceItem(): Boolean {
        return inventory.hasItem(1) { it.itemMeta.displayName.contains("复活币") }
    }
}