package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.JourneyRun;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.JourneyCaptiveSprite;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import java.io.IOException;

public class JourneyCaptive extends NPC {
    { spriteClass = JourneyCaptiveSprite.class; }

    @Override public Message nameMessage() { return Message.key("journey.captive_name"); }
    @Override public String displayName() { return Messages.get("journey.captive_name"); }
    @Override public String description() { return Messages.get("journey.captive_desc"); }
    @Override public String displayDescription() { return description(); }
    @Override public void damage(int damage, Object source) { }
    @Override public void add(Buff buff) { }
    @Override public void die(Object source) { }
    @Override public boolean reset() { return true; }

    @Override public void interact() {
        if (!JourneyRun.isCaptive(this)) return;
        if (JourneyRun.guardsRemaining() > 0) {
            GameScene.show(new WndMessage(Message.key("journey.guards_blocked")));
            return;
        }
        try {
            JourneyRun.rescue(this);
        } catch (IOException failure) {
            GameScene.show(new WndMessage(Message.key("journey.save_failed")));
        }
    }
}
