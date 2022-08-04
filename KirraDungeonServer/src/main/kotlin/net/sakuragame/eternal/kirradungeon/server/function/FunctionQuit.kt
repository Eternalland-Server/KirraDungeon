package net.sakuragame.eternal.kirradungeon.server.function

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import net.sakuragame.eternal.kirracore.bukkit.KirraCoreBukkitAPI
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServerAPI
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.turnToSpectator
import taboolib.common.platform.event.SubscribeEvent

object FunctionQuit {

    @SubscribeEvent
    fun e(e: UIFCompSubmitEvent) {
        val player = e.player
        val profile = player.profile() ?: return
        if (e.screenID != "dungeon_esc") {
            return
        }
        if (!KirraDungeonServerAPI.baffle.hasNext(player.name)) {
            return
        }
        KirraDungeonServerAPI.baffle.next(player.name)
        player.closeInventory()
        player.turnToSpectator()
        if (!profile.isChallenging) {
            return
        }
        KirraCoreBukkitAPI.showLoadingAnimation(player, "&6&l➱ &e正在将您传送回大厅. &f@", false)
        KirraCoreBukkitAPI.teleportPlayerToServerByBalancing("rpg-spawn", player.uniqueId)
        profile.isQuitting = true
    }
}