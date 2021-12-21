package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.manager

import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.data.DungeonSubCategory

/**
 * I screen manager
 *
 * @constructor Create empty I screen manager
 */
interface IRoomManager {

    fun get(subCategory: DungeonSubCategory): ScreenUI {
        return when (subCategory) {
            DungeonSubCategory.FIRST -> getFirst()
            DungeonSubCategory.SECOND -> getSecond()
            DungeonSubCategory.THIRD -> getThird()
            DungeonSubCategory.FOURTH -> getFourth()
            DungeonSubCategory.FIFTH -> getFifth()
        }
    }

    fun getFirst(): ScreenUI

    fun getSecond(): ScreenUI

    fun getThird(): ScreenUI

    fun getFourth(): ScreenUI

    fun getFifth(): ScreenUI
}