package net.sakuragame.eternal.kirradungeon.server.function.wand

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem

object FunctionWand {

    fun getEditingZone(player: Player): Zone? {
        val world = Zone.editingDungeonWorld ?: kotlin.run {
            player.sendMessage("&c无效编辑, 你并没有在配置副本".colored())
            return null
        }
        return Zone.getByID(world.worldIdentifier) ?: kotlin.run {
            player.sendMessage("&c错误, 副本不存在".colored())
            return null
        }
    }

    fun addPage(menu: Linked<*>) {
        menu.setNextPage(51) { _, hasNextPage ->
            if (hasNextPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) { name = "&7下一页".colored() }
            } else {
                buildItem(XMaterial.ARROW) { name = "&8下一页".colored() }
            }
        }
        menu.setPreviousPage(47) { _, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(XMaterial.SPECTRAL_ARROW) { name = "&7上一页".colored() }
            } else {
                buildItem(XMaterial.ARROW) { name = "&8上一页".colored() }
            }
        }
    }
}