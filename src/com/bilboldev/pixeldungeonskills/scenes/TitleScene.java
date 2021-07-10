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

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.net.Uri;
import android.opengl.GLES20;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.BillingHelper;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.VersionNewsInfo;
import com.bilboldev.pixeldungeonskills.actors.Online.OnlineHero;
import com.bilboldev.pixeldungeonskills.effects.BannerSprites;
import com.bilboldev.pixeldungeonskills.effects.Effects;
import com.bilboldev.pixeldungeonskills.effects.Fireball;
import com.bilboldev.pixeldungeonskills.ui.Archs;
import com.bilboldev.pixeldungeonskills.ui.DiscordButton;
import com.bilboldev.pixeldungeonskills.ui.ExitButton;
import com.bilboldev.pixeldungeonskills.ui.GoogleGameButton;
import com.bilboldev.pixeldungeonskills.ui.MoviesButton;
import com.bilboldev.pixeldungeonskills.ui.PrefsButton;
import com.bilboldev.pixeldungeonskills.windows.WndDiscord;
import com.bilboldev.pixeldungeonskills.windows.WndDonations;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.pixeldungeonskills.windows.WndStory;


public class TitleScene extends PixelScene {

	private static final String TXT_PLAY		= "Play";
	private static final String TXT_HIGHSCORES	= "Rankings";
	private static final String TXT_BADGES		= "Badges";
	private static final String TXT_ABOUT		= "About";
	
	@Override
	public void create() {
		
		super.create();

        if(Dungeon.hero != null && Dungeon.hero instanceof OnlineHero)
        {
            try {
                ((Game) Game.mContext).mService.mHubConnection.stop();
            }
            catch (Exception e)
            {

            }
        }

		Music.INSTANCE.play( Assets.THEME, true );
		Music.INSTANCE.volume( 1f );
        Effects.get(Effects.Type.RIPPLE);
        Effects.get(Effects.Type.LIGHTNING);
        Effects.get(Effects.Type.WOUND);
        Effects.get(Effects.Type.RAY);
		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );
		
