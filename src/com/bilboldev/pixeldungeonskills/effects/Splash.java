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
package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.pixeldungeonskills.network.PartyEffects;

import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.particles.PixelParticle;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.PointF;
import com.bilboldev.utils.Random;

public class Splash {

	public static void at( int cell, final int color, int n ) {
		at( DungeonTilemap.tileCenterToWorld( cell ), color, n );
	}

	public static void at( PointF p, final int color, int n ) {
		at(p, -3.1415926f / 2, 3.1415926f, color, n);
	}

	public static void at( PointF p, final float dir, final float cone, final int color, int n ) {

		if (n <= 0) {
			return;
		}

		Emitter emitter = GameScene.emitter();
		emitter.pos( p );
		PartyEffects.splash(p, dir, cone, color, n);
		emitter.burst(new SplashFactory(color, dir, cone), n);
	}

	public static void at(Group group, PointF p, float dir, float cone, int color, int n) {
		Emitter emitter = new Emitter();
		emitter.pos(p);
		emitter.burst(new SplashFactory(color, dir, cone), n);
		group.add(emitter);
	}

	private static class SplashFactory extends Emitter.Factory {

		private final int color;
		private final float dir, cone;

		SplashFactory(int color, float dir, float cone) {
			this.color = color;
			this.dir = dir;
			this.cone = cone;
		}

		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			PixelParticle p = (PixelParticle)emitter.recycle( PixelParticle.Shrinking.class );

			p.reset( x, y, color, 4, Random.Float( 0.5f, 1.0f ) );
			p.speed.polar( Random.Float( dir - cone / 2, dir + cone / 2 ), Random.Float( 40, 80 ) );
			p.acc.set( 0, +100 );
		}
	}
}
