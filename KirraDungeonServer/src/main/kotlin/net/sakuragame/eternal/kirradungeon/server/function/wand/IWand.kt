package net.sakuragame.eternal.kirradungeon.server.function.wand

import net.sakuragame.eternal.kirradungeon.server.zone.Zone
import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem

interface IWand {

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