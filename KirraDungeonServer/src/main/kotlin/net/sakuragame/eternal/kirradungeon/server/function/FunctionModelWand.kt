package net.sakuragame.eternal.kirradungeon.server.function

import net.sakuragame.eternal.kirradungeon.server.toCenter
import net.sakuragame.eternal.kirradungeon.server.zone.FunctionZone
import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import net.sakuragame.eternal.kirradungeon.server.zone.ZoneLocation
import net.sakuragame.eternal.kirramodel.KirraModelAPI
import net.sakuragame.eternal.kirramodel.model.Model
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action.LEFT_CLICK_AIR
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.inventoryCenterSlots

object FunctionModelWand {

    val modelWand by lazy {
        buildItem(Material.STICK) {
            name = "&e&l模型魔杖".colored()
            lore += ""
            lore += "&7左键创建模型".colored()
            lore += "&7右键删除模型".colored()
            lore += ""
            shiny()
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val item = e.item ?: return
        val block = e.clickedBlock ?: return
        val player = e.player
        if (!item.isSimilar(modelWand)) {
            return
        }
        when (e.action) {
            LEFT_CLICK_BLOCK, LEFT_CLICK_AIR -> {
                player.openMenu1(block.location)
            }
            else -> return
        }
    }

    private fun Player.openMenu1(loc: Location) {
        openMenu<Linked<Model>> {
            rows(6)
            slots(inventoryCenterSlots)
            elements {
                KirraModelAPI.models.values.filter { !it.temp }
            }
            onGenerate { _, element, _, _ ->
                buildItem(Material.MINECART) {
                    name = "&7&o${element.id}".colored()
                    lore += ""
                    lore += "&7* &e类型: &f${element.interactMeta.type}".colored()
                    lore += ""
                }
            }
            onClick { event, element ->
                val player = event.clicker
                player.closeInventory()
                val world = Zone.editingDungeonWorld ?: kotlin.run {
                    player.sendMessage("&c无效编辑, 你并没有在配置副本".colored())
                    return@onClick
                }
                val zone = Zone.getByID(world.worldIdentifier) ?: kotlin.run {
                    player.sendMessage("&c错误, 副本不存在".colored())
                    return@onClick
                }
                player.inputSign(arrayOf("", "", "请在第一行输入内容")) { arr ->
                    val id = arr[0]
                    if (id.isEmpty()) {
                        player.sendMessage("&c错误的内容".colored())
                        return@inputSign
                    }
                    if (zone.data.models.find { it.id == id } != null) {
                        player.sendMessage("&c内容与现存模型冲突".colored())
                        return@inputSign
                    }
                    FunctionZone.setModel(zone, id, element.id, ZoneLocation.parseToZoneLocation(loc.toCenter(0.5)))
                    player.sendMessage("&a配置成功.".colored())
                }
            }
            setNextPage(51) { _, hasNextPage ->
                if (hasNextPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "&7下一页".colored() }
                } else {
                    buildItem(XMaterial.ARROW) { name = "&8下一页".colored() }
                }
            }
            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "&7上一页".colored() }
                } else {
                    buildItem(XMaterial.ARROW) { name = "&8上一页".colored() }
                }
            }
        }
    }
}