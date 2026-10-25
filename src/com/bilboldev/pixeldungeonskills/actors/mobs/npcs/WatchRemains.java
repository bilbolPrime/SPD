package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;

public class WatchRemains extends RoomProp {
    @Override
    public int image() {
        return ItemSpriteSheet.BONES;
    }

    @Override
    public void interact() {
        if (available()) GameScene.show(new WndMessage(Message.of(this, "note")));
    }
}
