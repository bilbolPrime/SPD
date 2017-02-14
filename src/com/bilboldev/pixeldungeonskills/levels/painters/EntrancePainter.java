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
package com.bilboldev.pixeldungeonskills.levels.painters;

import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;

public class EntrancePainter extends Painter {

	public static void paint( Level level, Room room ) {
		
		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );
		
		for (Room.Door door : room.connected.values()) {
			door.set( Room.Door.Type.REGULAR );
		}

        int safety = 0;
        level.entrance = room.random( 1 );
        do {
            level.storage = room.random(2);
            safety ++;
        }
        while((level.storage == level.entrance || level.map[level.storage]== Terrain.SIGN) && safety < 100); // Still bugged, need a guaranteed way without risking infinte loops
		set( level, level.entrance, Terrain.ENTRANCE );

        for(int i = 0; i < Level.NEIGHBOURS8.length; i++)
        {
            if( level.map[level.entrance + Level.NEIGHBOURS8[i]] == Terrain.EMPTY)
                level.storage = level.entrance + Level.NEIGHBOURS8[i];
        }

        set(level, level.storage, Terrain.STORAGE);
	}
	
}
