package net.sakuragame.eternal.kirradungeon.server.zone.impl

import java.util.*

object FunctionDungeon {

    val dungeons = mutableListOf<IDungeon>()

    fun getByDungeonWorldUUID(uuid: UUID) = dungeons.find { it.dungeonWorld.uuid == uuid }

    fun getByBukkitWorldUUID(uuid: UUID) = dungeons.find { it.dungeonWorld.bukkitWorld.uid == uuid }

    fun getByPlayer(playerUUID: UUID): IDungeon? {
        dungeons.forEach { dungeon ->
            if (dungeon.playerUUIDList.find { it == playerUUID } != null) {
                return dungeon
            }
        }
        return null
    }

    fun getByMobUUID(mobUUID: UUID): IDungeon? {
        dungeons.forEach { dungeon ->
            if (dungeon.monsterUUIDList.find { it == mobUUID } != null) {
                return dungeon
            }
            if (dungeon.bossUUID == mobUUID) {
                return dungeon
            }
        }
        return null
    }

    fun create(dungeon: IDungeon) {
        dungeons += dungeon
    }
}