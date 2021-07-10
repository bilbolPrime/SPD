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

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.utils.PathFinder;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.noosa.Image;
import com.bilboldev.utils.PathFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DungeonTerrainTilemap extends DungeonTilemap {

	static DungeonTerrainTilemap instance;

	public DungeonTerrainTilemap(){
		super(Dungeon.level.tilesTex());
		instance = this;
		map( Dungeon.level.map, Dungeon.level.WIDTH );


	}

	@Override
	protected int getTileVisual(int pos, int tile, boolean flat) {
		if(offsetStuff().contains(tile)){
			return tile + 256;
		}

		int visual = DungeonTileSheet.directVisuals.get(tile, -1);
		if (visual != -1) return DungeonTileSheet.getVisualWithAlts(visual, pos);

		if (tile == Terrain.WATER) {
			return DungeonTileSheet.stitchWaterTile(
					map[pos + PathFinder.CIRCLE4[0]],
					map[pos + PathFinder.CIRCLE4[1]],
					map[pos + PathFinder.CIRCLE4[2]],
					map[pos + PathFinder.CIRCLE4[3]]
			);

		} else if (tile == Terrain.CHASM) {
			return DungeonTileSheet.stitchChasmTile( pos > mapWidth ? map[pos - mapWidth] : -1);
		}



		if (!flat) {
			if ((DungeonTileSheet.doorTile(tile))) {
				return DungeonTileSheet.getRaisedDoorTile(tile, map[pos - mapWidth]);
			} else if (tile == Terrain.WALL || tile == Terrain.WALL_DECO
					|| tile == Terrain.SECRET_DOOR || tile == Terrain.BOOKSHELF){
				return DungeonTileSheet.getRaisedWallTile(
						tile,
						pos,
						(pos+1) % mapWidth != 0 ?   map[pos + 1] : -1,
						pos + mapWidth < size ?     map[pos + mapWidth] : -1,
						pos % mapWidth != 0 ?       map[pos - 1] : -1
						);
			} else if (tile == Terrain.SIGN) {
				return DungeonTileSheet.RAISED_SIGN;
			} else if (tile == Terrain.STATUE) {
				return DungeonTileSheet.RAISED_STATUE;
			} else if (tile == Terrain.STATUE_SP) {
				return DungeonTileSheet.RAISED_STATUE_SP;
			} else if (tile == Terrain.BARRICADE) {
				return DungeonTileSheet.RAISED_BARRICADE;
			} else if (tile == Terrain.HIGH_GRASS) {
				return DungeonTileSheet.getVisualWithAlts(
						DungeonTileSheet.RAISED_HIGH_GRASS,
						pos);
			} else {
				return DungeonTileSheet.NULL_TILE;
			}
		} else {
			return DungeonTileSheet.getVisualWithAlts(
					DungeonTileSheet.directFlatVisuals.get(tile),
					pos);
		}

	}

	public static Image tile(int pos, int tile ) {
		Image img = new Image( instance.texture );
		img.frame( instance.tileset.get( instance.getTileVisual( pos, tile, true ) ) );
		return img;
	}

	@Override
	protected boolean needsRender(int pos) {
		return data[pos] >= 0 && data[pos] != DungeonTileSheet.WATER;
	}

	private List<Integer> offsetStuff(){
		return Arrays.asList(Terrain.STORAGE, Terrain.TOXIC_TRAP, Terrain.SECRET_TOXIC_TRAP, Terrain.FIRE_TRAP, Terrain.SECRET_FIRE_TRAP, Terrain.PARALYTIC_TRAP, Terrain.SECRET_PARALYTIC_TRAP, Terrain.INACTIVE_TRAP, Terrain.POISON_TRAP, Terrain.SECRET_POISON_TRAP, Terrain.ALARM_TRAP, Terrain.SECRET_ALARM_TRAP, Terrain.LIGHTNING_TRAP, Terrain.SECRET_LIGHTNING_TRAP, Terrain.GRIPPING_TRAP, Terrain.SECRET_GRIPPING_TRAP, Terrain.SUMMONING_TRAP, Terrain.SECRET_SUMMONING_TRAP);
	}
}
