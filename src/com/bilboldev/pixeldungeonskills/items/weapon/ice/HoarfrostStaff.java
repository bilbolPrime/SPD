package com.bilboldev.pixeldungeonskills.items.weapon.ice;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Slow;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;

public class HoarfrostStaff extends Wand implements IceWeapon {
    public HoarfrostStaff() {
        image = ItemSpriteSheet.HOARFROST_STAFF;
        unique = true;
    }

    @Override
    public HeroClass heroClass() {
        return HeroClass.MAGE;
    }

    @Override
    public boolean isKnown() {
        return true;
    }

    @Override
    public void setKnown() { }

    @Override
    protected int initialCharges() {
        return 3;
    }

    @Override
    public int min() {
        return 3 + effectiveLevel();
    }

    @Override
    public int max() {
        return 12 + effectiveLevel() * 2;
    }

    @Override
    public void execute(Hero hero, String action) {
        if (AC_ZAP.equals(action) && !IceWeapon.canUse(this, hero)) return;
        super.execute(hero, action);
    }

    @Override
    protected void onZap(int cell) {
        Char target = Actor.findChar(cell);
        if (target == null) return;
        damage(target, (int)(Random.NormalIntRange(8 + power(), 16 + power() * 2) * wandBonusDamageModifier()));
        if (IceWeapon.magicAllowed() && target.isAlive() && !target.immunities().contains(HoarfrostStaff.class)) {
            Buff.prolong(target, Slow.class, 2);
        }
    }

    @Override
    protected void fx(int cell, Callback callback) {
        MagicMissile.slowness(curUser.sprite.parent, curUser.pos, cell, callback);
        Sample.INSTANCE.play(Assets.SND_ZAP);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", 8 + power(), 16 + power() * 2);
    }
}
