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
package com.bilboldev.pixeldungeonskills.thetiles;

import com.bilboldev.noosa.Image;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.Tilemap;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.PointF;

public class DungeonTilemapOld extends Tilemap {

	public static final int SIZE = 16;

	private static DungeonTilemapOld instance;
	protected int[] map;

	public DungeonTilemapOld() {
		super(
			Dungeon.level.tilesTex(),
			new TextureFilm( Dungeon.level.tilesTex(), SIZE, SIZE ) );
		map( Dungeon.level.map, Level.WIDTH );

		instance = this;
	}

	@Override
	public void map(int[] data, int cols) {

		map = data;
		super.map(new int[data.length], cols);
	}

	@Override
	public synchronized void updateMap() {
		super.updateMap();
		for (int cell = 0; cell < data.length; cell++) data[cell] = getTileVisual(cell, map[cell]);
	}

	@Override
	public synchronized void updateMapCell(int cell) {
		int x = cell % mapWidth, y = cell / mapWidth;
		for (int row = Math.max(0, y - 1); row <= Math.min(mapHeight - 1, y + 1); row++) {
			for (int col = Math.max(0, x - 1); col <= Math.min(mapWidth - 1, x + 1); col++) {
				int pos = row * mapWidth + col;
				data[pos] = getTileVisual(pos, map[pos]);
				super.updateMapCell(pos);
			}
		}
	}

	public void refreshTexture() {
		texture = TextureCache.get(Dungeon.level.tilesTex());
		tileset = new TextureFilm(texture, SIZE, SIZE);
		updateMap();
	}

	protected int getTileVisual(int cell, int tile) {
		if ((Terrain.flags[tile] & Terrain.LIQUID) != 0) {
			if (cell < mapWidth || cell >= size - mapWidth || cell % mapWidth == 0 || (cell + 1) % mapWidth == 0)
				return Terrain.WATER;
			int visual = Terrain.WATER_TILES;
			for (int i = 0; i < Level.NEIGHBOURS4.length; i++) {
				if ((Terrain.flags[map[cell + Level.NEIGHBOURS4[i]]] & Terrain.UNSTITCHABLE) != 0)
					visual += 1 << i;
			}
			return visual;
		}
		if ((Terrain.flags[tile] & Terrain.PIT) != 0) {
			if (cell < mapWidth || (Terrain.flags[map[cell - mapWidth]] & Terrain.PIT) != 0)
				return Terrain.CHASM;
			int above = map[cell - mapWidth];
			if (above == Terrain.EMPTY_SP || above == Terrain.STATUE_SP) return Terrain.CHASM_FLOOR_SP;
			if ((Terrain.flags[above] & Terrain.LIQUID) != 0) return Terrain.CHASM_WATER;
			if ((Terrain.flags[above] & Terrain.UNSTITCHABLE) != 0) return Terrain.CHASM_WALL;
			return Terrain.CHASM_FLOOR;
		}
		return tile;
	}

	public int screenToTile( int x, int y ) {
		Point p = camera().screenToCamera( x, y ).
			offset( this.point().negate() ).
			invScale( SIZE ).
			floor();
		return p.x >= 0 && p.x < Level.WIDTH && p.y >= 0 && p.y < Level.HEIGHT ? p.x + p.y * Level.WIDTH : -1;
	}

	@Override
	public boolean overlapsPoint( float x, float y ) {
		return true;
	}

	public void discover( int pos, int oldValue ) {

		final Image tile = tile( oldValue );
		tile.point( tileToWorld( pos ) );

		tile.rm = tile.gm = tile.bm = rm;
		tile.ra = tile.ga = tile.ba = ra;
		parent.add( tile );

		parent.add( new AlphaTweener( tile, 0, 0.6f ) {
			protected void onComplete() {
				tile.killAndErase();
				killAndErase();
			};
		} );
	}

	public static PointF tileToWorld(int pos ) {
		return new PointF( pos % Level.WIDTH, pos / Level.WIDTH  ).scale( SIZE );
	}

	public static PointF tileCenterToWorld(int pos ) {
		return new PointF(
			(pos % Level.WIDTH + 0.5f) * SIZE,
			(pos / Level.WIDTH + 0.5f) * SIZE );
	}

	public static Image tile(int index ) {
		Image img = new Image( instance.texture );
		img.frame( instance.tileset.get( index ) );
		return img;
	}

	@Override
	public boolean overlapsScreenPoint( int x, int y ) {
		return true;
	}
}
