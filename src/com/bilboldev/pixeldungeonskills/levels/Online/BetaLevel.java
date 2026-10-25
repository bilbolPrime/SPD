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
package com.bilboldev.pixeldungeonskills.levels.Online;

import android.util.Log;

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Skeleton;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.ArcherMaidenHalo;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.online.OnlineSync;
import com.bilboldev.pixeldungeonskills.plants.Sungrass;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionStartScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ColdGirlSisterSprite;
import com.bilboldev.pixeldungeonskills.sprites.CursePersonificationSprite;
import com.bilboldev.pixeldungeonskills.sprites.LegendSprite;
import com.bilboldev.pixeldungeonskills.sprites.OnlineSprite;
import com.bilboldev.pixeldungeonskills.sprites.OnlineSpriteLoader;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.sprites.RedGirlSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierWarriorSprite;
import com.bilboldev.pixeldungeonskills.sprites.VanguardWarriorSprite;
import com.bilboldev.pixeldungeonskills.windows.PersistentWndOptions;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class BetaLevel extends Level {

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 8;

        Arrays.fill(fieldOfView, true);
	}

    public Connected connected;

	private static final int ROOM_LEFT		= WIDTH / 2 - 2;
	private static final int ROOM_RIGHT		= WIDTH / 2 + 2;
	private static final int ROOM_TOP		= HEIGHT / 2 - 2;
	private static final int ROOM_BOTTOM	= HEIGHT / 2 + 2;

	private int arenaDoor;
	private boolean enteredArena = false;
	private boolean keyDropped = false;

	@Override
	public String tilesTex() {
		if(!Difficulties.is3d)
			return Assets.TILES_CITY;

		return Assets.TILES_CITY_3D;
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

        try
        {
            ((Game)Game.mContext).BindSignarlR();
        }
        catch(Exception ex)
        {
            Log.e("", ex.getMessage());
        }

        int topMost = Integer.MAX_VALUE;

			int left, right, top, bottom;

				left = 5;
				right = WIDTH - 5;

				top = 5;
				bottom = HEIGHT - 5;

			Painter.fill( this, left, top, right - left + 1, bottom - top + 1, Terrain.EMPTY );

        exit = 0;

        Painter.fill( this, left, top, right - left + 1,  5, Terrain.WALL );
        Painter.fill( this, WIDTH * 5 + WIDTH / 2 - 2, 0, 5,  5, Terrain.EMPTY_SP );
        Painter.fill( this, right / 2 + 1, bottom / 3, 5,  1, Terrain.BARRICADE );
        Painter.fill( this, right / 2 + 3, top - 1, 1,  1, Terrain.DOOR );

		Painter.fill( this, ROOM_LEFT, ROOM_TOP + 1,
			ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP, Terrain.EMPTY );

        Painter.fill( this, 6,  bottom / 3 + 2, 6,  10, Terrain.WATER );
        Painter.fill( this, 7,  bottom / 3 + 3, 4,  8, Terrain.GRASS );
        Painter.fill( this, 6,  bottom / 3 + 6, 6,  2, Terrain.WATER );

        Painter.fill( this, 21,  bottom / 3 + 2, 6,  10, Terrain.WATER );
        Painter.fill( this, 22,  bottom / 3 + 3, 4,  8, Terrain.GRASS );
        Painter.fill( this, 21,  bottom / 3 + 6, 6,  2, Terrain.WATER );

		return true;
	}

    @Override
    public boolean[] updateFieldOfView( Char c ) {
        return fieldOfView;
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
        connected = new Connected();
        connected.pos = 0;
        mobs.add(connected);
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
		case Terrain.GRASS:
			return "Fluorescent moss";
		case Terrain.HIGH_GRASS:
			return "Fluorescent mushrooms";
		case Terrain.WATER:
			return "Freezing cold water.";
		default:
			return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
		case Terrain.ENTRANCE:
			return "The ladder leads up to the upper depth.";
		case Terrain.EXIT:
			return "The ladder leads down to the lower depth.";
		case Terrain.HIGH_GRASS:
			return "Huge mushrooms block the view.";
		case Terrain.WALL_DECO:
			return "A vein of some ore is visible on the wall. Gold?";
		default:
			return super.tileDesc( tile );
		}
	}

	@Override
	public void addVisuals( Scene scene )
    {

	}

    public class Connected extends Mob
    {
        public int internalClock = 0;

        {
            hostile = false;
            spriteClass = OnlineSpriteLoader.class;
        }

        @Override
        public boolean act()
        {
            OnlineSync.GetSingleton();
            sprite.visible = false;
            internalClock++;
            spend(1f);
            next();
            return true;
        }

        @Override
        public boolean attack( Char enemy ) {
            return true;
        }
    }
}
