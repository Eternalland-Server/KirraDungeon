package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import net.sakuragame.eternal.justlevel.api.JustLevelAPI
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.Dungeon
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.DungeonCard
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.DungeonRegion
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.DungeonRoom
import org.bukkit.entity.Player
import taboolib.module.configuration.util.getStringColored

private const val defaultNullName = "none"
private const val defaultRoomIconPath = "ui/dungeon/icon/general/sign_deny.png"

fun Player.getRealm(): Int {
    return JustLevelAPI.getRealm(this)
}

fun UIFCompSubmitEvent.isBelongDungeon(): Boolean {
    return when (screenID) {
        Dungeon.screenId, DungeonCard.screenId, net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.DungeonCategory.screenId, DungeonRegion.screenId, DungeonRoom.screenId -> {
            params.getParam(0) == KirraDungeonClient.plugin.name
        }
        else -> false
    }
}

fun DungeonCategory.display() = when (this) {
    DungeonCategory.NORMAL -> KirraDungeonClient.conf.getStringColored("settings.dungeon.normal-display")!!
    DungeonCategory.TEAM -> KirraDungeonClient.conf.getStringColored("settings.dungeon.team-display")!!
    DungeonCategory.ACTIVITY -> KirraDungeonClient.conf.getStringColored("settings.dungeon.activity-display")!!
    DungeonCategory.SPECIAL -> KirraDungeonClient.conf.getStringColored("settings.dungeon.special-display")!!
}

fun DungeonCategory.getParentScreen() = when (this) {
    DungeonCategory.NORMAL -> DungeonLoader.normalParentScreen
    DungeonCategory.TEAM -> DungeonLoader.teamParentScreen
    DungeonCategory.ACTIVITY -> DungeonLoader.activityParentScreen
    DungeonCategory.SPECIAL -> DungeonLoader.specialParentScreen
}