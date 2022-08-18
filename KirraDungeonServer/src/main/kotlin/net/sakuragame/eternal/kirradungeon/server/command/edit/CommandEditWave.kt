package net.sakuragame.eternal.kirradungeon.server.command.edit

import net.sakuragame.eternal.kirradungeon.server.getEditingZone
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.WaveDataWriter
import net.sakuragame.eternal.kirradungeon.server.zone.impl.getWaveIndex
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@CommandHeader("DungeonEditWave")
object CommandEditWave {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val addWaveMonster = subCommand {
        dynamic(commit = "monsterId") {
            dynamic(commit = "monsterAmount") {
                dynamic(commit = "monsterHealth") {
                    dynamic(commit = "wave") {
                        execute<Player> { player, context, _ ->
                            val zone = getEditingZone(player) ?: return@execute
                            val monsterId = context.get(1)
                            val monsterAmount = context.get(2).toIntOrNull() ?: 1
                            val monsterHealth = context.get(3).toDoubleOrNull() ?: 1.0
                            val wave = context.get(4).toIntOrNull() ?: getWaveIndex(zone.id)
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

    @CommandBody
    val setWaveBoss = subCommand {
        dynamic(commit = "monsterId") {
            dynamic(commit = "monsterHealth") {
                dynamic(commit = "wave") {
                    execute<Player> { player, context, _ ->
                        val zone = getEditingZone(player) ?: return@execute
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

    @CommandBody
    val addWaveLoc = subCommand {
        execute<Player> { player, context, _ ->
            val zone = getEditingZone(player) ?: return@execute
            WaveDataWriter.addWaveLoc(zone, player.location)
            player.sendMessage("&c[System] &7成功!".colored())
        }
    }
}