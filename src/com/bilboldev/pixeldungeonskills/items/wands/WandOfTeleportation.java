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
package com.bilboldev.pixeldungeonskills.items.wands;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfTeleportation;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Callback;

public class WandOfTeleportation extends Wand {

	{
		name = "Wand of Teleportation";
	}

	@Override
	protected void onZap( int cell ) {

		Char ch = Actor.findChar( cell );

		if (ch == curUser) {

			setKnown();
			ScrollOfTeleportation.teleportHero( curUser );

		} else if (LocalCoop.contains(ch)) {
			Hero hero = (Hero)ch;
			LocalCoop.withHero(hero, () -> ScrollOfTeleportation.teleportHero(hero));
			Dungeon.observe();
			GameScene.coopHeroChanged();

		} else if (ch != null) {

			int count = 10;
			int pos;
			do {
				pos = Dungeon.level.randomRespawnCell();
				if (count-- <= 0) {
					break;
				}
			} while (pos == -1);

			if (pos == -1) {

				GLog.w("custom.generated.entry_strong_magic_aura_of_this_place_prevents_you_fro_266e1f0419");

			} else {

				ch.pos = pos;
				ch.sprite.place( ch.pos );
				ch.sprite.visible = Dungeon.visible[pos];
				GLog.i("custom.generated.entry_s_teleported_s_to_somewhere_11efd1c2aa", Message.actor(curUser), Message.actor(ch));

			}

		} else {

			GLog.i("custom.generated.entry_nothing_happened_7fbc05693e");

		}
	}

	protected void fx( int cell, Callback callback ) {
		MagicMissile.coldLight( curUser.sprite.parent, curUser.pos, cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}

	@Override
	public String desc() {
		return
			"A blast from this wand will teleport a creature against " +
			"its will to a random place on the current level.";
	}
}
