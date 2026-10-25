package com.bilboldev.pixeldungeonskills.items.weapon.ice;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.GhostSummonedPet;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class GravefrostScythe extends MeleeWeapon implements IceWeapon {
    public GravefrostScythe() {
        super(4, 1, 1.1f);
        image = ItemSpriteSheet.GRAVEFROST_SCYTHE;
        unique = true;
    }

    @Override
    public HeroClass heroClass() {
        return HeroClass.NECROMANCER;
    }

    @Override
    protected int max0() {
        return 20;
    }

    public static int coldDamage(SummonedPet minion) {
        if (!IceWeapon.magicAllowed() || minion.hostile) return 0;
        Hero owner = minion.owner();
        if (owner == null || !owner.isAlive() || owner.heroClass != HeroClass.NECROMANCER
                || !(owner.belongings.weapon instanceof GravefrostScythe)) return 0;
        if (minion.petType != SummonedPet.PET_TYPES.SKELETON
                && minion.petType != SummonedPet.PET_TYPES.SKELETON_ARCHER
                && !(minion instanceof GhostSummonedPet)) return 0;
        return 2 + owner.belongings.weapon.effectiveLevel() / 2;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", 2 + effectiveLevel() / 2);
    }
}
