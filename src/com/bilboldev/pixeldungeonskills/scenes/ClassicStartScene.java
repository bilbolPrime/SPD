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

import java.util.EnumMap;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.BitmaskEmitter;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.effects.BannerSprites;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.Archs;
import com.bilboldev.pixeldungeonskills.ui.ExitButton;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;

public class ClassicStartScene extends PixelScene {
    private final EnumMap<HeroClass, ClassShield> shields = new EnumMap<>(HeroClass.class);
    private HeroClass selected;
    private RedButton play, gauntlet, party;
    private RenderedTextMultiline companion;

    @Override public void create() {
        super.create();
        Badges.loadGlobal();
        Difficulties.is3d = PixelDungeon.enabled3d();
        Music.INSTANCE.volume(1);
        Music.INSTANCE.play(Presentation.titleMusic(), true);
        uiCamera.visible = false;

        int w = Camera.main.width, h = Camera.main.height;
        boolean portrait = Game.height > Game.width;
        float width = Math.min(w - 16, portrait ? 116 : 224);
        float height = Math.min(h - 16, portrait ? 272 : 176);
        float left = (w - width) / 2, top = (h - height) / 2;
        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);
        Image title = BannerSprites.get(BannerSprites.Type.SELECT_YOUR_HERO);
        title.x = align((w - title.width()) / 2);
        title.y = align(top);
        add(title);

        play = new RedButton(Messages.get(StartScene.class, "play")) {
            @Override protected void onClick() { StartScene.beginStartFlow(ClassicStartScene.this, selected); }
        };
        play.setRect(left, top + height - 76, width, 24);
        add(play);
        gauntlet = new RedButton(Messages.get("gauntlet.title")) {
            @Override protected void onClick() { GauntletRun.start(); }
        };
        gauntlet.setRect(left, play.bottom() + 2, width, 24);
        add(gauntlet);
        party = new RedButton(Messages.get(StartScene.class, "party_mode")) {
            @Override protected void onClick() { StartScene.beginPartyFlow(ClassicStartScene.this, selected); }
        };
        party.setRect(left, gauntlet.bottom() + 2, width, 24);
        add(party);
        companion = PixelScene.renderMultiline("", 8);
        companion.maxWidth((int)width);
        companion.setPos(left, play.top() + 4);
        add(companion);

