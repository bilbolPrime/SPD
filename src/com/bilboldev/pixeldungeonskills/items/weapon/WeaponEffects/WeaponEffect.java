package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfAvalanche;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfDisintegration;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfFirebolt;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfLightning;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicMissile;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Gun;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Random;

import java.util.Locale;

public class WeaponEffect {
    public String Name = "";

    public boolean roll(Char source, Char target) {
        return canAffect(source, target) && Random.Int(100) < 20;
    }

    protected boolean canAffect(Char source, Char target) {
        return source != null && source.isAlive() && target != source
                && target instanceof Mob && target.isAlive()
                && ((Mob)target).hostile && !(target instanceof NPC);
    }

    public float damageMultiplier() {
        return 1f;
    }

    public void onHit(Char source, Char target, int damageDealt) {
    }

    public String name(String baseName) {
        return Messages.get(WeaponEffect.class, Name.toLowerCase(Locale.ROOT) + ".name", baseName);
    }

    public String prefix() {
        return Messages.get(WeaponEffect.class, Name.toLowerCase(Locale.ROOT) + ".prefix");
    }

    public String description() {
        return Messages.get(WeaponEffect.class, Name.toLowerCase(Locale.ROOT) + ".desc");
    }

    public Message activationText() {
        return Message.key(WeaponEffect.class, Name.toLowerCase(Locale.ROOT) + ".activate");
    }

    public String activationName() {
        return Messages.get(WeaponEffect.class, Name.toLowerCase(Locale.ROOT) + ".activate");
    }

    protected void showHit(Char target, int color, Emitter.Factory particles, String sound) {
        if (target.sprite == null || !target.sprite.visible) return;
        target.sprite.showStatus(color, activationText());
        target.sprite.centerEmitter().burst(particles, 6);
        Sample.INSTANCE.play(sound, 0.6f, 0.6f, 1f);
    }

    public float Effect(Char source, Char target) {
        if (!roll(source, target)) return 1f;
        YellText(source);
        return damageMultiplier();
    }

    public void YellText(Char source) {
        if (source.sprite != null) source.sprite.showStatus(CharSprite.NEUTRAL, activationText());
    }

    public static WeaponEffect restore(String name) {
        if (name == null) return null;
        switch (name) {
            case "Smite": return new Smite();
            case "KnockBack": return new KnockBack();
            case "Venom": return new Venom();
            case "KneeShot": return new KneeShot();
            case "Spark": return new Spark();
            case "Curse": return new Curse();
            case "DrainLife": return new DrainLife();
            default: return null;
        }
    }

    public static boolean supports(Item item) {
        if (item instanceof Gun) return false;
        if (item instanceof MeleeWeapon) return true;
        if (item instanceof Bow) return true;
        return item instanceof WandOfMagicMissile || item instanceof WandOfFirebolt
                || item instanceof WandOfLightning || item instanceof WandOfDisintegration
                || item instanceof WandOfAvalanche;
    }

    public static WeaponEffect random(Item item) {
        if (!supports(item)) return null;
        if (item instanceof MeleeWeapon) {
            switch (Random.Int(3)) {
                case 0: return new Smite();
                case 1: return new KnockBack();
                default: return new Venom();
            }
        }
        if (item instanceof Bow) {
            return Random.Int(2) == 0 ? new Venom() : new KneeShot();
        }
        switch (Random.Int(3)) {
            case 0: return new Spark();
            case 1: return new Curse();
            default: return new DrainLife();
        }
    }
}
