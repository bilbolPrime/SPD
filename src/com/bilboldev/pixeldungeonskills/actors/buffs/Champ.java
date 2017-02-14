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
package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class Champ extends Buff {

    private static final String TYPE		= "type";
    private static final String BONUS_APPLIED		= "bonusApplied";

    public static final int CHAMP_CHIEF = 1;
    public static final int CHAMP_CURSED = 2;
    public static final int CHAMP_FOUL = 3;
    public static final int CHAMP_VAMPERIC = 4;

    public int type = Random.Int(1, 5);

    private  boolean bonusApplied = false;

    private  boolean haloApplied = false;

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( TYPE, type );
        bundle.put( BONUS_APPLIED, bonusApplied );

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        type = bundle.getInt(TYPE);
        bonusApplied = bundle.getBoolean(BONUS_APPLIED);
    }

    @Override
    public boolean act() {

        if(bonusApplied == false)
        {

            bonusApplied = true;
            haloApplied = true;
            do
            {
                type = Random.Int(1, 5);
                if(type == 5)
                    type = 4;
            }
            while(Dungeon.currentDifficulty.disableChampion(type) == true);

            this.target.champ = type;

            switch(type)
            {
                case 5: type = CHAMP_VAMPERIC;
                case CHAMP_VAMPERIC: //red
                    this.target.name = "Vampiric " + this.target.name;
                    this.target.HT *= 1.5;
                    this.target.HP = this.target.HT;
                    ((Mob)this.target).defenseSkill *= 1.1;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champRedHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPRED);
                    }
                    break;
                case CHAMP_CHIEF: //white
                    this.target.name = "Chief " + this.target.name;
                    this.target.HT *= 2;
                    this.target.HP = this.target.HT;
                    ((Mob)this.target).defenseSkill *= 1.3;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champWhiteHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPWHITE);
                    }
                    break;
                case CHAMP_CURSED: //black
                    this.target.name = "Cursed " + this.target.name;
                    this.target.HT *= 1.5;
                    this.target.HP = this.target.HT;
                    ((Mob)this.target).defenseSkill *= 1.15;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champBlackHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPBLACK);
                    }
                    break;
                case CHAMP_FOUL: //yellow
                    this.target.name = "Foul " + this.target.name;
                    this.target.HT *= 1.5;
                    this.target.HP = this.target.HT;
                    ((Mob)this.target).defenseSkill *= 1.2;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champYellowHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPYELLOW);
                    }
                    break;

            }
        }
        else if (haloApplied == false)
        {
            haloApplied = true;
            switch(type)
            {
                case CHAMP_VAMPERIC: //red
                    this.target.name = "Vampiric " + this.target.name;
                    ((Mob)this.target).defenseSkill *= 1.1;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champRedHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPRED);
                    }
                    break;
                case CHAMP_CHIEF: //white
                    this.target.name = "Chief " + this.target.name;
                    ((Mob)this.target).defenseSkill *= 1.3;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champWhiteHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPWHITE);
                    }
                    break;
                case CHAMP_CURSED: //black
                    this.target.name = "Cursed " + this.target.name;
                    ((Mob)this.target).defenseSkill *= 1.15;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champBlackHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPBLACK);
                    }
                    break;
                case CHAMP_FOUL: //yellow
                    ((Mob)this.target).defenseSkill *= 1.2;
                    if(target.sprite != null)
                    {
                        if(target.sprite.champYellowHalo == null)
                            target.sprite.add(CharSprite.State.CHAMPYELLOW);
                    }
                    break;

            }
        }

        spend( TICK );
        if (target.isAlive()) {



        } else {

            detach();

        }

        return true;
    }
}
