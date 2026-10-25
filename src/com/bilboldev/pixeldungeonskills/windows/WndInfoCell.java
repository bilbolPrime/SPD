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
package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTerrainTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemapOld;

public class WndInfoCell extends WndScrollable {
	
	private static final float GAP	= 2;
	
	private static final int WIDTH = 120;
	
	private static final String TXT_NOTHING	= "There is nothing here.";
	
	public WndInfoCell( int cell ) {
		
		super();
		int windowWidth = boundedWidth(InspectionTitle.windowWidth(WIDTH));
		
		int tile = Dungeon.level.map[cell];
		if (Level.water[cell]) {
			tile = Terrain.WATER;
		} else if (Level.pit[cell]) {
			tile = Terrain.CHASM;
		}
		
		Image image;
		if (tile == Terrain.WATER) {
			image = new Image( Dungeon.level.waterTex() );
			image.frame( 0, 0, DungeonTilemap.SIZE, DungeonTilemap.SIZE );
		} else {
			image = Difficulties.is3d ? DungeonTerrainTilemap.tile( cell, tile ) : DungeonTilemapOld.tile( tile );
		}
		Component titlebar = InspectionTitle.create(image, Dungeon.level.tileName(tile));
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );
		
		BitmapTextMultiline info = PixelScene.createMultiline( 6 );
		add( info );
		
		StringBuilder desc = new StringBuilder( Dungeon.level.tileDesc( tile ) );
		
		final char newLine = '\n';
		for (Blob blob:Dungeon.level.blobs.values()) {
			String blobDesc = blob.displayTileDesc();
			if (blob.cur[cell] > 0 && blobDesc != null) {
				if (desc.length() > 0) {
					desc.append( newLine );
				}
				desc.append( blobDesc );
			}
		}
		
		info.text( desc.length() > 0 ? desc.toString() : Messages.get( WndInfoCell.class, "nothing" ) );
		info.maxWidth = textWidth(windowWidth);
		info.measure();
		layoutContent(windowWidth, titlebar, textContent(info), GAP);
	}
}
