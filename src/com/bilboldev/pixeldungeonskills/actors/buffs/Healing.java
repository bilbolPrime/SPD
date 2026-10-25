package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;

public class Healing extends Recovery {
    @Override
    protected boolean mana() { return false; }

    @Override
    public int icon() { return BuffIndicator.HEALING; }
}
