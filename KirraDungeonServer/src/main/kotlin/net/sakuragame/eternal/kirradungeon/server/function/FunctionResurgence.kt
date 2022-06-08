package net.sakuragame.eternal.kirradungeon.server.function

import net.sakuragame.eternal.dragoncore.api.CoreAPI
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent
import net.sakuragame.eternal.justmessage.api.MessageAPI
import net.sakuragame.eternal.justmessage.api.common.NotifyBox
import net.sakuragame.eternal.justmessage.api.event.notify.NotifyBoxConfirmEvent
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServerAPI
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.isSpectator
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.asLangText
import taboolib.platform.util.hasItem

object FunctionResurgence {

    private const val RESURGENCE_BOX_KEY = "RESURGENCE_BOX"

    private const val TRIGGER_KEY = "M"

    @Awake(LifeCycle.ENABLE)
    fun e() {
        CoreAPI.registerKey(TRIGGER_KEY)
    }

    @SubscribeEvent
    fun e(e: KeyPressEvent) {
        if (e.key != TRIGGER_KEY) {
            return
        }
        val player = e.player
        val profile = player.profile() ?: return
        if (profile.isQuitting) return
        if (!KirraDungeonServerAPI.baffle.hasNext(player.name)) {
            return
        }
        KirraDungeonServerAPI.baffle.next(player.name)
        val dungeon = profile.getIDungeon() ?: return
        if (player.isSpectator() && dungeon.canResurgence()) {
            if (!player.isPlayerHasResurgenceItem()) {
                MessageAPI.sendActionTip(player, player.asLangText("message-player-not-has-resurgence-item"))
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
        val profile = player.profile() ?: return
        val dungeon = profile.getIDungeon() ?: return
        player.closeInventory()
        dungeon.resurgence(player)
    }

    private fun Player.isPlayerHasResurgenceItem(): Boolean {
        return inventory.hasItem(1) { it.itemMeta.displayName.contains("复活币") }
    }
}