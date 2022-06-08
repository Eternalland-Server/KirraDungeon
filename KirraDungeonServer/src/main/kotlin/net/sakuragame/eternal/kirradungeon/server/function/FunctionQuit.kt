package net.sakuragame.eternal.kirradungeon.server.function

import net.sakuragame.eternal.dragoncore.api.CoreAPI
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent
import net.sakuragame.eternal.justmessage.api.common.NotifyBox
import net.sakuragame.eternal.justmessage.api.event.notify.NotifyBoxConfirmEvent
import net.sakuragame.eternal.kirracore.bukkit.KirraCoreBukkitAPI
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServerAPI
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.turnToSpectator
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored

object FunctionQuit {

    private const val QUIT_BOX_KEY = "QUIT_BOX"

    private const val TRIGGER_KEY = "N"

    @Awake(LifeCycle.ENABLE)
    fun e() {
        CoreAPI.registerKey(TRIGGER_KEY)
    }

    @SubscribeEvent
    fun e(e: KeyPressEvent) {
        val player = e.player
        if (!KirraDungeonServerAPI.baffle.hasNext(player.name)) {
            return
        }
        KirraDungeonServerAPI.baffle.next(player.name)
        if (e.key != TRIGGER_KEY) {
            return
        }
        NotifyBox(QUIT_BOX_KEY, "&6&l副本".colored(), listOf("是否退出副本?")).open(e.player, false)
    }

    @SubscribeEvent
    fun e(e: NotifyBoxConfirmEvent) {
        if (e.key != QUIT_BOX_KEY) {
            return
        }
        val player = e.player
        val profile = player.profile() ?: return
        player.closeInventory()
        player.turnToSpectator()
        DragonCoreCompat.closeFailHud(player)
        if (!profile.isChallenging) {
            return
        }
        KirraCoreBukkitAPI.showLoadingTitle(player, "&6&l➱ &e正在将您传送回大厅. &f@", false)
        KirraCoreBukkitAPI.teleportToSpawnServer(player)
        profile.isQuitting = true
    }
}