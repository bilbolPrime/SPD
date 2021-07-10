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
package com.bilboldev.pixeldungeonskills.levels.Campaigns;

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
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.ElmoParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Longsword;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Sword;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.painters.Painter;
import com.bilboldev.pixeldungeonskills.plants.Sungrass;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionStartScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ColdGirlSisterSprite;
import com.bilboldev.pixeldungeonskills.sprites.CursePersonificationSprite;
import com.bilboldev.pixeldungeonskills.sprites.LegendSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.sprites.RedGirlSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierArcherSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierThiefSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierWarriorSprite;
import com.bilboldev.pixeldungeonskills.sprites.VanguardWarriorSprite;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.windows.PersistentWndOptions;
import com.bilboldev.pixeldungeonskills.windows.WndTitledMessage;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class Betrayal extends Level {
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
		
		viewDistance = 8;

        Arrays.fill(fieldOfView, true);
	}

	public static int gold;
    public static int killCount;

    public Maestro maestro;
    public EnemyAI enemyAI;

	private static final int ROOM_LEFT		= WIDTH / 2 - 2;
	private static final int ROOM_RIGHT		= WIDTH / 2 + 2;
	private static final int ROOM_TOP		= HEIGHT / 2 - 2;
	private static final int ROOM_BOTTOM	= HEIGHT / 2 + 2;
	
	private int arenaDoor;
	private boolean enteredArena = false;
	private boolean keyDropped = false;

	public static boolean hireThief = false;
    public static boolean hireArcher = false;

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
     // Painter.fill( this, right / 2 + 1, bottom / 3, 5,  1, Terrain.BARRICADE );
      Painter.fill( this, right / 2 + 3, top - 1, 1,  1, Terrain.DOOR );

		Painter.fill( this, ROOM_LEFT, ROOM_TOP + 1,
			ROOM_RIGHT - ROOM_LEFT + 1, ROOM_BOTTOM - ROOM_TOP, Terrain.EMPTY );


    //  Painter.fill( this, 6,  bottom / 3 + 2, 6,  10, Terrain.WATER );
    //  Painter.fill( this, 7,  bottom / 3 + 3, 4,  8, Terrain.GRASS );
    //  Painter.fill( this, 6,  bottom / 3 + 6, 6,  2, Terrain.WATER );


    //  Painter.fill( this, 21,  bottom / 3 + 2, 6,  10, Terrain.WATER );
    //  Painter.fill( this, 22,  bottom / 3 + 3, 4,  8, Terrain.GRASS );
    //  Painter.fill( this, 21,  bottom / 3 + 6, 6,  2, Terrain.WATER );



        Painter.fill( this, 5,  bottom / 2 + 3, 23,  12, Terrain.EMBERS );

		
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

        maestro = new Maestro();
        maestro.pos = 0;
        mobs.add(maestro);

        enemyAI = new EnemyAI();
        enemyAI.pos = 0;
        mobs.add(enemyAI);
	}
	
	public Actor respawner() {
		return null;
	}
	
	@Override
	protected void createItems() {

	}
	
	@Override
	public int randomRespawnCell() {
		return WIDTH * 9 + WIDTH / 2;
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

    public class MovieGirl extends Mob
    {


        {
            spriteClass = ColdGirlSisterSprite.class;
            state = HUNTING;
            hostile = false;
            screams = false;
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
            screams = false;
        }

        public Char tmp = null;
        @Override
        public void onAttackComplete()
        {
            if(tmp != null)
                tmp.die(null);
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
            EXP = 0;
            screams = false;
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
            EXP = 0;
            screams = false;
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
            EXP = 0;
            screams = false;
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
            screams = false;
            EXP = 0;
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


    public class HostileSkeleton extends Skeleton
    {
        {
            EXP = 0;
            state = WANDERING;
            screams = false;
        }

        @Override
        public boolean act()
        {
            if(MissionScene.scenePause == true)
            {
                spend(1f);
                next();
                return false;
            }
            return super.act();
        }

        @Override
        public void die(Object reason)
        {
            super.die(reason);
            Betrayal.this.enemyAI.enemyCount--;
            killCount++;
            Betrayal.this.enemyAI.enemyKilled++;
            dropLoot();

            if(killCount == 10){
                for(int i = 0; i < 20; i++)
                {
                    Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                    tmp.pos = getSpawnLocation();
                    MissionScene.add(tmp);
                    tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    Betrayal.this.enemyAI.enemyCount++;
                }
            }

            if(killCount == 30){
                for(int i = 0; i < 20; i++)
                {
                    Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                    tmp.pos = getSpawnLocation();
                    MissionScene.add(tmp);
                    tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    Betrayal.this.enemyAI.enemyCount++;
                }
            }

            if(killCount == 50){
                for(int i = 0; i < 20; i++)
                {
                    Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                    tmp.pos = getSpawnLocation();
                    MissionScene.add(tmp);
                    tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    Betrayal.this.enemyAI.enemyCount++;
                }
            }


            if(killCount == 70){
                for(int i = 0; i < 20; i++)
                {
                    Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                    tmp.pos = getSpawnLocation();
                    MissionScene.add(tmp);
                    tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    Betrayal.this.enemyAI.enemyCount++;
                }
            }

            if(killCount == 100 && Dungeon.hero.HP > 0)
            {
                Betrayal.this.maestro.endScenario();
            }
        }

        @Override
        protected void dropLoot()
        {
            Gold tmp = new Gold();
            tmp = (Gold)tmp.random();
            Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "+" + tmp.quantity() + " Gold!");
            Dungeon.gold += tmp.quantity();
            gold += tmp.quantity();
        }
    }

    public class EnemyAI extends Mob
    {

        public int enemyCount = 0;
        public int enemyKilled = 0;
        public int internalClock = 0;
        public int lastAction = 0;

        public final int INTER_ACTION_TIME = 10;
        public final int ENEMY_COUNT_LIMIT = 10;

        {
            hostile = false;
            spriteClass = RatSprite.class;
        }

        public boolean temariAdded = false;
        public boolean generalAdded = false;

        public boolean spawnEnemies = true;

        public void finalWave()
        {
            spawnEnemies = false;
            for(int i = 0; i < 20; i++)
            {
                HostileSkeleton tmp = new HostileSkeleton();
                tmp.pos = getSpawnLocation();
                MissionScene.add(tmp);
                tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                Betrayal.this.enemyAI.enemyCount++;
            }
        }

        @Override
        public boolean act()
        {
            if(internalClock == 0)
            {
                sprite.visible = false;
            }


            internalClock++;
            spend(1f);
            next();
            return true;
        }

        int getSpawnLocation()
        {
            int tmp = (HEIGHT * WIDTH) - 5 * WIDTH + WIDTH / 3 +  Random.Int(WIDTH / 3);
            int safety = 0;
            while(Actor.findChar(tmp) != null && safety < 5)
            {
                tmp = (HEIGHT * WIDTH) - 5 * WIDTH + WIDTH / 3 +  Random.Int(WIDTH / 3);
                safety++; // Final wave will overlap
            }
            return tmp;
        }

        @Override
        public boolean attack( Char enemy ) {
            return true;
        }
    }

    public class CitySoldier extends HiredMerc
    {
        {
            mercType = MERC_TYPES.Brute;
            spriteClass = SoldierWarriorSprite.class;
            hackFix = true;
            screams = false;
        }



        @Override
        public CharSprite sprite() {
            CharSprite s = super.sprite();

            return s;
        }

        @Override
        public boolean act()
        {
            if(MissionScene.scenePause == true)
            {
                spend(1f);
                next();
                return false;
            }

            return super.act();
        }

        public void initStats()
        {
            HP = HT = 40;
            level = 10;
            mercType.setSkills(this);
            name = "Militia";
            baseSpeed = 0.5f;
            defenseSkill = 35;
        }



        @Override
        public void die(Object cause)
        {
            super.die(cause);
        }

        public void speak(String say)
        {
            sprite.showStatus(CharSprite.NEUTRAL, say);
        }

        @Override
        public String description() {
            return
                    "A volunteer defending the city of Boonamai.";
        }
    }

    public class CityThief extends HiredMerc
    {
        {
            mercType = MERC_TYPES.Thief;
            spriteClass = SoldierThiefSprite.class;
            hackFix = true;
            screams = false;
        }



        @Override
        public CharSprite sprite() {
            CharSprite s = super.sprite();

            return s;
        }

        @Override
        public boolean act()
        {
            if(MissionScene.scenePause == true)
            {
                spend(1f);
                next();
                return false;
            }

            return super.act();
        }

        public void initStats()
        {
            HP = HT = 60;
            level = 10;
            mercType.setSkills(this);
            name = "Thief";
            baseSpeed = 2f;
            defenseSkill = 150;
            weapon = new Longsword().identify();
        }



        @Override
        public void die(Object cause)
        {
            weapon = null;
            super.die(cause);
        }

        public void speak(String say)
        {
            sprite.showStatus(CharSprite.NEUTRAL, say);
        }

        @Override
        public String description() {
            return
                    "A mercenary thief... does not look innocent at all";
        }
    }

    public class CityArcher extends HiredMerc
    {
        {
            mercType = MERC_TYPES.ArcherMaiden;
            spriteClass = SoldierArcherSprite.class;
            hackFix = true;
            screams = false;
        }



        @Override
        public CharSprite sprite() {
            CharSprite s = super.sprite();

            return s;
        }

        @Override
        public boolean act()
        {
            if(MissionScene.scenePause == true)
            {
                spend(1f);
                next();
                return false;
            }

            return super.act();
        }

        public void initStats()
        {
            HP = HT = 40;
            level = 10;
            mercType.setSkills(this);
            name = "Archer";
            baseSpeed = 1.5f;
            defenseSkill = 125;
            weapon = new Sword().identify();
        }



        @Override
        public void die(Object cause)
        {
              weapon = null;
              super.die(cause);
        }

        public void speak(String say)
        {
            sprite.showStatus(CharSprite.NEUTRAL, say);
        }

        @Override
        public String description() {
            return
                    "A mercenary archer... I don't like her..";
        }
    }


    public class General extends HiredMerc
    {
        public boolean immortal = true;
        {
            mercType = MERC_TYPES.Brute;
            spriteClass = VanguardWarriorSprite.class;
            hackFix = true;
            screams = false;
        }

        public  boolean hasHalo = false;

        @Override
        public CharSprite sprite() {
            CharSprite s = super.sprite();

            return s;
        }

        @Override
        public boolean act()
        {
            if(MissionScene.scenePause == true)
            {
                spend(1f);
                next();
                return false;
            }

            return super.act();
        }

        public void initStats()
        {
            HP = HT = 100;
            level = 100;
            mercType.setSkills(this);
            name = "General";

            defenseSkill = 50;
        }

        public void haloUp()
        {

            if(hasHalo)
                return;
            hasHalo = true;
            sprite.add(CharSprite.State.ARCHERMAIDEN);
            GameScene.effect(sprite.archerMaidenHalo = new ArcherMaidenHalo(sprite));
        }

        @Override
        public void die(Object cause)
        {
            if(immortal){
                HP = 1;
                speak("I cannot fail");
            }
            else{
                weapon = null;
                sprite.archerMaidenHalo.putOut();
                super.die(cause);
            }
        }

        public void speak(String say)
        {
            sprite.showStatus(CharSprite.NEUTRAL, say);
        }

        @Override
        public String description() {
            return
                    "The general leading the army of Boonamai.";
        }
    }

    public class Temari extends HiredMerc
    {
        {
            mercType = MERC_TYPES.ArcherMaiden;
            screams = false;
        }

        public  boolean hasHalo = false;

        @Override
        public boolean act()
        {
            if(MissionScene.scenePause == true)
            {
                spend(1f);
                next();
                return false;
            }

            return super.act();
        }

        public void initStats()
        {
            HP = HT = 100;
            level = 100;
            mercType.setSkills(this);
            name = "Temari";
            defenseSkill = 30;
        }

        public void haloUp()
        {
            if(hasHalo)
                return;
            hasHalo = true;
            sprite.add(CharSprite.State.ARCHERMAIDEN);
            GameScene.effect(sprite.archerMaidenHalo = new ArcherMaidenHalo(sprite));
        }

        @Override
        public void die(Object cause)
        {
            HP = 1;
            speak("You enjoying this Hatsune?");
        }

        public void speak(String say)
        {
            sprite.showStatus(CharSprite.NEUTRAL, say);
        }

        @Override
        public String description() {
            return
                   "The only mercenary to earn Hatsune's trust.";
        }
    }

    public class Maestro extends ColdGirl
    {
        int phase = -1;
        static final int END_MOVIE = 720;
        int counter = 0;

        {
            hostile = false;
        }


        Char centerOfAttention = null;
        MovieGirl actress;
        Temari temari;
        General general;
        MovieMaiden actress2;
        VanguardWarrior vanguard;
        SoldierWarrior soldier1, soldier2, soldier3, soldier4, soldier5;

        SkelEnemy skeleton1, skeleton2, skeleton3, skeleton4, skeleton5, skeleton6, skeleton7;

        ArrayList<WraithEnemy> listWraiths = new ArrayList<>();

        public void setCenterOfAttention(Char who)
        {
            centerOfAttention = who;
        }

        public void endScenario()
        {
            MissionScene.scenePause = true;
            for (Mob mob : (Iterable<Mob>)Dungeon.level.mobs.clone()) {
                if (mob.hostile == true || mob instanceof SummonedPet || mob instanceof CitySoldier || mob instanceof CityThief || mob instanceof CityArcher) {
                    mob.die( null );
                    continue;
                }
                mob.sprite.idle();
                Dungeon.hero.sprite.idle();
            }


            Dungeon.hero.pos = (HEIGHT + 1) * WIDTH / 2 + WIDTH - 1;
            Dungeon.hero.sprite.place( Dungeon.hero.pos  );

            temari.pos = (HEIGHT + 1) * WIDTH / 2 -  WIDTH;
            temari.sprite.place(temari.pos  );

            general.pos = (HEIGHT + 1) * WIDTH / 2 - 3 * WIDTH ;
            general.sprite.place(general.pos  );

            nextPhase();
        }

        @Override
        public boolean act()
        {
            if(hireThief){
                hireThief = false;
                Betrayal.CityThief tmp = new Betrayal.CityThief();
                tmp.pos =  WIDTH * 9 + WIDTH / 2 - WIDTH * 5;
                tmp.initStats();
                MissionScene.add(tmp);
            }

            if(hireArcher){
                hireArcher = false;
                Betrayal.CityArcher tmp = new Betrayal.CityArcher();
                tmp.pos =  WIDTH * 9 + WIDTH / 2 - WIDTH * 5;
                tmp.initStats();
                MissionScene.add(tmp);
            }

            if(phase == -1){
                nextPhase();
            }
            if(phase == 0) {
                if (counter == 0) {


                //  actress2 = new MovieMaiden();
                //  actress2.pos = (HEIGHT + 1) * WIDTH / 2 - WIDTH * 3;
                //  MissionScene.add(actress2);
                //  //actress2.turnToSis();


                //  vanguard = new VanguardWarrior();
                //  vanguard.pos = (HEIGHT + 1) * WIDTH / 2 - WIDTH - WIDTH;
                //  MissionScene.add(vanguard);

                //  soldier1 = new SoldierWarrior();
                //  soldier1.pos = (HEIGHT + 1) * WIDTH / 2 - 2 - WIDTH;
                //  MissionScene.add(soldier1);

                //  soldier2 = new SoldierWarrior();
                //  soldier2.pos = (HEIGHT + 1) * WIDTH / 2 - 1 - WIDTH;
                //  MissionScene.add(soldier2);

                //  soldier3 = new SoldierWarrior();
                //  soldier3.pos = (HEIGHT + 1) * WIDTH / 2 - WIDTH;
                //  MissionScene.add(soldier3);

                //  soldier4 = new SoldierWarrior();
                //  soldier4.pos = (HEIGHT + 1) * WIDTH / 2 + 1 - WIDTH;
                //  MissionScene.add(soldier4);

                //  soldier5 = new SoldierWarrior();
                //  soldier5.pos = (HEIGHT + 1) * WIDTH / 2 + 2 - WIDTH;
                //  MissionScene.add(soldier5);

                ////  skeleton1 = new SkelEnemy();
                ////  skeleton1.pos = (HEIGHT + 1) * WIDTH / 2 - 3 + 3 * WIDTH;
                ////  MissionScene.add(skeleton1);

                //  skeleton2 = new SkelEnemy();
                //  skeleton2.pos = (HEIGHT + 1) * WIDTH / 2 - 2 + 2 * WIDTH;
                //  MissionScene.add(skeleton2);

                //  skeleton3 = new SkelEnemy();
                //  skeleton3.pos = (HEIGHT + 1) * WIDTH / 2 - 1 + 3 * WIDTH;
                //  MissionScene.add(skeleton3);

                //  skeleton4 = new SkelEnemy();
                //  skeleton4.pos = (HEIGHT + 1) * WIDTH / 2 + 3 * WIDTH;
                //  MissionScene.add(skeleton4);

                //  skeleton5 = new SkelEnemy();
                //  skeleton5.pos = (HEIGHT + 1) * WIDTH / 2 + 1 + 3 * WIDTH;
                //  MissionScene.add(skeleton5);

                //  skeleton6 = new SkelEnemy();
                //  skeleton6.pos = (HEIGHT + 1) * WIDTH / 2 + 2 + 2 * WIDTH;
                //  MissionScene.add(skeleton6);

                ////  skeleton7 = new SkelEnemy();
                ////  skeleton7.pos = (HEIGHT + 1) * WIDTH / 2 + 3 + 3 * WIDTH;
                ////  MissionScene.add(skeleton7);

                //  sprite.visible = false;
                // // Dungeon.hero.sprite.visible = false;
                //  centerOfAttention = vanguard;
                } else if (MissionScene.scenePause == true) {
                   // Camera.main.target  = centerOfAttention.sprite;
                }


                if (counter == 100) {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "This looks bad...");
                }

                if (counter == 175) {
                    general.sprite.showStatus(CharSprite.NEUTRAL, "Very bad");
                }

                if (counter == 250) {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "I can get us help you know");
                }

                if (counter == 325) {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "I don't trust mercs");
                }

                if (counter == 400) {
                    general.sprite.showStatus(CharSprite.NEUTRAL, "We may not have a choice");
                }

                if (counter == 450) {
                    general.sprite.showStatus(CharSprite.NEUTRAL, "We already lost half our men");
                }

                if (counter == 550) {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "I'll give you a discount");
                }

                if (counter == 650) {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "Fine.. just a few");
                }

                if(counter == 700){
                    MissionScene.show(new WndTitledMessage(Icons.ALL_MERCS.get(), "Mercs", "- Click on the merc button to hire mercs\n" +
                            "- Kill enemies to get gold\n" +
                            "- Kill 100 enemies to win scenario\n" +
                            "- Use merc button to track kill count"));
                }

                // new WndTitledMessage(Icons.SKILLS.get(), "Mercs", "Click on the merc button to hire mercs")
              //  add();

                // if (counter == 600)
                //  actress.sprite.showStatus(CharSprite.NEUTRAL, "This is a private conversation.. go away");


                if (counter == END_MOVIE) {
                    for(int i = 0; i < 20; i++)
                    {
                        Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                        tmp.pos = getSpawnLocation();
                        MissionScene.add(tmp);
                        tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                        Betrayal.this.enemyAI.enemyCount++;
                    }



                    MissionScene.scenePause = false;
                }

                counter++;
                spend(0.25f);

            }
            else if(phase == 1)
            {
                if(counter == 40)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "We won");
                }

                if(counter == 140)
                {
                    general.sprite.showStatus(CharSprite.NEUTRAL, "Those mercs sure can fight");
                }
                if(counter == 240)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "I guess I was wrong about mercs");
                }

                if(counter == 340)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "Erm...");
                }

                if(counter == 360)
                {
                    general.level = 1;
                    general.defenseSkill = 1;
                    temari.aggro(general);
                    temari.sprite.attack(general.pos);
                }

                if(counter == 380)
                {
                    general.sprite.showStatus(CharSprite.NEUTRAL, "?!");
                    general.immortal = false;
                    general.die(null);
                }

                if(counter == 430)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "NO!");
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "Jackpot");
                }

                if(counter == 600)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "You killed him!");
                }

                if(counter == 760)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "Yeh an arrow would do that");
                }

                if(counter == 860)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "?!!");
                }

                if(counter == 960)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "Bounty at 6k sweetie");
                }

                if(counter == 1060)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "I trusted you!");
                }

                if(counter == 1160)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "Never trust a merc honey");
                }

                if(counter == 1200){
                    Betrayal.CityThief tmp = new Betrayal.CityThief();
                    tmp.pos = temari.pos - WIDTH * 2;
                    tmp.initStats();
                    MissionScene.add(tmp);
                    CellEmitter.get(tmp.pos).burst(ElmoParticle.FACTORY, 8);
                }

                if(counter == 1210){
                    Betrayal.CityThief tmp = new Betrayal.CityThief();
                    tmp.pos = temari.pos - WIDTH * 2 - 1;
                    tmp.initStats();
                    MissionScene.add(tmp);
                    CellEmitter.get(tmp.pos).burst(ElmoParticle.FACTORY, 8);

                    tmp = new Betrayal.CityThief();
                    tmp.pos = temari.pos - WIDTH * 2 + 1;
                    tmp.initStats();
                    MissionScene.add(tmp);
                    CellEmitter.get(tmp.pos).burst(ElmoParticle.FACTORY, 8);
                }

                if(counter == 1220){
                    Betrayal.CityThief tmp = new Betrayal.CityThief();
                    tmp.pos = temari.pos - WIDTH * 2 - 2;
                    tmp.initStats();
                    MissionScene.add(tmp);
                    CellEmitter.get(tmp.pos).burst(ElmoParticle.FACTORY, 8);

                    tmp = new Betrayal.CityThief();
                    tmp.pos = temari.pos - WIDTH * 2 + 2;
                    tmp.initStats();
                    MissionScene.add(tmp);
                    CellEmitter.get(tmp.pos).burst(ElmoParticle.FACTORY, 8);
                }

                if(counter == 1230){
                    for(int i = 0; i < 5; i++){
                        Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                        tmp.pos = temari.pos + WIDTH * 4 + 2 - i;
                        MissionScene.add(tmp);
                        tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    }
                    for(int i = 0; i < 5; i++){
                        Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                        tmp.pos = temari.pos + WIDTH * 5 + 2 - i;
                        MissionScene.add(tmp);
                        tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    }
                    for(int i = 0; i < 5; i++){
                        Betrayal.HostileSkeleton tmp = new Betrayal.HostileSkeleton();
                        tmp.pos = temari.pos + WIDTH * 6 + 2 - i;
                        MissionScene.add(tmp);
                        tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                    }
                }


                if(counter == 1240)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "Never");
                }

                if(counter == 1260)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "...");
                }

                if(counter == 1280)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "I know all your tricks");
                }

                if(counter == 1300)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "Game over");
                }

                if(counter == 1320)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "Not all of them");
                }

                if(counter == 1330)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "5k is too little for you");
                }

                if(counter == 1340)
                {
                    temari.sprite.showStatus(CharSprite.NEUTRAL, "I will ask for 6");
                }

                if(counter == 1370)
                {
                    GameScene.show(new PersistentWndOptions("Victory?", "Boonamai is surrounded. No more militia. No more general. No more allies...", "Exit Scenario") {
                        @Override
                        protected void onSelect(int index) {
                            Game.switchScene(MissionStartScene.class);
                            Music.INSTANCE.play(Assets.THEME, true);
                            Music.INSTANCE.volume(1f);
                        }
                    });
                }

                counter++;
                spend(1f);
            }
            else if(phase == 2)
            {
                if(counter == 10)
                {
                    general.speak("I brought volunteers");
                    centerOfAttention = general;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 30)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "I said fall back to the city...");
                    centerOfAttention = Dungeon.hero;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 60)
                {
                    general.speak("This is our fight too");
                    centerOfAttention = general;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 90)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "Can they even fight?");
                    centerOfAttention = Dungeon.hero;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 120)
                {
                    temari.speak("Ouch...");
                    centerOfAttention = temari;
                    Camera.main.target  = centerOfAttention.sprite;
                }


                if(counter == 150)
                {
                    general.speak("...");
                    centerOfAttention = general;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 180)
                {
                    general.speak("Move out!");
                    centerOfAttention = general;
                    Camera.main.target  = centerOfAttention.sprite;
                    MissionScene.scenePause = false;
                    Betrayal.this.enemyAI.finalWave();
                }

                counter++;
                spend(1f);

            }
            else if(phase == 3)
            {
                if(counter == 10)
                {
                    general.speak("Not bad eh?");
                    centerOfAttention = general;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 30)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "If you say so");
                    centerOfAttention = Dungeon.hero;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 60)
                {
                    general.speak("Some fell... show respect Hatsune");
                    centerOfAttention = general;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 90)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "Because you brought them!");
                    centerOfAttention = Dungeon.hero;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 120)
                {
                    temari.speak("Down girl");
                    centerOfAttention = temari;
                    Camera.main.target  = centerOfAttention.sprite;
                }


                if(counter == 150)
                {
                    general.speak("The council will discuss this");
                    centerOfAttention = general;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 180)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "I'll be with my daughters if needed");
                    centerOfAttention =  Dungeon.hero;
                    Camera.main.target  = centerOfAttention.sprite;
                }

                if(counter == 230)
                {
                    GameScene.show(new PersistentWndOptions("Victory!", "The first wave has been repelled!", "Exit Scenario") {
                        @Override
                        protected void onSelect(int index) {
                            Game.switchScene(MissionStartScene.class);
                            Music.INSTANCE.play(Assets.THEME, true);
                            Music.INSTANCE.volume(1f);
                        }
                    });
                }

                counter++;
                spend(1f);

            }
            else
                spend(1f);

            if(MissionScene.scenePause == true)
                Camera.main.target  = centerOfAttention.sprite;
            next();
            return true;
        }

        public void nextPhase()
        {
            if(phase == -1){
                gold = 1000;
                killCount = 0;
                hireArcher = false;
                hireThief = false;
                general = new General();
                general.pos = WIDTH * 9 + WIDTH / 2 - 1;
                general.initStats();
                MissionScene.add( general);
                general.haloUp();
                setCenterOfAttention( general);


                temari = new Temari();
                temari.pos = Dungeon.hero.pos + 1;
                temari.initStats();
                MissionScene.add( temari);
                temari.haloUp();
                setCenterOfAttention( Dungeon.hero);
                ((LegendSprite) Dungeon.hero.sprite).haloUp();

                Camera.main.target  = centerOfAttention.sprite;

                for(int i = 0; i < 10; i++)
                {
                    Betrayal.CitySoldier tmp = new Betrayal.CitySoldier();
                    tmp.pos = Dungeon.hero.pos - (1 + (int)(i / 5)) * WIDTH - 2 + i % 5;
                    MissionScene.add(tmp);
                    // tmp.sprite.emitter().burst(ShadowParticle.CURSE, 5);
                }
            }
            if(phase == 0)
            {

             //   general = new General();
             //   general.pos = WIDTH * 9 + WIDTH / 2 - 1;
             //   general.initStats();
             //   MissionScene.add( general);
             //   general.haloUp();
//
             //   for(int i = 0; i < 10; i++)
             //   {
             //       CitySoldier tmp = new CitySoldier();
             //       tmp.pos = WIDTH * 7 + WIDTH / 2 + 2 - (i + 1) % 5 - (i > 4 ? 0 : 1) * WIDTH;
             //       tmp.initStats();
             //       MissionScene.add( tmp);
             //   }
//
             //   GameScene.add( Blob.seed(WIDTH * 9 + WIDTH / 2, 2, Fire.class) );
//
             //   setCenterOfAttention( general);
             //   Camera.main.target  = centerOfAttention.sprite;
//
             //   MissionScene.scenePause = true;

              //  temari = new Temari();
              //  temari.pos = Dungeon.hero.pos + 1;
              //  temari.initStats();
              //  MissionScene.add( temari);
              //  temari.haloUp();
              //  setCenterOfAttention( temari);
              //  Camera.main.target  = centerOfAttention.sprite;
              //  MissionScene.scenePause = true;
            }

            if(phase == 1)
            {
           // general = new General();
           // general.pos = WIDTH * 9 + WIDTH / 2 - 1;
           // general.initStats();
           // MissionScene.add( general);
           // general.haloUp();

           // for(int i = 0; i < 10; i++)
           // {
           //     CitySoldier tmp = new CitySoldier();
           //     tmp.pos = WIDTH * 7 + WIDTH / 2 + 2 - (i + 1) % 5 - (i > 4 ? 0 : 1) * WIDTH;
           //     tmp.initStats();
           //     MissionScene.add( tmp);
           // }

           // GameScene.add( Blob.seed(WIDTH * 9 + WIDTH / 2, 2, Fire.class) );

           // setCenterOfAttention( general);
           // Camera.main.target  = centerOfAttention.sprite;

           // MissionScene.scenePause = true;
            }

            if(Dungeon.hero.HP > 0){
                phase++;
                counter = 0;
            }
        }

        @Override
        public boolean attack( Char enemy ) {
            return true;
        }
    }

    int getSpawnLocation()
    {
        int tmp = (HEIGHT * WIDTH) - 5 * WIDTH + WIDTH / 3 +  Random.Int(WIDTH / 3);
        int safety = 0;
        while(Actor.findChar(tmp) != null && safety < 5)
        {
            tmp = (HEIGHT * WIDTH) - 5 * WIDTH + WIDTH / 3 +  Random.Int(WIDTH / 3);
            safety++; // Final wave will overlap
        }
        return tmp;
    }
}
