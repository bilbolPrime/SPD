package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;

public class ArmorEvasion extends FlavourBuff {
    public static final int BONUS = 5;
    public static final float DURATION = 5f;

    @Override
    public int icon() {
        return BuffIndicator.SHADOWS;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }
}
