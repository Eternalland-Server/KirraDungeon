package net.sakuragame.eternal.kirradungeon.server.compat.skript.expression

import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import net.sakuragame.eternal.kirradungeon.server.zone.impl.FunctionDungeon
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import org.bukkit.World
import org.bukkit.event.Event

@Suppress("UNCHECKED_CAST")
class ExpressionCurrentWave : SimpleExpression<String>() {

    var world: Expression<World>? = null

    override fun toString(e: Event?, debug: Boolean): String {
        return ""
    }

    override fun init(exprs: Array<Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        world = exprs[0] as Expression<World>
        return true
    }

    override fun isSingle(): Boolean {
        return true
    }

    override fun getReturnType(): Class<String> {
        return String::class.java
    }

    override fun get(e: Event): Array<String> {
        val internalWorld = world?.getSingle(e) ?: return arrayOf("-1")
        val dungeon = FunctionDungeon.getByBukkitWorldUUID(internalWorld.uid) as? DefaultDungeon ?: return arrayOf("-1")
        return arrayOf(dungeon.mobs.size.toString())
    }
}