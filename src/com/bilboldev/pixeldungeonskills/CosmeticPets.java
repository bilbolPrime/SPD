package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.MovieClip;
import com.bilboldev.pixeldungeonskills.Badges.Badge;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.sprites.*;

public final class CosmeticPets {
    private static final String UNLOCKED = "cosmetic_pets";
    private static final String SELECTED = "selected_pet";
    public static final int COUNT = 6;

    private CosmeticPets() { }

    public static String name(int pet) {
        return Messages.get("pets.name_" + pet);
    }

    public static String description(int pet) {
        return Messages.get("pets.desc_" + pet);
    }

    public static boolean unlocked(int pet) {
        if (pet < 1 || pet > COUNT) return false;
        if (pet == 1 || (Preferences.INSTANCE.getInt(UNLOCKED, 0) & (1 << pet)) != 0) return true;
        switch (pet) {
            case 2: return Badges.isUnlocked(Badge.BOSS_SLAIN_1);
            case 3:
                for (HeroClass hero : HeroClass.values()) {
                    if (Badges.isUnlocked(hero.masteryBadge())) return true;
                }
                return false;
            case 4: return Badges.isUnlocked(Badge.BOSS_SLAIN_3);
            case 6: return Badges.isUnlocked(Badge.VICTORY);
            default: return false;
        }
    }

    public static void unlock(int pet) {
        if (pet < 2 || pet > COUNT || unlocked(pet)) return;
        Preferences.INSTANCE.put(UNLOCKED, Preferences.INSTANCE.getInt(UNLOCKED, 0) | (1 << pet));
    }

    public static int forBoss(Mob mob) {
        if (mob instanceof Goo || mob instanceof RebornGoo) return 2;
        if (mob instanceof Tengu) return 3;
        if (mob instanceof DM300 || mob instanceof RebornDM300) return 4;
        if (mob instanceof OldRime) return 5;
        if (mob instanceof Yog || mob instanceof RebornYog) return 6;
        return 0;
    }

    public static void defeated(Mob mob) {
        int pet = forBoss(mob);
        if (pet == 0 || GauntletRun.active() || DailyRun.active() || Dungeon.hero == null) return;
        unlock(pet);
        PartySession.petUnlocked(pet);
    }

    public static int selected() {
        if (!SupporterEntitlements.ownsPets()) return 0;
        int pet = Preferences.INSTANCE.getInt(SELECTED, 1);
        return unlocked(pet) ? pet : 0;
    }

    public static void select(int pet) {
        if (pet != 0 && (!SupporterEntitlements.ownsPets() || !unlocked(pet))) return;
        Preferences.INSTANCE.put(SELECTED, pet);
    }

    public static MovieClip image(int pet) {
        CharSprite sprite = sprite(pet);
        MovieClip image = new MovieClip(sprite.texture);
        image.play(sprite.idleAnimation());
        sprite.destroy();
        image.scale.set(Math.min(1f, 9f / Math.max(image.width, image.height)));
        return image;
    }

    public static CharSprite sprite(int pet) {
        switch (pet) {
            case 2: return new GooSprite();
            case 3: return new TenguSprite();
            case 4: return new DM300Sprite();
            case 5: return new IceSprite.Rime();
            case 6: return new YogSprite();
            default: return new RatSprite();
        }
    }
}
