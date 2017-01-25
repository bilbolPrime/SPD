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
package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MirrorImage;

public class MercSprite extends MobSprite {

	private static final int FRAME_WIDTH	= 12;
	private static final int FRAME_HEIGHT	= 15;

    private HiredMerc.MERC_TYPES type = HiredMerc.MERC_TYPES.Brute;

	public MercSprite() {
		super();
	}
	
	public void updateArmor( HiredMerc.MERC_TYPES type) {

        this.type = type;



            switch (type) {
                case Brute:
                     texture(Assets.WARRIOR);
                    break;
                case Wizard:
                    texture(Assets.MAGE);
                    break;
                case Thief:
                    texture(Assets.ROGUE);
                    break;
            }


		TextureFilm film = new TextureFilm( HeroSprite.tiers(), 6, FRAME_WIDTH, FRAME_HEIGHT );
		
		idle = new Animation( 1, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );
		
		run = new Animation( 20, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );

		
		attack = new Animation( 15, false );
		attack.frames( film, 13, 14, 15, 0 );

        zap = attack.clone();

        die = new Animation( 20, false );
        die.frames( film, 8, 9, 10, 11, 12, 11 );


        idle();
	}

    public void updateArmor( ) {




        switch (type) {
            case Brute:
                texture(Assets.WARRIOR);
                break;
            case Wizard:
                texture(Assets.MAGE);
                break;
            case Thief:
                texture(Assets.ROGUE);
                break;
        }


        TextureFilm film = new TextureFilm( HeroSprite.tiers(), ((HiredMerc)ch).getArmorTier(), FRAME_WIDTH, FRAME_HEIGHT );

        idle = new Animation( 1, true );
        idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new Animation( 20, true );
        run.frames( film, 2, 3, 4, 5, 6, 7 );

        die = new Animation( 20, false );
        die.frames( film, 8, 9, 10, 11, 12, 11 );

        attack = new Animation( 15, false );
        attack.frames( film, 13, 14, 15, 0 );

        idle();
    }
}
