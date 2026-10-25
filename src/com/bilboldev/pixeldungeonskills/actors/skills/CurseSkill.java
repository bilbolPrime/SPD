package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class CurseSkill extends ActiveSkill1{

    {
        name = "Curse";
        castText = Message.key("custom.generated.necro_merc.curse_cast");
        tier = 1;
        image = 182;
        mana = 10;
        useDelay = 3f;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if (level > 0 && hero.MP >= getManaCost())
            actions.add(AC_CAST);

        if (hero.skillTree.canLevel(this)){
            actions.add(AC_ADVANCE);
        }
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action == Skill.AC_CAST && hero.MP >= getManaCost())
        {
            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.SPARK);
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
        return necroMercInfo("curse_desc", extendedInfo())

                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_weakens", i, (10 * i));

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
            return "\n" + necroMercText("requires_spirituality");
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(SpiritualitySkill.class);
        return toReturn;
    }
}
