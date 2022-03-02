package net.sakuragame.eternal.kirradungeon.server

import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import org.bukkit.entity.Player
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.util.concurrent.atomic.AtomicInteger

@Suppress("SpellCheckingInspection")
object Database {

    const val PREFIX = "kirradungeon"

    val host = KirraDungeonServer.conf.getHost("settings.database")

    val tableNumber = Table("${PREFIX}_table_number", host) {
        add { id() }
        add("uid") {
            type(ColumnTypeSQL.INT) {
                options(ColumnOptionSQL.UNIQUE_KEY, ColumnOptionSQL.NOTNULL)
            }
        }
        add("number") {
            type(ColumnTypeSQL.INT)
        }
    }

    private val dataSource by lazy {
        ClientManagerAPI.getDataManager().dataSource
    }

    init {
        tableNumber.createTable(dataSource)
    }

    fun getNumber(player: Player): Int? {
        val toReturn = AtomicInteger(-1)
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        if (uid == -1) return null
        tableNumber.select(dataSource) {
            where("uid" eq uid)
        }.first {
            toReturn.set(getInt("number"))
        }
        if (toReturn.get() == -1) return null
        return toReturn.get()
    }

    fun setNumber(player: Player, number: Int) {
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        if (uid == -1) return
        val isFind = tableNumber.find(dataSource) {
            where { where("uid" eq uid) }
        }
        if (isFind) {
            tableNumber.update(dataSource) {
                where { where("uid" eq uid) }
                set("number", number)
            }
        } else {
            tableNumber.insert(dataSource, "uid", "number") {
                value("$uid", "$number")
            }
        }
    }
}