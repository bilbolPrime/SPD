package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.levels.RebornSideRooms;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;

public class ForgottenOffering extends RoomProp {
    @Override
    public int image() {
        return ItemSpriteSheet.CRYSTAL_CHEST;
    }

    @Override
    public void interact() {
        if (!available()) return;
        GameScene.show(new WndOptions(Message.actor(this),
                Message.of(this, "desc"),
                Message.key(Food.class, "name"),
                Message.key(PotionOfHealing.class, "name"),
                Message.key(RebornSideRooms.class, "leave")) {
            @Override
            protected void onSelect(int index) {
                if (index < 2) choose(index == 0);
            }
        });
    }

    public void choose(boolean food) {

        if (!available()) return;
        Item reward = food ? new Food() : new PotionOfHealing();
        reward.identify();
        Dungeon.level.drop(reward, pos);
        consume();
        Dungeon.hero.spendAndNext(1f);
    }
}
