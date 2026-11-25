package com.bilboldev.pixeldungeonskills.items;

import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class JourneyCache extends Item {
    {
        image = ItemSpriteSheet.CHEST;
        unique = true;
    }

    @Override public boolean collect(Bag bag) { return false; }
    @Override public boolean isUpgradable() { return false; }
    @Override public boolean isIdentified() { return true; }

    @Override protected String message(String key, Object... args) {
        if (key.equals("name")) return Messages.get("journey.cache_name");
        if (key.equals("desc")) return Messages.get("journey.cache_desc");
        return super.message(key, args);
    }
}
