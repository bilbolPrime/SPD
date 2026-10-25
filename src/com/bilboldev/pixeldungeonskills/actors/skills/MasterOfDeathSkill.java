package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.utils.GLog;

import java.util.ArrayList;

public class MasterOfDeathSkill extends PassiveSkillA3{

    {
        name = "Master Of Death";
        tier = 3;
        image = 181;
        level = 0;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        if (hero.skillTree.canLevel(this)){
            actions.add(AC_ADVANCE);
        }

        return actions;
    }

    @Override
    public int incomingDamageReduction(int damage)
    {
        return 0;
    }

    @Override
    public String info()
    {
        return necroMercInfo("master_of_death_desc", extendedInfo())
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_turn_cooldown", i, (1000 - 50 * i));
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
    public String costString(){
        if (coolDown()) {
            return "\n\n" + highlightNecroMercText("on_cooldown", (int)(availableAfter - skillTrack));
        }
        return "\n\n" + highlightNecroMercText("passive_skill");
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(SpiritBinderSkill.class);
        return toReturn;
    }

    public boolean canRevive() {
        return level > 0 && !coolDown();
    }

    public int cooldownTurns() {
        return Math.max( 1, 1000 - 50 * level );
    }

    public void triggerReviveCooldown() {
        availableAfter = skillTrack + cooldownTurns();
    }
}
