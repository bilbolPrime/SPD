package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.actors.Char;

public class RebornCurse extends NecroWeakened {
    public RebornCurse() {
        setLevel(4);
    }

    public static int spellDamage(Char caster, int damage) {
        RebornChampion champion = caster.buff(RebornChampion.class);
        if (champion != null) damage = champion.damage(damage);
        NecroWeakened curse = caster.buff(NecroWeakened.class);
        return curse == null ? damage : Math.max(0, (int)(damage * curse.outgoingDamageModifier()));
    }
}
