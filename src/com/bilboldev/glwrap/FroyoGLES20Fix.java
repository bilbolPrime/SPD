/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2018 Evan Debenham
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

package com.bilboldev.glwrap;

@SuppressWarnings("JniMissingFunction")
public class FroyoGLES20Fix {

	static
	{
		System.loadLibrary("FroyoGLES20Fix");
	}

	native public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset);

	native public static void glDrawElements(int mode, int count, int type, int offset);

}
