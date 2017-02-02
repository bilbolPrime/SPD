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
package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;

public class Toast extends Component {

	private static final float MARGIN_HOR	= 2;
	private static final float MARGIN_VER	= 2;
	
	protected NinePatch bg;
	protected SimpleButton close;
	protected BitmapTextMultiline text;
	
	public Toast( String text ) {
		super();
		text( text );
		
		width = this.text.width() + close.width() + bg.marginHor() + MARGIN_HOR * 3;
		height = Math.max( this.text.height(), close.height() ) + bg.marginVer() + MARGIN_VER * 2;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		bg = Chrome.get( Chrome.Type.TOAST_TR );
		add( bg );
		
		close = new SimpleButton( Icons.get( Icons.CLOSE ) ) {
			protected void onClick() {
				onClose();
			};
		};
		add( close );
		text = PixelScene.createMultiline( 8 );
		add( text );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		bg.x = x;
		bg.y = y;
		bg.size( width, height );
		
		close.setPos( 
			bg.x + bg.width() - bg.marginHor() / 2 - MARGIN_HOR - close.width(),
			y + (height - close.height()) / 2 );
		
		text.x = close.left() - MARGIN_HOR - text.width();
		text.y = y + (height - text.height()) / 2;
		PixelScene.align( text );
	}
	
	public void text( String txt ) {
		text.text( txt );
		text.measure();
	}
	
	protected void onClose() {};
}
