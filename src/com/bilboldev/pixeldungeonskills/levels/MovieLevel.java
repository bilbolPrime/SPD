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

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Bones;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.EnslavedSouls;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ColdGirlSisterSprite;
import com.bilboldev.pixeldungeonskills.sprites.CursePersonificationSprite;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
import com.bilboldev.pixeldungeonskills.sprites.RedGirlSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierWarriorSprite;
import com.bilboldev.pixeldungeonskills.sprites.VanguardWarriorSprite;
import com.bilboldev.pixeldungeonskills.sprites.WraithSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class MovieLevel extends Level {
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
		
		viewDistance = 8;

        Arrays.fill(fieldOfView, true);
	}

    public Maestro maestro;


	private static final int ROOM_LEFT		= WIDTH / 2 - 2;
	private static final int ROOM_RIGHT		= WIDTH / 2 + 2;
	private static final int ROOM_TOP		= HEIGHT / 2 - 2;
	private static final int ROOM_BOTTOM	= HEIGHT / 2 + 2;
	
	private int arenaDoor;
	private boolean enteredArena = false;
	private boolean keyDropped = false;
	
	@Override
	public String tilesTex() {
		return Assets.TILES_CITY;
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
		
		int topMost = Integer.MAX_VALUE;
		

			int left, right, top, bottom;

				left = 5;
				right = WIDTH - 5;


				top = 5;
				bottom = HEIGHT - 5;

			
			Painter.fill( this, left, top, right - left + 1, bottom - top + 1, Terrain.EMPTY );

        exit = 0;


		Painter.fill( this, ROOM_LEFT, ROOM_TOP + 1, 
			ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP, Terrain.EMPTY );

		







		
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

	}
	
	@Override
	protected void createMobs() {

        maestro = new Maestro();
        maestro.pos = 0;
        mobs.add(maestro);


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
	public void addVisuals( Scene scene ) {
		CavesLevel.addVisuals( this, scene );
	}

    public class MovieGirl extends Mob
    {


        {
            spriteClass = ColdGirlSisterSprite.class;
            state = HUNTING;
            hostile = false;
        }

        @Override
        public boolean attack( Char enemy ) {
            return true;
        }

        @Override
        public boolean act()
        {

            spend(1f);


            next();
            return true;
        }
    }

    public class MovieMaiden extends Mob
    {


        {
            spriteClass = RedGirlSprite.class;
            state = HUNTING;
            hostile = false;
        }

        public Char tmp = null;
        @Override
        public void onAttackComplete()
        {
            if(tmp != null)
                tmp.sprite.die();
            Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
        }

        @Override
        public boolean attack( Char enemy ) {
            tmp = enemy;
            return true;
        }

        @Override
        public boolean act()
        {
            spend(1f);


            next();
            return true;
        }
    }

    public class VanguardWarrior extends Mob
    {


        {
            spriteClass = VanguardWarriorSprite.class;
            state = HUNTING;
            hostile = false;
        }


        @Override
        public boolean attack( Char enemy ) {
            return true;
        }

        @Override
        public boolean act()
        {
            spend(1f);


            next();
            return true;
        }
    }

    public class SoldierWarrior extends Mob
    {


        {
            spriteClass = SoldierWarriorSprite.class;
            state = HUNTING;
            hostile = false;
        }


        @Override
        public boolean attack( Char enemy ) {
            return true;
        }

        @Override
        public boolean act()
        {
            spend(1f);


            next();
            return true;
        }
    }

    public class SkelEnemy extends Mob
    {


        {
            spriteClass = SkeletonSprite.class;
            state = HUNTING;
            hostile = false;
        }


        @Override
        public boolean attack( Char enemy ) {
            return true;
        }

        @Override
        public boolean act()
        {
            spend(1f);


            next();
            return true;
        }
    }

    public class WraithEnemy extends Mob
    {


        {
            spriteClass = CursePersonificationSprite.class;
            state = HUNTING;
            hostile = false;
        }


        @Override
        public boolean attack( Char enemy ) {
            return true;
        }

        @Override
        public boolean act()
        {
            spend(1f);


            next();
            return true;
        }
    }

    public class Maestro extends ColdGirl
    {
        static final int END_MOVIE = 632;
        int counter = 0;

        {
            hostile = false;
        }


        MovieGirl actress;
        MovieMaiden actress2;
        VanguardWarrior vanguard;
        SoldierWarrior soldier1, soldier2, soldier3, soldier4, soldier5;

        SkelEnemy skeleton1, skeleton2, skeleton3, skeleton4, skeleton5, skeleton6, skeleton7;

        ArrayList<WraithEnemy> listWraiths = new ArrayList<>();

        @Override
        public boolean act()
        {
            if(counter == 0)
            {
                /*
                actress = new MovieGirl();
                actress.pos = (HEIGHT + 1) * WIDTH / 2;
                GameScene.add(actress);

                */
                actress2 = new MovieMaiden();
                actress2.pos = (HEIGHT + 1) * WIDTH / 2 - WIDTH * 3;
                MissionScene.add(actress2);
                //actress2.turnToSis();


                vanguard = new VanguardWarrior();
                vanguard.pos  = (HEIGHT + 1) * WIDTH / 2 - WIDTH  - WIDTH;
                MissionScene.add(vanguard);

                soldier1 = new SoldierWarrior();
                soldier1.pos  = (HEIGHT + 1) * WIDTH / 2 - 2  - WIDTH;
                MissionScene.add(soldier1);

                soldier2 = new SoldierWarrior();
                soldier2.pos  = (HEIGHT + 1) * WIDTH / 2 - 1  - WIDTH;
                MissionScene.add(soldier2);

                soldier3 = new SoldierWarrior();
                soldier3.pos  = (HEIGHT + 1) * WIDTH / 2  - WIDTH;
                MissionScene.add(soldier3);

                soldier4 = new SoldierWarrior();
                soldier4.pos  = (HEIGHT + 1) * WIDTH / 2 + 1  - WIDTH;
                MissionScene.add(soldier4);

                soldier5 = new SoldierWarrior();
                soldier5.pos  = (HEIGHT + 1) * WIDTH / 2 + 2  - WIDTH;
                MissionScene.add(soldier5);

                skeleton1 = new SkelEnemy();
                skeleton1.pos  = (HEIGHT + 1) * WIDTH / 2 - 3 + 3 * WIDTH;
                MissionScene.add(skeleton1);

                skeleton2 = new SkelEnemy();
                skeleton2.pos  = (HEIGHT + 1) * WIDTH / 2 - 2 + 2 * WIDTH;
                MissionScene.add(skeleton2);

                skeleton3 = new SkelEnemy();
                skeleton3.pos  = (HEIGHT + 1) * WIDTH / 2 - 1 + 3 * WIDTH;
                MissionScene.add(skeleton3);

                skeleton4 = new SkelEnemy();
                skeleton4.pos  = (HEIGHT + 1) * WIDTH / 2 + 3 * WIDTH;
                MissionScene.add(skeleton4);

                skeleton5 = new SkelEnemy();
                skeleton5.pos  = (HEIGHT + 1) * WIDTH / 2 + 1 + 3 * WIDTH;
                MissionScene.add(skeleton5);

                skeleton6 = new SkelEnemy();
                skeleton6.pos  = (HEIGHT + 1) * WIDTH / 2 + 2 + 2 * WIDTH;
                MissionScene.add(skeleton6);

                skeleton7 = new SkelEnemy();
                skeleton7.pos  = (HEIGHT + 1) * WIDTH / 2 + 3 + 3 * WIDTH;
                MissionScene.add(skeleton7);

                sprite.visible = false;
                Music.INSTANCE.enable(true);
            }
            else
            {
                Camera.main.focusOn( vanguard.sprite );
            }



                if (counter == 100) {
                    soldier1.sprite.showStatus(CharSprite.NEUTRAL, "Too many!");
                    soldier5.sprite.showStatus(CharSprite.NEUTRAL, "Mommy...");
                    skeleton1.sprite.move(skeleton1.pos, skeleton1.pos - WIDTH);
                    skeleton3.sprite.move(skeleton3.pos, skeleton3.pos - WIDTH);
                    skeleton5.sprite.move(skeleton5.pos, skeleton5.pos - WIDTH);
                    skeleton7.sprite.move(skeleton7.pos, skeleton7.pos - WIDTH);

                    actress2.tmp = skeleton4;
                    actress2.sprite.attack(skeleton4.pos);
                }
                if (counter == 120)
                {
                    vanguard.sprite.showStatus(CharSprite.NEUTRAL, "Hold the line!");
                    skeleton1.sprite.move(skeleton1.pos, skeleton1.pos - 2 * WIDTH);
                    skeleton3.sprite.move(skeleton3.pos, skeleton3.pos - 2 * WIDTH);
                    skeleton5.sprite.move(skeleton5.pos, skeleton5.pos - 2 * WIDTH);
                    skeleton2.sprite.move(skeleton2.pos, skeleton2.pos -  WIDTH);
                    skeleton6.sprite.move(skeleton6.pos, skeleton6.pos -  WIDTH);
                    skeleton7.sprite.move(skeleton7.pos, skeleton7.pos - 2 * WIDTH);
                }
                if (counter == 125)
                {
                    //skeleton4.sprite.die();
                }
                if (counter == 130)
                {
                    skeleton1.sprite.move(skeleton1.pos, skeleton1.pos - 3 * WIDTH);
                    skeleton3.sprite.move(skeleton3.pos, skeleton3.pos - 3 * WIDTH);
                    skeleton5.sprite.move(skeleton5.pos, skeleton5.pos - 3 * WIDTH);
                    skeleton2.sprite.move(skeleton2.pos, skeleton2.pos - 2 *  WIDTH);
                    skeleton6.sprite.move(skeleton6.pos, skeleton6.pos - 2 * WIDTH);
                    skeleton7.sprite.move(skeleton7.pos, skeleton7.pos - 3 * WIDTH);
                }
                if (counter == 135)
                {
                    skeleton1.pos = skeleton1.pos - 3 * WIDTH;
                    skeleton1.sprite.attack(skeleton1.pos - WIDTH);
                    soldier1.sprite.die();
                    soldier1.sprite.bloodBurstA( sprite.center(), 10 );
                    Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                    soldier3.sprite.attack(soldier3.pos + WIDTH);
                    skeleton2.sprite.die();
                    Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                    actress2.sprite.attack(skeleton5.pos - 3 * WIDTH);
                    actress2.tmp = skeleton5;
                }
                if (counter == 155)
                {
                    skeleton1.sprite.move(skeleton1.pos, skeleton1.pos - WIDTH + 1);
                    skeleton1.pos = skeleton1.pos - WIDTH + 1;
                    vanguard.sprite.move(vanguard.pos, vanguard.pos - 2 + WIDTH);
                    vanguard.pos = vanguard.pos - 2 + WIDTH;
                }
                if(counter == 157)
                {
                    skeleton1.sprite.die();
                    Actor.addDelayed( new Pushing( skeleton1,  skeleton1.pos,  skeleton1.pos + WIDTH - 1 ), -1 );
                    Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                    skeleton7.pos = skeleton7.pos - 3 * WIDTH;
                    skeleton7.sprite.attack(soldier5.pos);
                    skeleton6.pos = skeleton6.pos - 2 * WIDTH;
                    soldier5.sprite.attack(skeleton6.pos);
                    skeleton6.sprite.die();
                    soldier5.sprite.die();
                    soldier5.sprite.bloodBurstA( sprite.center(), 10 );
                    Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                    Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                }
                if(counter == 160)
                {
                    actress2.sprite.attack(skeleton7.pos);
                    actress2.tmp = skeleton7;
                    vanguard.sprite.idle();
                    vanguard.sprite.showStatus(CharSprite.NEUTRAL, "Do not fail!");
                    skeleton2.pos =  skeleton2.pos - 2 *  WIDTH;
                    soldier3.sprite.attack(skeleton2.pos);
                    skeleton2.sprite.die();
                }

                if(counter == 182)
                {
                    //skeleton7.sprite.die();
                    skeleton3.pos = skeleton3.pos - 3 * WIDTH;
                    vanguard.sprite.attack(skeleton3.pos);
                    Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                    skeleton3.sprite.die();
                }

                if(counter == 220)
                {
                    WraithEnemy tmp = new WraithEnemy();
                    Sample.INSTANCE.play( Assets.SND_GHOST );
                    tmp.pos = actress2.pos;
                    MissionScene.add(tmp);
                    tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    soldier4.sprite.showStatus(CharSprite.NEUTRAL, "She was possessed!");
                    actress2.sprite.die();
                    listWraiths.add(tmp);
                }

                //if (counter == 200)
                   // actress.sprite.showStatus(CharSprite.NEUTRAL, "I have better hair");
                if (counter == 300)
                {
                    for(int i = -1; i < 4; i++)
                    {
                        WraithEnemy tmp = new WraithEnemy();
                        Sample.INSTANCE.play( Assets.SND_GHOST );
                        do {
                            tmp.pos = vanguard.pos - WIDTH - Random.Int(3) * WIDTH + i;
                        }while(tmp.pos == ((HEIGHT + 1) * WIDTH / 2));

                        MissionScene.add( tmp );
                        tmp.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
                        listWraiths.add(tmp);

                        tmp = new WraithEnemy();
                        Sample.INSTANCE.play( Assets.SND_GHOST );
                        do {
                            tmp.pos = vanguard.pos + WIDTH + Random.Int(3) * WIDTH + i;
                        }while(tmp.pos == ((HEIGHT + 1) * WIDTH / 2));

                        MissionScene.add( tmp );
                        tmp.sprite.emitter().burst( ShadowParticle.CURSE, 5 );

                        listWraiths.add(tmp);
                    }
                    soldier3.sprite.showStatus(CharSprite.NEUTRAL, "Too many!");
                    //actress.sprite.attack(pos - 1);
                   // Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
                }

                if(counter == 350)
                {
                    vanguard.sprite.showStatus(CharSprite.NEUTRAL, "It's been an ho...");
                }
                if(counter == 400)
                {
                  //  GameScene.flash( 0x0042ff );
                    actress = new MovieGirl();
                    actress.pos = (HEIGHT + 1) * WIDTH / 2;
                    MissionScene.add(actress);
                    actress.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
                    actress.sprite.showStatus(CharSprite.NEUTRAL, "A bit dramatic are we not now?");
                }
                if(counter == 450)
                {
                    soldier3.sprite.showStatus(CharSprite.NEUTRAL, "We're saved!");
                }
                if(counter == 470)
                {
                    vanguard.sprite.showStatus(CharSprite.NEUTRAL, "There's too man..");
                }
                if(counter == 490)
                {
                    Camera.main.shake( 5, 0.07f * (30) );
                 //   GameScene.flash( 0x0042ff );
                    ((ColdGirlSisterSprite)actress.sprite).haloUp();
                    for(int i = 0; i < listWraiths.size(); i++)
                        listWraiths.get(i).sprite.die();
                }
                if(counter == 550)
                {
                    actress.sprite.showStatus(CharSprite.NEUTRAL, "Fall back to the city");
                }
                if(counter == 600)
                {
                    vanguard.sprite.showStatus(CharSprite.NEUTRAL, "As you command");
                }
                if(counter == 630)
                {
                    vanguard.sprite.move(vanguard.pos, vanguard.pos - WIDTH);
                    soldier3.sprite.move(soldier3.pos, soldier3.pos - WIDTH);
                    soldier4.sprite.move(soldier4.pos, soldier4.pos - WIDTH);
                    soldier2.sprite.move(soldier2.pos, soldier2.pos - WIDTH);
                }


            // if (counter == 600)
                  //  actress.sprite.showStatus(CharSprite.NEUTRAL, "This is a private conversation.. go away");



            if(counter == END_MOVIE)
            {
                Music.INSTANCE.enable(PixelDungeon.music());
                InterlevelScene.mode = InterlevelScene.Mode.MOVIE_OUT;
                Game.switchScene(InterlevelScene.class);
                //Dungeon.observe();
            }

            counter++;
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
