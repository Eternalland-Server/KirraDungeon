package net.sakuragame.eternal.kirradungeon.server.command

import ink.ptms.adyeshach.api.AdyeshachAPI
import net.sakuragame.dungeonsystem.common.configuration.DungeonProperties
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.justlevel.api.PropGenerateAPI
import net.sakuragame.eternal.kirradungeon.server.Loader
import net.sakuragame.eternal.kirradungeon.server.command.edit.CommandEditMain
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.serversystems.manage.api.runnable.RunnableVal
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@CommandHeader("Dungeon", aliases = ["dungeon"])
object CommandMain {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val commandEditMain = CommandEditMain

    @CommandBody
    val commandHelper = CommandHelper

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&c[System] &7目前可用的副本如下 ".colored())
            sender.sendMessage("&c[System] &7默认类型:".colored())
            Zone.zones.filter { it.data.type == ZoneType.DEFAULT }.forEach {
                sender.sendMessage("&a${it.id} &7(${it.name}&7)".colored())
            }
            sender.sendMessage("&c[System] &7特殊类型:".colored())
            Zone.zones.filter { it.data.type == ZoneType.SPECIAL }.forEach {
                sender.sendMessage("&a${it.id} &7(${it.name}&7)".colored())
            }
            sender.sendMessage("&c[System] &7无限制 (武神塔) 类型:".colored())
            Zone.zones.filter { it.data.type == ZoneType.UNLIMITED }.forEach {
                sender.sendMessage("&a${it.id} &7(${it.name}&7)".colored())
            }
            sender.sendMessage("&c[System] &7波次类型:".colored())
            Zone.zones.filter { it.data.type == ZoneType.WAVE }.forEach {
                sender.sendMessage("&a${it.id} &7(${it.name}&7)".colored())
            }
        }
    }

    @CommandBody
    val info = subCommand {
        dynamic("dungeonID") {
            execute<CommandSender> { sender, _, argument ->
                val zone = Zone.getByID(argument)
                if (zone == null) {
                    sender.sendMessage("&c[System] &7当前副本不存在.".colored())
                    return@execute
                }
                sender.sendMessage("&a${zone}".colored())
            }
        }
    }

    @CommandBody
    val create = subCommand {
        dynamic(commit = "dungeonID") {
            dynamic(commit = "dungeonName") {
                execute<Player> { player, context, argument ->
                    val zoneId = context.argument(-1)
                    if (Zone.getByID(zoneId) != null) {
                        player.sendMessage("&7该副本已存在.".colored())
                        return@execute
                    }
                    // argument = zoneName
                    Zone.create(zoneId, argument)
                    DungeonServerAPI.getWorldManager().createEmptyDungeon(zoneId, DungeonProperties(), object : RunnableVal<DungeonWorld>() {

                        override fun run(value: DungeonWorld?) {
                            if (value == null) return
                            Zone.editingDungeonWorld = value
                            player.teleport(value.bukkitWorld.spawnLocation)
                        }
                    })

                    player.sendMessage("&a成功创建.".colored())
                }
            }
        }
    }

    @CommandBody
    val edit = subCommand {
        dynamic(commit = "dungeonID") {
            execute<Player> { player, _, argument ->
                val zone = Zone.getByID(argument)
                if (zone == null) {
                    player.sendMessage("&7该副本不存在.".colored())
                    return@execute
                }
                if (Zone.editingDungeonWorld != null) {
                    player.sendMessage("&7不能两个人同时编辑！".colored())
                }
                DungeonServerAPI.getWorldManager().loadDungeon(zone.id, object : RunnableVal<DungeonWorld>() {
                    override fun run(value: DungeonWorld?) {
                        if (value == null) return
                        Zone.editingDungeonWorld = value
                        value.bukkitWorld.also { world ->
                            world.entities.forEach { it.remove() }
                        }
                        zone.data.holograms.forEach {
                            AdyeshachAPI.createHologram(player, it.loc.toBukkitLocation(value.bukkitWorld), it.contents.colored())
                        }
                        zone.data.parkourDrops.forEach {
                            PropGenerateAPI.spawn(it.type, it.loc.toBukkitLocation(value.bukkitWorld), it.value, it.amount)
                        }
                        player.teleport(zone.data.spawnLoc.toBukkitLocation(value.bukkitWorld))
                    }
                })
                player.sendMessage("&a正在编辑: ${zone.id}".colored())
            }
        }
    }

    @CommandBody
    val save = subCommand {
        execute<Player> { player, _, _ ->
            val editingDungeonWorld = Zone.editingDungeonWorld
            if (editingDungeonWorld == null) {
                player.sendMessage("&7无法进行该操作, 副本丢失.".colored())
                return@execute
            }
            KirraMinerAPI.removeAllOresInWorld(editingDungeonWorld.bukkitWorld)
            DungeonServerAPI.getWorldManager().saveDungeon(editingDungeonWorld)
            player.teleport(Bukkit.getWorld("world").spawnLocation)
            DungeonServerAPI.getWorldManager().unloadDungeon(editingDungeonWorld, true)
            Zone.editingDungeonWorld = null
            player.sendMessage("&a已保存.".colored())
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            Loader.reload()
            sender.sendMessage("&c[System] &7完成".colored())
        }
    }

    @CommandBody
    val refreshMapFile = subCommand {
        dynamic(commit = "dungeonID") {
            execute<Player> { player, _, argument ->
                val zone = Zone.getByID(argument)
                if (zone == null) {
                    player.sendMessage("&7该副本不存在.".colored())
                    return@execute
                }
                DungeonServerAPI.getWorldManager().deleteDungeon(zone.id)
                player.sendMessage("&a已删除数据库内的地图数据.".colored())
                player.sendMessage("&a正在创建新文件...".colored())
                DungeonServerAPI.getWorldManager().createEmptyDungeon(zone.id, DungeonProperties(), object : RunnableVal<DungeonWorld>() {

                    override fun run(value: DungeonWorld?) {
                        if (value == null) return
                        Zone.editingDungeonWorld = value
                        player.sendMessage("&a成功!".colored())
                        player.sendMessage("&a正在传送.".colored())
                        player.teleport(value.bukkitWorld.spawnLocation)
                    }
                })
            }
        }
    }
}