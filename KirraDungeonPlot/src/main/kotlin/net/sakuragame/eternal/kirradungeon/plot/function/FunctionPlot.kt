package net.sakuragame.eternal.kirradungeon.plot.function

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import com.dscalzi.skychanger.core.api.SkyPacket
import net.sakuragame.eternal.dragoncore.config.FolderType
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.plot.KirraDungeonPlot
import net.sakuragame.eternal.kirradungeon.plot.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.plot.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.plot.data.ZoneLocation
import net.sakuragame.eternal.kirradungeon.plot.spawnArmorStand
import net.sakuragame.eternal.script.api.NergiganteAPI
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit

/**
 * KirraPlotZone
 * net.sakuragame.eternal.kirraplotzone.function.FunctionPlot
 *
 * @author kirraObj
 * @since 2021/11/30 19:12
 */
@Suppress("SpellCheckingInspection")
object FunctionPlot {

    const val battleThemeBgmId = "nergigante_dragon_battle_theme.ogg"

    private val playerSpawnLoc by lazy {
        val strLoc = KirraDungeonPlot.conf.getString("settings.spawn-point.player")!!
        requireNotNull(ZoneLocation.parseToZoneLocation(strLoc))
    }

    private val mobSpawnLoc by lazy {
        val strLoc = KirraDungeonPlot.conf.getString("settings.spawn-point.mob")!!
        requireNotNull(ZoneLocation.parseToZoneLocation(strLoc))
    }

    fun start(player: Player) {
        startBound(player, getPlayerSpawnLoc(player) ?: return)
        KirraDungeonPlot.skyAPI.changeSky(SkyChanger.wrapPlayer(player), SkyPacket.RAIN_LEVEL_CHANGE, 4f)
        player.health = player.maxHealth
        submit(delay = 40) {
            spawnEntity(player, "dragon_dome")
        }
        submit(delay = 60) {
            PacketSender.sendPlaySound(player, battleThemeBgmId, "bgms/nergigante_dragon_battle_theme.ogg", 0.2f, 1f, true, 0f, 0f, 0f)
            NergiganteAPI.startConversation(player, 0)
        }
    }

    private fun getPlayerSpawnLoc(player: Player): Location? {
        val profile = player.profile() ?: return null
        return playerSpawnLoc.toBukkitLocation(profile.dungeonWorld!!.bukkitWorld)
    }

    private fun getMobSpawnLoc(player: Player): Location? {
        val profile = player.profile() ?: return null
        return mobSpawnLoc.toBukkitLocation(profile.dungeonWorld!!.bukkitWorld)
    }

    fun dataRecycle(player: Player) {
        player.profile()?.removeAllEntities()
    }

    private fun startBound(player: Player, loc: Location) {
        val armorStand = spawnArmorStand(loc)
        val profile = player.profile() ?: return
        profile.entityList.add(armorStand)
        player.teleport(loc)
        submit(delay = 8L) {
            PacketSender.setThirdPersonView(player, 1)
            player.gameMode = GameMode.SPECTATOR
            submit(delay = 5L) {
                player.spectatorTarget = armorStand
            }
        }
    }

    fun endBound(player: Player) {
        player.spectatorTarget = null
        player.gameMode = GameMode.ADVENTURE
        PacketSender.setThirdPersonView(player, 3)
    }

    fun playDome(player: Player) {
        dataRecycle(player)
        startBound(player, getPlayerSpawnLoc(player) ?: return)
        spawnEntity(player, "dragon_dome")
    }

    fun spawnEntity(player: Player, entityType: String): LivingEntity? {
        val entity = KirraDungeonPlot.mythicmobsAPI.spawnMythicMob(entityType, getMobSpawnLoc(player)) as LivingEntity
        val profile = player.profile() ?: return null
        profile.entityList.add(entity)
        return entity
    }

    fun showJoinHud(player: Player, dungeonName: String) {
        PacketSender.sendYaml(player, FolderType.Gui, DragonCoreCompat.joinTitleHudID, DragonCoreCompat.joinTitleHudYaml)
        DragonCoreCompat.updateDragonVars(player, dungeonName)
        PacketSender.sendOpenHud(player, DragonCoreCompat.joinTitleHudID)
    }
}