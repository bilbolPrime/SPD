package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class DarkBolt extends ActiveSkill3{

    {
        name = "Dark Bolt";
        castText = Message.key("custom.generated.entry_i_must_6ab83c4e86");
        tier = 3;
        image = 113;
        mana = 5;
    }

    @Override
    public float getAlpha()
    {
        return 1f;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if(level > 0 && hero.MP >= getManaCost())
            actions.add(AC_CAST);
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if(action == Skill.AC_CAST && hero.MP >= getManaCost())
        {

            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.DARK_BOLT);
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
        return "Hurls concentrated dark energy into a target shredding their essence to pieces.\n What's left is a fate worse than death.\n"

                + costUpgradeInfo();
    }

}
