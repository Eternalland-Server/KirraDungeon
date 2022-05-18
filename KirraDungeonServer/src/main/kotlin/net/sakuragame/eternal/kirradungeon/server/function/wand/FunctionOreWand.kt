package net.sakuragame.eternal.kirradungeon.server.function.wand

import net.sakuragame.eternal.kirradungeon.server.toCenter
import net.sakuragame.eternal.kirradungeon.server.zone.FunctionZone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.ore.Ore
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.inventoryCenterSlots

object FunctionOreWand {

    val oreWand by lazy {
        buildItem(Material.STICK) {
            name = "&e&l矿物魔杖".colored()
            lore += ""
            lore += "&7左键创建矿物".colored()
            lore += "&7右键删除矿物".colored()
            lore += ""
            shiny()
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val item = e.item ?: return
        val block = e.clickedBlock ?: return
        val player = e.player
        if (!item.isSimilar(oreWand)) {
            return
        }
        when (e.action) {
            Action.LEFT_CLICK_BLOCK, Action.LEFT_CLICK_AIR -> {
                e.isCancelled = true
                player.openMenu1(block.location)
            }
            else -> return
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractAtEntityEvent) {
        val player = e.player
        val item = player.inventory.itemInMainHand
        if (!item.isSimilar(oreWand)) {
            return
        }
        val zone = FunctionWand.getEditingZone(player) ?: return
        val ore = KirraMinerAPI.getOreByEntityUUID(e.rightClicked.uniqueId) ?: kotlin.run {
            player.sendMessage("&c错误, 这并不是一个矿物.".colored())
            return
        }
        val mapId = KirraMinerAPI.getOreMapId(ore) ?: return
        KirraMinerAPI.removeOre(mapId)
        FunctionZone.removeOre(zone, mapId)
    }

    private fun Player.openMenu1(loc: Location) {
        openMenu<Linked<Ore>>("矿物列表") {
            rows(6)
            slots(inventoryCenterSlots)
            elements {
                KirraMinerAPI.ores.values.filter { !it.isTemp && it.loc == null }
            }
            onGenerate { _, element, _, _ ->
                buildItem(Material.MINECART) {
                    name = "&7&o${element.id}".colored()
                    lore += ""
                    lore += "&7* &e刷新时间: &f${element.refreshTime}".colored()
                    lore += ""
                }
            }
            onClick { _, element ->
                val zone = FunctionWand.getEditingZone(this@openMenu1) ?: return@onClick
                val player = this@openMenu1
                val spawnLoc = loc.add(0.0, 1.0, 0.0).toCenter(0.5)
                player.closeInventory()
                player.inputSign(arrayOf("", "", "请在第一行输入内容")) { arr ->
                    val id = "${element.id}_${arr[0]}"
                    if (id.isEmpty()) {
                        player.sendMessage("&c错误的内容".colored())
                        return@inputSign
                    }
                    if (zone.data.ores.find { it.id == id } != null) {
                        player.sendMessage("&c内容与现存矿物冲突".colored())
                        return@inputSign
                    }
                    FunctionZone.setOre(zone, id, element.id, ZoneLocation.parseToZoneLocation(spawnLoc))
                    KirraMinerAPI.createTempOre(id, element, loc)
                    player.sendMessage("&a配置成功.".colored())
                }
            }
            FunctionWand.addPage(this)
        }
    }
}