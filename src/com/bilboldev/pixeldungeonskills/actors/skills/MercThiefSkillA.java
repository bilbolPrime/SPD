package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Random;

public class MercThiefSkillA extends Venom {
    {
        tag = "mercA";
    }

    @Override
    protected boolean upgrade()
    {
        return false;
    }

    @Override
    public boolean venomousAttack()
    {
        if(Random.Int(100) < 5 * level + 15)
        {
            return true;
        }
        return false;
    }

    @Override
    public void castTextYell()
    {
        if(castText != null)
            Dungeon.hero.hiredMerc.sprite.showStatus(CharSprite.NEUTRAL, castText);
    }

    @Override
    public String info()
    {
        return "Chance to poison target.\n";
    }
}
