package net.sakuragame.eternal.kirradungeon.plot

import org.apache.commons.lang.RandomStringUtils
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import pl.betoncraft.betonquest.BetonQuest
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored

fun Player.reset() {
    health = maxHealth
    gameMode = GameMode.ADVENTURE
    activePotionEffects.forEach {
        removePotionEffect(it.type)
    }
}

@Suppress("SpellCheckingInspection")
fun sendBrokenTitleAnimation(player: Player) {
    for (index in 1..140) {
        submit(delay = index / 2L, async = true) {
            if (player.hasMetadata("NergiganteClear")) {
                return@submit
            }
            val randomTitle = "&k${RandomStringUtils.randomAlphanumeric(index / 2)}".colored()
            val randomSubTitle = "&k${RandomStringUtils.randomAlphanumeric(index)}".colored()
            player.sendTitle(randomTitle, randomSubTitle, 0, 3000, 0)
        }
    }
}

fun String.splitWithNoSpace(regex: String): List<String> {
    val string = this.replace(" ", "")
    return string.split(regex)
}

fun getMobMaxHealth(type: String) = KirraDungeonPlot.mythicmobsAPI.getMythicMob(type).health.get()

fun spawnArmorStand(loc: Location) = (loc.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand).also {
    it.setGravity(false)
    it.isVisible = false
    it.isMarker = true
}

fun Player.getNoobiePoints(): Int? {
    return BetonQuest.getInstance().getPlayerData(uniqueId).points.firstOrNull { it.category == "noobie_quest" }?.count
}

fun Player.addNoobiePoints(int: Int) {
    BetonQuest.getInstance().getPlayerData(uniqueId).modifyPoints("noobie_quest", int)
}