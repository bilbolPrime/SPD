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

import java.util.List;
import java.util.LinkedHashSet;

import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Bones;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.RivalEncounter;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.levels.Room.Type;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Graph;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.Random;

public class PrisonBossLevel extends RegularLevel {

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
	}

	private Room anteroom;
	private int arenaDoor;

	private boolean enteredArena = false;
	private boolean keyDropped = false;

    private int[] rebornTrapPads = new int[0];

	@Override
	public String tilesTex() {
		if(!Difficulties.is3d)
			return Assets.TILES_PRISON;

		return Assets.TILES_PRISON_3D;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_PRISON;
	}

	private static final String ARENA	= "arena";
	private static final String DOOR	= "door";
	private static final String ENTERED	= "entered";
	private static final String DROPPED	= "droppped";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARENA, roomExit );
		bundle.put( DOOR, arenaDoor );
		bundle.put( ENTERED, enteredArena );
		bundle.put( DROPPED, keyDropped );
        if (rebornTrapPads.length > 0) bundle.put("reborn_trap_pads", rebornTrapPads);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		roomExit = (Room)bundle.get( ARENA );
		arenaDoor = bundle.getInt( DOOR );
		enteredArena = bundle.getBoolean( ENTERED );
		keyDropped = bundle.getBoolean( DROPPED );
        rebornTrapPads = bundle.getIntArray("reborn_trap_pads");
        if (rebornTrapPads == null) rebornTrapPads = new int[0];
	}

	@Override
	protected boolean build() {
        if (Dungeon.rules().hasRebornEncounters()) return buildReborn();

		initRooms();

		int distance;
		int retry = 0;

		do {

			if (retry++ > 10) {
				return false;
			}

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
			} while (
				roomExit == roomEntrance ||
				roomExit.width() < 7 ||
				roomExit.height() < 7 ||
				roomExit.top == 0);

			Graph.buildDistanceMap( rooms, roomExit );
			distance = Graph.buildPath( rooms, roomEntrance, roomExit ).size();

		} while (distance < 3);

		roomEntrance.type = Type.ENTRANCE;
		roomExit.type = Type.BOSS_EXIT;

		List<Room> path = Graph.buildPath( rooms, roomEntrance, roomExit );
		Graph.setPrice( path, roomEntrance.distance );

		Graph.buildDistanceMap( rooms, roomExit );
		path = Graph.buildPath( rooms, roomEntrance, roomExit );

		anteroom = path.get( path.size() - 2 );
		anteroom.type = Type.STANDARD;

		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
		}

		for (Room r : rooms) {
			if (r.type == Type.NULL && r.connected.size() > 0) {
				r.type = Type.PASSAGE;
			}
		}

		paint();

		Room r = (Room)roomExit.connected.keySet().toArray()[0];
		if (roomExit.connected.get( r ).y == roomExit.top) {
			return false;
		}

		paintWater();
		paintGrass();

		placeTraps();

		return true;
	}

    private boolean buildReborn() {
        rooms = new LinkedHashSet<>();
        roomEntrance = (Room)new Room().set(2, 22, 11, 30);
        anteroom = (Room)new Room().set(11, 22, 23, 30);
        Room passage = (Room)new Room().set(11, 13, 23, 22);
        roomExit = (Room)new Room().set(11, 3, 21, 13);
        roomEntrance.type = Type.ENTRANCE;
        anteroom.type = Type.STANDARD;
        passage.type = Type.PASSAGE;
        roomExit.type = Type.BOSS_EXIT;
        rooms.add(roomEntrance);
        rooms.add(anteroom);
        rooms.add(passage);
        rooms.add(roomExit);
        if (Random.Int(2) == 0) for (Room room : rooms) {
            int left = room.left;
            room.left = WIDTH-1-room.right;
            room.right = WIDTH-1-left;
        }
        roomEntrance.connect(anteroom);
        anteroom.connect(passage);
        passage.connect(roomExit);
        paint();
        paintWater();
        paintGrass();
        Painter.fill(this, roomExit, 1, Terrain.EMPTY);
        Painter.set(this, roomExit.left+3, roomExit.top+3, Terrain.WALL);
        Painter.set(this, roomExit.right-3, roomExit.bottom-3, Terrain.WALL);
        java.util.ArrayList<Integer> pads = new java.util.ArrayList<>();
        for (int y = roomExit.top + 2; y < roomExit.bottom - 1; y += 2)
            for (int x = roomExit.left + 2; x < roomExit.right - 1; x += 2) {
                int cell = x + y * WIDTH;
                if (map[cell] == Terrain.EMPTY) {
                    map[cell] = Terrain.INACTIVE_TRAP;
                    pads.add(cell);
                }
            }
        rebornTrapPads = new int[pads.size()];
        for (int i = 0; i < pads.size(); i++) rebornTrapPads[i] = pads.get(i);
        return true;
    }

    public void shuffleRebornTraps() {
        if (!Dungeon.rules().hasRebornEncounters() || rebornTrapPads.length == 0 || keyDropped) return;
        java.util.ArrayList<Integer> choices = new java.util.ArrayList<>();
        java.util.HashSet<Integer> previous = new java.util.HashSet<>();
        for (int cell : rebornTrapPads) {
            if (cell < 0 || cell >= LENGTH || !insideArena(cell)) continue;
            if (map[cell] == Terrain.POISON_TRAP) {
                previous.add(cell);
                Level.set(cell, Terrain.INACTIVE_TRAP);
                GameScene.updateMap(cell);
            }
            if (map[cell] == Terrain.INACTIVE_TRAP && Actor.findChar(cell) == null) choices.add(cell);
        }

        java.util.ArrayList<Integer> fresh = new java.util.ArrayList<>(choices);
        fresh.removeAll(previous);
        for (int i = 0; i < 4 && !choices.isEmpty(); i++) {
            int cell = Random.element(fresh.isEmpty() ? choices : fresh);
            choices.remove(Integer.valueOf(cell));
            fresh.remove(Integer.valueOf(cell));
            Level.set(cell, Terrain.POISON_TRAP);
            GameScene.updateMap(cell);
        }
    }

    public boolean insideArena(int cell) {
        return roomExit != null && roomExit.inside(cell);
    }

    public boolean hasRebornTraps() {
        return rebornTrapPads.length > 0;
    }

	protected boolean[] water() {
		return Patch.generate( 0.45f, 5 );
	}

	protected boolean[] grass() {
		return Patch.generate( 0.30f, 4 );
	}

	protected void paintDoors( Room r ) {
		for (Room n : r.connected.keySet()) {

			if (r.type == Type.NULL) {
				continue;
			}

			Point door = r.connected.get( n );

			if (r.type == Room.Type.PASSAGE && n.type == Room.Type.PASSAGE) {

				Painter.set( this, door, Terrain.EMPTY );

			} else {

				Painter.set( this, door, Terrain.DOOR );

			}

		}
	}

	@Override
	protected void placeTraps() {

		int nTraps = nTraps();

		for (int i=0; i < nTraps; i++) {

			int trapPos = Random.Int( LENGTH );

			if (map[trapPos] == Terrain.EMPTY) {
				map[trapPos] = Terrain.POISON_TRAP;
			}
		}
	}

	@Override
	protected void decorate() {

		for (int i=WIDTH + 1; i < LENGTH - WIDTH - 1; i++) {
			if (map[i] == Terrain.EMPTY) {

				float c = 0.15f;
				if (map[i + 1] == Terrain.WALL && map[i + WIDTH] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i - 1] == Terrain.WALL && map[i + WIDTH] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i + 1] == Terrain.WALL && map[i - WIDTH] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i - 1] == Terrain.WALL && map[i - WIDTH] == Terrain.WALL) {
					c += 0.2f;
				}

				if (Random.Float() < c) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}

		for (int i=0; i < WIDTH; i++) {
			if (map[i] == Terrain.WALL &&
				(map[i + WIDTH] == Terrain.EMPTY || map[i + WIDTH] == Terrain.EMPTY_SP) &&
				Random.Int( 4 ) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		for (int i=WIDTH; i < LENGTH - WIDTH; i++) {
			if (map[i] == Terrain.WALL &&
				map[i - WIDTH] == Terrain.WALL &&
				(map[i + WIDTH] == Terrain.EMPTY || map[i + WIDTH] == Terrain.EMPTY_SP) &&
				Random.Int( 2 ) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		while (true) {
			int pos = roomEntrance.random();
			if (pos != entrance && pos != storage) {
				map[pos] = Terrain.SIGN;
				break;
			}
		}

		Point door = roomExit.entrance();
		arenaDoor = door.x + door.y * WIDTH;
		Painter.set( this, arenaDoor, Terrain.LOCKED_DOOR );

        if (Dungeon.rules().hasRebornEncounters()) return;
		Painter.fill( this,
			roomExit.left + 2,
			roomExit.top + 2,
			roomExit.width() - 3,
			roomExit.height() - 3,
			Terrain.INACTIVE_TRAP );
	}

	@Override
	protected void createMobs() {
	}

	public Actor respawner() {
		return null;
	}

	@Override
	protected void createItems() {
		int keyPos = anteroom.random();
		while (!passable[keyPos]) {
			keyPos = anteroom.random();
		}
		drop( new IronKey(), keyPos ).type = Heap.Type.CHEST;

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
	public void press( int cell, Char ch ) {

		super.press( cell, ch );

		if ((ch == Dungeon.hero || LocalCoop.contains(ch)) && !enteredArena && roomExit.inside( cell )) {
			if (!gatherParty(ch, cell, arenaDoor)) return;

			enteredArena = true;

			int pos;
            if (Dungeon.rules().hasRebornEncounters()) {
                java.util.ArrayList<Integer> candidates = new java.util.ArrayList<>();
                for (int p = 0; p < LENGTH; p++)
                    if (insideArena(p) && passable[p] && !avoid[p] && Actor.findChar(p) == null
                            && Level.distance(p, cell) >= 3) candidates.add(p);
                if (candidates.isEmpty()) {
                    enteredArena = false;
                    return;
                }
                pos = Random.element(candidates);
            } else do {
				pos = roomExit.random();
			} while (pos == cell || Actor.findChar( pos ) != null);

			Mob boss = RivalEncounter.boss( Dungeon.depth );
			boss.state = boss.HUNTING;
			boss.pos = pos;
			GameScene.add( boss );
            RivalEncounter.enter(boss);
			boss.notice();
			shuffleRebornTraps();

			mobPress( boss );

			set( arenaDoor, Terrain.LOCKED_DOOR );
			GameScene.updateMap( arenaDoor );
			Dungeon.observe();
		}
	}

	@Override
	public Heap drop( Item item, int cell ) {

		if (!keyDropped && item instanceof SkeletonKey) {

			keyDropped = true;

			set( arenaDoor, Terrain.DOOR );
			GameScene.updateMap( arenaDoor );
			Dungeon.observe();
		}

		return super.drop( item, cell );
	}

	@Override
	public int randomRespawnCell() {
		return -1;
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
			return "Dark cold water.";
		default:
			return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.EMPTY_DECO:
			return "There are old blood stains on the floor.";
		default:
			return super.tileDesc( tile );
		}
	}

	@Override
	public void addVisuals( Scene scene ) {
		PrisonLevel.addVisuals( this, scene );
	}
}
