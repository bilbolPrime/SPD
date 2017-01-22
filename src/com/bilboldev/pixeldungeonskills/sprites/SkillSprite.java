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


import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;


public class SkillSprite extends MovieClip {

	public static final int SIZE	= 16;



	protected static TextureFilm film;



	public SkillSprite() {
		this( ItemSpriteSheet.SMTH );
	}



	public SkillSprite(int image) {
		super( Assets.HERO_SKILL );

        if (film == null) {
            film = new TextureFilm( texture, SIZE, SIZE );
        }

		view( image );
	}
	
	public void originToCenter() {
		origin.set(SIZE / 2 );
	}
	

	
	public SkillSprite view( int image ) {
		frame( film.get( image ) );
		return this;
	}
	
	@Override
	public void update() {
		super.update();



	}
	


}
