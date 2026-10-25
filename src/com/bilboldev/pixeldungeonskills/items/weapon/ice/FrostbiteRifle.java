package com.bilboldev.pixeldungeonskills.items.weapon.ice;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frostbite;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Rifle;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class FrostbiteRifle extends Rifle implements IceWeapon {
    public FrostbiteRifle() {
        image = ItemSpriteSheet.FROSTBITE_RIFLE;
        unique = true;
    }

    @Override
    public HeroClass heroClass() {
        return HeroClass.MERCENARY;
    }

    @Override
    protected int max0() {
        return 18;
    }

    @Override
    protected void onHit(Char target) {
        if (IceWeapon.magicAllowed() && target.isAlive()) Buff.prolong(target, Frostbite.class, 2);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }
}
