package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.noosa.Camera;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.ChampWhiteHalo;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Death;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.SoulBlade;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.LegendSprite;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class EnabledSoulFury extends SoulFury {



    {
        name = "Soul Fury";
        castText = "Forgive me girls";
        tier = 3;
        image = 123;
        mana = 3;
    }


    @Override
    public float getAlpha()
    {
        return 1f;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if(active == false)
            actions.add(AC_CAST);


        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if(action == Skill.AC_CAST)
        {
            Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, castText);
            Dungeon.hero.HP = Dungeon.hero.HT = 1000;
            Dungeon.hero.MP = Dungeon.hero.MMP = 1000;
            SoulBlade soulBlade = (SoulBlade)new SoulBlade().identify();
            soulBlade.level(150);
            soulBlade.enchant(new Death());
            Dungeon.hero.belongings.weapon = soulBlade;
            ((LegendSprite) Dungeon.hero.sprite).haloUpFury();

            ChampWhiteHalo tmp = new ChampWhiteHalo( hero.sprite ) ;
            tmp.radius(25f);
            GameScene.effect( tmp);
            tmp.putOut();
            Camera.main.shake( 3, 0.07f * (10) );

            active = true;
        }

    }

    @Override
    public int getManaCost()
    {
        return 0;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Sets spiritual energy ablaze unlocking god-like powers.\n"
                + "No one ever survived Soul Fury...\n" +
                "- 1000% health and health regeneration\n" +
                "- 1000% mana and mana regeneration\n" +
                "- Divine Blade\n" +
                "- Enemies around you take damage with every step\n" +
                "- A slow painful death once you burn out";
    }

}
