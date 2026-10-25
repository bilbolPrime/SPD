package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

public class Toughness extends PassiveSkillA3{

    {
        name = "Toughness";
        tier = 3;
        image = 3;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float incomingDamageModifier()
    {
        return 1f - level * reductionPerLevel();
    }

    protected float reductionPerLevel() {
        return 0.02f;
    }

    @Override
    public String info()
    {
        return "Toughness reduces the amount of incoming damage.\n\n"

                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": -" + Math.round(100 * i * reductionPerLevel()) + "% damage taken.";
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

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Endurance";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Endurance.class);
        return toReturn;
    }
}
