package com.bilboldev.pixeldungeonskills.actors.skills;

public class PackratSkill extends PassiveSkillA1 {

    {
        name = "Packrat";
        image = 163;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    public int gunAmmoBonus() {
        return level;
    }

    @Override
    public String info()
    {
        return "Can't have enough.\n\n"
            + extendedInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + i + " ammo on firearms.";
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
