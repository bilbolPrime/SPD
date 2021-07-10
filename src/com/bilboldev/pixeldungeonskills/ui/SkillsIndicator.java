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
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;

public class SkillsIndicator extends Component {

	private static final float TIME	= 2f;
	
	private int lastValue = 0;
	
	private BitmapText tf;
	
	private float time;

	public boolean skillCooldown = false;

	@Override
	protected void createChildren() {
		tf = new BitmapText( PixelScene.font1x );
		tf.hardlight( 0xFFFF00 );
		add( tf );
		
		visible = false;
	}
	
	@Override
	protected void layout() {
		tf.x = x + (width - tf.width()) / 2;
		tf.y = bottom() - tf.height() - 7;
	}
	
	@Override
	public void update() {
		super.update();

		if(!skillCooldown){
			if (Skill.availableSkill != lastValue) {

				lastValue = Skill.availableSkill;

				tf.text( Integer.toString( lastValue ) );
				tf.measure();

				visible = lastValue != 0;
				//time = TIME;

				layout();
			}
		}
		else {
			try
			{
				if (Dungeon.hero.heroSkills.lastUsed != null && Dungeon.hero.heroSkills.lastUsed.coolDown()) {
					if(Dungeon.hero.heroSkills.lastUsed.availableAfter - Skill.skillTrack != lastValue){
						lastValue = (int) Dungeon.hero.heroSkills.lastUsed.availableAfter - (int) Skill.skillTrack;

						tf.text( "CD:" + Integer.toString( lastValue ) );
						tf.measure();


						//time = TIME;

						layout();
					}
					visible = true;
				}
				else{
					visible = false;
				}
			}
			catch (Exception e){

			}
		}
	}
}
