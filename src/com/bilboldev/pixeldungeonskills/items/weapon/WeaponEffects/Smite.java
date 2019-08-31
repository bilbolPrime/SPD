package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 03-Mar-18.
 */
public class Smite extends WeaponEffect {
    {
        Name = "Smite";
        Prefix = "Barbaric";
        Description = "It has a 20% chance of casting level 1 Smite when hitting a target.";
    }

    @Override
    public float Effect(Char source, Char target)
    {
        if(Random.Int(100) < 20 ) {
            YellText(source);
            return 1.2f;
        }
        return 1f;
    }
}
