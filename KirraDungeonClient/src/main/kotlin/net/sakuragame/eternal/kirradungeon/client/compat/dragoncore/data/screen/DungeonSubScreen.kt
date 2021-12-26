package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen

data class DungeonSubScreen(
    val name: String,
    val iconPath: String,
    val description: ScreenDescription,
    val frameVisible: Boolean = false,
    val lockedByProgress: Boolean = true,
    val isSingle: Boolean = false,
    val dungeonId: String? = null
) {

    data class ScreenDescription(val text: List<String>, val bgPath: String)
}
