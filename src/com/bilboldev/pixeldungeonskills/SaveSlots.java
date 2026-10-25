package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SaveSlots {
    public static final int FIRST = 1;
    public static final int COUNT = 6;

    private SaveSlots() {}

    public static int validate(int slot) {
        if (slot < FIRST || slot > COUNT) throw new IllegalArgumentException("Invalid save slot: " + slot);
        return slot;
    }

    private static String stem(HeroClass heroClass) {
        switch (heroClass) {
            case WARRIOR: return "warrior";
            case MERCENARY: return "mercenary";
            case MAGE: return "mage";
            case NECROMANCER: return "necromancer";
            case HUNTRESS: return "ranger";
            case ROGUE: return "game";
            default: return "dummygame";
        }
    }

    public static String gameFile(HeroClass heroClass, int slot) {
        validate(slot);
        return stem(heroClass) + (slot == FIRST ? "" : "_slot" + slot) + ".dat";
    }

    private static String levelPrefix(HeroClass heroClass, int slot) {
        validate(slot);
        if (slot != FIRST) return stem(heroClass) + "_slot" + slot + "_depth";
        if (heroClass == HeroClass.ROGUE) return "depth";
        String stem = stem(heroClass);
        return "dummygame".equals(stem) ? "dummydepth" : stem;
    }

    public static String levelFile(HeroClass heroClass, int slot, int depth) {
        return levelPrefix(heroClass, slot) + String.format(Locale.ROOT, "%d", depth) + ".dat";
    }

    public static boolean isLevelFile(HeroClass heroClass, int slot, String filename) {
        return filename.matches(Pattern.quote(levelPrefix(heroClass, slot)) + "[0-9]+\\.dat");
    }

    public static boolean exists(HeroClass heroClass, int slot) {
        String filename = gameFile(heroClass, slot);
        for (String file : Game.instance.fileList()) if (filename.equals(file)) return true;
        return false;
    }
}
