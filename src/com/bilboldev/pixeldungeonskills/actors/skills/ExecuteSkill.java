package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

public class ExecuteSkill extends PassiveSkillA3 {

    {
        name = "Execute";
        image = 168;
        tier = 4;
    }

    @Override
    protected boolean upgrade() {
        return true;
    }

    @Override
    public float executeThreshold() {
        return 0.05f * level;
    }

    @Override
    public String info() {
        return "Finish the job.\n\n"
                + extendedInfo()
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++) {
            String levelDescription = "Level " + i + ": instantly kill enemies below " + (i * 5) + "% health.";
            if (i == level) {
                sb.append(highlight(levelDescription));
            } else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String requiresInfo() {
        if (level == 0) {
            return "\nRequires: No Witnesses";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements() {
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(NoWitnessesSkill.class);
        return toReturn;
    }
}
