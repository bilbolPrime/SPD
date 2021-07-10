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

import com.bilboldev.glwrap.Matrix;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.MobProjectile;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Callback;

public class RatScoutSprite extends RatSprite {
	public RatScoutSprite() {
		super();

	}

    @Override
    protected void updateMatrix() {
        super.updateMatrix();
        Matrix.scale(matrix, 0.7f, 0.7f);
        Matrix.translate(matrix, width * 0.15f, height  * 0.3f);
        Matrix.copy(matrix, shadowMatrix);
        Matrix.translate(shadowMatrix,
                (width() * (1f - shadowWidth)) / 2f,
                (height() * (1f - shadowHeight)) + shadowOffset);
        Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
    }
}
