package com.bilboldev.pixeldungeonskills.actors.skills;

public class WantedSkill extends PassiveSkillA1 {

    {
        name = "Wanted";
        tier = 1;
        image = 162;
    }

    @Override
    protected boolean upgrade() {
        return true;
    }

    public int wantedGoldBonus() {
        return level;
    }

    @Override
    public String info() {
        return "Everybody is a target.\n\n"
                + extendedInfo()
                + costString();
    }

    @Override
    public String extendedInfo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++) {
            String levelDescription = "Level " + i + ": +" + i + " gold per mob killed.";
            if (i == level) {
                sb.append(highlight(levelDescription));
            } else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
