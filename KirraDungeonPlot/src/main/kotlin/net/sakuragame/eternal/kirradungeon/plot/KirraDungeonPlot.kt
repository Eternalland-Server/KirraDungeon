package net.sakuragame.eternal.kirradungeon.plot

import com.dscalzi.skychanger.bukkit.api.SkyChanger
import io.lumine.xikage.mythicmobs.MythicMobs
import net.luckperms.api.LuckPerms
import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin

@Suppress("SpellCheckingInspection")
object KirraDungeonPlot : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val skyAPI by lazy {
        SkyChanger.getAPI()!!
    }

    val mythicmobsAPI by lazy {
        MythicMobs.inst().apiHelper!!
    }

    val luckPermsAPI by lazy {
        Bukkit.getServicesManager().getRegistration(LuckPerms::class.java).provider!!
    }
}