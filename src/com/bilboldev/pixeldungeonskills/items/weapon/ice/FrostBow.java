package com.bilboldev.pixeldungeonskills.items.weapon.ice;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import java.util.ArrayList;

public class FrostBow extends Bow implements IceWeapon {
    public FrostBow() {
        image = ItemSpriteSheet.FROST_BOW;
        unique = true;
    }

    @Override
    public HeroClass heroClass() {
        return HeroClass.HUNTRESS;
    }

    @Override
    public boolean doEquip(Hero hero) {
        return IceWeapon.canUse(this, hero) && super.doEquip(hero);
    }

    @Override
    public Bow enchant() {
        return this;
    }

    @Override
    public float damageBonus() {
        return 0.4f + super.damageBonus();
    }

    @Override
    public void bowSpecial(Hero archer, Char target, int damage) {
        if (!IceWeapon.magicAllowed() || damage < 4) return;
        int cell = target.pos;
        for (Mob mob : new ArrayList<>(Dungeon.level.mobs)) {
            if (mob == target || !mob.hostile || !mob.isAlive() || Level.distance(cell, mob.pos) != 1) continue;

            mob.damageFrom(damage / 4, this, archer);
        }
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", Math.round((damageBonus() - 1) * 100));
    }
}
