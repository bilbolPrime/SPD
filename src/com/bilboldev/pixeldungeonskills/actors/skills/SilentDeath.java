package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class SilentDeath extends PassiveSkillB3{


    {
        name = "Silent Death";
        castText = "Eternal Slumber";
        tier = 3;
        image = 59;
    }

    public boolean instantKill()
    {

        if(Random.Int(100) < 10 * level)
        {
            castText = "Eternal Slumber";
            castTextYell();
            return true;
        }
        castText = "Almost had him...";
        castTextYell();
        return false;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public String info()
    {
        return "10% per level chance to instantly kill a sleeping enemy.\n"
                + costUpgradeInfo();
    }
}
