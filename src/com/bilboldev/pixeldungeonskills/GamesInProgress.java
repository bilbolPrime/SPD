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
package com.bilboldev.pixeldungeonskills;

import java.util.HashMap;

import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.utils.Bundle;

public class GamesInProgress {

	private static final HashMap<String, Info> state = new HashMap<String, Info>();

	public static Info check(HeroClass cl, int slot) {
		String filename = SaveSlots.gameFile(cl, slot);
		if (state.containsKey(filename)) return state.get(filename);
		Info info = null;
		if (SaveSlots.exists(cl, slot)) {
			info = new Info();
			try {
				Bundle bundle = Dungeon.gameBundle(filename);
				Dungeon.preview(info, bundle);
				if (HeroClass.restoreInBundle(bundle.getBundle("hero")) != cl)
					info.unavailable = true;
			} catch (Exception e) {

				info.unavailable = true;
			}
		}
		state.put(filename, info);
		return info;
	}

	public static int count(HeroClass cl) {
		int count = 0;
		for (int slot = SaveSlots.FIRST; slot <= SaveSlots.COUNT; slot++)
			if (check(cl, slot) != null) count++;
		return count;
	}

	public static void setUnknown(HeroClass cl, int slot) {
		state.remove(SaveSlots.gameFile(cl, slot));
	}

	public static class Info {
		public int depth;
		public int level;
		public int armorTier;
		public boolean challenges;
		public int difficulty;
		public RunRules rules = RunRules.CLASSIC;
		public boolean unavailable;
	}
}
