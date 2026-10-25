package com.bilboldev.pixeldungeonskills.items.potions;

import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.ManaRecovery;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

public class PotionOfRejuvenation extends Potion {
    public PotionOfRejuvenation() {
        super(ItemSpriteSheet.POTION_REJUVENATION);
        name = "Potion of Rejuvenation";
    }

    @Override
    protected void apply(Hero hero) {
        PotionOfHealing.heal(hero);
        hero.MP = hero.MMP;
        Buff.detach(hero, ManaRecovery.class);
        StatusPane.takingDamage = StatusPane.manaDropping = 0;
    }

    @Override
    public boolean isKnown() { return true; }

    @Override
    public void setKnown() { }

    @Override
    public String desc() { return Messages.get(this, "desc"); }

    @Override
    public int price() { return 60 * quantity; }
}
