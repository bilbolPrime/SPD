package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.GhostSummonedPet;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.WraithSprite;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class SummonGhostSkill extends ActiveSkill3{

    {
        name = "Summon Ghost";
        castText = Message.key("custom.generated.necro_merc.haunt");
        image = 180;
        tier = 3;
        mana = 15;
        useDelay = 3f;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if (level > 0 && hero.MP >= getManaCost())
            actions.add(AC_SUMMON);

        if (hero.skillTree.canLevel(this)){
            actions.add(AC_ADVANCE);
        }

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action == Skill.AC_SUMMON)
        {
            boolean spawned = false;
            int newPos = hero.pos;
            if (Actor.findChar(newPos) != null) {
                ArrayList<Integer> candidates = new ArrayList<Integer>();
                for (int n : Level.NEIGHBOURS4) {
                    int c = hero.pos + n;
                    if (c < 0 || c >= Level.passable.length)
                        continue;
                    if (Level.passable[c] && Actor.findChar(c) == null) {
                        candidates.add(c);
                    }
                }
                newPos = candidates.size() > 0 ? Random.element(candidates) : -1;
                if (newPos != -1) {
                    spawned = true;
                    SummonedPet ghost = new GhostSummonedPet();
                    ghost.spawn(level);
                    ghost.name = "Summoned Ghost";
                    ghost.spriteClass = WraithSprite.class;
                    ghost.pos = newPos;
                    GameScene.add(ghost);
                    Actor.addDelayed(new Pushing(ghost, hero.pos, newPos), -1);
                }
            }

            if (spawned) {
                hero.MP -= getManaCost();
                StatusPane.manaDropping += getManaCost();
                castTextYell();
                hero.spend( TIME_TO_USE );
                hero.busy();
                hero.sprite.operate( hero.pos );
            }
        }

        if (action == Skill.AC_ADVANCE )
        {
            super.execute(hero, action);
        }
    }

    @Override
    public String info()
    {
        return necroMercInfo("summon_ghost_desc", extendedInfo())
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_ghost", i, i);
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

    @Override
    public String requiresInfo(){
        if (level == 0){
            return "\n" + necroMercText("requires_spirit_binder");
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(SpiritBinderSkill.class);
        return toReturn;
    }
}
