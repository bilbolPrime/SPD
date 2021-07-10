package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class SummonRat extends ActiveSkill1{


    {
        name = "Summon Minion";
        castText = "Rise Rodent!";
        tier = 1;
        image = 41;
        mana = 15;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if(level > 0 && hero.MP >= getManaCost())
            actions.add(AC_SUMMON);

        if(hero.skillTree.canLevel(this)){
            actions.add(AC_ADVANCE);
        }
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if(action == Skill.AC_SUMMON)
        {
            boolean spawned = false;
            for (int nu = 0; nu < 1 ; nu++) {
                int newPos = hero.pos;
                if (Actor.findChar(newPos) != null) {
                    ArrayList<Integer> candidates = new ArrayList<Integer>();
                    boolean[] passable = Level.passable;

                    for (int n : Level.NEIGHBOURS4) {
                        int c = hero.pos + n;
                        if(c < 0 || c >= Level.passable.length)
                            continue;
                        if (passable[c] && Actor.findChar(c) == null) {
                            candidates.add(c);
                        }
                    }
                    newPos = candidates.size() > 0 ? Random.element(candidates) : -1;
                    if (newPos != -1) {
                        spawned = true;

                            SummonedPet rat = new SummonedPet(SummonedPet.PET_TYPES.RAT);
                            rat.spawn(level);
                            rat.defenseSkill = ratMinionAttack();
                            rat.HP = rat.HT = ratMinionHealth();
                            rat.pos = newPos;
                            GameScene.add(rat);
                            Actor.addDelayed(new Pushing(rat, hero.pos, newPos), -1);
                            rat.sprite.alpha(0);
                            rat.sprite.parent.add(new AlphaTweener(rat.sprite, 1, 0.15f));
                        }
                }
            }

            if(spawned == true) {
                hero.MP -= getManaCost();
                StatusPane.manaDropping += getManaCost();
                castTextYell();
                hero.spend( TIME_TO_USE );
                hero.busy();
                hero.sprite.operate( hero.pos );
            }
            Dungeon.hero.heroSkills.lastUsed = this;
        }
        else {
            super.execute(hero, action);
        }
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Summons a minion to serve your cause.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + (int)(3 + i) + " attack and " + (int)(10 + i * 3) + " health.";

            if(i == level){
                sb.append(highlight(levelDescription));
            }
            else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return  sb.toString();
    }

    public int ratMinionAttack(){
        return 3 + level;
    }

    public int ratMinionHealth(){
        return 10 + level * 3;
    }


    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Spirituality";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Spirituality.class);
        return toReturn;
    }
}
