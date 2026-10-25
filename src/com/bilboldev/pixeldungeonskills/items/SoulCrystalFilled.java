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
package com.bilboldev.pixeldungeonskills.items;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.EliteJoeSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.JoeSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public class SoulCrystalFilled extends Item {

	{
		name = "soul crystal";
		image = ItemSpriteSheet.CRYSTAL_FULL;
        stackable = false;
        quantity = 1;
	}

    public Class<? extends CharSprite> minionSprite;
    public int HT, defenceSkill;
    public Message captured;

	public SoulCrystalFilled() {
		this( RatSprite.class, 5, 1, Message.key(com.bilboldev.pixeldungeonskills.actors.mobs.Rat.class, "name") );
	}

	public SoulCrystalFilled(Class<? extends CharSprite> minionSprite, int HT, int defenceSkill, Message captured) {
		super();
		this.minionSprite = minionSprite;
        this.HT = HT;
        this.defenceSkill = defenceSkill;
        this.captured = captured;
	}

    @Override
    protected void onThrow( int cell ) {
        if (Level.pit[cell]) {
            super.onThrow( cell );
        } else {
            shatter( cell );
        }
    }

    private void shatter( int pos ) {
        Sample.INSTANCE.play( Assets.SND_SHATTER );

        int newPos = pos;
        if (Actor.findChar( pos ) != null) {
            ArrayList<Integer> candidates = new ArrayList<Integer>();
            boolean[] passable = Level.passable;

            for (int n : Level.NEIGHBOURS4) {
                int c = pos + n;
                if (passable[c] && Actor.findChar( c ) == null) {
                    candidates.add( c );
                }
            }

            newPos = candidates.size() > 0 ? Random.element(candidates) : -1;
        }

        if (newPos != -1) {

            SummonedPet minion = new SummonedPet(minionSprite);
            minion.nameText(captured);
            minion.HT = HT;
            minion.HP = minion.HT;
            minion.defenseSkill = defenceSkill;
            minion.pos = newPos;

            GameScene.add(minion);
            Actor.addDelayed( new Pushing( minion, pos, newPos ), -1 );

            minion.sprite.alpha( 0 );
            minion.sprite.parent.add( new AlphaTweener( minion.sprite, 1, 0.15f ) );

            if(minion.sprite instanceof JoeSprite){
                minion.sprite.showStatus(CharSprite.NEUTRAL, Message.key("custom.generated.entry_i_am_joe_394f863746"));
            }
            if(minion.sprite instanceof EliteJoeSprite){
                minion.sprite.add(CharSprite.State.CHAMPWHITE);
            }
        }
    }

    private static final String SPRITE	= "sprite";
    private static final String HEALTH = "HT";
    private static final String DEFENCE_SKILL = "defenceskill";
    private static final String CAPTURED_NAME = "capturedname";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );

        bundle.put( HEALTH, HT );
        bundle.put( DEFENCE_SKILL, defenceSkill );
        if (captured.localText() != null) bundle.put(CAPTURED_NAME, captured.localText());
        else bundle.put("captured_text", captured.store());
        bundle.put( SPRITE, minionSprite.toString().replace("class ","" ));
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        HT = bundle.getInt(HEALTH);
        defenceSkill = bundle.getInt(DEFENCE_SKILL);
        captured = bundle.contains("captured_text") ? Message.restore(bundle.getBundle("captured_text"))
                : Message.local(bundle.getString(CAPTURED_NAME));
        try {
            minionSprite = (Class<? extends CharSprite>)Class.forName(bundle.getString(SPRITE));
        }
        catch (Exception ex)
        {
            minionSprite = RatSprite.class;
        }
    }

	@Override
	public String desc() {
        return Message.key("items.soulcrystalfilled.desc", captured).toString();
	}

    @Override
    public String info() {

        StringBuilder info = new StringBuilder( desc() );

        return info.toString();
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

	@Override
	public int price() {
		return quantity * 12;
	}
}
