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

import android.graphics.Bitmap;

import com.bilboldev.gltextures.BufferTexture;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.glwrap.Texture;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NoosaScript;
import com.bilboldev.noosa.NoosaScriptNoLighting;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Rect;

import java.util.ArrayList;
import java.util.Arrays;

public class FogOfWarNew extends Image {

	private static final int FOG_COLORS[][] = new int[][]{{

			0x55000000, 0x00000000,
			0x00000000,
			0x00000000, 0x00000000
	}, {

			0xDD000000, 0xBB000000,
			0x99000000,
			0x77000000, 0x55000000
	}, {

			0xDD221108, 0xBB442211,
			0x99663319,
			0x77884411, 0x55AA552A
	}, {

			0xFF000000, 0xFF000000,
			0xFF000000,
			0xFF000000, 0xFF000000
	}};

	private static final int VISIBLE    =   0;
	private static final int VISITED    =   1;
	private static final int MAPPED     =   2;
	private static final int INVISIBLE  =   3;

	public static int color(boolean visible, boolean visited, boolean mapped, int brightness) {
		int state = visible ? VISIBLE : visited ? VISITED : mapped ? MAPPED : INVISIBLE;
		return FOG_COLORS[state][Math.max(0, Math.min(4, brightness + 2))];
	}

	private int mapWidth;
	private int mapHeight;
	private int mapLength;

	private int pWidth;
	private int pHeight;

	private int width2;
	private int height2;

	private volatile ArrayList<Rect> toUpdate;
	private volatile ArrayList<Rect> updating;

	private static final int PIX_PER_TILE = 2;

	public FogOfWarNew( int mapWidth, int mapHeight ) {

		super();

		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		mapLength = mapHeight * mapWidth;

		pWidth = mapWidth * PIX_PER_TILE;
		pHeight = mapHeight * PIX_PER_TILE;

		width2 = 1;
		while (width2 < pWidth) {
			width2 <<= 1;
		}

		height2 = 1;
		while (height2 < pHeight) {
			height2 <<= 1;
		}

		float size = DungeonTilemap.SIZE / PIX_PER_TILE;
		width = width2 * size;
		height = height2 * size;

		BufferTexture tx = new BufferTexture(width2, height2);
		TextureCache.add(FogOfWarNew.class, tx);
		texture( tx );

		scale.set(
				DungeonTilemap.SIZE / PIX_PER_TILE,
				DungeonTilemap.SIZE / PIX_PER_TILE);

		toUpdate = new ArrayList<>();
		toUpdate.add(new Rect(0, 0, mapWidth, mapHeight));
	}

	public synchronized void updateFog(){
		toUpdate.clear();
		toUpdate.add(new Rect(0, 0, mapWidth, mapHeight));
	}

	public synchronized void updateFog(Rect update){
		for (Rect r : toUpdate.toArray(new Rect[0])){
			if (!r.intersect(update).isEmpty()){
				toUpdate.remove(r);
				toUpdate.add(r.union(update));
				return;
			}
		}
		toUpdate.add(update);
	}

	public synchronized void updateFog( int cell, int radius ){
		Rect update = new Rect(
				(cell % mapWidth) - radius,
				(cell / mapWidth) - radius,
				(cell % mapWidth) - radius + 1 + 2*radius,
				(cell / mapWidth) - radius + 1 + 2*radius);
		update.left = Math.max(0, update.left);
		update.top = Math.max(0, update.top);
		update.right = Math.min(mapWidth, update.right);
		update.bottom = Math.min(mapHeight, update.bottom);
		if (update.isEmpty()) return;
		updateFog( update );
	}

	public synchronized void updateFogArea(int x, int y, int w, int h){
		updateFog(new Rect(x, y, x + w, y + h));
	}

	private synchronized void moveToUpdating(){
		updating = toUpdate;
		toUpdate = new ArrayList<>();
	}

	private boolean[] visible;
	private boolean[] visited;
	private boolean[] mapped;
	private int brightness;

