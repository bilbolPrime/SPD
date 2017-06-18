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
package com.bilboldev.pixeldungeonskills.items.weapon.missiles;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Paralysis;
import com.bilboldev.pixeldungeonskills.actors.buffs.SnipersMark;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.BlastParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.PurpleParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.SmokeParticle;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class Arrow extends MissileWeapon {


	{
		name = "arrow";
		image = ItemSpriteSheet.Arrow;

        stackable = true;
	}

	public Arrow() {
		this( 1 );
	}

	public Arrow(int number) {
		super();
		quantity = number;
	}

    @Override
    public void cast( final Hero user, int dst ) {

        if(Dungeon.hero.heroSkills.passiveB3.passThroughTargets(false) > 0)
        {
            castSPD(user, dst, Dungeon.hero.heroSkills.passiveB3.passThroughTargets(true));
        }
        else
            super.cast(user, dst);
    }

    @Override
    protected void onThrow( int cell ) {


        if(Dungeon.hero.heroSkills.passiveB3.multiTargetActive == false || Dungeon.hero.heroSkills.active3.active == true) { //  bombvoyage
            // Turn to bomb
            if(Dungeon.hero.heroSkills.active3.arrowToBomb() == true) {
            if (Level.pit[cell]) {
                super.onThrow(cell);
            } else {
                Sample.INSTANCE.play(Assets.SND_BLAST, 2);

                if (Dungeon.visible[cell]) {
                    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
                }

                boolean terrainAffected = false;
                for (int n : Level.NEIGHBOURS9) {
                    int c = cell + n;
                    if (c >= 0 && c < Level.LENGTH) {
                        if (Dungeon.visible[c]) {
                            CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                        }

                        if (Level.flamable[c]) {
                            Level.set(c, Terrain.EMBERS);
                            GameScene.updateMap(c);
                            terrainAffected = true;
                        }

                        Char ch = Actor.findChar(c);
                        if (ch != null) {
                            int dmg = Random.Int(1 + Dungeon.depth, 10 + Dungeon.depth * 2) - Random.Int(ch.dr());
                            if (dmg > 0) {
                                ch.damage(dmg, this);
                                if (ch.isAlive()) {
                                    Buff.prolong(ch, Paralysis.class, 2);
                                }
                            }
                        }
                    }
                }

                if (terrainAffected) {
                    Dungeon.observe();
                }
            }
        }
            // End turn to bomb
         else   super.onThrow(cell);
            return;
        }

        Ballistica.distance = Math.min( Ballistica.distance, Level.distance(Dungeon.hero.pos, cell) );

        ArrayList<Char> chars = new ArrayList<Char>();

        for (int i=1; i < Ballistica.distance + 1; i++) {

            int c = Ballistica.trace[i];

            Char ch;
            if ((ch = Actor.findChar(c)) != null) {
                chars.add( ch );
            }
        }

        GLog.i(chars.size() + " targets");
        boolean hitOne = false;
        for (Char ch : chars) {
            if (!curUser.shootThrough(ch, this)) {

            }
            else
                hitOne = true;
        }

        if(hitOne == false)
            miss( cell );

        Dungeon.hero.rangedWeapon = null;
    }

    public void arrowEffect(Char attacker, Char defender)
    {

    }

	@Override
	public String desc() {
		return 
			"Arrows are more powerful and accurate than darts, but they require an equipped bow.";
	}
	
	@Override
	public Item random() {
        quantity = Random.Int(5, 20);
		return this;
	}
	
	@Override
	public int price() {
		return quantity * 5;
	}

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if(Dungeon.hero.belongings.bow != null) {
            if(actions.contains(AC_THROW) == false)
            actions.add(AC_THROW);
        }
        else
            actions.remove( AC_THROW );
        actions.remove(AC_EQUIP);

        return actions;
    }

    @Override
    public int min(){return 3;}


    @Override
    public int max(){return 5;}

    @Override
    public String info() {

        StringBuilder info = new StringBuilder( desc() );


        return info.toString();
    }

}
