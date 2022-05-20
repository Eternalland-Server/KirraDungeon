package net.sakuragame.eternal.kirradungeon.client

import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI
import org.bukkit.entity.Player
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost

@Suppress("SpellCheckingInspection")
object Database {

    private const val PREFIX = "kirradungeon"

    private val host = KirraDungeonClient.conf.getHost("settings.database")

    private val tableNumber = Table("${PREFIX}_table_number", host) {
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
        val uid = ClientManagerAPI.getUserID(player.uniqueId)
        if (uid == -1) return null
        return tableNumber.select(dataSource) {
            where("uid" eq uid)
        }.firstOrNull {
            getInt("number")
        }
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
                value(uid, number)
            }
        }
    }
}