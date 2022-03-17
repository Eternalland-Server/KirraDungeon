package net.sakuragame.eternal.kirradungeon.server.zone.data.sub.wave

data class ZoneWaveData(val monsterData: List<ZoneWaveMonsterData>, val bossData: ZoneWaveBossData) {

    data class ZoneWaveMonsterData(val monsterId: String, val amount: Int, val health: Double)

    data class ZoneWaveBossData(val bossId: String, val health: Double)
}