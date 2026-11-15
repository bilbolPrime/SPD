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
import com.bilboldev.noosa.Tilemap;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.GameMath;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.PointF;

public abstract class DungeonTilemap extends DungeonTilemapOld {

	public static final int SIZE = 16;

	public DungeonTilemap(String tex) {
		super();
	}

	@Override
	protected int getTileVisual(int pos, int tile) {
		return getTileVisual(pos, tile, false);
	}

	protected abstract int getTileVisual(int pos, int tile, boolean flat);

	public int screenToTile(int x, int y ){
		return screenToTile(x, y, false);
	}

	public int screenToTile(int x, int y, boolean wallAssist ) {
		PointF p = camera().screenToCamera( x, y ).
				offset( this.point().negate() ).
				invScale( SIZE );

		p.x = GameMath.gate(0, p.x, Dungeon.level.width()-0.001f);
		p.y = GameMath.gate(0, p.y, Dungeon.level.height()-0.001f);

		int cell = (int)p.x + (int)p.y * Dungeon.level.width();

		if (wallAssist
				&& map != null
				&& DungeonTileSheet.wallStitcheable(map[cell])){

			if (cell + mapWidth < size
					&& p.y % 1 >= 0.75f
					&& !DungeonTileSheet.wallStitcheable(map[cell + mapWidth])){
				cell += mapWidth;
			}

		}

		return cell;
	}

	@Override
	public boolean overlapsPoint( float x, float y ) {
		return true;
	}

	public void discover( int pos, int oldValue ) {

		int visual = getTileVisual( pos, oldValue, false);
		if (visual < 0) return;

		final Image tile = new Image( texture );
		tile.frame( tileset.get( getTileVisual( pos, oldValue, false)));
		tile.point( tileToWorld( pos ) );

		parent.add( tile );

		parent.add( new AlphaTweener( tile, 0, 0.6f ) {
			protected void onComplete() {
				tile.killAndErase();
				killAndErase();
			};
		} );
	}

	public static PointF tileToWorld( int pos ) {
		return new PointF( pos % Dungeon.level.width(), pos / Dungeon.level.width()  ).scale( SIZE );
	}

	public static PointF raisedTileToWorld( int pos ) {
		return new PointF( pos % Dungeon.level.width(), pos / Dungeon.level.width() - 0.2f  ).scale( SIZE );
	}

	public static PointF tileCenterToWorld( int pos ) {
		return new PointF(
				(pos % Dungeon.level.width() + 0.5f) * SIZE,
				(pos / Dungeon.level.width() + 0.5f) * SIZE );
	}

	public static PointF raisedTileCenterToWorld( int pos ) {
		return new PointF(
				(pos % Dungeon.level.width() + 0.5f) * SIZE,
				(pos / Dungeon.level.width() + 0.1f) * SIZE );
	}

	@Override
	public boolean overlapsScreenPoint( int x, int y ) {
		return true;
	}

}
