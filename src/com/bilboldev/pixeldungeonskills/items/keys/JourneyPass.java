package com.bilboldev.pixeldungeonskills.items.keys;

import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class JourneyPass extends Key {
    {
        image = ItemSpriteSheet.IRON_KEY;
        unique = true;
    }

    @Override public String status() { return null; }

    @Override protected String message(String key, Object... args) {
        if (key.equals("name")) return Messages.get("journey.pass_name");
        if (key.equals("desc")) return Messages.get("journey.pass_desc");
        return super.message(key, args);
    }
}
