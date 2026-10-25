package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;

public class FrostCoating extends FlavourBuff {
    @Override
    public int icon() {
        return BuffIndicator.FROST;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }
}
