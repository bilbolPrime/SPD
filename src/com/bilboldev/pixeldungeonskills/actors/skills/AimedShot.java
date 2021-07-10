package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.ElmoParticle;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class AimedShot extends ActiveSkill1{


    boolean cast;
    {
        name = "Aimed Shot";
        castText = "I see him";
        image = 93;
        tier = 1;
        mana = 3;
        useDelay = 4f;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active2.active = false; // Disable Double shot
            hero.heroSkills.active3.active = false; // Disable Bombvoyage
        }
    }

    @Override
    public float rangedDamageModifier()
    {
        float toReturn = 1f;
        toReturn += cast ? 0.05f * level : 0;
        cast = false;
        return toReturn;
    }

    @Override
    public int aimedShot()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
        {
            cast = false;
            return 0;
        }

        cast = true;


        Dungeon.hero.MP -= getManaCost();
        StatusPane.manaDropping += getManaCost();

        castTextYell();
        return level * 2;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "A very accurate strong attack.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ":  +" + (int)(2 * i) + " attack skill, +" + (int)(i * 5f) + "% damage.";

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
            return "\nRequires: Accuracy";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Accuracy.class);
        return toReturn;
    }
}
