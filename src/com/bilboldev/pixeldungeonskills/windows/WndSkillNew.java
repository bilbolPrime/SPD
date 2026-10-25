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

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.messages.ActionMessages;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;

import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.skills.BranchSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Negotiations;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import java.util.ArrayList;

public class WndSkillNew extends WndScrollable {

    private final Hero hero = Dungeon.hero;

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.ARCANE;
    }

	private static final float BUTTON_WIDTH		= 36;
	private static final float BUTTON_HEIGHT	= 16;

	private static final float GAP	= 2;

	private static final int DEFAULT_RENDERED_TEXT_SIZE = 9;
	private static final int DEFAULT_HIGHLIGHTED_TEXT_SIZE = 8;
	private final int windowWidth;

	@Override
	protected boolean usesDesktopHeightConstraint() {
		return false;
	}

	@Override
	protected float overlayTextScale() {
		return 1f;
	}

	public WndSkillNew(final WndSkillsNew owner, final Skill skill) {

		super();
        menuModel = new MenuModel(Message.capitalize(Message.skillName(skill)), Message.skillDescription(skill));
		windowWidth = boundedWidth(landscapeLayout() ? 180 : 150);
		ArrayList<RedButton> buttons = new ArrayList<>();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new SkillSprite( skill.image() ) );
		titlebar.label( Messages.capitalize( Messages.maybeTranslate( skill.name ) ) );
        titlebar.setRect( 0, 0, windowWidth - (owner == null ? 0 : 17), 0 );

		Component info = buildInfoComponent(buildInfoText(skill));

		if (hero.isAlive() && !skill.coolDown()) {
			for (final String action:skill.actions( hero )) {

				RedButton btn = new ActionButton( action ) {
					@Override
					protected float textScale() {
						return WndSkillNew.this.overlayTextDrawScale();
					}

					@Override
					protected void onClick() {
						Runnable use = () -> {
							if (!LocalCoop.active() || hero.isAlive() && !skill.coolDown()
									&& skill.actions(hero).contains(action)) {
								skill.execute(hero, action);
							}
						};
						if (!LocalCoop.defer(hero, use)) use.run();
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
				buttons.add(btn);
                menuModel.add(btn, ActionMessages.skill(action));
			}
		}

		if(owner != null){
			if(!PixelDungeon.skillFontSize()){
				RedButton btn = new RedButton( "A+" ) {
					@Override
					protected float textScale() {
						return WndSkillNew.this.overlayTextDrawScale();
					}

					@Override
					protected void onClick() {
						PixelDungeon.skillFontSize(true);
						hide();
						GameScene.show(new WndSkillNew(owner, skill));
					};
				};
				btn.setSize( 15f, 15f );
				btn.setPos( windowWidth - 15f, 0 );

				add( btn );
			}
			else {
				RedButton btn = new RedButton( "A-" ) {
					@Override
					protected float textScale() {
						return WndSkillNew.this.overlayTextDrawScale();
					}

					@Override
					protected void onClick() {
						PixelDungeon.skillFontSize(false);
						hide();
						GameScene.show(new WndSkillNew(owner, skill));
					};
				};
				btn.setSize( 15f, 15f );
				btn.setPos( windowWidth - 15f, 0 );

				add( btn );
			}

		}

		layoutContent(windowWidth, titlebar, info, GAP, buttons.toArray(new RedButton[0]));
	}

	public WndSkillNew(final WndSkills owner, final Skill skill) {

		super();
        menuModel = new MenuModel(Message.capitalize(Message.skillName(skill)), Message.skillDescription(skill));
		windowWidth = boundedWidth(landscapeLayout() ? 180 : 150);
		ArrayList<RedButton> buttons = new ArrayList<>();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new SkillSprite( skill.image() ) );
		titlebar.label( Messages.capitalize( Messages.maybeTranslate( skill.name ) ) );
		titlebar.setRect( 0, 0, windowWidth, 0 );

		Component info = buildInfoComponent(buildInfoText(skill));

		if (hero.isAlive() && !skill.coolDown()) {
			for (final String action:skill.actions( hero )) {
				RedButton btn = new ActionButton( action ) {
					@Override
					protected float textScale() {
						return WndSkillNew.this.overlayTextDrawScale();
					}

					@Override
					protected void onClick() {
						Runnable use = () -> {
							if (!LocalCoop.active() || hero.isAlive() && !skill.coolDown()
									&& skill.actions(hero).contains(action)) {
								skill.execute(hero, action);
							}
						};
						if (!LocalCoop.defer(hero, use)) use.run();
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
				buttons.add(btn);
                menuModel.add(btn, ActionMessages.skill(action));

				if (action == Negotiations.TXT_HIRE_ARCHER_MAIDEN) {
					btn.textColor( titleColor() );
				}

			}
		}

		layoutContent(windowWidth, titlebar, info, GAP, buttons.toArray(new RedButton[0]));
	}

	private String buildInfoText( Skill skill ) {
		String infoText = localizeSkillInfo( skill.info() );
		if (skill.coolDown()) {
			infoText += "\n" + Messages.maybeTranslate( "This skill is on cool down" );
		}
		return infoText;
	}

	private String localizeSkillInfo( String text ) {
		if (text == null || text.indexOf('\n') < 0) {
			return Messages.maybeTranslate( text );
		}

		String[] lines = text.split("\\n", -1);
		StringBuilder builder = new StringBuilder(text.length());
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				builder.append('\n');
			}
			if (lines[i].length() == 0) {
				continue;
			}
			builder.append( Messages.maybeTranslate( lines[i] ) );
		}
		return builder.toString();
	}

	private Component buildInfoComponent(String infoText) {
		int textSize = overlayTextSize(PixelDungeon.skillFontSize() ? DEFAULT_RENDERED_TEXT_SIZE : DEFAULT_HIGHLIGHTED_TEXT_SIZE);
		if (PixelDungeon.skillFontSize()) {
			RenderedTextMultiline info = PixelScene.renderMultiline( "", textSize );
			info.text( infoText, false );
			info.maxWidth(textWidth(windowWidth));
			return info;
		}

		HighlightedText info = new HighlightedText( textSize );
		info.text( infoText, textWidth(windowWidth), false );
		return info;
	}

}
