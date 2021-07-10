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

import android.graphics.Bitmap;

import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.glwrap.Matrix;
import com.bilboldev.glwrap.Vertexbuffer;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.NoosaScript;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.PointF;
import com.bilboldev.utils.Random;

public class ItemSprite extends MovieClip {

	public static final int SIZE	= 16;
	
	private static final float DROP_INTERVAL = 0.4f;
	
	protected static TextureFilm film;
	
	public Heap heap;
	
	private Glowing glowing;
	private float phase;
	private boolean glowUp;
	
	private float dropInterval;

	//the amount the sprite is raised from flat when viewed in a raised perspective
	protected float perspectiveRaise    = 5 / 16f; //5 pixels

	//the width and height of the shadow are a percentage of sprite size
	//offset is the number of pixels the shadow is moved down or up (handy for some animations)
	protected boolean renderShadow  = false;
	protected float shadowWidth     = 1f;
	protected float shadowHeight    = 0.25f;
	protected float shadowOffset    = 0.5f;

	public ItemSprite() {
		this( ItemSpriteSheet.SMTH, null );
	}
	
	public ItemSprite( Item item ) {
		this( item.image(), item.glowing() );
	}
	
	public ItemSprite( int image, Glowing glowing ) {
		super( Assets.ITEMS );

		if (film == null) {
			film = new TextureFilm( texture, SIZE, SIZE );
		}
		
		view( image, glowing );
	}
	
	public void originToCenter() {
		origin.set(SIZE / 2 );
	}
	
	public void link() {
		link( heap );
	}
	
	public void link( Heap heap ) {
		this.heap = heap;
		view( heap.image(), heap.glowing() );
		renderShadow =  Difficulties.is3d;
		place( heap.pos );
	}
	
	@Override
	public void revive() {
		super.revive();
		
		speed.set( 0 );
		acc.set( 0 );
		dropInterval = 0;
		
		heap = null;
	}
	
	public PointF worldToCamera( int cell ) {
		final int csize = DungeonTilemap.SIZE;

		if(!Difficulties.is3d)
			return new PointF(
				cell % Level.WIDTH * csize + (csize - SIZE) * 0.5f,
				cell / Level.WIDTH * csize + (csize - SIZE) * 0.5f
			);

		return new PointF(
				cell % Dungeon.level.width() * csize + (csize - width()) * 0.5f,
				cell / Dungeon.level.width() * csize + (csize - height()) - csize * perspectiveRaise
		);
	}
	
	public void place( int p ) {
		point( worldToCamera( p ) );
	}
	
	public void drop() {

		if (heap.isEmpty()) {
			return;
		}
			
		dropInterval = DROP_INTERVAL;
		
		speed.set( 0, -100 );
		acc.set( 0, -speed.y / DROP_INTERVAL * 2 );
		
		if (visible && heap != null && heap.peek() instanceof Gold) {
			CellEmitter.center( heap.pos ).burst( Speck.factory( Speck.COIN ), 5 );
			Sample.INSTANCE.play( Assets.SND_GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
		}
	}
	
	public void drop( int from ) {

		if (heap.pos == from) {
			drop();
		} else {
			
			float px = x;
			float py = y;		
			drop();
			
			place( from );
	
			speed.offset( (px-x) / DROP_INTERVAL, (py-y) / DROP_INTERVAL );
		}
	}
	
	public ItemSprite view( int image, Glowing glowing ) {
		frame( film.get( image ) );
		if ((this.glowing = glowing) == null) {
			resetColor();
		}
		return this;
	}

	private float[] shadowMatrix = new float[16];

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		Matrix.copy(matrix, shadowMatrix);
		Matrix.translate(shadowMatrix,
				(width() * (1f - shadowWidth)) / 2f,
				(height() * (1f - shadowHeight)) + shadowOffset);
		Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
	}


	@Override
	public void draw() {
		if (texture == null || (!dirty && buffer == null))
			return;

		if (renderShadow) {
			if (dirty) {
				verticesBuffer.position(0);
				verticesBuffer.put(vertices);
				if (buffer == null)
					buffer = new Vertexbuffer(verticesBuffer);
				else
					buffer.updateVertices(verticesBuffer);
				dirty = false;
			}

			NoosaScript script = script();

			texture.bind();

			script.camera(camera());

			updateMatrix();

			script.uModel.valueM4(shadowMatrix);
			script.lighting(
					0, 0, 0, am * .6f,
					0, 0, 0, aa * .6f);

			script.drawQuad(buffer);
		}

		super.draw();

	}

	@Override
	public void update() {
		super.update();

		visible = (heap == null || Dungeon.visible[heap.pos]);
		
		if (dropInterval > 0 && (dropInterval -= Game.elapsed) <= 0) {
			
			speed.set( 0 );
			acc.set( 0 );
			place( heap.pos );
			
			if (visible) {
				boolean water = Level.water[heap.pos];
				
				if (water) {
					GameScene.ripple( heap.pos );
				} else {
					int cell = Dungeon.level.map[heap.pos];
					water = (cell == Terrain.WELL || cell == Terrain.ALCHEMY);
				}
				
				if (!(heap.peek() instanceof Gold)) {
					Sample.INSTANCE.play( water ? Assets.SND_WATER : Assets.SND_STEP, 0.8f, 0.8f, 1.2f );
				}
			}
		}
		
		if (visible && glowing != null) {
			if (glowUp && (phase += Game.elapsed) > glowing.period) {
				
				glowUp = false;
				phase = glowing.period;
				
			} else if (!glowUp && (phase -= Game.elapsed) < 0) {
				
				glowUp = true;
				phase = 0;
				
			}
			
			float value = phase / glowing.period * 0.6f;
			
			rm = gm = bm = 1 - value;
			ra = glowing.red * value;
			ga = glowing.green * value;
			ba = glowing.blue * value;
		}
	}
	
	public static int pick( int index, int x, int y ) {
		Bitmap bmp = TextureCache.get( Assets.ITEMS ).bitmap;
		int rows = bmp.getWidth() / SIZE;
		int row = index / rows;
		int col = index % rows;
		return bmp.getPixel( col * SIZE + x, row * SIZE + y );
	}
	
	public static class Glowing {
		
		public static final Glowing WHITE = new Glowing( 0xFFFFFF, 0.6f );
		
		public int color;
		public float red;
		public float green;
		public float blue;
		public float period;
		
		public Glowing( int color ) {
			this( color, 1f );
		}
		
		public Glowing( int color, float period ) {
			
			this.color = color;
			
			red = (color >> 16) / 255f;
			green = ((color >> 8) & 0xFF) / 255f;
			blue = (color & 0xFF) / 255f;
			
			this.period = period;
		}
	}
}
