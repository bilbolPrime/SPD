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


public class SkillSpriteNew extends MovieClip {

	public static final int SIZE	= 16;



	protected static TextureFilm film;



	public SkillSpriteNew() {
		this( ItemSpriteSheet.SMTH );
	}



	public SkillSpriteNew(int image) {
		super( Assets.HERO_SKILL );

        if (film == null) {
            film = new TextureFilm( texture, SIZE, SIZE );
        }

		view( image );
	}
	
	public void originToCenter() {
		origin.set(SIZE / 2 );
	}
	

	
	public SkillSpriteNew view(int image ) {
		frame( film.get( image ) );
		return this;
	}
	
	@Override
	public void update() {
		super.update();

		scale.x = 0.75f;
		scale.y = 0.75f;

	}
	


}
