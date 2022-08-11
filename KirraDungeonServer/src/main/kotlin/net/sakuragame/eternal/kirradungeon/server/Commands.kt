package net.sakuragame.eternal.kirradungeon.server

import com.dscalzi.skychanger.core.api.SkyPacket
import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.common.util.Inputs.inputBook
import net.sakuragame.dungeonsystem.common.configuration.DungeonProperties
import net.sakuragame.dungeonsystem.server.api.DungeonServerAPI
import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.function.wand.FunctionModelWand
import net.sakuragame.eternal.kirradungeon.server.function.wand.FunctionOreWand
import net.sakuragame.eternal.kirradungeon.server.function.wand.FunctionTriggerWand
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.Zone.Companion.editingDungeonWorld
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneType
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneSkyData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.*
import net.sakuragame.eternal.kirradungeon.server.zone.impl.getWaveIndex
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.serversystems.manage.api.runnable.RunnableVal
import org.bukkit.Bukkit
import org.bukkit.Effect
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
                    DungeonServerAPI.getWorldManager().createEmptyDungeon(zoneId, DungeonProperties(), object : RunnableVal<DungeonWorld>() {

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
                        editingDungeonWorld = value
                        value.bukkitWorld.also { world ->
                            world.entities.forEach { it.remove() }
                        }
                        zone.data.holograms.forEach {
                            AdyeshachAPI.createHologram(player, it.loc.toBukkitLocation(value.bukkitWorld), it.contents.colored())
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
            val zone = getEditingZone(player) ?: return@execute
            SpawnLocWriter.set(zone, ZoneLocation.parseToZoneLocation(player.location))
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
    val getOreWand = subCommand {
        execute<Player> { player, _, _ ->
            player.sendMessage("&a已给予矿物魔杖".colored())
            player.giveItem(FunctionOreWand.oreWand)
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
                        editingDungeonWorld = value
                        player.sendMessage("&a成功!".colored())
                        player.sendMessage("&a正在传送.".colored())
                        player.teleport(value.bukkitWorld.spawnLocation)
                    }
                })
            }
        }
    }

    @CommandBody
    val addDrop = subCommand {
        dynamic(commit = "mobId") {
            dynamic(commit = "itemId") {
                dynamic(commit = "dropChance") {
                    dynamic(commit = "dropRange") {
                        execute<Player> { player, context, _ ->
                            val zone = getEditingZone(player) ?: return@execute
                            val mobId = context.get(1)
                            val itemId = context.get(2)
                            val dropChance = context.get(3).toDouble().coerceAtMost(1.0)
                            val dropRange = context.get(4).parseIntRange() ?: return@execute
                            DropItemWriter.setDrop(zone, mobId, itemId, dropChance, dropRange)
                            player.sendMessage("&a成功给 $mobId &a添加掉落物 = &f$itemId ($dropChance, $dropRange)".colored())
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val addMob = subCommand {
        dynamic(commit = "mobType") {
            dynamic(commit = "mobAmount") {
                dynamic(commit = "levelRange") {
                    execute<Player> { player, context, _ ->
                        val zone = getEditingZone(player) ?: return@execute
                        val mobType = context.get(1)
                        val amount = context.get(2).toIntOrNull() ?: 1
                        val levelRange = context.get(3).parseIntRange() ?: return@execute
                        val zoneLoc = ZoneLocation.parseToZoneLocation(player.location)
                        MonsterWriter.setMob(zone, zoneLoc, mobType, amount, levelRange)
                        player.sendMessage("&a成功在 &f$zoneLoc &a添加怪物 = &f$mobType x $amount".colored())
                    }
                }
            }
        }
    }

    @CommandBody
    val setBoss = subCommand {
        dynamic(commit = "mobType") {
            dynamic(commit = "levelRange") {
                execute<Player> { player, context, _ ->
                    val zone = getEditingZone(player) ?: return@execute
                    val zoneLoc = ZoneLocation.parseToZoneLocation(player.location)
                    val mobType = context.get(1)
                    val levelRange = context.get(2).parseIntRange() ?: return@execute
                    MonsterWriter.setBoss(zone, zoneLoc, mobType, levelRange)
                    player.sendMessage("&a成功在 &f$zoneLoc &a设置怪物首领 = &f$mobType".colored())
                }
            }
        }
    }

    @CommandBody
    val clearMonsters = subCommand {
        execute<Player> { player, _, _ ->
            val zone = getEditingZone(player) ?: return@execute
            MonsterWriter.clear(zone)
            player.sendMessage("&a成功.".colored())
        }
    }

    @CommandBody
    val setSkyColor = subCommand {
        dynamic(commit = "skyColorType") {
            dynamic(commit = "skyColorValue") {
                execute<Player> { player, context, argument ->
                    val zone = getEditingZone(player) ?: return@execute
                    val packetType = if (context.argument(-1).toBooleanStrictOrNull() == true) {
                        SkyPacket.RAIN_LEVEL_CHANGE
                    } else {
                        SkyPacket.THUNDER_LEVEL_CHANGE
                    }
                    val value = argument.toFloatOrNull() ?: 4f
                    SkyDataWriter.set(zone, ZoneSkyData(packetType, value))
                    player.sendMessage("&a您设置了副本 &f${zone.name} &a的天空颜色为: &f(${packetType.name}, $value)")
                }
            }
        }
    }

    @CommandBody
    val setTrigger = subCommand {
        literal("wand") {
            execute<Player> { player, _, _ ->
                player.giveItem(FunctionTriggerWand.triggerWand)
                player.sendMessage("&a已给予魔杖.".colored())
                return@execute
            }
        }
        literal("on") {
            execute<Player> { player, _, _ ->
                if (getEditingZone(player) == null) {
                    return@execute
                }
                FunctionTriggerWand.editingBlockPlayers += player.uniqueId
                player.sendMessage("&a已开启".colored())
            }
        }
        literal("off") {
            execute<Player> { player, _, _ ->
                if (getEditingZone(player) == null) {
                    return@execute
                }
                FunctionTriggerWand.editingBlockPlayers -= player.uniqueId
                player.sendMessage("&a已关闭".colored())
            }
        }
        literal("export") {
            execute<Player> { player, _, _ ->
                val zone = getEditingZone(player) ?: return@execute
                TriggerWriter.setBlock(zone, FunctionTriggerWand.currentBlocks)
                FunctionTriggerWand.currentBlocks.forEach {
                    val loc = it.loc.toBukkitLocation(player.world)
                    loc.world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1)
                }
                FunctionTriggerWand.currentBlocks.clear()
                player.sendMessage("&a已导出".colored())
            }
        }
    }

    @CommandBody
    val hologram = subCommand {
        literal("create") {
            dynamic(commit = "id") {
                execute<Player> { player, _, argument ->
                    val zone = getEditingZone(player) ?: return@execute
                    val loc = ZoneLocation.parseToZoneLocation(player.location)
                    player.inputBook {
                        HologramWriter.set(zone, argument, it, loc)
                        val hologram = AdyeshachAPI.createHologram(player, player.location, it)
                        HologramWriter.editingHolograms += argument to hologram
                        player.sendMessage("&a完成.".colored())
                    }
                }
            }
        }
        literal("delete") {
            dynamic(commit = "id") {
                execute<Player> { player, _, argument ->
                    if (getEditingZone(player) == null) {
                        return@execute
                    }
                    val hologram = HologramWriter.editingHolograms[argument] ?: return@execute
                    hologram.delete()
                    HologramWriter.editingHolograms.remove(argument)
                    player.sendMessage("&a完成.".colored())
                }
            }
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
                                WaveDataWriter.setWaveMob(wave, zone, monsterId, monsterAmount, monsterHealth)
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
                            WaveDataWriter.setWaveBoss(wave, zone, bossId, bossHealth)
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
                WaveDataWriter.addWaveLoc(zone, player.location)
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

    @CommandBody
    val setMetadata = subCommand {
        dynamic(commit = "key") {
            dynamic(commit = "value") {
                execute<Player> { player, context, _ ->
                    val editingZone = getEditingZone(player) ?: return@execute
                    val key = context.get(1)
                    val value = context.get(2)
                    if (value == "@NULL") {
                        MetadataWriter.remove(editingZone, key)
                        player.sendMessage("&c[System] &7移除成功".colored())
                        return@execute
                    }
                    MetadataWriter.set(editingZone, key, value)
                    player.sendMessage("&c[System] &7设置 $key 为 $value 成功.".colored())
                }
            }
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
}
