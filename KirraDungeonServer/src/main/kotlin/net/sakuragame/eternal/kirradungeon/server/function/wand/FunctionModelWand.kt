package net.sakuragame.eternal.kirradungeon.server.function.wand

import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import net.sakuragame.eternal.kirradungeon.server.toCenter
import net.sakuragame.eternal.kirradungeon.server.zone.FunctionZone
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
import taboolib.module.chat.colored
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.inventoryCenterSlots

@Suppress("SpellCheckingInspection")
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
                e.isCancelled = true
                player.openMenu1(block.location)
            }
            else -> return
        }
    }

    @SubscribeEvent
    fun e(e: AdyeshachEntityInteractEvent) {
        if (!e.isMainHand) {
            return
        }
        val player = e.player
        val item = player.inventory.itemInMainHand
        if (!item.isSimilar(modelWand)) {
            return
        }
        val zone = FunctionWand.getEditingZone(player) ?: return
        val model = zone.data.models.find { it.loc.toBukkitLocation(player.world).distance(e.entity.getLocation()) < 0.1 } ?: kotlin.run {
            player.sendMessage("&c错误, 该模型并不由 KirraModel 托管".colored())
            return
        }
        FunctionZone.removeModel(zone, model.id)
        KirraModelAPI.removeModel(model.id)
        player.sendMessage("&a已移除".colored())
    }

    private fun Player.openMenu1(loc: Location) {
        openMenu<Linked<Model>>("模型列表") {
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
                    if (zone.data.models.find { it.id == id } != null) {
                        player.sendMessage("&c内容与现存模型冲突".colored())
                        return@inputSign
                    }
                    FunctionZone.setModel(zone, id, element.id, ZoneLocation.parseToZoneLocation(spawnLoc))
                    KirraModelAPI.createTempModel(spawnLoc, element, id)
                    player.sendMessage("&a配置成功.".colored())
                }
            }
            FunctionWand.addPage(this)
        }
    }
}