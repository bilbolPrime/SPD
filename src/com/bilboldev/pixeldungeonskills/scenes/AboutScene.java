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
package com.bilboldev.pixeldungeonskills.scenes;

import android.content.Intent;
import android.net.Uri;

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.ui.Archs;
import com.bilboldev.pixeldungeonskills.ui.ExitButton;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.Window;

public class AboutScene extends PixelScene {

    private static final String TXTFirst = "SkillFull Pixel Dungeon is coded by BilbolDev\n"
     + "Based on Pixel Dugeaon made by Watabou\n"
     + "Source code is available on GitHub\n";

    private static final String TXT =
		"Code & graphics: Watabou\n" +
		"Music: Cube_Code\n\n" + 
		"This game is inspired by Brian Walker's Brogue. " +
		"Try it on Windows, Mac OS or Linux - it's awesome! ;)\n\n" +
		"Please visit official website for additional info:";
	
	private static final String LNK = "pixeldungeon.watabou.ru";
    private static final String LNK_SPD = "https://github.com/bilbolPrime/SPD";
    private static final String LNK_SPD_WIKI = "http://pixeldungeon.wikia.com";
	
	@Override
	public void create() {
		super.create();

        BitmapTextMultiline textfirst = createMultiline( TXTFirst, 8 );
        textfirst.maxWidth = Math.min( Camera.main.width, 120 );
        textfirst.measure();
        add( textfirst );

        textfirst.x = align( (Camera.main.width - textfirst.width()) / 2 );
        textfirst.y = align( (Camera.main.height - textfirst.height()) / 2 ) - 45;

        BitmapTextMultiline link_SPD = createMultiline( LNK_SPD, 8 );
        link_SPD.maxWidth = Math.min( Camera.main.width, 120 );
        link_SPD.measure();
        link_SPD.hardlight( Window.TITLE_COLOR );
        add( link_SPD );

        TouchArea hotArea_SPD = new TouchArea( link_SPD ) {
            @Override
            protected void onClick( Touch touch ) {
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( LNK_SPD ) );
                Game.instance.startActivity( intent );
            }
        };
        add( hotArea_SPD );

        link_SPD.x = textfirst.x;
        link_SPD.y = textfirst.y + textfirst.height();

        BitmapTextMultiline link_SPD_Wiki = createMultiline( LNK_SPD_WIKI, 8 );
        link_SPD_Wiki.maxWidth = Math.min( Camera.main.width, 120 );
        link_SPD_Wiki.measure();
        link_SPD_Wiki.hardlight( Window.TITLE_COLOR );
        add( link_SPD_Wiki );

        link_SPD_Wiki.x = link_SPD.x;
        link_SPD_Wiki.y = link_SPD.y + link_SPD.height();


        TouchArea hotArea_SPD_WIKI = new TouchArea( link_SPD_Wiki ) {
            @Override
            protected void onClick( Touch touch ) {
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( LNK_SPD_WIKI ) );
                Game.instance.startActivity( intent );
            }
        };
        add( hotArea_SPD_WIKI );

		BitmapTextMultiline text = createMultiline( TXT, 8 );
		text.maxWidth = Math.min( Camera.main.width, 120 );
		text.measure();
		add( text );

		text.x = align( (Camera.main.width - text.width()) / 2 );
		text.y = align( (Camera.main.height - text.height()) / 2 ) + 45;

		BitmapTextMultiline link = createMultiline( LNK, 8 );
		link.maxWidth = Math.min( Camera.main.width, 120 );
		link.measure();
		link.hardlight( Window.TITLE_COLOR );
		add( link );

		link.x = text.x;
		link.y = text.y + text.height();

		TouchArea hotArea = new TouchArea( link ) {
			@Override
			protected void onClick( Touch touch ) {
				Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "http://" + LNK ) );
				Game.instance.startActivity( intent );
			}
		};
		add( hotArea );

		Image wata = Icons.WATA.get();
		wata.x = align( (Camera.main.width - wata.width) / 2 );
		wata.y = text.y - wata.height - 8;
		add( wata );

		new Flare( 7, 64 ).color( 0x112233, true ).show( wata, 0 ).angularSpeed = +20;

		Archs archs = new Archs();
		archs.setSize( Camera.main.width, Camera.main.height );
		addToBack( archs );

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );

		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		PixelDungeon.switchNoFade( TitleScene.class );
	}
}
