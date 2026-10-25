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
package com.bilboldev.pixeldungeonskills.levels;

import java.util.ArrayList;
import java.util.List;

import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Bones;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.RivalEncounter;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalBoss;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Room.Type;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Graph;
import com.bilboldev.utils.Random;

public class SewerBossLevel extends RegularLevel {

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
	}

	private int stairs = 0;
    private int rebornGate;

	@Override
	public String tilesTex() {
		if(!Difficulties.is3d)
			return Assets.TILES_SEWERS;

		return Assets.TILES_SEWERS_3D;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_SEWERS;
	}

	@Override
	protected boolean build() {
		if (Dungeon.rules().hasRebornBosses()) return buildReborn();

		initRooms();

		int distance;
		int retry = 0;
		int minDistance = (int)Math.sqrt( rooms.size() );
		do {
			int innerRetry = 0;
			do {
				if (innerRetry++ > 10) {
					return false;
				}
				roomEntrance = Random.element( rooms );
			} while (roomEntrance.width() < 4 || roomEntrance.height() < 4);

			innerRetry = 0;
			do {
				if (innerRetry++ > 10) {
					return false;
				}
				roomExit = Random.element( rooms );
			} while (roomExit == roomEntrance || roomExit.width() < 6 || roomExit.height() < 6 || roomExit.top == 0);

			Graph.buildDistanceMap( rooms, roomExit );
			distance = roomEntrance.distance();

			if (retry++ > 10) {
				return false;
			}

		} while (distance < minDistance);

		roomEntrance.type = Type.ENTRANCE;
		roomExit.type = Type.BOSS_EXIT;

		Graph.buildDistanceMap( rooms, roomExit );
		List<Room> path = Graph.buildPath( rooms, roomEntrance, roomExit );

		Graph.setPrice( path, roomEntrance.distance );

		Graph.buildDistanceMap( rooms, roomExit );
		path = Graph.buildPath( rooms, roomEntrance, roomExit );

		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
		}

		room = (Room)roomExit.connected.keySet().toArray()[0];
		if (roomExit.top == room.bottom) {
			return false;
		}

		for (Room r : rooms) {
			if (r.type == Type.NULL && r.connected.size() > 0) {
				r.type = Type.TUNNEL;
			}
		}

		ArrayList<Room> candidates = new ArrayList<Room>();
		for (Room r : roomExit.neigbours) {
			if (!roomExit.connected.containsKey( r ) &&
				(roomExit.left == r.right || roomExit.right == r.left || roomExit.bottom == r.top)) {
				candidates.add( r );
			}
		}
		if (candidates.size() > 0) {
			Room kingsRoom = Random.element( candidates );
			kingsRoom.connect( roomExit );
			kingsRoom.type = Room.Type.RAT_KING;
		}

		paint();

		paintWater();
		paintGrass();

		placeTraps();

		return true;
	}

    private boolean buildReborn() {
        rooms = new java.util.HashSet<Room>();
        roomEntrance = new Room();
        roomEntrance.set(11, 21, 19, 27);
        roomEntrance.type = Type.ENTRANCE;
        roomExit = new Room();
        roomExit.set(8, 3, 22, 17);
        roomExit.type = Type.BOSS_EXIT;
        rooms.add(roomEntrance);
        rooms.add(roomExit);
        com.bilboldev.pixeldungeonskills.levels.painters.Painter.fill(this, roomExit, 1, Terrain.EMPTY);
        com.bilboldev.pixeldungeonskills.levels.painters.Painter.fill(this, roomEntrance, 1, Terrain.EMPTY);

        for (int y = 4; y <= 16; y++) for (int x = 9; x <= 21; x++) {
            int edge = Math.min(Math.min(x - 9, 21 - x), Math.min(y - 4, 16 - y));
            if ((x < 11 || x > 19) && (y < 6 || y > 14)) map[x + y * WIDTH] = Terrain.WALL;
            else if (edge == 0 || ((x == 11 || x == 19) && y >= 8 && y <= 12)) map[x + y * WIDTH] = Terrain.WATER;
        }
        for (int y = 15; y <= 23; y++) map[15 + y * WIDTH] = Terrain.EMPTY;
        rebornGate = 15 + 17 * WIDTH;
        map[rebornGate] = Terrain.DOOR;
        entrance = 15 + 25 * WIDTH;
        map[entrance] = Terrain.ENTRANCE;
        storage = 17 + 25 * WIDTH;
        map[storage] = Terrain.STORAGE;
        exit = 15 + 3 * WIDTH;
        map[exit] = Terrain.LOCKED_EXIT;
        return true;
    }

	protected boolean[] water() {
		return Patch.generate( 0.5f, 5 );
	}

	protected boolean[] grass() {
		return Patch.generate( 0.40f, 4 );
	}

	@Override
	protected void decorate() {
		int start = roomExit.top * WIDTH + roomExit.left + 1;
		int end = start + roomExit.width() - 1;
		for (int i=start; i < end; i++) {
			if (i != exit) {
				map[i] = Terrain.WALL_DECO;
				map[i + WIDTH] = Terrain.WATER;
			} else {
				map[i + WIDTH] = Terrain.EMPTY;
			}
		}

		while (true) {
			int pos = roomEntrance.random();
			if (pos != entrance && pos != storage) {
				map[pos] = Terrain.SIGN;
				break;
			}
		}
	}

	@Override
	public void addVisuals( Scene scene ) {
		SewerLevel.addVisuals( this, scene );
	}

	@Override
	protected void createMobs() {
		Mob mob = RivalEncounter.boss( Dungeon.depth );
		mob.pos = roomExit.random();
        if (rebornGate != 0) mob.pos = 15 + 8 * WIDTH;
		mobs.add( mob );
	}

	public Actor respawner() {
		return null;
	}

	@Override
	protected void createItems() {
		Item item = Bones.get();
		if (item != null) {
			int pos;
			do {
				pos = roomEntrance.random();
			} while (pos == entrance || map[pos] == Terrain.SIGN);
			drop( item, pos ).type = Heap.Type.SKELETON;
		}
	}

    @Override
    public void press(int cell, Char ch) {
        super.press(cell, ch);
        if (rebornGate != 0 && cell / WIDTH < rebornGate / WIDTH && ch instanceof Hero) {
            for (Mob mob : mobs) {
                if (mob instanceof RivalBoss) {
                    seal();
                    if (entrance == 0) ((RivalBoss)mob).enter();
                    break;
                }
            }
        }
    }

	public void seal() {
        Hero entering = Dungeon.hero;
        if (LocalCoop.active() && rebornGate != 0) {
            for (Hero hero : LocalCoop.heroes()) {
                if (hero.isAlive() && hero.pos / WIDTH < rebornGate / WIDTH) {
                    entering = hero;
                    break;
                }
            }
        }
        if (rebornGate != 0 && entering.pos / WIDTH >= rebornGate / WIDTH) return;
		if (entrance != 0) {
            if (!gatherParty(entering, entering.pos, rebornGate != 0 ? rebornGate : entrance)) return;
            if (rebornGate != 0) {
                set(rebornGate, Terrain.LOCKED_DOOR);
                GameScene.updateMap(rebornGate);
            }

			set( entrance, Terrain.WATER_TILES );
			GameScene.updateMap( entrance );
			GameScene.ripple( entrance );

			stairs = entrance;
			entrance = 0;
		}
	}

	public void unseal() {
        if (rebornGate != 0) {
            set(rebornGate, Terrain.DOOR);
            GameScene.updateMap(rebornGate);
        }
		if (stairs != 0) {

			entrance = stairs;
			stairs = 0;

			set( entrance, Terrain.ENTRANCE );
			GameScene.updateMap( entrance );
		}
	}

	private static final String STAIRS	= "stairs";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( STAIRS, stairs );
        if (rebornGate != 0) bundle.put("reborn_gate", rebornGate);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		stairs = bundle.getInt( STAIRS );
        rebornGate = bundle.getInt("reborn_gate");
        if (rebornGate != 0) for (Room room : rooms) {
            if (room.type == Type.ENTRANCE) roomEntrance = room;
            if (room.type == Type.BOSS_EXIT) roomExit = room;
        }
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
			return "Murky water";
		default:
			return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
		case Terrain.EMPTY_DECO:
			return "Wet yellowish moss covers the floor.";
		default:
			return super.tileDesc( tile );
		}
	}
}
