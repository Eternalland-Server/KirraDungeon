package net.sakuragame.eternal.kirradungeon.server.command.edit

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.server.v1_12_R1.TileEntitySkull
import net.sakuragame.eternal.justlevel.api.PropGenerateAPI
import net.sakuragame.eternal.kirradungeon.server.getEditingZone
import net.sakuragame.eternal.kirradungeon.server.toCenter
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.ParkourDropWriter
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.ParkourLocationWriter
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored
import java.util.*

@Suppress("DuplicatedCode")
@CommandHeader("DungeonEditParkour")
object CommandEditParkour {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val addParkourLocations = subCommand {
        execute<Player> { player, _, _ ->
            val zone = getEditingZone(player) ?: return@execute
            val zoneLoc = ZoneLocation.parseToZoneLocation(player.location)
            ParkourLocationWriter.set(zone, zoneLoc)
            player.sendMessage("&a成功在 &f$zoneLoc &a设置跑酷信标".colored())
        }
    }

    @CommandBody
    val addParkourDrop = subCommand {
        dynamic(commit = "type") {
            dynamic(commit = "value") {
                dynamic(commit = "amount") {

                    execute<Player> { player, context, _ ->
                        val zone = getEditingZone(player) ?: return@execute
                        val zoneLoc = ZoneLocation.parseToZoneLocation(player.location.block.location.toCenter(0.5))
                        val type = context.get(1).toIntOrNull() ?: return@execute
                        val value = context.get(2).toIntOrNull() ?: return@execute
                        val amount = context.get(3).toIntOrNull() ?: return@execute
                        ParkourDropWriter.set(zone, zoneLoc, type, value, amount)
                        PropGenerateAPI.spawn(type, zoneLoc.toBukkitLocation(player.world), value, amount)
                        player.sendMessage("&a成功在 &f$zoneLoc &a设置跑酷奖励 = &f$type ($value, $amount)".colored())
                    }
                }
            }
        }
    }

    @CommandBody
    val setParkourBlock = subCommand {
        dynamic(commit = "skullId") {
            dynamic(commit = "multiplyValue") {
                dynamic(commit = "yValue") {
                    execute<Player> { player, context, _ ->
                        val skullId = context.get(1)
                        val multiplyValue = context.get(2).toDoubleOrNull() ?: return@execute
                        val yValue = context.get(3).toDoubleOrNull() ?: return@execute
                        val block = player.getTargetBlock(setOf(Material.AIR), 3)
                        val entity = (block.world as CraftWorld).getTileEntityAt(block.x, block.y, block.z) as? TileEntitySkull ?: run {
                            player.sendMessage("&c[System] &7无法转换成 TileEntity 数据.".colored())
                            return@execute
                        }
                        entity.gameProfile = GameProfile(UUID.randomUUID(), skullId).apply {
                            properties.put("textures", Property("", ""))
                            properties.put("model", Property("model", "model: $skullId"))
                            properties.put("multiplyValue", Property("$multiplyValue", "$multiplyValue"))
                            properties.put("yValue", Property("$yValue", "$yValue"))
                        }
                        entity.update()
                        block.state.update()
                        player.sendMessage("&c[System] &7成功设置.".colored())
                    }
                }
            }
        }
    }

    @CommandBody
    val getParkourBlockStatus = subCommand {
        execute<Player> { player, _, _ ->
            val block = player.getTargetBlock(setOf(Material.AIR), 3)
            val entity = (block.world as CraftWorld).getTileEntityAt(block.x, block.y, block.z) as? TileEntitySkull ?: run {
                player.sendMessage("&c[System] &7无法转换成 TileEntity 数据.".colored())
                return@execute
            }
            val gameProfile = entity.gameProfile
            if (gameProfile == null) {
                player.sendMessage("&c[System] &7方块不存在.".colored())
                return@execute
            }
            val multiplyValue = gameProfile.properties.get("multiplyValue")?.firstOrNull()?.value?.toDoubleOrNull()
            val yValue = gameProfile.properties.get("yValue")?.firstOrNull()?.value?.toDoubleOrNull()
            player.sendMessage("&c[System] &7MultiplyValue = $multiplyValue, yValue: $yValue".colored())
        }
    }

    @CommandBody
    val resetStatus = subCommand {
        execute<Player> { player, _, _ ->
            val block = player.getTargetBlock(setOf(Material.AIR), 3)
            val entity = (block.world as CraftWorld).getTileEntityAt(block.x, block.y, block.z) as? TileEntitySkull ?: run {
                player.sendMessage("&c[System] &7无法转换成 TileEntity 数据.".colored())
                return@execute
            }
            val gameProfile = entity.gameProfile
            if (gameProfile == null) {
                player.sendMessage("&c[System] &7方块不存在.".colored())
                return@execute
            }
            val multiplyValue = gameProfile.properties.get("multiplyValue")?.firstOrNull()?.value?.toDoubleOrNull()
            val yValue = gameProfile.properties.get("yValue")?.firstOrNull()?.value?.toDoubleOrNull()
            val skullId = gameProfile.properties.get("model")?.firstOrNull()?.value?.replace("model: ", "") ?: return@execute
            entity.gameProfile = GameProfile(UUID.randomUUID(), skullId).apply {
                properties.put("textures", Property("", ""))
                properties.put("model", Property("model", "model: $skullId"))
                properties.put("multiplyValue", Property("$multiplyValue", "$multiplyValue"))
                properties.put("yValue", Property("$yValue", "$yValue"))
            }
            entity.update()
            block.state.update()
            player.sendMessage("&c[System] &7成功设置.".colored())
        }
    }
}