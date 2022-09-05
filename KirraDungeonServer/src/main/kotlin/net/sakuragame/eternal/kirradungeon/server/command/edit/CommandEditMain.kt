package net.sakuragame.eternal.kirradungeon.server.command.edit

import com.dscalzi.skychanger.core.api.SkyPacket
import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.common.util.Inputs.inputBook
import net.sakuragame.eternal.kirradungeon.server.function.wand.FunctionTriggerWand
import net.sakuragame.eternal.kirradungeon.server.getEditingZone
import net.sakuragame.eternal.kirradungeon.server.parseIntRange
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.ZoneSkyData
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.*
import org.bukkit.Effect
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import taboolib.platform.util.giveItem

@CommandHeader("DungeonEdit")
object CommandEditMain {

    @CommandBody
    val main = mainCommand {
        createHelper()
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
    val clearDrops = subCommand {
        execute<Player> { player, _, _ ->
            val zone = getEditingZone(player) ?: return@execute
            DropItemWriter.clear(zone)
            player.sendMessage("&a成功.".colored())
        }
    }

    @CommandBody
    val addMob = subCommand {
        dynamic(commit = "mobAmount") {
            dynamic(commit = "levelRange") {
                execute<Player> { player, context, _ ->
                    val zone = getEditingZone(player) ?: return@execute
                    val amount = context.get(1).toIntOrNull() ?: 1
                    val levelRange = context.get(2).parseIntRange() ?: return@execute
                    val zoneLoc = ZoneLocation.parseToZoneLocation(player.location)
                    MonsterWriter.setMob(zone, zoneLoc, amount, levelRange)
                    player.sendMessage("&a成功在 &f$zoneLoc &a添加怪物 = &f$amount".colored())
                }
            }
        }
    }

    @CommandBody
    val setMultiplier = subCommand {
        dynamic(commit = "multiplier") {
            execute<Player> { player, context, _ ->
                val zone = getEditingZone(player) ?: return@execute
                val multiplier = context.get(1).parseIntRange() ?: return@execute
                StagedLevelWriter.set(zone, multiplier)
                player.sendMessage("&a成功设置阶段等级 = &f$multiplier".colored())
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
                        val hologram = AdyeshachAPI.createHologram(player, player.location, it.colored())
                        HologramWriter.editingHolograms[argument] = hologram
                        player.sendMessage("&a完成.".colored())
                    }
                }
            }
        }
        literal("delete") {
            dynamic(commit = "id") {
                execute<Player> { player, _, argument ->
                    val zone = getEditingZone(player) ?: return@execute
                    val hologram = HologramWriter.editingHolograms[argument] ?: return@execute
                    hologram.delete()
                    HologramWriter.editingHolograms.remove(argument)
                    HologramWriter.remove(zone, argument)
                    player.sendMessage("&a完成.".colored())
                }
            }
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
}