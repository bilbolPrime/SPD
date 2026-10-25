package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.utils.Bundle;

public class NecroWeakened extends FlavourBuff {

    private float outgoingDamageModifier = 1f;

    public NecroWeakened setLevel(int level) {
        outgoingDamageModifier = Math.max(0.2f, 1f - 0.05f * level);
        return this;
    }

    public float outgoingDamageModifier() {
        return outgoingDamageModifier;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("damage_modifier", outgoingDamageModifier);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains("damage_modifier")) {
            outgoingDamageModifier = bundle.getFloat("damage_modifier");
        } else if (outgoingDamageModifier == 1f) {
            outgoingDamageModifier = 0.95f;
        }
    }

    @Override
    public int icon() {
        return BuffIndicator.WEAKNESS;
    }

    @Override
    public String toString() {
        return "Weakened";
    }
}
