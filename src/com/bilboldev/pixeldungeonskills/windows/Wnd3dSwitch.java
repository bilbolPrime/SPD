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

import com.bilboldev.pixeldungeonskills.ui.Icons;

public class Wnd3dSwitch extends WndTitledMessage {

	private static final String TXT_TITLE_ON =  "2.5D Mode";
	private static final String TXT_TITLE_OFF =  "Classic Mode";
	private static final String TXT_MESSAGE_ON =
			"You are playing in 2.5D mode!\n \n"
			+ "- The game is given a \"3d rendered\" feel.\n"
			+ "- All sprites are raised and given shadows.\n"
			+ "- New Fog of War implementation.\n"
			+ "- Some kinks are still being worked on.\n"
			+ "- You can switch between modes from this screen (top). It is not tied to a save file. Change game modes at will.\n\n"
			+ "Warning: Some tiles (water and the like) will show inaccurate boundaries when loading a classic map in 2.5D mode. The effect is only visual.\n"
            + "\nPlease notify me of any malfuntion you encounter to fix asap.";
	private static final String TXT_MESSAGE_OFF =  "You are playing in classic mode!\n\n"
			+ "- You are playing in classic 2D mode.\n" +
			"- You can switch to 2.5D mode from this screen (top). It is not tied to a save file. Change game modes at will.";

	public Wnd3dSwitch(boolean on) {
		super( Icons.SKILLS.get(), on ? TXT_TITLE_ON : TXT_TITLE_OFF, on ? TXT_MESSAGE_ON : TXT_MESSAGE_OFF );
	}


	public static WndTitledMessage campaign3DWarning() {
		return new WndTitledMessage( Icons.SKILLS.get(), TXT_TITLE_OFF, "- Campaigns are only available in classic mode." );
	}
}
