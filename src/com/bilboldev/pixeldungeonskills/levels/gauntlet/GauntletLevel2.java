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

import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Bones;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Champ;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bat;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Brute;
import com.bilboldev.pixeldungeonskills.actors.mobs.Crab;
import com.bilboldev.pixeldungeonskills.actors.mobs.DM300;
import com.bilboldev.pixeldungeonskills.actors.mobs.Elemental;
import com.bilboldev.pixeldungeonskills.actors.mobs.Eye;
import com.bilboldev.pixeldungeonskills.actors.mobs.Gnoll;
import com.bilboldev.pixeldungeonskills.actors.mobs.Golem;
import com.bilboldev.pixeldungeonskills.actors.mobs.Goo;
import com.bilboldev.pixeldungeonskills.actors.mobs.King;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Monk;
import com.bilboldev.pixeldungeonskills.actors.mobs.Rat;
import com.bilboldev.pixeldungeonskills.actors.mobs.RatArcher;
import com.bilboldev.pixeldungeonskills.actors.mobs.RatBrute;
import com.bilboldev.pixeldungeonskills.actors.mobs.Scorpio;
import com.bilboldev.pixeldungeonskills.actors.mobs.Shaman;
import com.bilboldev.pixeldungeonskills.actors.mobs.Skeleton;
import com.bilboldev.pixeldungeonskills.actors.mobs.Spinner;
import com.bilboldev.pixeldungeonskills.actors.mobs.Succubus;
import com.bilboldev.pixeldungeonskills.actors.mobs.Swarm;
import com.bilboldev.pixeldungeonskills.actors.mobs.Tengu;
import com.bilboldev.pixeldungeonskills.actors.mobs.Thief;
import com.bilboldev.pixeldungeonskills.actors.mobs.Warlock;
import com.bilboldev.pixeldungeonskills.actors.mobs.Yog;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RatKing;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.levels.Patch;
import com.bilboldev.pixeldungeonskills.levels.PrisonLevel;
import com.bilboldev.pixeldungeonskills.levels.RegularLevel;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Room.Type;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndMercs;
import com.bilboldev.pixeldungeonskills.windows.WndRatKingGauntlet;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Graph;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.Rect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GauntletLevel2 extends RegularLevel {

	private static int RatKingTrack = 0;

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
	}
	
	private Room anteroom;
	private int arenaDoor;

	private HashSet<Room> spawnRooms;
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
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		roomExit = (Room)bundle.get( ARENA );
		arenaDoor = bundle.getInt( DOOR );
		enteredArena = bundle.getBoolean( ENTERED );
		keyDropped = bundle.getBoolean( DROPPED );
	}

	@Override
	protected boolean initRooms() {
		rooms = new HashSet<Room>();
		split( new Rect( 0, 0, WIDTH - 1, HEIGHT - 1 ) );

		if (rooms.size() < 8) {
			return false;
		}

		Room[] ra = rooms.toArray( new Room[0] );
		for (int i=0; i < ra.length-1; i++) {
			for (int j=i+1; j < ra.length; j++) {
				ra[i].addNeigbour( ra[j] );
			}
		}

		return true;
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
			
		} while (distance != 3);
		
		roomEntrance.type = Type.ENTRANCE;
		roomExit.type = Type.BOSS_EXIT;
		
		List<Room> path = Graph.buildPath( rooms, roomEntrance, roomExit );	
		Graph.setPrice( path, roomEntrance.distance );
		
		Graph.buildDistanceMap( rooms, roomExit );
		path = Graph.buildPath( rooms, roomEntrance, roomExit );

		HashSet<Room> connected = new HashSet<Room>();
		connected.add( roomEntrance );

		//anteroom = path.get( path.size() - 2 );
		//anteroom.type = Type.STANDARD;


		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
			connected.add( room );
		}

		int nConnected = 4;
		while (connected.size() < nConnected) {
			Room cr = Random.element( connected );
			Room or = Random.element( cr.neigbours );
			if(cr == roomEntrance || cr == roomExit){
				continue;
			}
			if (!connected.contains( or )) {
				cr.connect( or );
				connected.add( or );
			}
		}

		for (Room r : rooms) {
			if (r.type == Type.NULL && r.connected.size() > 0) {
				r.type = Type.PASSAGE;
			}
		}

		for (Room r : rooms) {
			if (r.type == Type.NULL || r.type == Type.PASSAGE) {
				int connections = r.connected.size();
				if (connections == 0) {

				} else if (Random.Int( connections * connections ) == 0) {
					r.type = Type.STANDARD;
				} else if(r.type == Type.NULL){
					r.type = Type.PASSAGE;
					//r.type = Type.TUNNEL;
				}
			}
		}

		paint();

		spawnRooms = connected;
		Room r = (Room)roomExit.connected.keySet().toArray()[0];
		if (roomExit.connected.get( r ).y == roomExit.top) {
			return false;
		}
		
		paintWater();
		paintGrass();
		
		placeTraps();
		
		return true;
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
			
			if (r.type == Type.PASSAGE && n.type == Type.PASSAGE) {
				
				Painter.set( this, door, Terrain.EMPTY );
				
			} else {
				
				Painter.set( this, door, Terrain.DOOR );
				
			}
			
		}
	}
	
	@Override
	protected void placeTraps() {


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

		map[storage] = Terrain.EMPTY;
		map[entrance] = Terrain.EMPTY;

		
		Point door = roomEntrance.entrance();
		arenaDoor = door.x + door.y * WIDTH;
		Painter.set( this, arenaDoor, Terrain.DOOR );
	}
	
	@Override
	protected void createMobs() {

		int pos = 0;
		do{pos = roomEntrance.random(); }while(pos == entrance);

		for (Mob mob : Dungeon.level.mobs) {
			if (mob instanceof RatKingGauntlet) {
				WandOfBlink.appear(mob, pos);
				return;
			}
		}


		Mob boss = new RatKingGauntlet();
		boss.state = boss.HUNTING;
		boss.pos = pos;
		mobs.add(boss);
	}
	
	public Actor respawner() {
		return null;
	}
	
	@Override
	protected void createItems() {

	}

	@Override
	public void press( int cell, Char ch ) {
		
		super.press( cell, ch );
		
		if (ch == Dungeon.hero && !enteredArena && !roomEntrance.inside( cell ) && arenaDoor != cell) {
			
			enteredArena = true;
			spawnMobs();


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
	public int randomRespawnCell(){
		return Dungeon.level.entrance;
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

	private void spawnMobs(){
		int pos;
		do {
			pos = roomExit.random();
		} while (Actor.findChar( pos ) != null);

		if(GauntletScene.level == 1){
			Mob mob = Bestiary.mob( 1 );
			mob.state = mob.WANDERING;
			mob.pos = pos;
			mob.HP = 1;
			mob.noLoot();
			GauntletScene.add( mob );
			mob.notice();
			mobPress( mob );
			return;
		}

		int nMobs = 1 + GauntletScene.level / 5 + GauntletScene.level / 3;


		boolean champ = false;

		for(int i = 0; i < nMobs; i++){

			int safety = 10;
			do {
				safety--;
				Room chosenRoom = (Room) Random.element( spawnRooms.toArray() );

				//if(chosenRoom == roomEntrance){
					chosenRoom = roomExit;
				//}

				pos = chosenRoom.random();
			} while (Actor.findChar( pos ) != null && safety > 0);


			Mob mob = mob( GauntletScene.level );

			if(GauntletScene.level > 5 && !champ){
				Buff.affect(mob, Champ.class);
				champ = true;
			}

			if(i % 4 == 0){
				champ = false;
			}


			mob.state = mob.WANDERING;
			mob.pos = pos;
			mob.noLoot();
			GauntletScene.add( mob );
			mob.notice();
			mobPress( mob );
		}
	}

	private static Mob mob( int depth ) {

		float[] chances= new float[]{ 1 };
		Class<?>[] classes= new Class<?>[]{ Rat.class };

		if(depth < 5){
			chances = new float[]{ 1, 0.1f };
			classes = new Class<?>[]{ Rat.class, RatArcher.class };
		}
		else if(depth < 10){
			chances = new float[]{ 1, 1f, 0.1f };
			classes = new Class<?>[]{ Rat.class, RatArcher.class, RatBrute.class};
		}
		else{
			chances = new float[]{ 1f, 1f, 1f };
			classes = new Class<?>[]{ Rat.class, RatArcher.class, RatBrute.class};
		}



		@SuppressWarnings("unchecked")
		Class<? extends Mob> cl = (Class<? extends Mob>)classes[ Random.chances( chances )];
		try {
			return cl.newInstance();
		} catch (Exception e) {
			return null;
		}
	}


	private class  RatKingGauntlet extends RatKing {
		int ratKingLevel = 0;
		{
			state = WANDERING;
			RatKingTrack = 0;
			ratKingLevel = GauntletScene.level;
		}

		@Override
		public void interact() {
			sprite.turnTo( pos, Dungeon.hero.pos );

			if(RatKingTrack < GauntletScene.level){
				itemWindow(GauntletScene.level == 1 ? WndRatKingGauntlet.Mode.INTRO : WndRatKingGauntlet.Mode.NEWLEVEL);
			}
			else {
				itemWindow( WndRatKingGauntlet.Mode.WAITING);
			}

			RatKingTrack = GauntletScene.level;
		}

		@Override
		public String description() {
			return
					"This rat is a little bigger than a regular marsupial rat " +
							"and it's wearing a tiny crown on its head.";
		}

		@Override
		protected boolean act() {
			spend(0.1f);

			if(Dungeon.hero.HP < 1 && HP > 0){
				HP = 0;
				die(null);
			}

			boolean allDead = true;
			for (Mob mob : Dungeon.level.mobs) {
				if (mob instanceof RatKingGauntlet) {
					continue;
				}
				allDead = false;

			}



			if(allDead && !keyDropped && enteredArena){
				keyDropped = true;
				Dungeon.level.drop( new SkeletonKey(), Dungeon.hero.pos ).sprite.drop();
				die(null);
			}

			return true;
		}

		private void itemWindow(WndRatKingGauntlet.Mode mode){
			GauntletScene.show(new WndRatKingGauntlet(mode));
		}
	}
}
