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
package com.bilboldev.pixeldungeonskills.levels.features;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.ElmoParticle;
import com.bilboldev.pixeldungeonskills.levels.DeadEndLevel;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;

public class Sign {

	private static final String KEY_DEAD_END = "levels.sign.dead_end";

	private static final String[] TIP_KEYS = {
		"levels.sign.tip_01",
		"levels.sign.tip_02",
		"levels.sign.tip_03",
		"levels.sign.tip_04",
		"levels.sign.tip_05",
		"levels.sign.tip_06",
		"levels.sign.tip_07",
		"levels.sign.tip_08",
		"levels.sign.tip_09",
		"levels.sign.tip_10",
		"levels.sign.tip_11",
		"levels.sign.tip_12",
		"levels.sign.tip_13",
		"levels.sign.tip_14",
		"levels.sign.tip_15",
		"levels.sign.tip_16",
		"levels.sign.tip_17",
		"levels.sign.tip_18",
		"levels.sign.tip_19",
		"levels.sign.tip_20",
		"levels.sign.tip_21"
	};

	private static final String KEY_BURN = "levels.sign.burn";

	public static void read( int pos ) {

		if (Dungeon.level instanceof DeadEndLevel) {

			GameScene.show( new WndMessage(Message.key(KEY_DEAD_END)) );

		} else {

			int index = Dungeon.depth - 1;

			if (index < TIP_KEYS.length) {
				GameScene.show( new WndMessage(Message.key(TIP_KEYS[index])) );
			} else {

				Dungeon.level.destroy( pos );
				GameScene.updateMap( pos );
				GameScene.discoverTile( pos, Terrain.SIGN );

				CellEmitter.get( pos ).burst( ElmoParticle.FACTORY, 6 );
				Sample.INSTANCE.play( Assets.SND_BURNING );

				GLog.w(KEY_BURN);

			}
		}
	}
}
