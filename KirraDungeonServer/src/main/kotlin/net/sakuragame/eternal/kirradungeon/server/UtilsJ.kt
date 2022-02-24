package net.sakuragame.eternal.kirradungeon.server

import org.bukkit.GameMode
import org.bukkit.entity.Player

fun Player.playDeathAnimation() = world.strikeLightningEffect(location)!!

fun Player.reset() {
    gameMode = GameMode.ADVENTURE
}

fun Player.isSpectator() = gameMode == GameMode.SPECTATOR

fun Player.turnToSpectator() {
    gameMode = GameMode.SPECTATOR
}