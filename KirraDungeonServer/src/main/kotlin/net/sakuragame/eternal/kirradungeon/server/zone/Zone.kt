package net.sakuragame.eternal.kirradungeon.server.zone

import net.sakuragame.dungeonsystem.server.api.world.DungeonWorld
import net.sakuragame.eternal.kirradungeon.server.Loader
import net.sakuragame.eternal.kirradungeon.server.zone.data.writer.implement.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 副本类
 *
 * @property id 副本 id
 * @property name 副本显示名称
 * @property data 副本数据
 */
data class Zone(val id: String, val name: String, val data: ZoneData) {

    override fun toString() = "Zone($id, ${name.uncolored()}, $data)"

    companion object {

        var editingDungeonWorld: DungeonWorld? = null

        val zones = CopyOnWriteArrayList<Zone>()

        fun getByName(name: String) = zones.find { it.name == name }

        fun getByID(id: String) = zones.find { it.id == id }

        @Awake(LifeCycle.ENABLE)
        fun i() {
            clear()
            Loader.files.forEach {
                val id = it.getString("id") ?: return@forEach
                val name = it.getString("name") ?: return@forEach
                zones += Zone(
                    id, name.colored(), ZoneData(
                        type = TypeWriter.read(id),
                        maxLastTime = MaxLastTimeWriter.read(id),
                        monsterData = MonsterWriter.read(id),
                        monsterDropData = DropItemWriter.read(id),
                        spawnLoc = SpawnLocWriter.read(id)!!,
                        zoneSkyData = SkyDataWriter.read(id),
                        number = NumberWriter.read(id),
                        iconNumber = IconNumberWriter.read(id),
                        resurgenceTime = ResurgenceTimeWriter.read(id),
                        models = ModelWriter.read(id),
                        ores = OreWriter.read(id),
                        trigger = TriggerWriter.read(id),
                        holograms = HologramWriter.read(id),
                        metadataMap = MetadataWriter.read(id),
                        stagedMobs = StagedMobWriter.read(id),
                        waveData = WaveDataWriter.readData(id),
                        waveSpawnLocs = WaveDataWriter.readLoc(id)
                    )
                )
            }
        }

        fun clear() {
            zones.clear()
        }

        fun create(id: String, name: String) {
            DefaultConfigWriter.set(id, name)
            i()
        }
    }
}
