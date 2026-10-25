package com.bilboldev.pixeldungeonskills.items.armor;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.ArmorEvasion;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.EarthParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.utils.Random;

import java.util.Locale;

public enum ArmorPrefix {
    STALWART("Toughness"),
    VIGILANT("Awareness"),
    ELUSIVE("Evasion");

    public final String id;

    ArmorPrefix(String id) {
        this.id = id;
    }

    public String name(String armorName) {
        return Messages.get(ArmorPrefix.class, name().toLowerCase(Locale.ROOT) + ".name", armorName);
    }

    public String description() {
        return Messages.get(ArmorPrefix.class, name().toLowerCase(Locale.ROOT) + ".desc");
    }

    public Message activationText() {
        return Message.key(ArmorPrefix.class, name().toLowerCase(Locale.ROOT) + ".activate");
    }

    public String activationName() {
        return Messages.get(ArmorPrefix.class, name().toLowerCase(Locale.ROOT) + ".activate");
    }

    public static ArmorPrefix restore(String id) {
        for (ArmorPrefix prefix : values()) {
            if (prefix.id.equals(id)) return prefix;
        }
        return null;
    }

    public static boolean canAffect(Char attacker, Hero wearer) {
        return wearer != null && wearer.isAlive() && attacker != wearer
                && attacker instanceof Mob && ((Mob)attacker).hostile && !(attacker instanceof NPC);
    }

    private boolean roll(Char attacker, Hero wearer) {
        return canAffect(attacker, wearer) && wearer.belongings.armor != null
                && wearer.belongings.armor.prefix == this && Random.Int(100) < 20;
    }

    public int reduceDamage(Char attacker, Hero wearer, int damage) {
        if (this != STALWART || damage <= 0 || !roll(attacker, wearer)) return damage;
        show(wearer, 0xE4C278, EarthParticle.FACTORY, Assets.SND_HIT);
        return damage - (int)Math.ceil(damage * 0.2);
    }

    public boolean dodge(Char attacker, Hero wearer) {
        if (this != VIGILANT || wearer == null || wearer.paralysed || !roll(attacker, wearer)) return false;
        show(wearer, 0x9DD5E8, Speck.factory(Speck.LIGHT), Assets.SND_MISS);
        return true;
    }

    public void afterHit(Char attacker, Hero wearer) {
        if (this != ELUSIVE || !roll(attacker, wearer)) return;
        Buff.prolong(wearer, ArmorEvasion.class, ArmorEvasion.DURATION);
        show(wearer, 0xC4ACEC, ShadowParticle.UP, Assets.SND_PUFF);
    }

    private void show(Hero wearer, int color, Emitter.Factory particles, String sound) {
        if (wearer.sprite == null || !wearer.sprite.visible) return;
        wearer.sprite.showStatus(color, activationText());
        wearer.sprite.centerEmitter().burst(particles, 6);
        Sample.INSTANCE.play(sound, 0.6f, 0.6f, 1f);
    }
}
