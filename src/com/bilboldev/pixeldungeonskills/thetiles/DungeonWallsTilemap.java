/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2017 Evan Debenham
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

import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.Visual;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.scenes.PartyPresentation;
import com.bilboldev.pixeldungeonskills.sprites.DiscardedItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public class DungeonWallsTilemap extends DungeonTilemap {

	private final BitSet grassBehindSprites = new BitSet();
	private final BitSet grassChanges = new BitSet();
	private final Image grass = new Image();
	private final ArrayList<Visual> sprites = new ArrayList<>();

	public DungeonWallsTilemap(){
		super(Dungeon.level.tilesTex());
		map( Dungeon.level.map, Dungeon.level.WIDTH );
	}

	public boolean drawSprites(Group heaps, Group mobs) {
		sprites.clear();
		grassChanges.clear();
		grassChanges.or(grassBehindSprites);
		grassBehindSprites.clear();

		collectSprites(heaps);
		collectSprites(mobs);
		for (Visual sprite : sprites) {
			addGrassBehind(sprite, groundY(sprite));
		}

		grassChanges.xor(grassBehindSprites);
		for (int pos = grassChanges.nextSetBit(0); pos >= 0; pos = grassChanges.nextSetBit(pos + 1)) {
			updateMapCell(pos);
		}
		if (grassBehindSprites.isEmpty()) {
			sprites.clear();
			return false;
		}

		for (int i = 0; heaps != null && i < heaps.length; i++) {
			Gizmo child = heaps.memberAt(i);
			if (child != null && child.exists && child.visible && !depthSorted(child)) child.draw();
		}

		for (int i = 0; mobs != null && i < mobs.length; i++) {
			Gizmo child = mobs.memberAt(i);
			if (depthSorted(child)) break;
			if (child != null && child.exists && child.visible) child.draw();
		}

		grass.texture = texture;
		grass.camera = camera();
		grass.rm = rm;
		grass.gm = gm;
		grass.bm = bm;
		grass.am = am;
		grass.ra = ra;
		grass.ga = ga;
		grass.ba = ba;
		grass.aa = aa;
		Collections.sort(sprites, (a, b) -> Float.compare(groundY(a), groundY(b)));
		int pos = grassBehindSprites.nextSetBit(0);
		for (Visual sprite : sprites) {
			float depth = groundY(sprite);
			while (pos >= 0 && y + (pos / mapWidth + 2) * SIZE <= depth + 0.001f) {
				drawGrass(pos);
				pos = grassBehindSprites.nextSetBit(pos + 1);
			}
			sprite.draw();
		}
		while (pos >= 0) {
			drawGrass(pos);
			pos = grassBehindSprites.nextSetBit(pos + 1);
		}
		sprites.clear();
		return true;
	}

	public void drawEffects(Group mobs) {
		boolean afterSprite = false;
		for (int i = 0; i < mobs.length; i++) {
			Gizmo child = mobs.memberAt(i);
			if (depthSorted(child)) afterSprite = true;
			else if (afterSprite && child != null && child.exists && child.visible) child.draw();
		}
	}

	private void collectSprites(Group group) {
		for (int i = 0; group != null && i < group.length; i++) {
			Gizmo child = group.memberAt(i);
			if (depthSorted(child) && child.exists && child.visible) sprites.add((Visual)child);
		}
	}

	private static boolean depthSorted(Gizmo child) {
		return child instanceof CharSprite || child instanceof PartyPresentation.Death || child instanceof MissileSprite
				|| child instanceof DiscardedItemSprite
				|| child instanceof ItemSprite && ((ItemSprite)child).isDropping();
	}

	private static float groundY(Visual sprite) {
		if (sprite instanceof PartyPresentation.Death) return ((PartyPresentation.Death)sprite).groundY();
		return sprite instanceof CharSprite ? ((CharSprite)sprite).groundY() : ((ItemSprite)sprite).groundY();
	}

	private void drawGrass(int pos) {
		grass.frame(tileset.get(data[pos]));
		grass.x = x + (pos % mapWidth) * SIZE;
		grass.y = y + (pos / mapWidth) * SIZE;
		grass.draw();
	}

	private void addGrassBehind(Visual sprite, float groundY) {
		float cos = (float)Math.cos(Math.toRadians(sprite.angle));
		float sin = (float)Math.sin(Math.toRadians(sprite.angle));
		float cx = (sprite.width / 2 - sprite.origin.x) * sprite.scale.x;
		float cy = (sprite.height / 2 - sprite.origin.y) * sprite.scale.y;
		float centerX = sprite.x + sprite.origin.x + cx * cos - cy * sin;
		float centerY = sprite.y + sprite.origin.y + cx * sin + cy * cos;
		float halfWidth = (Math.abs(sprite.width() * cos) + Math.abs(sprite.height() * sin)) / 2;
		float halfHeight = (Math.abs(sprite.width() * sin) + Math.abs(sprite.height() * cos)) / 2;
		int left = Math.max(0, (int)Math.floor((centerX - halfWidth - x) / SIZE));
		int right = Math.min(mapWidth - 1, (int)Math.ceil((centerX + halfWidth - x) / SIZE) - 1);
		int top = Math.max(0, (int)Math.floor((centerY - halfHeight - y) / SIZE));
		int bottom = Math.min(mapHeight - 2, (int)Math.ceil((centerY + halfHeight - y) / SIZE) - 1);

		bottom = Math.min(bottom, (int)Math.floor((groundY - y + 0.001f) / SIZE) - 2);
		for (int row = top; row <= bottom; row++) {
			for (int col = left; col <= right; col++) {
				int pos = col + row * mapWidth;
				if (map[pos + mapWidth] == Terrain.HIGH_GRASS
						&& data[pos] == DungeonTileSheet.getVisualWithAlts(
								DungeonTileSheet.HIGH_GRASS_OVERHANG, pos + mapWidth)) {
					grassBehindSprites.set(pos);
				}
			}
		}
	}

	@Override
	protected int getTileVisual(int pos, int tile, boolean flat){

		if (flat) return -1;

		if (DungeonTileSheet.wallStitcheable(tile)) {
			if (pos + mapWidth < size && !DungeonTileSheet.wallStitcheable(map[pos + mapWidth])){

				if (map[pos + mapWidth] == Terrain.DOOR){
					return DungeonTileSheet.DOOR_SIDEWAYS;
				} else if (map[pos + mapWidth] == Terrain.LOCKED_DOOR){
					return DungeonTileSheet.DOOR_SIDEWAYS_LOCKED;
				} else if (map[pos + mapWidth] == Terrain.OPEN_DOOR){
					return DungeonTileSheet.NULL_TILE;
				}

			} else {
				return DungeonTileSheet.stitchInternalWallTile(
						tile,
						(pos+1) % mapWidth != 0 ?                           map[pos + 1] : -1,
						(pos+1) % mapWidth != 0 && pos + mapWidth < size ?  map[pos + 1 + mapWidth] : -1,
						pos % mapWidth != 0 && pos + mapWidth < size ?      map[pos - 1 + mapWidth] : -1,
						pos % mapWidth != 0 ?                               map[pos - 1] : -1
				);
			}

		}

		if (pos + mapWidth < size && DungeonTileSheet.wallStitcheable(map[pos+mapWidth])) {

			return DungeonTileSheet.stitchWallOverhangTile(
					tile,
					(pos+1) % mapWidth != 0 ?   map[pos + 1 + mapWidth] : -1,
												map[pos + mapWidth],
					pos % mapWidth != 0 ?       map[pos - 1 + mapWidth] : -1
			);

		} else if (Dungeon.level.insideMap(pos) && (map[pos+mapWidth] == Terrain.DOOR || map[pos+mapWidth] == Terrain.LOCKED_DOOR) ) {
			return DungeonTileSheet.DOOR_OVERHANG;
		} else if (Dungeon.level.insideMap(pos) && map[pos+mapWidth] == Terrain.OPEN_DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG_OPEN;
		} else if (pos + mapWidth < size && (map[pos+mapWidth] == Terrain.STATUE || map[pos+mapWidth] == Terrain.STATUE_SP)){
			return DungeonTileSheet.STATUE_OVERHANG;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.BARRICADE){
			return DungeonTileSheet.BARRICADE_OVERHANG;
		} else if (pos + mapWidth < size && map[pos+mapWidth] == Terrain.HIGH_GRASS){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.HIGH_GRASS_OVERHANG, pos + mapWidth);
		}

		return -1;
	}

	@Override
	public boolean overlapsPoint( float x, float y ) {
		return true;
	}

	@Override
	public boolean overlapsScreenPoint( int x, int y ) {
		return true;
	}

	@Override
	protected boolean needsRender(int pos) {
		return data[pos] != -1 && !grassBehindSprites.get(pos);
	}

	@Override
	public void destroy() {
		grass.destroy();
		super.destroy();
	}
}
