package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Skeleton;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class SummonSkeleton extends ActiveSkill3{


    {
        name = "Summon Skeleton";
        castText = "The dead shall obey!";
        tier = 3;
        image = 43;
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
                        Skeleton skeleton = new Skeleton();
                        skeleton.spawn(level * 5 + 5);
                        skeleton.HP = skeleton.HT;
                        skeleton.pos = newPos;
                        GameScene.add(skeleton);
                        Actor.addDelayed(new Pushing(skeleton, hero.pos, newPos), -1);
                        skeleton.sprite.alpha(0);
                        skeleton.sprite.parent.add(new AlphaTweener(skeleton.sprite, 1, 0.15f));
                    }
                }
            }

            if(spawned == true) {
                hero.MP -= getManaCost();
                castTextYell();
            }
            Dungeon.hero.heroSkills.lastUsed = this;
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
        return "Summons Skeletons for your service.\n"
                + costUpgradeInfo();
    }

}
