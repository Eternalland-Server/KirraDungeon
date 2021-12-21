package net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.manager.normal

import com.taylorswiftcn.megumi.uifactory.generate.ui.component.base.TextureComp
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.DungeonRoom
import net.sakuragame.eternal.kirradungeon.client.compat.dragoncore.manager.IRoomManager

object NormalRoomManager : IRoomManager {

    override fun getFirst(): ScreenUI {
        return ScreenUI(DungeonRoom.screenId)
            .addComponent(TextureComp("room_4_icon", "ui/dungeon/icon/general/sign.png")
                .setExtend("dungeon_4")
            )
            .addComponent(TextureComp("room_4_card", "ui/dungeon/card.png")
                .setExtend("dungeon_4")
            )
            .addComponent(TextureComp("room_4_name", "0,0,0,0")
                .setText("&6&l亘古主城")
                .setXY("dungeon_4.x", "dungeon_4.y + 73")
                .setWidth("dungeon_4.width")
                .setHeight("10")
            )

            .addComponent(TextureComp("room_2_icon", "ui/dungeon/icon/general/sign.png")
                .setExtend("dungeon_2")
            )
            .addComponent(TextureComp("room_2_card", "ui/dungeon/card.png")
                .setExtend("dungeon_2")
            )
            .addComponent(TextureComp("room_2_name", "0,0,0,0")
                .setText("&f&l钓鱼场")
                .setXY("dungeon_2.x", "dungeon_2.y + 73")
                .setWidth("dungeon_2.width")
                .setHeight("10")
            )

            .addComponent(TextureComp("room_3_icon", "ui/dungeon/icon/general/sign.png")
                .setExtend("dungeon_3")
            )
            .addComponent(TextureComp("room_3_card", "ui/dungeon/card.png")
                .setExtend("dungeon_3")
            )
            .addComponent(TextureComp("room_3_name", "0,0,0,0")
                .setText("&f&l泡点专区")
                .setXY("dungeon_3.x", "dungeon_3.y + 73")
                .setWidth("dungeon_3.width")
                .setHeight("10")
            )

            .addComponent(TextureComp("room_1_icon", "ui/dungeon/icon/general/sign.png")
                .setExtend("dungeon_1")
            )
            .addComponent(TextureComp("room_1_card", "ui/dungeon/card.png")
                .setExtend("dungeon_1")
            )
            .addComponent(TextureComp("room_1_name", "0,0,0,0")
                .setText("&f&l会员专区")
                .setXY("dungeon_1.x", "dungeon_1.y + 73")
                .setWidth("dungeon_1.width")
                .setHeight("10")
            )

            .addComponent(TextureComp("room_5_icon", "ui/dungeon/icon/general/sign_deny.png")
                .setExtend("dungeon_5")
            )
            .addComponent(TextureComp("room_5_card", "ui/dungeon/card.png")
                .setExtend("dungeon_5")
            )
            .addComponent(TextureComp("room_5_name", "0,0,0,0")
                .setText("&7&o暂未解锁")
                .setXY("dungeon_5.x", "dungeon_5.y + 73")
                .setWidth("dungeon_5.width")
                .setHeight("10")
            )
    }

    override fun getSecond(): ScreenUI {
        TODO("Not yet implemented")
    }

    override fun getThird(): ScreenUI {
        TODO("Not yet implemented")
    }

    override fun getFourth(): ScreenUI {
        TODO("Not yet implemented")
    }

    override fun getFifth(): ScreenUI {
        TODO("Not yet implemented")
    }
}