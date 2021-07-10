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
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.skills.BranchSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Negotiations;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndSkillNew extends Window {

	private static final float BUTTON_WIDTH		= 36;
	private static final float BUTTON_HEIGHT	= 16;

	private static final float GAP	= 2;

	private static final int WIDTH = 120;

	public WndSkillNew(final WndSkillsNew owner, final Skill skill) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new SkillSprite( skill.image() ) );
		titlebar.label( Utils.capitalize( skill.name ) );
        titlebar.setRect( 0, 0, WIDTH, 0 );
        add(titlebar);
		
        String cooldown = "\nThis skill is on cool down";
		float y =0;
		float x = 0;

		if(PixelDungeon.skillFontSize())
		{
			//BitmapTextMultiline info = PixelScene.createMultiline( skill.info() + (skill.coolDown() ? cooldown : ""), 6 );
			RenderedTextMultiline info = PixelScene.renderMultiline( skill.info() + (skill.coolDown() ? cooldown : ""), 6 );
			info.maxWidth(WIDTH);
			//info.measure();
			info.setPos(titlebar.left(), titlebar.bottom() + GAP);
			//info.x = titlebar.left();
			//info.y = titlebar.bottom() + GAP;
			add( info );
			y = info.bottom() + GAP;
		}
		else {
			//BitmapTextMultiline info = PixelScene.createMultiline( skill.info() + (skill.coolDown() ? cooldown : ""), 6 );
			HighlightedText info = new HighlightedText(7);//PixelScene.renderMultiline( skill.info() + (skill.coolDown() ? cooldown : ""), 6 );
			info.text( skill.info() + (skill.coolDown() ? cooldown : ""), WIDTH);
			//info.measure();
			info.setPos(titlebar.left(), titlebar.bottom() + GAP);
			//info.x = titlebar.left();
			//info.y = titlebar.bottom() + GAP;
			add( info );
			y = info.bottom() + GAP;
		}





		if (Dungeon.hero.isAlive() && !skill.coolDown()) {
			for (final String action:skill.actions( Dungeon.hero )) {
				
				RedButton btn = new RedButton( action ) {
					@Override
					protected void onClick() {
                        skill.execute( Dungeon.hero, action );
						hide();
                        if(owner != null)
                        {
                            owner.hide();
                            if(skill instanceof BranchSkill)
                                GameScene.show(new WndSkills(null, null));
                        }
					};
				};
				btn.setSize( Math.max( BUTTON_WIDTH, btn.reqWidth() ), BUTTON_HEIGHT );
				if (x + btn.width() > WIDTH) {
					x = 0;
					y += BUTTON_HEIGHT + GAP;
				}
				btn.setPos( x, y );

                add( btn );

				x += btn.width() + GAP;
			}
		}

		if(owner != null){
			if(!PixelDungeon.skillFontSize()){
				RedButton btn = new RedButton( "A+" ) {
					@Override
					protected void onClick() {
						PixelDungeon.skillFontSize(true);
						hide();
						owner.add(new WndSkillNew(owner, skill));
					};
				};
				btn.setSize( 15f, 15f );
				btn.setPos( WIDTH - 15f, 0 );

				add( btn );
			}
			else {
				RedButton btn = new RedButton( "A-" ) {
					@Override
					protected void onClick() {
						PixelDungeon.skillFontSize(false);
						hide();
						owner.add(new WndSkillNew(owner, skill));
					};
				};
				btn.setSize( 15f, 15f );
				btn.setPos( WIDTH - 15f, 0 );

				add( btn );
			}

		}

		resize( WIDTH, (int)(y + (x > 0 ? BUTTON_HEIGHT : 0)) );
	}


	public WndSkillNew(final WndSkills owner, final Skill skill) {

		super();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new SkillSprite( skill.image() ) );
		titlebar.label( Utils.capitalize( skill.name ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add(titlebar);

		String cooldown = "\nThis skill is on cool down";
		//BitmapTextMultiline info = PixelScene.createMultiline( skill.info() + (skill.coolDown() ? cooldown : ""), 6 );
		RenderedTextMultiline info = PixelScene.renderMultiline( skill.info() + (skill.coolDown() ? cooldown : ""), 6 );
		info.maxWidth(WIDTH);
		//info.measure();
		info.setPos(titlebar.left(), titlebar.bottom() + GAP);
		//info.x = titlebar.left();
		//info.y = titlebar.bottom() + GAP;
		add( info );

		float y = info.bottom() + GAP;
		float x = 0;

		if (Dungeon.hero.isAlive() && !skill.coolDown()) {
			for (final String action:skill.actions( Dungeon.hero )) {

				RedButton btn = new RedButton( action ) {
					@Override
					protected void onClick() {
						skill.execute( Dungeon.hero, action );
						hide();
						if(owner != null)
						{
							owner.hide();
							if(skill instanceof BranchSkill)
								GameScene.show(new WndSkills(null, null));
						}
					};
				};
				btn.setSize( Math.max( BUTTON_WIDTH, btn.reqWidth() ), BUTTON_HEIGHT );
				if (x + btn.width() > WIDTH) {
					x = 0;
					y += BUTTON_HEIGHT + GAP;
				}
				btn.setPos( x, y );


				if (action == Negotiations.TXT_HIRE_ARCHER_MAIDEN) {
					btn.textColor( TITLE_COLOR );
				}


				add( btn );

				x += btn.width() + GAP;
			}
		}

		resize( WIDTH, (int)(y + (x > 0 ? BUTTON_HEIGHT : 0)) );
	}
}
