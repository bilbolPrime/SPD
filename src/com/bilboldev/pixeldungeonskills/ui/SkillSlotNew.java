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
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkillSpriteNew;

public class SkillSlotNew extends Button {

	public static final int DEGRADED	= 0xFF4444;
	public static final int UPGRADED	= 0x44FF44;
	public static final int WARNING		= 0xFF8800;

	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;

	protected SkillSpriteNew icon;
	protected BitmapText activeText;
	protected BitmapText levelText;

	Image activeImage;

	public SkillSlotNew() {
		super();
	}

	public SkillSlotNew(Skill skill) {
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
            icon.alpha( skill.level > 0 ? 0.9f : alpha);
        }



        if(skill.level > 0){
			levelText = new BitmapText(PixelScene.font1x);
			levelText.text(skill.level + "");
			add(levelText);
		}

		if( skill.useDelay != 0 &&  skill.availableAfter > skill.skillTrack){
			activeText = new BitmapText(PixelScene.font1x);
			activeText.text("" + (int)(skill.availableAfter - skill.skillTrack));
			activeText.hardlight( Window.TITLE_COLOR );
			add(activeText);
		}

		else if(skill.active) {
			activeText = new BitmapText(PixelScene.font1x);
			activeText.text("Active");
			activeText.hardlight( Window.TITLE_COLOR );
			//add(activeText);

			Image tmp =  Icons.get(Icons.SKILL_ACTIVE);
			activeImage  = new Image();
			activeImage.copy( tmp );
			activeImage.scale.x = 0.85f;
			activeImage.scale.y = 0.85f;
			add(activeImage);
		}

        layout();
	}
		
	@Override
	protected void createChildren() {
		
		super.createChildren();
		
		icon = new SkillSpriteNew();
		add( icon );

	}
	
	@Override
	protected void layout() {
		super.layout();
		
		icon.x = x + (width - icon.width) / 2;
		icon.y = y + (height - icon.height) / 2;
		
		if (activeText != null) {
            activeText.x = x + 2;
            activeText.y = y + 2;
		}

		if (levelText != null) {
			levelText.x = x + 7 - (levelText.text().length() > 1 ? 4 : 0);
			levelText.y = y + 0;
			levelText.alpha(0.8f);
		}

		if(activeImage != null){
			activeImage.alpha(0.8f);
			activeImage.scale.x = 0.9f;
			activeImage.scale.y = 0.9f;
			activeImage.x = x - 2;
			activeImage.y = y - 2;
		}
	}

	
	public void enable( boolean value ) {
		
		active = value;
		
		float alpha = value ? ENABLED : DISABLED;
		icon.alpha( alpha );
	}
}
