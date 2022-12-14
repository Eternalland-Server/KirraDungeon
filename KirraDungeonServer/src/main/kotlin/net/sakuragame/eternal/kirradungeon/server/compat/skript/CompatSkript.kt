package net.sakuragame.eternal.kirradungeon.server.compat.skript

import ch.njol.skript.Skript
import ch.njol.skript.lang.ExpressionType
import net.sakuragame.eternal.kirradungeon.server.compat.skript.effect.EffectDungeonDoSpawn
import net.sakuragame.eternal.kirradungeon.server.compat.skript.expression.ExpressionCurrentWave
import net.sakuragame.eternal.kirradungeon.server.compat.skript.expression.ExpressionDungeonIdentifier
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info

@Suppress("SpellCheckingInspection")
object CompatSkript {

    @Awake(LifeCycle.ENABLE)
    fun i() {
        if (Bukkit.getPluginManager().getPlugin("Skript") == null) {
            return
        }
        info("与 Skript 进行挂钩.")
        registerExpressions()
        registerEffects()
    }

    private fun registerExpressions() {
        Skript.registerExpression(
            ExpressionDungeonIdentifier::class.java,
            String::class.java,
            ExpressionType.SIMPLE,
            "dungeon identifier of %world%"
        )
        Skript.registerExpression(
            ExpressionCurrentWave::class.java,
            String::class.java,
            ExpressionType.SIMPLE,
            "current wave of default dungeon from %world%"
        )
    }

    private fun registerEffects() {
        Skript.registerEffect(
            EffectDungeonDoSpawn::class.java,
            "do spawn next wave for %player%"
        )
    }
}