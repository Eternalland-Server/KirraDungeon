package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent
import net.sakuragame.eternal.justlevel.api.JustLevelAPI
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory.*
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.DungeonCard
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.DungeonRegion
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.screen.DungeonRoom
import org.bukkit.entity.Player
import taboolib.module.configuration.util.getStringColored

fun Player.getRealm(): Int {
    return JustLevelAPI.getRealm(this)
}

fun UIFCompSubmitEvent.isBelongDungeon(): Boolean {
    return when (screenID) {
        "dungeon", DungeonCard.screenId, "dungeon_category", DungeonRegion.screenId, DungeonRoom.screenId -> {
            params.getParam(0) == KirraDungeonClient.plugin.name
        }
        else -> false
    }
}

fun DungeonCategory.display() = when (this) {
    NORMAL -> KirraDungeonClient.conf.getStringColored("settings.screen-dungeon.normal-display")!!
    TEAM -> KirraDungeonClient.conf.getStringColored("settings.screen-dungeon.team-display")!!
    ACTIVITY -> KirraDungeonClient.conf.getStringColored("settings.screen-dungeon.activity-display")!!
    SPECIAL -> KirraDungeonClient.conf.getStringColored("settings.screen-dungeon.special-display")!!
}

fun DungeonCategory.getParentScreen() = when (this) {
    NORMAL -> DungeonLoader.parentScreens[NORMAL]
    TEAM -> DungeonLoader.parentScreens[TEAM]
    ACTIVITY -> DungeonLoader.parentScreens[ACTIVITY]
    SPECIAL -> DungeonLoader.parentScreens[SPECIAL]
}!!

fun DungeonCategory.getIndex() = when (this) {
    NORMAL -> 1
    TEAM -> 2
    ACTIVITY -> 3
    SPECIAL -> 4
}