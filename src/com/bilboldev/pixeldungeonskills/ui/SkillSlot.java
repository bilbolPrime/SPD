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
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;

public class SkillSlot extends Button {

	public static final int DEGRADED	= 0xFF4444;
	public static final int UPGRADED	= 0x44FF44;
	public static final int WARNING		= 0xFF8800;

	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;

	protected SkillSprite icon;
	protected BitmapText activeText;




	public SkillSlot() {
		super();
	}

	public SkillSlot(Skill skill) {
		this();
        if (skill == null) {

            active = false;
            icon.visible  = false;

        }
        else {
            active = true;
            icon.visible  = true;

            icon.view(skill.image());

            float alpha = skill.getAlpha();
            icon.alpha( alpha );
        }

        if(skill.active) {
            activeText = new BitmapText(PixelScene.font1x);
            activeText.text("Active");
            activeText.hardlight( Window.TITLE_COLOR );
            add(activeText);
        }

        layout();
	}
		
	@Override
	protected void createChildren() {
		
		super.createChildren();
		
		icon = new SkillSprite();
		add( icon );


	}
	
	@Override
	protected void layout() {
		super.layout();
		
		icon.x = x + (width - icon.width) / 2;
		icon.y = y + (height - icon.height) / 2;
		
		if (activeText != null) {
            activeText.x = x + 3;
            activeText.y = y + 11;
		}
	}

	
	public void enable( boolean value ) {
		
		active = value;
		
		float alpha = value ? ENABLED : DISABLED;
		icon.alpha( alpha );
	}
}
