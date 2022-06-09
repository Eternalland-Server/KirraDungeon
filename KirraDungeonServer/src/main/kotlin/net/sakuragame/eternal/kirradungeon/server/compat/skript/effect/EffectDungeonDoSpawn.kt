package net.sakuragame.eternal.kirradungeon.server.compat.skript.effect

import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import net.sakuragame.eternal.kirradungeon.server.Profile.Companion.profile
import net.sakuragame.eternal.kirradungeon.server.zone.impl.type.DefaultDungeon
import org.bukkit.entity.Player
import org.bukkit.event.Event

@Suppress("UNCHECKED_CAST")
class EffectDungeonDoSpawn : Effect() {

    private var player: Expression<Player>? = null

    override fun toString(e: Event?, debug: Boolean) = ""

    override fun init(exprs: Array<Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        player = exprs[0] as Expression<Player>?
        return true
    }

    override fun execute(e: Event?) {
        val player = player?.getSingle(e) ?: return
        val profile = player.profile() ?: return
        val dungeon = profile.getIDungeon() as? DefaultDungeon ?: return
        dungeon.doSpawn()
    }
}