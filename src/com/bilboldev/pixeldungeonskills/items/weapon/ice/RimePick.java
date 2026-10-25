package com.bilboldev.pixeldungeonskills.items.weapon.ice;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class RimePick extends MeleeWeapon implements IceWeapon {
    public RimePick() {
        super(3, 1.1f, 0.9f);
        image = ItemSpriteSheet.RIME_PICK;
        unique = true;
    }

    @Override
    public HeroClass heroClass() {
        return HeroClass.ROGUE;
    }

    @Override
    protected int max0() {
        return 18;
    }

    public static int pierce(Char target, int armor) {
        if (!IceWeapon.magicAllowed()) return armor;
        boolean surprise = target instanceof Mob && ((Mob)target).surprised();
        return armor - (surprise ? (armor + 1) / 2 : (armor + 3) / 4);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }
}
