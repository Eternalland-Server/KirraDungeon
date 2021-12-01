package net.sakuragame.eternal.KirraDungeons.server.compat


import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.KirraDungeonServer
import net.sakuragame.eternal.kirradungeon.server.spawnArmorStand
import net.sakuragame.eternal.kirradungeon.server.zone.PlayerZone
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.script.api.NergiganteAPI
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import java.util.*

@Suppress("SpellCheckingInspection")
object NergiganteScriptCompat {

    const val id = "nergigante_dragon"

    const val battleThemeBgmId = "nergigante_dragon_battle_theme"

    val uuidToEntityList = HashMap<UUID, MutableList<LivingEntity>>()

    val dragonSpawnZoneLoc by lazy {
        ZoneLocation(53.7, 66.0, 63.2, 89.6f, -9.0f)
    }

    val spawnZoneLoc by lazy {
        Zone.getByID(id)!!.data.spawnLoc
    }

    fun shouldPlay(name: String) = name == id

    fun getSpawnLoc(player: Player) = spawnZoneLoc.toBukkitLocation(player.world)

    fun forceBoundToEntity(player: Player, loc: Location) {
        uuidToEntityList[player.uniqueId]!!.add(spawnArmorStand(loc).also {
            player.teleport(loc)
            submit(delay = 8) {
                player.gameMode = GameMode.SPECTATOR
                player.spectatorTarget = it
            }
        })
    }

    fun endBound(player: Player) {
        player.spectatorTarget = null
        player.gameMode = GameMode.ADVENTURE
    }

    fun play(player: Player, playerZone: PlayerZone) {
        forceBoundToEntity(player, spawnZoneLoc.toBukkitLocation(player.world))
        submit(delay = 40, async = false) {
            spawnEntity(player, playerZone, "nergigante_dragon_dome")
        }
        submit(delay = 60) {
            PacketSender.sendPlaySound(player,
                battleThemeBgmId,
                "bgms/nergigante_dragon_battle_theme.ogg",
                1f,
                1f,
                true,
                player.location.x.toFloat(),
                player.location.z.toFloat(),
                player.location.z.toFloat()
            )
            NergiganteAPI.startConversation(player, 0)
        }
    }

    fun spawnEntity(player: Player, playerZone: PlayerZone, entityType: String): LivingEntity {
        val loc = dragonSpawnZoneLoc.toBukkitLocation(playerZone.dungeonWorld.bukkitWorld)
        val entity = KirraDungeonServer.mythicmobsAPI.spawnMythicMob(entityType, loc) as LivingEntity
        uuidToEntityList[player.uniqueId]!!.add(entity)
        return entity
    }

    fun dataRecycle(player: Player, clearRoot: Boolean = false) {
        uuidToEntityList[player.uniqueId]!!.forEach {
            it.remove()
        }
        if (clearRoot) {
            uuidToEntityList.remove(player.uniqueId)
        } else {
            uuidToEntityList[player.uniqueId]!!.clear()
        }
    }
}