package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.GameUpdates;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class WndUpdateAvailable extends WndOptions {
    public WndUpdateAvailable() {
        super(new ItemSprite(ItemSpriteSheet.SCROLL_UPGRADE, null),
                Messages.get("updates.title"), Messages.get("updates.message"),
                Messages.get("updates." + GameUpdates.store()));
    }

    @Override
    public void onBackPressed() {
        hide();
        Game.scene().add(new WndUpdateHistory());
    }

    @Override
    protected void onSelect(int index) {
        Game.scene().add(new WndUpdateHistory());
        if (!GameUpdates.open()) Game.scene().add(new WndMessage(Messages.get("updates.open_failed")));
    }
}