        float areaTop = title.y + title.height() + 6;
        float areaHeight = play.top() - areaTop - 6;
        for (int i = 0; i < TitleScene.HERO_CLASSES.length; i++) {
            HeroClass heroClass = TitleScene.HERO_CLASSES[i];
            ClassShield shield = new ClassShield(heroClass);
            shields.put(heroClass, shield);
            add(shield);
            if (portrait) {
                float cardWidth = (width - 18) / 2;
                float cardHeight = (areaHeight - 12) / 3;
                shield.setRect(left + (i / 3) * (cardWidth + 18),
                        areaTop + (i % 3) * (cardHeight + 6), cardWidth, cardHeight);
            } else {
                shield.setRect(left + i * width / 6, areaTop, width / 6, areaHeight);
            }
        }
        HeroClass initial = StartScene.pendingHero();
        if (StartScene.companionLeader() != null && !StartScene.choosingCompanion()) initial = StartScene.curClass;
        if (initial == null) {
            int last = Math.max(0, Math.min(HeroClass.values().length - 1, PixelDungeon.lastClass()));
            initial = HeroClass.values()[last];
        }
        updateClass(shields.containsKey(initial) ? initial : HeroClass.WARRIOR);
        refreshHeroes();
        ExitButton exit = new ExitButton() {
            @Override protected void onClick() { ClassicStartScene.this.onBackPressed(); }
        };
        exit.setPos(w - exit.width(), 0);
        add(exit);
        StartScene.ensureStartDialog(this);
        fadeIn();
    }

    private void updateClass(HeroClass heroClass) {
        if (selected != null) shields.get(selected).highlight(false);
        selected = StartScene.curClass = heroClass;
        PixelDungeon.lastClass(heroClass.ordinal());
        shields.get(heroClass).highlight(true);
    }

    void refreshHeroes() {
        HeroClass leader = StartScene.companionLeader();
        boolean choosingCompanion = StartScene.choosingCompanion();
        play.visible = play.active = leader == null;
        gauntlet.visible = gauntlet.active = leader == null;
        party.visible = party.active = leader == null;
        companion.visible = leader != null;
        if (leader != null) {
            String leaderName = Messages.capitalize(Messages.maybeTranslate(leader.title()));
            if (choosingCompanion) {
                companion.text(Messages.get(TitleScene.class, "companion_body", leaderName));
            } else {
                String partnerName = Messages.capitalize(Messages.maybeTranslate(selected.title()));
                companion.text(leaderName + " + " + partnerName + "\n\n" + Messages.get(TitleScene.class, "party_departure"));
            }
        }
        for (HeroClass hero : shields.keySet()) {
            ClassShield shield = shields.get(hero);
            shield.active = leader == null || (choosingCompanion && hero != leader);
            shield.highlight(hero == selected || hero == leader);
        }
    }

    @Override public void update() {
        super.update();
        StartScene.ensureStartDialog(this);
        StartScene.updateDeparture(Math.min(Game.elapsed, .1f));
    }

    @Override public void destroy() {
        StartScene.releaseWindow();
        Badges.saveGlobal();
        super.destroy();
    }

    @Override protected void onBackPressed() {
        HeroClass leader = StartScene.companionLeader();
        StartScene.cancelStartFlow();
        if (leader != null) {
            updateClass(leader);
            refreshHeroes();
            return;
        }
        PixelDungeon.switchNoFade(TitleScene.class);
    }

	private class ClassShield extends Button {

		private static final float MIN_BRIGHTNESS	= 0.6f;

		private static final int BASIC_NORMAL		= 0x444444;
		private static final int BASIC_HIGHLIGHTED	= 0xCACFC2;

		private static final int MASTERY_NORMAL		= 0x666644;
		private static final int MASTERY_HIGHLIGHTED= 0xFFFF88;

		private static final int WIDTH	= 24;
		private static final int HEIGHT	= 28;
		private static final float SCALE	= 1.8f;

		private HeroClass cl;

		private Image avatar;
		private BitmapText name;
		private Emitter emitter;

		private float brightness;

		private int normal;
		private int highlighted;

		public ClassShield( HeroClass cl ) {
			super();

			this.cl = cl;

			avatar.frame( avatarIndexForClass(cl) * WIDTH, 0, WIDTH, HEIGHT );
			avatar.scale.set( SCALE );

			if (Badges.isUnlocked( cl.masteryBadge() )) {
				normal = MASTERY_NORMAL;
				highlighted = MASTERY_HIGHLIGHTED;
			} else {
				normal = BASIC_NORMAL;
				highlighted = BASIC_HIGHLIGHTED;
			}

			name.text( classLabel( cl ) );
			name.measure();
			name.hardlight( normal );

			brightness = MIN_BRIGHTNESS;
			updateBrightness();
		}

		@Override
		protected void createChildren() {

			super.createChildren();

			avatar = new Image( Assets.AVATARS );
			add( avatar );

			name = PixelScene.createText( 9 );
			add( name );

			emitter = new BitmaskEmitter( avatar );
			add( emitter );
		}

		@Override
		protected void layout() {

			super.layout();

			avatar.scale.set(Math.min(SCALE, (width - 4) / WIDTH));
			avatar.x = align( x + (width - avatar.width()) / 2 );
			avatar.y = align( y + (height - avatar.height() - name.height()) / 2 );

			name.x = align( x + (width - name.width()) / 2 );
			name.y = avatar.y + avatar.height() + SCALE;
		}

		@Override
		protected void onTouchDown() {
			if (StartScene.companionLeader() != null && !StartScene.chooseCompanion(cl)) return;
			emitter.revive();
			emitter.start( Speck.factory( Speck.LIGHT ), 0.05f, 7 );

			Sample.INSTANCE.play( Assets.SND_CLICK, 1, 1, 1.2f );
			updateClass( cl );
			refreshHeroes();
		}

		@Override
		public void update() {
			super.update();

			if (brightness < 1.0f && brightness > MIN_BRIGHTNESS) {
				if ((brightness -= Game.elapsed) <= MIN_BRIGHTNESS) {
					brightness = MIN_BRIGHTNESS;
				}
				updateBrightness();
			}
		}

		public void highlight( boolean value ) {
			if (value) {
				brightness = 1.0f;
				name.hardlight( highlighted );
			} else {
				brightness = 0.999f;
				name.hardlight( normal );
			}

			updateBrightness();
		}

		private void updateBrightness() {
			avatar.gm = avatar.bm = avatar.rm = avatar.am = brightness;
		}

		private int avatarIndexForClass(HeroClass heroClass) {
			switch (heroClass) {
				case WARRIOR:
					return 0;
				case MAGE:
					return 1;
				case ROGUE:
					return 2;
				case HUNTRESS:
					return 3;
				case NECROMANCER:
					return 4;
				case MERCENARY:
					return 5;
				default:
					return 0;
			}
		}

		private String classLabel(HeroClass heroClass) {
			if ((Game.width > Game.height)) {
				if (heroClass == HeroClass.NECROMANCER) {
					return "necro";
				}
				if (heroClass == HeroClass.MERCENARY) {
					return "merc";
				}
			}
			return heroClass.title();
		}
	}

}
