package com.bilboldev.pixeldungeonskills.items.food;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.BarbecueKing;

public class RoyalDinner extends ChargrilledMeat {
    @Override
    public boolean doPickUp(Hero hero) {
        if (!super.doPickUp(hero)) return false;
        for (Mob mob : Dungeon.level.mobs)
            if (mob instanceof BarbecueKing) ((BarbecueKing)mob).takeDinner();
        return true;
    }
}
