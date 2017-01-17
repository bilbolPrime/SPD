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
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.HealthBar;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class IconTitle extends Component {

	private static final float FONT_SIZE = 9;
	
	private static final float GAP = 2;
	
	protected Image imIcon;
	protected BitmapTextMultiline tfLabel;
	protected HealthBar health;
	
	private float healthLvl = Float.NaN;
	
	public IconTitle() {
		super();
	}
	
	public IconTitle( Item item ) {
		this( 
			new ItemSprite( item.image(), item.glowing() ), 
			Utils.capitalize( item.toString() ) );
	}
	
	public IconTitle( Image icon, String label ) {
		super();
		
		icon( icon );
		label( label );
	}
	
	@Override
	protected void createChildren() {
		imIcon = new Image();
		add( imIcon );
		
		tfLabel = PixelScene.createMultiline( FONT_SIZE );
		tfLabel.hardlight( Window.TITLE_COLOR );
		add( tfLabel );
		
		health = new HealthBar();
		add( health );
	}
	
	@Override
	protected void layout() {
		
		health.visible = !Float.isNaN( healthLvl );
		
		imIcon.x = x;
		imIcon.y = y;
		
		tfLabel.x = PixelScene.align( PixelScene.uiCamera, imIcon.x + imIcon.width() + GAP );
		tfLabel.maxWidth = (int)(width - tfLabel.x);
		tfLabel.measure();
		tfLabel.y =  PixelScene.align( PixelScene.uiCamera,
			imIcon.height > tfLabel.height() ?
				imIcon.y + (imIcon.height() - tfLabel.baseLine()) / 2 :
				imIcon.y );
		
		if (health.visible) {
			health.setRect( tfLabel.x, Math.max( tfLabel.y + tfLabel.height(), imIcon.y + imIcon.height() - health.height() ), tfLabel.maxWidth, 0 );
			height = health.bottom();
		} else {
			height = Math.max( imIcon.y + imIcon.height(), tfLabel.y + tfLabel.height() );
		}
	}
	
	public void icon( Image icon ) {
		remove( imIcon );
		add( imIcon = icon );
	}
	
	public void label( String label ) {
		tfLabel.text( label );
	}
	
	public void label( String label, int color ) {
		tfLabel.text( label );
		tfLabel.hardlight( color );
	}
	
	public void color( int color ) {
		tfLabel.hardlight( color );
	}
	
	public void health( float value ) {
		health.level( healthLvl = value );
		layout();
	}
}
