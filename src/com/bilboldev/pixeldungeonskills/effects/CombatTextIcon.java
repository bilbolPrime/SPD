package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.DeathBomb;
import com.bilboldev.pixeldungeonskills.actors.blobs.ToxicGas;
import com.bilboldev.pixeldungeonskills.actors.buffs.Bleeding;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.actors.buffs.Ooze;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyRaider;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyCityGuard;
import com.bilboldev.pixeldungeonskills.items.Bomb;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.RemoteBombGround;
import com.bilboldev.pixeldungeonskills.items.armor.glyphs.Multiplicity;
import com.bilboldev.pixeldungeonskills.items.armor.glyphs.Viscosity;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfThorns;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfBloodyRitual;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfPsionicBlast;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfFirebolt;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfLightning;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.DrainLife;
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.Spark;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Death;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Fire;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Luck;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.FrostBow;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.HoarfrostStaff;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.SoulBlade;
import com.bilboldev.pixeldungeonskills.levels.traps.LightningTrap;

public enum CombatTextIcon {
    NONE(-1), PHYSICAL(0), FIRE(1), POISON(2), MAGIC(3), LIGHTNING(4), COLD(5),
    BLEEDING(6), HUNGER(7), GAS(8), ACID(9), EXPLOSION(10), FALL(11),
    REFLECTION(12), DEFERRED(13), STEAM(14), NO_DAMAGE(15), HEALING(16), MANA(17), ABSORBED(18);

    public static final int WIDTH = 7, HEIGHT = 8;
    private static final CombatTextIcon[] TYPES = values();
    public final int id;

    CombatTextIcon(int id) {
        this.id = id;
    }

    public static CombatTextIcon fromId(int id) {
        for (CombatTextIcon type : TYPES) if (type.id == id) return type;
        return NONE;
    }

    public static CombatTextIcon of(Object source) {
        if (source instanceof Burning || source instanceof Fire || source instanceof WandOfFirebolt) return FIRE;
        if (source instanceof Poison || source instanceof ToxicGas) return POISON;
        if (source instanceof Bleeding) return BLEEDING;
        if (source instanceof Hunger) return HUNGER;
        if (source instanceof Ooze) return ACID;
        if (source instanceof LightningTrap.Electricity || source instanceof Spark
                || source instanceof WandOfLightning) return LIGHTNING;
        if (source instanceof FrostBow || source instanceof HoarfrostStaff) return COLD;
        if (source instanceof Bomb || source == Bomb.class || source instanceof DeathBomb
                || source instanceof RemoteBombGround) return EXPLOSION;
        if (source instanceof RingOfThorns.Thorns) return REFLECTION;
        if (source instanceof Viscosity.DeferedDamage) return DEFERRED;
        if (source instanceof Wand || source instanceof DrainLife || source instanceof Death
                || source instanceof Luck || source instanceof Multiplicity || source instanceof Heap
                || source instanceof ScrollOfBloodyRitual || source instanceof ScrollOfPsionicBlast
                || source instanceof SoulBlade) return MAGIC;
        if (source instanceof DailyRaider && ((DailyRaider)source).castsMagic()) return MAGIC;
        if (source instanceof DailyCityGuard && ((DailyCityGuard)source).kind == 3) return MAGIC;
        if (source instanceof Char || source instanceof Weapon) return PHYSICAL;
        return NONE;
    }
}