	private void updateTexture( boolean[] visible, boolean[] visited, boolean[] mapped ) {
		this.visible = visible;
		this.visited = visited;
		this.mapped = mapped;
		this.brightness = PixelDungeon.brightnessNew() + 2;

		moveToUpdating();

		boolean fullUpdate = false;
		if (updating.size() == 1){
			Rect update = updating.get(0);
			if (update.height() == mapHeight && update.width() == mapWidth){
				fullUpdate = true;
			}
		}

		BufferTexture fog = (BufferTexture) texture;

		int cell;

		for (Rect update : updating) {
			for (int i = update.top; i <= update.bottom; i++) {
				cell = mapWidth * i + update.left;
				for (int j = update.left; j <= update.right; j++) {

					if (cell >= Dungeon.level.length()) continue;

					if (!Dungeon.level.discoverable[cell]
							|| (!visible[cell] && !visited[cell] && !mapped[cell])) {

						if (fullUpdate)
							fillCell(fog, j, i, FOG_COLORS[INVISIBLE][brightness]);
						cell++;
						continue;
					}

					if (wall(cell)) {

						if (cell + mapWidth >= mapLength) {
							fillCell(fog, j, i, FOG_COLORS[INVISIBLE][brightness]);

						} else if (wall(cell + mapWidth)) {

							if (cell % mapWidth != 0) {

								if (wall(cell - 1)) {

									if (wall(cell + mapWidth - 1)) {
										fillLeft(fog, j, i, FOG_COLORS[INVISIBLE][brightness]);
									} else {
										fillLeft(fog, j, i, FOG_COLORS[Math.max(getCellFog(cell), Math.max(getCellFog(cell + mapWidth - 1), getCellFog(cell - 1)))][brightness]);
									}

								} else {
									fillLeft(fog, j, i, FOG_COLORS[Math.max(getCellFog(cell), getCellFog(cell - 1))][brightness]);
								}

							} else {
								fillLeft(fog, j, i, FOG_COLORS[INVISIBLE][brightness]);
							}

							if ((cell + 1) % mapWidth != 0) {

								if (wall(cell + 1)) {

									if (wall(cell + mapWidth + 1)) {
										fillRight(fog, j, i, FOG_COLORS[INVISIBLE][brightness]);
									} else {
										fillRight(fog, j, i, FOG_COLORS[Math.max(getCellFog(cell), Math.max(getCellFog(cell + mapWidth + 1), getCellFog(cell + 1)))][brightness]);
									}

								} else {
									fillRight(fog, j, i, FOG_COLORS[Math.max(getCellFog(cell), getCellFog(cell + 1))][brightness]);
								}

							} else {
								fillRight(fog, j, i, FOG_COLORS[INVISIBLE][brightness]);
							}

						} else {
							fillCell(fog, j, i, FOG_COLORS[Math.max(getCellFog(cell), getCellFog(cell + mapWidth))][brightness]);
						}

					} else {
						fillCell(fog, j, i, FOG_COLORS[getCellFog(cell)][brightness]);
					}

					cell++;
				}
			}

		}

		if (updating.size() == 1 && !fullUpdate){
			fog.update(updating.get(0).top * PIX_PER_TILE, updating.get(0).bottom * PIX_PER_TILE);
		} else {
			fog.update();

		}

	}

	private boolean wall(int cell) {
		return DungeonTileSheet.wallStitcheable(Dungeon.level.map[cell]);
	}

	private int getCellFog( int cell ){

		if (visible[cell]) {
			return VISIBLE;
		} else if (visited[cell]) {
			return VISITED;
		} else if (mapped[cell] ) {
			return MAPPED;
		} else {
			return INVISIBLE;
		}
	}

	private void fillLeft( BufferTexture fog, int x, int y, int color){
		for (int i = 0; i < PIX_PER_TILE; i++){
			fog.pixels.position(((y * PIX_PER_TILE)+i)*width2 + x * PIX_PER_TILE);
			for (int j = 0; j < PIX_PER_TILE/2; j++) {
				fog.pixels.put(color);
			}
		}
	}

	private void fillRight( BufferTexture fog, int x, int y, int color){
		for (int i = 0; i < PIX_PER_TILE; i++){
			fog.pixels.position(((y * PIX_PER_TILE)+i)*width2 + x * PIX_PER_TILE + PIX_PER_TILE/2);
			for (int j = PIX_PER_TILE/2; j < PIX_PER_TILE; j++) {
				fog.pixels.put(color);
			}
		}
	}

	private void fillCell( BufferTexture fog, int x, int y, int color){
		for (int i = 0; i < PIX_PER_TILE; i++){
			fog.pixels.position(((y * PIX_PER_TILE)+i)*width2 + x * PIX_PER_TILE);
			for (int j = 0; j < PIX_PER_TILE; j++) {
				fog.pixels.put(color);
			}
		}
	}

	@Override
	public void draw() {

		if (!toUpdate.isEmpty()){
			updateTexture(Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped);
		}

		super.draw();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (texture != null){
			TextureCache.remove(FogOfWarNew.class);
		}
	}

}
