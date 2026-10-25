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

import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;

public class HealthIndicator extends Component {

	private static final float HEIGHT	= 2;

	public static HealthIndicator instance;

	private Char target;

	private Image bg;
	private Image level;
	private final Group sprites;
	private final MobHealth mobHealth = new MobHealth();
	private MobHealthBar bars;

	public HealthIndicator() {
		this(null);
	}

	public HealthIndicator(Group sprites) {
		this.sprites = sprites;
		instance = this;
	}

	@Override
	protected void createChildren() {
		bg = new Image( TextureCache.createSolid( 0xFFcc0000 ) );
		bg.scale.y = HEIGHT;
		add( bg );

		level = new Image( TextureCache.createSolid( 0xFF00cc00 ) );
		level.scale.y = HEIGHT;
		add( level );
	}

	@Override
	public void update() {
		super.update();
		if (Presentation.reborn() && sprites != null) {
			mobHealth.update(sprites, target, Game.elapsed);
			visible = true;
			return;
		}

		if (target != null && target.isAlive() && target.sprite.visible) {
			CharSprite sprite = target.sprite;
			bg.scale.x = sprite.width;
			level.scale.x = sprite.width * target.HP / target.HT;
			bg.x = level.x = sprite.x;
			bg.y = level.y = sprite.y - HEIGHT - 1;

			visible = true;
		} else {
			visible = false;
		}
	}

	public void target( Char ch ) {
		if (ch != null && ch.isAlive()) {
			target = ch;
		} else {
			target = null;
		}
	}

	public Char target() {
		return target;
	}

	public MobHealth mobHealth() {
		return mobHealth;
	}

	@Override
	public void draw() {
		if (!Presentation.reborn() || sprites == null) {
			super.draw();
			return;
		}
		if (bars == null) bars = new MobHealthBar();
		for (int i = 0; i < mobHealth.bars().size(); i++) {
			MobHealth.State bar = mobHealth.bars().get(i);
			if (!bar.isVisible()) continue;
			bars.draw(bar, camera(), bar.worldX, bar.worldY, 1, 1, bar.selected ? 1 : 0.5f);
		}
	}

	@Override
	public void destroy() {
		if (bars != null) bars.destroy();
		mobHealth.clear();
		if (instance == this) instance = null;
		super.destroy();
	}
}
