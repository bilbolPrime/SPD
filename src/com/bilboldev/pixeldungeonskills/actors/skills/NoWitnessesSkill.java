package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillTree;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Gun;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

public class NoWitnessesSkill extends PassiveSkillB2{

    {
        name = "No Witnesses";
        image = 169;
        tier = 2;
        mana = 4;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if (active == false && level > 0)
            actions.add(AC_ACTIVATE);
        else if (level > 0)
            actions.add(AC_DEACTIVATE);

        if (hero.skillTree.canLevel(this)) {
            actions.add(AC_ADVANCE);
        }

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action == Skill.AC_ACTIVATE)
        {
            active = true;
        }
        else if (action == Skill.AC_DEACTIVATE)
        {
            active = false;
        }
        else {
            super.execute(hero, action);
        }
    }

    @Override
    public void applyToGunShot(Hero hero, SkillTree.GunShotData shotData) {
        if (!active || level == 0 || !(hero.belongings.weapon instanceof Gun) || hero.MP < getManaCost()) {
            return;
        }

        hero.MP -= getManaCost();
        StatusPane.manaDropping += getManaCost();
        shotData.damageModifier *= 1f + 0.1f * level;
        shotData.flee = true;
    }

    @Override
    public String info()
    {
        return "No one saw anything.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (i * 10) + "% damage, non-target mobs flee.";
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
            return "\nRequires: Packrat";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(PackratSkill.class);
        return toReturn;
    }

    @Override
    public String costString(){
        return "\n\n" + highlight("This is a passive skill that can be enabled or disabled.")
                + "\n" + highlight("Each gun shot costs " + mana + " mana while active.");
    }
}
