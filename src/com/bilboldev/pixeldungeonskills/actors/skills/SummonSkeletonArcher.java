package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

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

public class SummonSkeletonArcher extends ActiveSkill3{

    {
        name = "Summon Skeleton Archer";
        castText = Message.key("custom.generated.entry_the_dead_shall_obey_f749b0c50c");
        tier = 3;
        image = 44;
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
            for (int nu = 0; nu < 1; nu++) {
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
                        SummonedPet skeleton = new SummonedPet(SummonedPet.PET_TYPES.SKELETON_ARCHER);
                        skeleton.spawn(level);
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
                StatusPane.manaDropping += getManaCost();
                castTextYell();
                hero.spend( TIME_TO_USE );
                hero.busy();
                hero.sprite.operate( hero.pos );
            }
            Dungeon.hero.heroSkills.lastUsed = this;
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
        return "Summons a skeleton archer only if the target cell has a corpse.\n\n"
                + extendedInfo()
                + costUpgradeInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": Summons power " + i + " skeleton archer.";
            if (i == level){
                sb.append(highlight(levelDescription));
            }
            else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return  sb.toString();
    }

}