		Image title = BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON );
		add( title );
		
		float height = title.height + 
			(PixelDungeon.landscape() ? DashboardItem.SIZE : DashboardItem.SIZE * 2);
		
		title.x = (w - title.width()) / 2;
		title.y = (h - height) / 2;
		
		placeTorch( title.x + 18, title.y + 20 );
		placeTorch( title.x + title.width - 18, title.y + 20 );
		
		Image signs = new Image( BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON_SIGNS ) ) {
			private float time = 0;
			@Override
			public void update() {
				super.update();
				am = (float)Math.sin( -(time += Game.elapsed) );
			}
			@Override
			public void draw() {
				GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
				super.draw();
				GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
			}
		};
		signs.x = title.x;
		signs.y = title.y;
		add( signs );

        Image signsSkillful = new Image( BannerSprites.get( BannerSprites.Type.SKILLFUL_SIGNS ) );
        signsSkillful.x = title.x;
        signsSkillful.y = title.y + 20;
        add( signsSkillful );
		
		DashboardItem btnBadges = new DashboardItem( TXT_BADGES, 3 ) {
			@Override
			protected void onClick() {
				PixelDungeon.switchNoFade( BadgesScene.class );
			}
		};
		add( btnBadges );
		
		DashboardItem btnAbout = new DashboardItem( TXT_ABOUT, 1 ) {
			@Override
			protected void onClick() {
				PixelDungeon.switchNoFade( AboutScene.class );
			}
		};
		add( btnAbout );
		
		DashboardItem btnPlay = new DashboardItem( TXT_PLAY, 0 ) {
			@Override
			protected void onClick() {
				chooseGameMode();

			}
		};
		add( btnPlay );
		
		DashboardItem btnHighscores = new DashboardItem( TXT_HIGHSCORES, 2 ) {
			@Override
			protected void onClick() {
				PixelDungeon.switchNoFade( RankingsScene.class );
			}
		};
		add( btnHighscores );

        DashboardItem btndonate = new DashboardItem( "Donate", 4 ) {
            @Override
            protected void onClick() {
                parent.add(new WndDonations(WndDonations.Mode.NORMAL));
            }
        };

        add( btndonate );

		BillingHelper.getInstance();
		
		if (PixelDungeon.landscape()) {
			float y = (h + height) / 2 - DashboardItem.SIZE;
			btnHighscores	.setPos( w / 2 - btnHighscores.width(), y );
			btnBadges		.setPos( w / 2, y );
			btnPlay			.setPos( btnHighscores.left() - btnPlay.width(), y );
			btnAbout		.setPos( btnBadges.right(), y );
            btndonate       .setPos( btnAbout.right(), y );
		} else {
			btnBadges.setPos( w / 2 - btnBadges.width(), (h + height) / 2 - DashboardItem.SIZE );
			btnAbout.setPos( w / 2, (h + height) / 2 - DashboardItem.SIZE );
			btnPlay.setPos( w / 2 - btnPlay.width(), btnAbout.top() - DashboardItem.SIZE );
			btnHighscores.setPos( w / 2, btnPlay.top() );
            btndonate.setPos(w / 2 - btndonate.width() / 2, (h + height) / 2);
		}

        BitmapText version = new BitmapText( "v " + Game.version + " (Build " + Game.versionBuild + ")", font1x );
        version.measure();
        version.hardlight( 0xFFFFFF );
        version.x = w - version.width();
        version.y = h - version.height() - 9;
        add( version );

        BitmapText versionPD = new BitmapText( Game.vanillaVersion , font1x );
        versionPD.measure();
        versionPD.hardlight( 0x666666 );
        versionPD.x = w - versionPD.width();
        versionPD.y = h - versionPD.height();
        add( versionPD );
		
		PrefsButton btnPrefs = new PrefsButton();
		btnPrefs.setPos( 0, 0 );
		add( btnPrefs );

		DiscordButton btnGame = new DiscordButton() {
            @Override
            public void onClick()
            {
            	try
				{
					add(new WndDiscord(WndDiscord.Mode.DISCORD));
				}
				catch (Exception e){

				}
			}
        };
        btnGame.setPos( btnPrefs.right() + 2, 2 );
        add( btnGame );


      //  MoviesButton btnMovie = new MoviesButton() {

     //   };
     //   btnMovie.setPos( btnGame.right() + 4, 1 );
      //  add( btnMovie );
		
		ExitButton btnExit = new ExitButton();
		btnExit.setPos( w - btnExit.width(), 0 );
		add( btnExit );

        if(VersionNewsInfo.haveMessage())
        {
            add(VersionNewsInfo.getWelcomeWindow());
        }

		fadeIn();
	}

	private void chooseGameMode()
	{

		PixelDungeon.switchNoFade( StartScene.class );
		/*TitleScene.this.add( new WndOptions( "Game Mode", "Please select a game mode", "Enter The Dungeon","Gauntlet" ) {
			@Override
			protected void onSelect( int index ) {
				if(index == 0)
				{
					PixelDungeon.switchNoFade( StartScene.class );
				}
				else
				{
					Dungeon.hero = null;
					Dungeon.difficulty = 1;
					Dungeon.currentDifficulty = Difficulties.values()[1];
					Dungeon.currentDifficulty.reset();
					InterlevelScene.mode = InterlevelScene.Mode.GUANTLET;
					Game.switchScene( InterlevelScene.class );
				}
			}
		} );

*/
	}

	private void placeTorch( float x, float y ) {
		Fireball fb = new Fireball();
		fb.setPos( x, y );
		add( fb );
	}
	
	private static class DashboardItem extends Button {
		
		public static final float SIZE	= 48;
		
		private static final int IMAGE_SIZE	= 32;
		
		private Image image;
		private BitmapText label;
		
		public DashboardItem( String text, int index ) {
			super();
			
			image.frame( image.texture.uvRect( index * IMAGE_SIZE, 0, (index + 1) * IMAGE_SIZE, IMAGE_SIZE ) );
			this.label.text( text );
			this.label.measure();
			
			setSize( SIZE, SIZE );
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			image = new Image( Assets.DASHBOARD );
			add( image );
			
			label = createText( 9 );
			add( label );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			image.x = align( x + (width - image.width()) / 2 );
			image.y = align( y );
			
			label.x = align( x + (width - label.width()) / 2 );
			label.y = align( image.y + image.height() +2 );
		}
		
		@Override
		protected void onTouchDown() {
			image.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.SND_CLICK, 1, 1, 0.8f );
		}
		
		@Override
		protected void onTouchUp() {
			image.resetColor();
		}
	}
}
