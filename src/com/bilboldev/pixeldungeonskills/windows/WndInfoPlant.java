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
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.PlantSprite;

public class WndInfoPlant extends WndScrollable {
	
	private static final float GAP	= 2;
	
	private static final int WIDTH = 120;
	
	public WndInfoPlant( Plant plant ) {
		
		super();
		int windowWidth = boundedWidth(InspectionTitle.windowWidth(WIDTH));
		
		Component titlebar = InspectionTitle.create(new PlantSprite(plant.image), plant.displayName());
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );
		
		BitmapTextMultiline info = PixelScene.createMultiline( 6 );
		add( info );
		
		info.text( plant.displayDesc() );
		info.maxWidth = textWidth(windowWidth);
		info.measure();
		layoutContent(windowWidth, titlebar, textContent(info), GAP);
	}
}
