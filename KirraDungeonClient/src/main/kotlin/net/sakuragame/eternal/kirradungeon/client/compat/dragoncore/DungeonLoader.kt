package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore

import net.sakuragame.eternal.dragoncore.api.CoreAPI
import net.sakuragame.eternal.kirradungeon.client.KirraDungeonClient
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonCategory
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.DungeonSubScreen
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.sub.ScreenDescription
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.sub.ScreenLimitRealm
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.sub.ScreenLimitTime
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.screen.sub.ScreenTeleportType
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

    // 存储所有界面的表
    val parentScreens = mutableMapOf<DungeonCategory, MutableList<DungeonScreen>>()

    @Awake(LifeCycle.ENABLE)
    fun i() {
        submit(async = true) {
            log("-- 正在清理变量...")
            clear()
            log("-- 正在读取常规界面...")
            read(DungeonCategory.NORMAL)
            log("-- 正在读取团队界面...")
            read(DungeonCategory.TEAM)
            log("-- 正在读取活动界面...")
            read(DungeonCategory.ACTIVITY)
            log("-- 正在读取特殊界面...")
            read(DungeonCategory.SPECIAL)
            log("-- 正在注册按键...")
            CoreAPI.registerKey(DungeonAPI.triggerKey)
        }
    }

    private fun clear() {
        parentScreens.clear()
    }

    private fun read(category: DungeonCategory) {
        val folder = category.getFolder()
        if (!folder.exists()) {
            folder.mkdirs()
            return
        }
        val yamlFiles = folder.listFiles()!!.map { loadFromFile(it, Type.YAML) }
        val dungeonScreens = mutableListOf<DungeonScreen>().also { screenList ->
            yamlFiles.forEach { file ->
                screenList += readScreen(category, file)
            }
        }
        parentScreens[category] = dungeonScreens
            .sortedByDescending { it.priority }
            .toMutableList()
    }

    private fun DungeonCategory.getFolder() = when (this) {
        DungeonCategory.NORMAL -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/01")
        DungeonCategory.TEAM -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/02")
        DungeonCategory.ACTIVITY -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/03")
        DungeonCategory.SPECIAL -> File(KirraDungeonClient.plugin.dataFolder, "dungeon/04")
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
        val dungeonSubScreens = mutableMapOf(0 to firstSubScreen, 1 to secondSubScreen, 2 to thirdSubScreen, 3 to fourthSubScreen, 4 to fifthSubScreen)
        return DungeonScreen(defaultSelectScreen, category, priority, name, mapBgPath, dungeonSubScreens = dungeonSubScreens)
    }

    private fun readSubScreen(section: ConfigurationSection?): DungeonSubScreen? {
        if (section == null) return null
        val name = section.getStringColored("name") ?: return null
        val iconPath = section.getString("icon") ?: return null
        val description = ScreenDescription(
            section["description.text"]?.asList() ?: return null,
            section.getString("description.tip") ?: "",
            section.getString("description.bg") ?: return null
        )
        val frameVisible = section.getBoolean("frame-visible")
        val isSingle = section.getBoolean("is-single")
        val forceLock = section.getBoolean("force-lock", false)
        val forceEmpty = section.getBoolean("force-empty", false)
        val teleportType = ScreenTeleportType.values()
            .find { section.getString("teleport.type")?.uppercase() == it.name }
            ?: return null
        val teleportData = section.getString("teleport.data") ?: return null
        val droppedItems = section.getStringList("dropped-item")
        val limitTime = ScreenLimitTime(0, 0).apply {
            val split = section.getString("limit-time")?.split("-") ?: return@apply
            to = split[0].toInt()
            from = split[1].toInt()
        }
        val limitRealm = ScreenLimitRealm(section.getInt("limit-realm", 0))
        val shopCommand = section.getString("shop")
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
            limitTime = limitTime,
            limitRealm = limitRealm,
            shopId = shopCommand
        )
    }
}