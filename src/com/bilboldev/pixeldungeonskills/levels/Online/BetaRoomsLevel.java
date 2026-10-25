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

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.online.OnlineSync;
import com.bilboldev.pixeldungeonskills.sprites.OnlineSpriteLoader;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

import java.util.Arrays;

public class BetaRoomsLevel extends Level {

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 8;

        Arrays.fill(fieldOfView, true);
	}

    public Connected connected;

    private int LOCAL_WIDTH = 64;
    private int LOCAL_HEIGHT = 64;
	private static final int ROOM_LEFT		= WIDTH / 2 - 2;
	private static final int ROOM_RIGHT		= WIDTH / 2 + 2;
	private static final int ROOM_TOP		= HEIGHT / 2 - 2;
	private static final int ROOM_BOTTOM	= HEIGHT / 2 + 2;

	private int arenaDoor;
	private boolean enteredArena = false;
	private boolean keyDropped = false;

	@Override
	public String tilesTex() {
		return Assets.TILES_SEWERS;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_SEWERS;
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

				left = 1;
				right = WIDTH - 1;

				top = 0;
				bottom = HEIGHT - 1;

			Painter.fill( this, left, top, right - left + 1, bottom - top + 1, Terrain.EMPTY );

        exit = 0;

        Painter.fill( this, left, top, right - left + 1,  1, Terrain.WALL );

		Painter.fill( this, ROOM_LEFT, ROOM_TOP + 1,
			ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP, Terrain.EMPTY );
        Painter.fill( this, left, bottom, right - left + 1,  1, Terrain.WALL );
        Painter.fill( this, right, top, 1,  bottom, Terrain.WALL );

        Painter.fill( this, left, 8, right - left + 1,  1, Terrain.WALL );

        int offset = top;

        Painter.fill( this, right - 6, offset, 1,  8, Terrain.WALL );
        Painter.fill( this, right - 5, offset + 1, 5,  7, Terrain.EMPTY_SP );
        Painter.fill( this, right - 6, offset + 4, 1,  1, Terrain.DOOR );
        Painter.fill( this, right - 1, offset + 4, 1,  1, Terrain.PEDESTAL );
        Painter.fill( this, right - 5, offset + 1, 5,  1, Terrain.BOOKSHELF );
        Painter.fill( this, right - 5, offset + 7, 5,  1, Terrain.BOOKSHELF );
        Painter.fill( this, right - 3, offset + 7, 1,  1, Terrain.EMPTY_SP );
        Painter.fill( this, right - 3, offset + 8, 1,  1, Terrain.DOOR );

        Painter.fill( this, left, 16, right - left + 1,  1, Terrain.WALL );

        Painter.fill( this, left, 24, right - left + 1,  1, Terrain.WALL );

        offset += 8;
        Painter.fill( this, left + 5, offset, 1,  8, Terrain.WALL );
        Painter.fill( this, left, offset + 1, 5,  7, Terrain.WATER );
        Painter.fill( this, left + 5, offset + 4, 1,  1, Terrain.DOOR );

        Painter.fill( this, left, offset + 4, 1,  1, Terrain.PEDESTAL );
        Painter.fill( this, left + 1, offset + 4, 4,  1, Terrain.EMPTY_SP );
        Painter.fill( this, left + 1, offset + 4, 1,  4, Terrain.EMPTY_SP );
        Painter.fill( this, left + 1, offset + 8, 1,  1, Terrain.DOOR );

        offset += 8;
        Painter.fill( this, right - 6, offset, 1,  8, Terrain.WALL );
        Painter.fill( this, right - 5, offset + 1, 5,  7, Terrain.GRASS );
        Painter.fill( this, right - 6, offset + 4, 1,  1, Terrain.DOOR );

        Painter.fill( this, right - 1, offset + 4, 1,  1, Terrain.PEDESTAL );
        Painter.fill( this, right - 5, offset + 4, 4,  1, Terrain.EMPTY_SP );
        Painter.fill( this, right - 3, offset + 4, 1,  4, Terrain.EMPTY_SP );
        Painter.fill( this, right - 3, offset + 8, 1,  1, Terrain.DOOR );

		return true;
	}

    @Override
    public boolean[] updateFieldOfView( Char c ) {
        return fieldOfView;
    }

	@Override
	protected void decorate() {

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
            die(null);
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
