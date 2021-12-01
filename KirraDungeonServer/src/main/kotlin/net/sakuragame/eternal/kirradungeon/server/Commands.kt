package net.sakuragame.eternal.kirradungeon.server

import com.dscalzi.skychanger.core.api.SkyPacket
import net.sakuragame.dungeonsystem.common.configuration.DungeonProperties
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.zone.PlayerZone
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneData
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.serversystems.manage.api.runnable.RunnableVal
import org.bukkit.Bukkit
import org.bukkit.WorldType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@CommandHeader(name = "KirraDungeonServer", aliases = ["dungeon"], permissionDefault = PermissionDefault.OP)
object Commands {

    var editingDungeonWorld: DungeonWorld? = null

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&7副本列表: ".colored())
            Zone.zones.forEach {
                sender.sendMessage("&a$it".colored())
            }
        }
    }

    val create = subCommand {
        dynamic(repeat = 2) {
            execute<Player> { player, context, argument ->
                val zoneId = context.argument(-1)
                if (!Zone.getByID(zoneId).hasValue()) {
                    player.sendMessage("&7该副本已存在.".colored())
                    return@execute
                }
                // argument = zoneName
                Zone.createZone(zoneId, argument)
                DungeonServerAPI.getWorldManager().createEmptyDungeon(zoneId, getDefaultDungeonProperties(), object : RunnableVal<DungeonWorld>() {

                    override fun run(value: DungeonWorld?) {
                        if (value == null) return
                        editingDungeonWorld = value
                        player.teleport(value.bukkitWorld.spawnLocation)
                    }
                })

                player.sendMessage("&a成功创建.".colored())
            }
        }
    }

    val edit = subCommand {
        dynamic {
            execute<Player> { player, _, argument ->
                val zone = Zone.getByID(argument)
                if (zone == null) {
                    player.sendMessage("&7该副本不存在.".colored())
                    return@execute
                }
                DungeonServerAPI.getWorldManager().loadDungeon(zone.id, object : RunnableVal<DungeonWorld>() {

                    override fun run(value: DungeonWorld?) {
                        if (value == null) return
                        editingDungeonWorld = value
                        player.teleport(zone.data.spawnLoc.toBukkitLocation(value.bukkitWorld))
                    }
                })
                player.sendMessage("&a正在编辑: ${zone.id}".colored())
            }
        }
    }

    val setSpawn = subCommand {
        execute<Player> { player, _, _ ->
            if (editingDungeonWorld == null) {
                player.sendMessage("&c您没有在编辑一个世界.".colored())
                return@execute
            }
            val zone = Zone.getByID(editingDungeonWorld!!.worldIdentifier)
            if (zone == null) {
                player.sendMessage("&c未找到该副本.".colored())
                return@execute
            }
            Zone.setZoneLoc(zone, ZoneLocation.parseToZoneLocation(player.location))
            player.sendMessage("&a设置成功!".colored())
        }
    }

    val save = subCommand {
        execute<Player> { player, _, _ ->
            if (editingDungeonWorld == null) {
                player.sendMessage("&7无法进行该操作, 副本丢失.".colored())
                return@execute
            }
            DungeonServerAPI.getWorldManager().saveDungeon(editingDungeonWorld!!)
            player.teleport(Bukkit.getWorld("world").spawnLocation)
            DungeonServerAPI.getWorldManager().unloadDungeon(editingDungeonWorld!!, true)
            editingDungeonWorld = null
            player.sendMessage("&a已保存.".colored())
        }
    }

    val addMob = subCommand {
        dynamic(repeat = 3) {
            execute<Player> { player, context, argument ->
                val zone = Zone.getByID(context.get(-2))
                if (zone == null) {
                    player.sendMessage("&c未找到该副本.".colored())
                    return@execute
                }
                val mobType = context.get(-1)
                val amount = argument.toIntOrNull() ?: 1
                val zoneLoc = ZoneLocation.parseToZoneLocation(player.location)
                Zone.addZoneMob(zone, zoneLoc, mobType, amount)
                player.sendMessage("&a成功在 &f$zoneLoc &a添加怪物 = &f$mobType x $amount".colored())
            }
        }
    }

    val setSkyColor = subCommand {
        dynamic(repeat = 3) {
            execute<Player> { player, context, argument ->
                val zone = Zone.getByID(context.get(-2))
                if (zone == null) {
                    player.sendMessage("&c未找到该副本.".colored())
                    return@execute
                }
                val packetType = if (context.get(-1).toBooleanStrictOrNull() == true) {
                    SkyPacket.RAIN_LEVEL_CHANGE
                } else {
                    SkyPacket.THUNDER_LEVEL_CHANGE
                }
                val value = argument.toFloat()
                Zone.setZoneSkyColor(zone, ZoneData.Companion.ZoneSkyData(packetType, value))
                player.sendMessage("&a您设置了副本${zone.name}的天空颜色为: (${packetType.name}, $value)")
            }
        }
    }

    val openDragonCoreUI = subCommand {
        execute<Player> { player, _, _ ->
            val playerZone = PlayerZone.getByPlayer(player.uniqueId) ?: return@execute
            DragonCoreCompat.updateDragonVars(player, playerZone.zone.name)
            PacketSender.sendOpenHud(player, DragonCoreCompat.joinTitleHud.id)
        }
    }

    val getZoneLoc = subCommand {
        execute<Player> { player, _, _ ->
            val loc = ZoneLocation.parseToZoneLocation(player.location).toString()
            player.sendMessage("&a坐标: &f$loc".colored())
        }
    }

    private fun getDefaultDungeonProperties() = DungeonProperties().also {
        it.type = WorldType.CUSTOMIZED
        it.addGameRule("announceAdvancements", "false")
        it.addGameRule("keepInventory", "true")
        it.addGameRule("doDaylightCycle", "false")
        it.addGameRule("showDeathMessages", "false")
    }
}
