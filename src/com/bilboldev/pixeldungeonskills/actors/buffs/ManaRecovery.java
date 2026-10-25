package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;

public class ManaRecovery extends Recovery {
    @Override
    protected boolean mana() { return true; }

    @Override
    public int icon() { return BuffIndicator.MANA_RECOVERY; }
}
