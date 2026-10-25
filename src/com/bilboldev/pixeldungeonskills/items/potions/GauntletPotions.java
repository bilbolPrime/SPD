package com.bilboldev.pixeldungeonskills.items.potions;

import com.bilboldev.pixeldungeonskills.actors.hero.GauntletHero;

public final class GauntletPotions {
    private GauntletPotions() {}

    public static void applyRewards(GauntletHero hero) {
        new PotionOfExperience().apply(hero);
        new PotionOfStrength().apply(hero);
    }
}
