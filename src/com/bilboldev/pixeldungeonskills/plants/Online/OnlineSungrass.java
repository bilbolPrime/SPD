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
package com.bilboldev.pixeldungeonskills.plants.Online;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.LeafParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.ShaftParticle;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.utils.Bundle;

public class OnlineSungrass extends Plant {

	private static final String TXT_DESC = "Sungrass is renowned for its sap's healing properties.";

	{
		image = 4;
		plantName = "Sungrass";
	}

	@Override
	public void activate( Char ch ) {

	}

    public void serverActivate()
    {
        sprite.kill();
        CellEmitter.get( pos ).burst( LeafParticle.GENERAL, 6 );
    }

	@Override
	public String desc() {
		return TXT_DESC;
	}

	public static class Seed extends Plant.Seed {
		{
			plantName = "Sungrass";

			name = "seed of " + plantName;
			image = ItemSpriteSheet.SEED_SUNGRASS;

			plantClass = OnlineSungrass.class;
			alchemyClass = PotionOfHealing.class;
		}

		@Override
		public String desc() {
			return TXT_DESC;
		}
	}
}
