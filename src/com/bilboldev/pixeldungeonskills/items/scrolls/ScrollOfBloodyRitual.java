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
package com.bilboldev.pixeldungeonskills.items.scrolls;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Random;

public class ScrollOfBloodyRitual extends Scroll {

	{
		name = "Scroll of Bloody Ritual";
	}
	
	@Override
	protected void doRead() {
		
		GameScene.flash( 0xFF0000 );
		
		Sample.INSTANCE.play( Assets.SND_BLAST );
		Invisibility.dispel();

        int damage = (int)Math.round(Dungeon.hero.HT * 0.2);

		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Level.fieldOfView[mob.pos]) {
				mob.damage( Random.IntRange( damage , damage ), this );
			}
		}

        Dungeon.hero.HT -= damage;
        Dungeon.hero.HP = Dungeon.hero.HT;
        StatusPane.takingDamage = 0;

        GLog.n("Scroll of bloody ritual took away " + damage + " of your max hp!");
        GLog.p("A dark aura heals all your wounds... but you are not comfortable with the aura around you...");
		Dungeon.observe();
		
		setKnown();
		
		curUser.spendAndNext( TIME_TO_READ );
	}
	
	@Override
	public String desc() {
		return
			"The scroll holds the secret of a forbidden ritual. In exchange for the reader's well being, the scroll damages everyone nearby and fully heals him.";
	}
	
	@Override
	public int price() {
		return isKnown() ? 80 * quantity : super.price();
	}
}
