package com.bilboldev.pixeldungeonskills.items.weapon.ice;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.FrostCoating;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class GlacierCleaver extends MeleeWeapon implements IceWeapon {
    public GlacierCleaver() {
        super(4, 1, 1);
        image = ItemSpriteSheet.GLACIER_CLEAVER;
        unique = true;
    }

    @Override
    public HeroClass heroClass() {
        return HeroClass.WARRIOR;
    }

    public int strike(Char target, int damage) {
        if (!IceWeapon.magicAllowed() || damage <= 0 || !target.isAlive()) return damage;
        FrostCoating coating = target.buff(FrostCoating.class);
        if (coating == null) {
            Buff.affect(target, FrostCoating.class, 4);
            return damage;
        }
        coating.detach();
        return damage + 4 + effectiveLevel();
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", 4 + effectiveLevel());
    }
}
