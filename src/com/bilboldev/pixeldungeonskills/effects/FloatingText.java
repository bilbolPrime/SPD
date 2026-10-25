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

import java.util.ArrayList;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.SparseArray;

public class FloatingText extends BitmapText {

	private static final float LIFESPAN	= 1f;
	private static final float DISTANCE	= DungeonTilemap.SIZE;

	private float timeLeft;

	private int key = -1;
	private CharSprite owner;
	private int remoteActor = -1;
	private float startY, anchorHeight;

	public CharSprite owner() {
		if (remoteActor >= 0) {
			com.bilboldev.pixeldungeonskills.actors.Actor actor =
					com.bilboldev.pixeldungeonskills.actors.Actor.findById(remoteActor);
			return actor instanceof com.bilboldev.pixeldungeonskills.actors.Char
					? ((com.bilboldev.pixeldungeonskills.actors.Char)actor).sprite : null;
		}
		return owner;
	}

	public int cell() {
		return key;
	}

	public float anchorHeight() {
		return anchorHeight;
	}

	public float rise() {
		return startY - y - height();
	}

	private float cameraZoom = -1;

	private static SparseArray<ArrayList<FloatingText>> stacks = new SparseArray<ArrayList<FloatingText>>();

	public FloatingText() {
		super();
		speed.y = - DISTANCE / LIFESPAN;
	}

	@Override
	public void update() {
		super.update();

		if (timeLeft > 0) {
			if ((timeLeft -= Game.elapsed) <= 0) {
				kill();
			} else {
				float p = timeLeft / LIFESPAN;
				alpha( p > 0.5f ? 1 : p * 2 );
			}
		}
	}

	@Override
	public void kill() {
		remoteActor = -1;
		owner = null;
		if (key != -1) {
			stacks.get( key ).remove( this );
			key = -1;
		}
		super.kill();
	}

	@Override
	public void destroy() {
		kill();
		super.destroy();
	}

	public void reset( float x, float y, String text, int color ) {

		revive();
		owner = null;
		startY = y;
		remoteActor = -1;
		anchorHeight = 1;

		if (cameraZoom != Camera.main.zoom) {
			cameraZoom = Camera.main.zoom;
			PixelScene.chooseFont( 9, cameraZoom );
			font = PixelScene.font;
			scale.set( PixelScene.scale );
		}

		text( text, false );
		hardlight( color );

		measure();
		this.x = PixelScene.align( x - width() / 2 );
		this.y = y - height();

		timeLeft = LIFESPAN;
	}

	public void resetAt(float x, float y, int cell, int actor, float height, String text, int color) {
		reset(x, y, text, color);
		remoteActor = actor;
		anchorHeight = Math.max(0.5f, height / DungeonTilemap.SIZE);
		push(this, cell);
	}

	public static void show( float x, float y, CharSprite owner, String text, int color ) {
		FloatingText txt = GameScene.status();
		txt.reset( x, y, text, color );
		txt.owner = owner;
		txt.anchorHeight = Math.max(0.5f, owner.height / DungeonTilemap.SIZE);
		push( txt, owner.ch.pos );
	}

	public static void show( float x, float y, String text, int color ) {
		GameScene.status().reset( x,  y,  text, color );
	}

	public static void show( float x, float y, int key, String text, int color ) {
		FloatingText txt = GameScene.status();
		txt.reset( x,  y,  text, color );
		push( txt, key );
	}

	private static void push( FloatingText txt, int key ) {

		txt.key = key;

		ArrayList<FloatingText> stack = stacks.get( key );
		if (stack == null) {
			stack = new ArrayList<FloatingText>();
			stacks.put( key, stack );
		}

		if (stack.size() > 0) {
			FloatingText below = txt;
			int aboveIndex = stack.size() - 1;
			while (aboveIndex >= 0) {
				FloatingText above = stack.get( aboveIndex );
				if (above.y + above.height() > below.y) {
					above.y = below.y - above.height();

					below = above;
					aboveIndex--;
				} else {
					break;
				}
			}
		}

		stack.add( txt );
	}
}
