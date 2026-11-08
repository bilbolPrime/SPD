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
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
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
	private CombatTextIcon icon = CombatTextIcon.NONE;
	private Image iconImage;

	public CombatTextIcon icon() {
		return icon;
	}

	@Override
	public float width() {
		return super.width() + (icon == CombatTextIcon.NONE ? 0 : CombatTextIcon.WIDTH + 1);
	}

	@Override
	public float height() {
		return icon == CombatTextIcon.NONE ? super.height() : Math.max(super.height(), CombatTextIcon.HEIGHT);
	}

	@Override
	public void draw() {
		super.draw();
		drawIcon(camera(), x + super.width() + 1, y, 1, 1);
	}

	public void drawIcon(Camera camera, float x, float y, float scaleX, float scaleY) {
		if (icon == CombatTextIcon.NONE) return;
		iconImage.camera = camera;
		iconImage.scale.set(scaleX, scaleY);
		iconImage.x = PixelScene.align(camera, x);
		iconImage.y = PixelScene.align(camera, y);
		iconImage.alpha(am);
		iconImage.draw();
	}

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
		if (iconImage != null) iconImage.destroy();
		super.destroy();
	}

	public void reset( float x, float y, String text, int color ) {
		reset(x, y, text, color, CombatTextIcon.NONE);
	}

	public void reset(float x, float y, String text, int color, CombatTextIcon type) {

		revive();
		owner = null;
		startY = y;
		remoteActor = -1;
		anchorHeight = 1;
		icon = type;
		if (type != CombatTextIcon.NONE) {
			if (iconImage == null) iconImage = new Image(Assets.DAMAGE_ICONS);
			iconImage.frame(type.id * CombatTextIcon.WIDTH, 0, CombatTextIcon.WIDTH, CombatTextIcon.HEIGHT);
		}

		if (cameraZoom != Camera.main.zoom) {
			cameraZoom = Camera.main.zoom;
			PixelScene.chooseFont( 9, cameraZoom );
			font = PixelScene.font;
			scale.set( PixelScene.scale );
		}

		text( text, false );
		hardlight( color );

		measure();
		this.x = PixelScene.align(x - width() / 2);
		this.y = y - height();

		timeLeft = LIFESPAN;
	}

	public void resetAt(float x, float y, int cell, int actor, float height, String text, int color) {
		resetAt(x, y, cell, actor, height, text, color, CombatTextIcon.NONE);
	}

	public void resetAt(float x, float y, int cell, int actor, float height, String text, int color, CombatTextIcon type) {
		reset(x, y, text, color, type);
		remoteActor = actor;
		anchorHeight = Math.max(0.5f, height / DungeonTilemap.SIZE);
		push(this, cell);
	}

	public static void show( float x, float y, CharSprite owner, String text, int color ) {
		show(x, y, owner, text, color, CombatTextIcon.NONE);
	}

	public static void show(float x, float y, CharSprite owner, String text, int color, CombatTextIcon type) {
		FloatingText txt = GameScene.status();
		if (txt == null) return;
		txt.reset(x, y, text, color, type);
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
				float gap = above.icon != CombatTextIcon.NONE || below.icon != CombatTextIcon.NONE ? 1 : 0;
				if (above.y + above.height() + gap > below.y) {
					above.y = below.y - above.height() - gap;

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
