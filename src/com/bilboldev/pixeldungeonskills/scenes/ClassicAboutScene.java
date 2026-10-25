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

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.ui.Archs;
import com.bilboldev.pixeldungeonskills.ui.ExitButton;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.Window;

public class ClassicAboutScene extends PixelScene {

    private static final String TXTFirst = "SkillFull Pixel Dungeon \n \n"
     + "Code & graphics: BilbolDev\n"
     + "Source code is available on GitHub\n";

    private static final String TXT =
         "Based on Pixel Dungeon \n \n" +
		"Code & graphics: Watabou\n" +
		"Music: Cube_Code\n\n";

	private static final String LNK = "pixeldungeon.watabou.ru";
    private static final String LNK_SPD = "github.com/bilbolPrime/SPD";
    private static final String LNK_SPD_TARGET = "https://github.com/bilbolPrime/SPD";

    float GAP = 2;
    float pos = 0f;
	@Override
	public void create() {
		super.create();
        com.bilboldev.noosa.audio.Music.INSTANCE.volume(1);
        com.bilboldev.noosa.audio.Music.INSTANCE.play(Presentation.titleMusic(), true);

        BitmapTextMultiline textfirst = createMultiline( "SkillFull Pixel Dungeon", 8 );
        textfirst.hardlight( Window.TITLE_COLOR );
        textfirst.maxWidth = Math.min( Camera.main.width, 120 );
        textfirst.measure();
        add( textfirst );

        textfirst.x = align( (Camera.main.width - textfirst.width()) / 2 );
        textfirst.y = align( ((Camera.main.height / 2) - textfirst.height()) / 2 );

        pos =  textfirst.y + textfirst.height() + GAP;

        textfirst = createMultiline( "Code & graphics: BilbolDev", 8 );
        textfirst.maxWidth = Math.min( Camera.main.width, 120 );
        textfirst.measure();
        add( textfirst );

        textfirst.x = align( (Camera.main.width - textfirst.width()) / 2 );
        textfirst.y = pos;

        pos =  textfirst.y + textfirst.height() + GAP;

        textfirst = createMultiline( "Source code is available on GitHub", 8 );
        textfirst.maxWidth = Math.min( Camera.main.width, 120 );
        textfirst.measure();
        add( textfirst );

        textfirst.x = align( (Camera.main.width - textfirst.width()) / 2 );
        textfirst.y = pos;

        pos =  textfirst.y + textfirst.height() + GAP;

        BitmapTextMultiline link_SPD = createMultiline( LNK_SPD, 8 );
        link_SPD.maxWidth = Math.min( Camera.main.width, 120 );
        link_SPD.measure();
        link_SPD.hardlight( Window.TITLE_COLOR );
        add( link_SPD );

        TouchArea hotArea_SPD = new TouchArea( link_SPD ) {
            @Override
            protected void onClick( Touch touch ) {
                Window.openUrl( LNK_SPD_TARGET );
            }
        };
        add( hotArea_SPD );

        link_SPD.x = align( (Camera.main.width - link_SPD.width()) / 2 );
        link_SPD.y = pos;

        pos =  link_SPD.y + link_SPD.height() + 4 * GAP;

        BitmapTextMultiline text = createMultiline( "Based on Pixel Dungeon", 8 );
		text.maxWidth = Math.min( Camera.main.width, 120 );
		text.measure();
		add( text );

		text.x = align( (Camera.main.width - text.width()) / 2 );
		text.y = pos;

        pos =  text.y + text.height() + GAP;

        text = createMultiline( "Code & graphics: Watabou", 8 );
        text.maxWidth = Math.min( Camera.main.width, 120 );
        text.measure();
        add( text );

        text.x = align( (Camera.main.width - text.width()) / 2 );
        text.y = pos;

        pos =  text.y + text.height() + GAP;

        text = createMultiline( "Music: Cube_Code", 8 );
        text.maxWidth = Math.min( Camera.main.width, 120 );
        text.measure();
        add( text );

        text.x = align( (Camera.main.width - text.width()) / 2 );
        text.y = pos;

        pos =  text.y + text.height() + GAP;

		BitmapTextMultiline link = createMultiline( LNK, 8 );
		link.maxWidth = Math.min( Camera.main.width, 120 );
		link.measure();
		link.hardlight( Window.TITLE_COLOR );
		add( link );

		link.x = align( (Camera.main.width - link.width()) / 2 );
		link.y = pos;

		TouchArea hotArea = new TouchArea( link ) {
			@Override
			protected void onClick( Touch touch ) {
                Window.openUrl( "http://" + LNK );
			}
		};
		add( hotArea );

		Image wata = Icons.WATA.get();
		wata.x = align( (Camera.main.width - wata.width) / 2 );
		wata.y = text.y + text.height() + wata.height + 8;
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
