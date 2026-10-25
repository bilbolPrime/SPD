package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.items.potions.*;
import com.bilboldev.pixeldungeonskills.plants.*;
import java.util.function.Supplier;

public enum AlchemyRecipe {
    HEALING(Sungrass.Seed.class, PotionOfHealing.class, PotionOfHealing::new),
    LIQUID_FLAME(Firebloom.Seed.class, PotionOfLiquidFlame.class, PotionOfLiquidFlame::new),
    FROST(Icecap.Seed.class, PotionOfFrost.class, PotionOfFrost::new),
    TOXIC_GAS(Sorrowmoss.Seed.class, PotionOfToxicGas.class, PotionOfToxicGas::new),
    PARALYTIC_GAS(Earthroot.Seed.class, PotionOfParalyticGas.class, PotionOfParalyticGas::new),
    INVISIBILITY(Dreamweed.Seed.class, PotionOfInvisibility.class, PotionOfInvisibility::new),
    MIND_VISION(Fadeleaf.Seed.class, PotionOfMindVision.class, PotionOfMindVision::new);

    public final Class<? extends Plant.Seed> seed;
    public final Class<? extends Potion> potionType;
    private final Supplier<Potion> potion;

    AlchemyRecipe(Class<? extends Plant.Seed> seed, Class<? extends Potion> potionType, Supplier<Potion> potion) {
        this.seed = seed;
        this.potionType = potionType;
        this.potion = potion;
    }

    public Potion potion() {
        if (this == HEALING && Dungeon.rules().mode == GameplayMode.REBORN) return new PotionOfRejuvenation();
        return potion.get();
    }

    public Class<? extends Potion> lorePotion() {
        return this == HEALING ? PotionOfRejuvenation.class : potionType;
    }

    public static AlchemyRecipe forSeed(Plant.Seed seed) {
        for (AlchemyRecipe recipe : values()) {
            if (recipe.seed == seed.getClass()) return recipe;
        }
        return null;
    }
}
