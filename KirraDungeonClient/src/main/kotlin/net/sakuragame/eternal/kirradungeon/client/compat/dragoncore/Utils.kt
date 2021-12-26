package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import com.taylorswiftcn.megumi.uifactory.generate.function.SubmitParams
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import taboolib.module.configuration.util.getStringColored

private const val defaultNullName = "none"
private const val defaultRoomIconPath = "ui/dungeon/icon/general/sign_deny.png"

fun SubmitParams.isBelongDungeon() = getParam(0) == KirraDungeonClient.plugin.name

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