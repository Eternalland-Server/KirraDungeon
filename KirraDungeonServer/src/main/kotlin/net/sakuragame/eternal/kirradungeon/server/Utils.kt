package net.sakuragame.eternal.kirradungeon.server

import org.apache.commons.lang.RandomStringUtils
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.asLangTextList

fun <T> T.hasValue() = this != null

fun Player.playDeathAnimation() = world.strikeLightningEffect(location)!!

fun String.splitWithNoSpace(regex: String) = this
    .replace(" ", "")
    .split(regex)

@Suppress("SpellCheckingInspection")
fun sendBrokenTitleAnimation(player: Player) {
    for (index in 1..140) {
        submit(delay = index / 2L, async = true) {
            if (player.hasMetadata("NergiganteClear")) return@submit
            val randomTitle = "&k${RandomStringUtils.randomAlphanumeric(index / 2)}".colored()
            val randomSubTitle = "&k${RandomStringUtils.randomAlphanumeric(index)}".colored()
            player.sendTitle(randomTitle, randomSubTitle, 0, 999999, 0)
        }
    }
}

fun kickPlayerByNotFoundData(player: Player) {
    val strBuilder = StringBuilder()
    player.asLangTextList("message-player-not-found-data").forEach {
        strBuilder.append("$it\n")
    }
    player.kickPlayer(strBuilder.toString())
}

fun spawnArmorStand(loc: Location) = (loc.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand).also {
    it.setGravity(false)
    it.isVisible = false
    it.isMarker = true
}

fun getMobMaxHealth(type: String) = KirraDungeonServer.mythicmobsAPI.getMythicMob(type).health.get()

fun getMobMaxHealth(entity: LivingEntity) = KirraDungeonServer.mythicmobsAPI.getMythicMobInstance(entity).type.health.get()