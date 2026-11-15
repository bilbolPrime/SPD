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

import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.OffBalance;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.utils.SparseArray;

public class BuffIndicator extends Component {

	public static final int NONE	= -1;

	public static final int MIND_VISION	= 0;
	public static final int LEVITATION	= 1;
	public static final int FIRE		= 2;
	public static final int POISON		= 3;
	public static final int PARALYSIS	= 4;
	public static final int HUNGER		= 5;
	public static final int STARVATION	= 6;
	public static final int SLOW		= 7;
	public static final int OOZE		= 8;
	public static final int AMOK		= 9;
	public static final int TERROR		= 10;
	public static final int ROOTS		= 11;
	public static final int INVISIBLE	= 12;
	public static final int SHADOWS		= 13;
	public static final int WEAKNESS	= 14;
	public static final int FROST		= 15;
	public static final int BLINDNESS	= 16;
	public static final int COMBO		= 17;
	public static final int FURY		= 18;
	public static final int HEALING		= 19;
	public static final int ARMOR		= 20;
	public static final int HEART		= 21;
	public static final int LIGHT		= 22;
	public static final int CRIPPLE		= 23;
	public static final int BARKSKIN	= 24;
	public static final int IMMUNITY	= 25;
	public static final int BLEEDING	= 26;
	public static final int MARK		= 27;
	public static final int DEFERRED	= 28;
	public static final int VERTIGO		= 29;
	public static final int RAGE		= 30;
	public static final int SACRIFICE	= 31;
	public static final int FOCUS = 32;
	public static final int MANA_RECOVERY = 33;
	public static final int OFF_BALANCE = 34;
    public static final int ESSENCE_FIRE = 35;
    public static final int ESSENCE_COLD = 36;
    public static final int ESSENCE_POISON = 37;
    public static final int ESSENCE_IRON = 38;
    public static final int ESSENCE_WARD = 39;

	public static final int SIZE	= 7;

	private static BuffIndicator heroInstance;

	private SmartTexture texture;
	private TextureFilm film;

	private SparseArray<Image> icons = new SparseArray<Image>();
	private Counter counter;

	private Char ch;

	public BuffIndicator( Char ch ) {
		super();

		this.ch = ch;
		if (ch == Dungeon.hero) {
			heroInstance = this;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		if (counter != null) counter.destroy();

		if (this == heroInstance) {
			heroInstance = null;
		}
	}

	@Override
	protected void createChildren() {
		texture = TextureCache.get( Assets.BUFFS_SMALL );
		film = new TextureFilm( texture, SIZE, SIZE );
	}

	@Override
	protected void layout() {
		clear();

		SparseArray<Image> newIcons = new SparseArray<Image>();

		for (Buff buff : ch.buffs()) {
			int icon = buff.icon();
			if (icon != NONE && newIcons.get(icon) == null) {
				Image img = new Image( texture );
				img.frame( film.get( icon ) );
				img.x = x + members.size() * (SIZE + 2);
				img.y = y;
				add( img );

				newIcons.put( icon, img );
			}
		}

		for (Integer key : icons.keyArray()) {
			if (newIcons.get( key ) == null) {
				Image icon = icons.get( key );
				icon.origin.set( SIZE / 2 );
				add( icon );
				add( new AlphaTweener( icon, 0, 0.6f ) {
					@Override
					protected void updateValues( float progress ) {
						super.updateValues( progress );
						image.scale.set( 1 + 5 * progress );
					};
				} );
			}
		}

		icons = newIcons;
	}

	@Override
	public void draw() {
		super.draw();
		if (counter == null) counter = new Counter();
		for (int i = 0; i < icons.size(); i++) {
			Image icon = icons.valueAt(i);
			counter.draw(turns(ch, icons.keyAt(i)), icons.keyAt(i), camera(), icon.x + SIZE / 2f,
					icon.y + SIZE + 1, SIZE + 1, 1, 1);
		}
	}

	static int turns(Char ch, int icon) {
		int turns = -1;
		for (Buff buff : ch.buffs()) {
			if (buff.icon() != icon) continue;
			int remaining = buff.remainingTurns();
			if (remaining < 0) return -1;
			turns = Math.max(turns, remaining);
		}
		return turns;
	}

	static final class Counter {
		static final int HEIGHT = 6;
		private final BitmapText[] digits = new BitmapText[10];

		Counter() {
			for (int i = 0; i < digits.length; i++) {
				digits[i] = new BitmapText(Integer.toString(i), PixelScene.font1x, false);
				digits[i].measure();
			}
		}

		void draw(int turns, int icon, Camera camera, float centerX, float y, float width, float scaleX, float scaleY) {
			if (turns < 0) return;
			turns = Math.min(turns, 99);
			int divisor = turns >= 10 ? 10 : 1;
			float textWidth = 1;
			for (int place = divisor; place > 0; place /= 10) {
				textWidth += digits[turns / place % 10].width - 1;
			}
			float fit = Math.min((float)HEIGHT / digits[0].height, width / textWidth);
			float x = centerX - textWidth * fit * scaleX / 2;
			for (int place = divisor; place > 0; place /= 10) {
				BitmapText digit = digits[turns / place % 10];
				digit.hardlight(icon == OFF_BALANCE && turns >= OffBalance.WARNING_STACKS ? 0xFF4444 : 0xEEEEDE);
				digit.camera = camera;
				digit.scale.set(fit * scaleX, fit * scaleY);
				digit.x = PixelScene.align(camera, x);
				digit.y = PixelScene.align(camera, y);
				digit.draw();
				x += (digit.width - 1) * fit * scaleX;
			}
		}

		void destroy() {
			for (BitmapText digit : digits) digit.destroy();
		}
	}

	public static void refreshHero() {
		if (heroInstance != null && heroInstance.ch == Dungeon.hero) {
			heroInstance.layout();
		}
	}

	public void rebind(Char ch) {
		this.ch = ch;
		java.util.HashSet<Integer> current = new java.util.HashSet<>();
		for (Buff buff : ch.buffs()) if (buff.icon() != NONE) current.add(buff.icon());
		if (current.size() != icons.size()) {
			layout();
			return;
		}
		for (int icon : current) {
			if (icons.get(icon) == null) {
				layout();
				return;
			}
		}
	}
}
