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
package com.bilboldev.pixeldungeonskills.levels.gauntlet;

import android.util.Log;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Bones;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Online.BetaLevel;
import com.bilboldev.pixeldungeonskills.levels.Patch;
import com.bilboldev.pixeldungeonskills.levels.PrisonLevel;
import com.bilboldev.pixeldungeonskills.levels.RegularLevel;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Room.Type;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.online.OnlineSync;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.OnlineSpriteLoader;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Graph;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.Rect;

import java.util.HashSet;
import java.util.List;

public class GauntletLevel extends RegularLevel {

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
	}

	private static final int ROOM_LEFT		= WIDTH / 2 - 2;
	private static final int ROOM_RIGHT		= WIDTH / 2 + 2;
	private static final int ROOM_TOP		= HEIGHT / 2 - 2;
	private static final int ROOM_BOTTOM	= HEIGHT / 2 + 2;

	private Room anteroom;
	private int arenaDoor;
	
	private boolean enteredArena = false;
	private boolean keyDropped = false;

	@Override
	public String tilesTex() {
		if(!Difficulties.is3d)
			return Assets.TILES_PRISON;

		return Assets.TILES_PRISON_3D;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_CAVES;
	}

	private static final String DOOR	= "door";
	private static final String ENTERED	= "entered";
	private static final String DROPPED	= "droppped";

	@Override
	public void storeInBundle( Bundle bundle ) {

	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {

	}

	@Override
	protected boolean build() {

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

	@Override
	public boolean[] updateFieldOfView( Char c ) {
		return super.updateFieldOfView(c);
	}

	@Override
	protected void decorate() {

		for (int i=WIDTH + 1; i < LENGTH - WIDTH; i++) {
			if (map[i] == Terrain.EMPTY) {
				int n = 0;
				if (map[i+1] == Terrain.WALL) {
					n++;
				}
				if (map[i-1] == Terrain.WALL) {
					n++;
				}
				if (map[i+WIDTH] == Terrain.WALL) {
					n++;
				}
				if (map[i-WIDTH] == Terrain.WALL) {
					n++;
				}
				if (Random.Int( 8 ) <= n) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}


		for (int i=0; i < LENGTH; i++) {
			if (map[i] == Terrain.WALL && Random.Int( 8 ) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}

		for(int i = WIDTH * 10; i < WIDTH * 24; i += 2 * WIDTH)
		{
			map[i + WIDTH / 3 + 3] = Terrain.STATUE;
			map[i + 2 * WIDTH / 3 - 2] = Terrain.STATUE;
		}

		for(int i = WIDTH * 22 + 5; i < WIDTH * 22 + WIDTH / 3 + 4; i += 2)
			map[i] = Terrain.STATUE;

		for(int i = WIDTH * 22 + 2 * WIDTH / 3 - 2; i < WIDTH * 23 - 4; i += 2)
			map[i] = Terrain.STATUE;


	}

	@Override
	protected void createMobs() {

	}

	public Actor respawner() {
		return null;
	}

	@Override
	protected void createItems() {

	}

	@Override
	public int randomRespawnCell() {
		return (HEIGHT + 1) * WIDTH / 2;
	}

	@Override
	public void mobPress(Mob mob)
	{
		super.mobPress(mob);
	}

	@Override
	public void press( int cell, Char hero ) {
		super.press(cell, hero);
	}

	@Override
	public Heap drop( Item item, int cell ) {



		return super.drop( item, cell );
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



	protected boolean[] water() {
		return Patch.generate( 0.45f, 5 );
	}

	protected boolean[] grass() {
		return Patch.generate( 0.30f, 4 );
	}

}
