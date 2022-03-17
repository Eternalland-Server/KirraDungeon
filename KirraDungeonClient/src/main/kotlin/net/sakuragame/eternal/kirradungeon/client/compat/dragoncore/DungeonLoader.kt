package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import net.sakuragame.eternal.dragoncore.api.CoreAPI
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen.ScreenDescription
import net.sakuragame.eternal.kirradungeon.client.log
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration.Companion.loadFromFile
import taboolib.module.configuration.Type
import taboolib.module.configuration.util.getStringColored
import java.io.File

object DungeonLoader {

    // "常规" 大栏界面.
    val normalParentScreen = mutableListOf<DungeonScreen>()

    // "活动" 大栏界面.
    val activityParentScreen = mutableListOf<DungeonScreen>()

    // "特殊" 大栏界面.
    val specialParentScreen = mutableListOf<DungeonScreen>()

    // "团队" 大栏界面.
    val teamParentScreen = mutableListOf<DungeonScreen>()

    @Awake(LifeCycle.ENABLE)
    fun i() {
        submit(async = true) {
            // 清理变量.
            log("-- 正在清理变量...")
            clear()
            // 读取常规界面.
            log("-- 正在读取常规界面...")
            read(DungeonCategory.NORMAL)
            // 读取团队界面.
            log("-- 正在读取团队界面...")
            read(DungeonCategory.TEAM)
            // 读取活动界面.
            log("-- 正在读取活动界面...")
            read(DungeonCategory.ACTIVITY)
            // 读取特殊界面.
            log("-- 正在读取特殊界面...")
            read(DungeonCategory.SPECIAL)
            // 注册按键.
            log("-- 正在注册按键...")
            CoreAPI.registerKey(DungeonAPI.triggerKey)
        }
    }

    private fun clear() {
        normalParentScreen.clear()
        activityParentScreen.clear()
        specialParentScreen.clear()
        teamParentScreen.clear()
    }

    private fun read(category: DungeonCategory) {
        val folder = category.getFolder()
        if (!folder.exists()) {
            folder.mkdirs()
            return
        }
        val yamlFiles = folder.listFiles()!!.map { loadFromFile(it, Type.YAML) }
        val dungeonScreens = mutableListOf<DungeonScreen>().also { screenList ->
            yamlFiles.forEachIndexed { index, file ->
                if (index > 4) return
                screenList += readScreen(category, file)
            }
        }
        dungeonScreens.sortedByDescending { it.priority }.forEach {
            category.getParentScreen() += it
        }
    }

    private fun DungeonCategory.getFolder() = when (this) {
        DungeonCategory.NORMAL -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/normal")
        DungeonCategory.ACTIVITY -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/activity")
        DungeonCategory.SPECIAL -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/special")
        DungeonCategory.TEAM -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/team")
    }

    private fun readScreen(category: DungeonCategory, conf: ConfigFile): DungeonScreen {
        val defaultSelectScreen = conf.getInt("default-select-screen", 1)
        val priority = conf.getInt("settings.priority")
        val name = conf.getStringColored("settings.name")!!
        val mapBgPath = conf.getString("settings.map-bg")!!
        val firstSubScreen = readSubScreen(conf.getConfigurationSection("settings.first"))
        val secondSubScreen = readSubScreen(conf.getConfigurationSection("settings.second"))
        val thirdSubScreen = readSubScreen(conf.getConfigurationSection("settings.third"))
        val fourthSubScreen = readSubScreen(conf.getConfigurationSection("settings.fourth"))
        val fifthSubScreen = readSubScreen(conf.getConfigurationSection("settings.fifth"))
        val dungeonSubScreens = arrayOf(firstSubScreen, secondSubScreen, thirdSubScreen, fourthSubScreen, fifthSubScreen)
        return DungeonScreen(defaultSelectScreen, category, priority, name, mapBgPath, dungeonSubScreens = dungeonSubScreens)
    }

    @Suppress("FoldInitializerAndIfToElvis")
    private fun readSubScreen(section: ConfigurationSection?): DungeonSubScreen? {
        if (section == null) return null
        val name = section.getStringColored("name") ?: return null
        val iconPath = section.getString("icon") ?: return null
        val description = ScreenDescription(
            section["description.text"]?.asList() ?: return null,
            section.getString("description.bg") ?: return null
        )
        val frameVisible = section.getBoolean("frame-visible")
        val isSingle = section.getBoolean("is-single")
        val forceLock = section.getBoolean("force-lock", false)
        val forceEmpty = section.getBoolean("force-empty", false)
        val teleportType = DungeonSubScreen.ScreenTeleportType
            .values()
            .find { section.getString("teleport.type")?.uppercase() == it.name }
        if (teleportType == null) {
            return null
        }
        val teleportData = section.getString("teleport.data") ?: return null
        val droppedItems = section.getStringList("dropped-item")
        val limitTime = DungeonSubScreen.ScreenLimitTime(0, 0).apply {
            val split = section.getString("limit-time")?.split("-") ?: return@apply
            to = split[0].toInt()
            from = split[1].toInt()
        }
        return DungeonSubScreen(
            name,
            iconPath,
            description,
            frameVisible = frameVisible,
            forceLock = forceLock,
            forceEmpty = forceEmpty,
            isSingle = isSingle,
            teleportType = teleportType,
            teleportData = teleportData,
            droppedItems = droppedItems,
            limitTime = limitTime
        )
    }
}