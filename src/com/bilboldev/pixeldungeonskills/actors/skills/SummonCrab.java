package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Crab;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class SummonCrab extends ActiveSkill2{


    {
        name = "Summon Crab";
        castText = "Fight for me!";
        tier = 2;
        image = 42;
        mana = 3;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if(level > 0 && hero.MP >= getManaCost())
            actions.add(AC_SUMMON);
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if(action == Skill.AC_SUMMON)
        {
            boolean spawned = false;
            for (int nu = 0; nu < 1 + hero.heroSkills.passiveB3.summoningLimitBonus(); nu++) { // <--- Mage Summoner when present
                int newPos = hero.pos;
                if (Actor.findChar(newPos) != null) {
                    ArrayList<Integer> candidates = new ArrayList<Integer>();
                    boolean[] passable = Level.passable;

                    for (int n : Level.NEIGHBOURS4) {
                        int c = hero.pos + n;
                        if (passable[c] && Actor.findChar(c) == null) {
                            candidates.add(c);
                        }
                    }
                    newPos = candidates.size() > 0 ? Random.element(candidates) : -1;
                    if (newPos != -1) {
                        spawned = true;
                        Crab crab = new Crab();
                        crab.spawn(level * 5 + 5);
                        crab.HP = crab.HT;
                        crab.pos = newPos;
                        GameScene.add(crab);
                        Actor.addDelayed(new Pushing(crab, hero.pos, newPos), -1);
                        crab.sprite.alpha(0);
                        crab.sprite.parent.add(new AlphaTweener(crab.sprite, 1, 0.15f));
                    }
                }
            }

            if(spawned == true) {
                hero.MP -= getManaCost();
                castTextYell();
            }
        }
    }

    @Override
    public int getManaCost()
    {
        return (int)Math.ceil(mana * (1 + 0.55 * level));
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Summons Crabs for your service.\n"
                + costUpgradeInfo();
    }

}
