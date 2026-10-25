/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.items.weapon.melee;

import com.bilboldev.noosa.Camera;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.ChampWhiteHalo;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfRemoveCurse;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public class SoulBlade extends MeleeWeapon {

    public static final String AC_HEAL = "Recover";
    public static final String AC_CLEANSE = "Cleanse";
    public static final String AC_FURY = "Fury";

    {
		name = "soul blade";
		image = ItemSpriteSheet.SoulBlade_1;
	}

    public int charge = 0;

	public SoulBlade() {
		super( 3, 0.7f, 1f );
	}

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if(Dungeon.hero != null){
            charge = Dungeon.hero.MP;
        }

        if(level > 2 && charge >= 20)
            actions.add(AC_HEAL);
        if(level > 4 && charge >= 50)
            actions.add(AC_CLEANSE);
        if(level > 6 && charge >= 50)
            actions.add(AC_FURY);

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action.equals( AC_HEAL )) {

            hero.MP -= 20;
            hero.HP += 20;
            if(hero.HP > hero.HT)
                hero.HP = hero.HT;
            GLog.p("custom.generated.entry_soul_blade_heals_20_hp_0eba83fafc");

         CellEmitter.center(hero.pos).burst(Speck.factory(Speck.HEALING), 1);

        }else if (action.equals(AC_CLEANSE)) {
            hero.MP -= 50;
            ScrollOfRemoveCurse.uncurse( hero, hero.belongings.backpack.items.toArray( new Item[0] ) );
            ScrollOfRemoveCurse.uncurse( hero,
                    hero.belongings.weapon,
                    hero.belongings.armor,
                    hero.belongings.ring1,
                    hero.belongings.ring2 );

            GLog.p("custom.generated.entry_you_feel_the_blade_cleanse_any_dark_presence_2cbd48d1e1");
        }
        else if(action.equals(AC_FURY)){
            int damage = 10 + hero.MP / 5;

            ChampWhiteHalo tmp = new ChampWhiteHalo( hero.sprite ) ;
            tmp.radius(25f);
            GameScene.effect( tmp);
            tmp.putOut();
            Camera.main.shake( 3, 0.07f * (10) );

            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                if (Level.fieldOfView[mob.pos]) {
                    mob.damage( Random.IntRange( (int)(damage * 0.75) , damage ), this );
                }
            }

            hero.MP = 0;

            GLog.p("custom.generated.entry_you_are_safe_they_are_not_6417970aaf");

        }
        else
        {

            super.execute( hero, action );

        }
    }

    @Override
    public int damageRoll( Hero hero ) {
        int damage = super.damageRoll( hero );

        return damage;
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        charge = bundle.getInt("CHARGE");
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put( "CHARGE", charge );
    }

    @Override
	public String desc() {
		return "A blade that can only be held by the purest of champions. The soul blade is powered by the very soul essence of its wielder.\n" +
                "\n" +
                highlight("Level 3: Recover 20 health for 20 mana") + " \n\n"+
                highlight("Level 5: Dispel any curses for 50 mana") + " \n\n"+
                        highlight("Level 7: Channels all spiritual energy into the blade damage all hostiles; at least 50 mana") +  " \n";
	}
}
