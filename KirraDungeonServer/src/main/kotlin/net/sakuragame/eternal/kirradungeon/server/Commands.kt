package net.sakuragame.eternal.kirradungeon.server

import com.dscalzi.skychanger.core.api.SkyPacket
import net.sakuragame.dungeonsystem.common.configuration.DungeonProperties
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.dragoncore.network.PacketSender
import net.sakuragame.eternal.kirradungeon.server.compat.DragonCoreCompat
import net.sakuragame.eternal.kirradungeon.server.function.FunctionModelWand
import net.sakuragame.eternal.kirradungeon.server.zone.FunctionZone
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.Zone.Companion.editingDungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneSkyData
import net.sakuragame.eternal.kirradungeon.server.zone.impl.getWaveIndex
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import net.sakuragame.eternal.kirramodel.KirraModelAPI
import net.sakuragame.serversystems.manage.api.runnable.RunnableVal
import org.bukkit.Bukkit
import org.bukkit.WorldType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.giveItem

@CommandHeader(name = "KirraDungeonServer", aliases = ["dungeon"], permission = "admin")
object Commands {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

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
                if (editingDungeonWorld != null) {
                    player.sendMessage("&7不能两个人同时编辑！".colored())
                }
                DungeonServerAPI.getWorldManager().loadDungeon(zone.id, object : RunnableVal<DungeonWorld>() {
                    override fun run(value: DungeonWorld?) {
                        if (value == null) return
                        value.properties.isAllowMonsters
                        editingDungeonWorld = value
                        value.bukkitWorld.also { world ->
                            world.entities.forEach { it.remove() }
                        }
                        zone.data.models.forEach {
                            val model = KirraModelAPI.models[it.model] ?: return@forEach
                            KirraModelAPI.createTempModel(it.loc.toBukkitLocation(value.bukkitWorld), model, it.id)
                        }
                        player.teleport(zone.data.spawnLoc.toBukkitLocation(value.bukkitWorld))
                    }
                })
                player.sendMessage("&a正在编辑: ${zone.id}".colored())
            }
        }
    }

    @CommandBody
    val setSpawn = subCommand {
        execute<Player> { player, _, _ ->
            if (editingDungeonWorld == null) {
                player.sendMessage("&c您没有在编辑一个世界.".colored())
                return@execute
            }
            val zone = Zone.getByID(editingDungeonWorld!!.worldIdentifier)!!
            FunctionZone.setLoc(zone, ZoneLocation.parseToZoneLocation(player.location))
            player.sendMessage("&a设置成功!".colored())
        }
    }

    @CommandBody
    val getModelWand = subCommand {
        execute<Player> { player, _, _ ->
            player.sendMessage("&a已给予模型魔杖".colored())
            player.giveItem(FunctionModelWand.modelWand)
        }
    }

    @CommandBody
    val checkModels = subCommand {
        dynamic(commit = "dungeonId") {
            execute<Player> { player, context, _ ->
                val zone = getZone(player, context.get(1)) ?: return@execute
                player.sendMessage("&a${zone.id} 的副本模型:".colored())
                zone.data.models.forEach {
                    player.sendMessage("&7${it.id} - ${it.loc} - ${it.model}".colored())
                }
            }
        }
    }

    @CommandBody
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

    @CommandBody
    val delDBFile = subCommand {
        dynamic(commit = "dungeonID") {
            execute<Player> { player, _, argument ->
                val zone = Zone.getByID(argument)
                if (zone == null) {
                    player.sendMessage("&7该副本不存在.".colored())
                    return@execute
                }
                DungeonServerAPI.getWorldManager().deleteDungeon(zone.id)
                player.sendMessage("&a已删除数据库内的地图数据.".colored())
            }
        }
    }

    @CommandBody
    val addMob = subCommand {
        dynamic(commit = "mobType") {
            dynamic(commit = "mobAmount") {
                execute<Player> { player, context, argument ->
                    if (editingDungeonWorld == null) {
                        player.sendMessage("&c您没有在编辑一个世界.".colored())
                        return@execute
                    }
                    val zone = Zone.getByID(editingDungeonWorld!!.worldIdentifier)!!
                    val mobType = context.argument(-1)
                    val amount = argument.toIntOrNull() ?: 1
                    val zoneLoc = ZoneLocation.parseToZoneLocation(player.location)
                    FunctionZone.addMob(zone, zoneLoc, mobType, amount)
                    player.sendMessage("&a成功在 &f$zoneLoc &a添加怪物 = &f$mobType x $amount".colored())
                }
            }
        }
    }

    @CommandBody
    val setBoss = subCommand {
        dynamic(commit = "mobType") {
            execute<Player> { player, _, argument ->
                if (editingDungeonWorld == null) {
                    player.sendMessage("&c您没有在编辑一个世界.".colored())
                    return@execute
                }
                val zone = Zone.getByID(editingDungeonWorld!!.worldIdentifier)!!
                val zoneLoc = ZoneLocation.parseToZoneLocation(player.location)
                FunctionZone.setBoss(zone, zoneLoc, argument)
                player.sendMessage("&a成功在 &f$zoneLoc &a设置怪物首领 = &f$argument".colored())
            }
        }
    }

    @CommandBody
    val setSkyColor = subCommand {
        dynamic(commit = "skyColorType") {
            dynamic(commit = "skyColorValue") {
                execute<Player> { player, context, argument ->
                    if (editingDungeonWorld == null) {
                        player.sendMessage("&c您没有在编辑一个世界.".colored())
                        return@execute
                    }
                    val zone = Zone.getByID(editingDungeonWorld!!.worldIdentifier)!!
                    val packetType = if (context.argument(-1).toBooleanStrictOrNull() == true) {
                        SkyPacket.RAIN_LEVEL_CHANGE
                    } else {
                        SkyPacket.THUNDER_LEVEL_CHANGE
                    }
                    val value = argument.toFloatOrNull() ?: 4f
                    FunctionZone.setSkyColor(zone, ZoneSkyData(packetType, value))
                    player.sendMessage("&a您设置了副本 &f${zone.name} &a的天空颜色为: &f(${packetType.name}, $value)")
                }
            }
        }
    }

    @CommandBody
    val openDragonCoreUI = subCommand {
        execute<Player> { player, _, _ ->
            val playerZone = DefaultDungeon.getByPlayer(player.uniqueId) ?: return@execute
            DragonCoreCompat.updateDragonVars(player, playerZone.zone.name)
            PacketSender.sendOpenHud(player, DragonCoreCompat.joinTitleHud.id)
        }
    }

    @CommandBody
    val addWaveMonster = subCommand {
        dynamic(commit = "dungeonID") {
            dynamic(commit = "monsterId") {
                dynamic(commit = "monsterAmount") {
                    dynamic(commit = "monsterHealth") {
                        dynamic(commit = "wave") {
                            execute<Player> { player, context, _ ->
                                val zone = getZone(player, context.get(1)) ?: return@execute
                                val monsterId = context.get(2)
                                val monsterAmount = context.get(3).toIntOrNull() ?: 1
                                val monsterHealth = context.get(4).toDoubleOrNull() ?: 1.0
                                val wave = context.get(5).toIntOrNull() ?: getWaveIndex(zone.id)
                                if (wave == null) {
                                    player.sendMessage("&c[System] &7波次数据错误!".colored())
                                    return@execute
                                }
                                FunctionZone.addWaveMob(wave, zone, monsterId, monsterAmount, monsterHealth)
                                player.sendMessage("&c[System] &7成功!".colored())
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val setWaveBoss = subCommand {
        dynamic(commit = "dungeonID") {
            dynamic(commit = "monsterId") {
                dynamic(commit = "monsterHealth") {
                    dynamic(commit = "wave") {
                        execute<Player> { player, context, _ ->
                            val zone = getZone(player, context.get(1)) ?: return@execute
                            val bossId = context.get(2)
                            val bossHealth = context.get(3).toDoubleOrNull() ?: 1.0
                            val wave = context.get(4).toIntOrNull() ?: getWaveIndex(zone.id)
                            if (wave == null) {
                                player.sendMessage("&c[System] &c波次数据错误!".colored())
                                return@execute
                            }
                            FunctionZone.setWaveBoss(wave, zone, bossId, bossHealth)
                            player.sendMessage("&c[System] &7成功!".colored())
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val addWaveLoc = subCommand {
        dynamic(commit = "dungeonId") {
            execute<Player> { player, context, _ ->
                val zone = getZone(player, context.get(1)) ?: return@execute
                FunctionZone.addWaveLoc(zone, player.location)
                player.sendMessage("&c[System] &7成功!".colored())
            }
        }
    }

    @CommandBody
    val getZoneLoc = subCommand {
        execute<Player> { player, _, _ ->
            val loc = ZoneLocation.parseToZoneLocation(player.location).toString()
            player.sendMessage("&a坐标: &f$loc".colored())
        }
    }

    private fun getZone(player: Player, zoneId: String): Zone? {
        val zone = Zone.getByID(zoneId)
        if (zone == null) {
            player.sendMessage("&c[System] &7错误的名字或类型.".colored())
            return null
        }
        return zone
    }

    private fun getDefaultDungeonProperties() = DungeonProperties().also {
        it.type = WorldType.CUSTOMIZED
        it.addGameRule("announceAdvancements", "false")
        it.addGameRule("keepInventory", "true")
        it.addGameRule("doDaylightCycle", "false")
        it.addGameRule("showDeathMessages", "false")
    }
}
