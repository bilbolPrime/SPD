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
package com.bilboldev.pixeldungeonskills;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.Online.OnlineHero;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Light;
import com.bilboldev.pixeldungeonskills.actors.buffs.Rage;
import com.bilboldev.pixeldungeonskills.actors.hero.GauntletHero;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Blacksmith;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Imp;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Ghost;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Wandmaker;
import com.bilboldev.pixeldungeonskills.actors.skills.CurrentSkills;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.items.Ankh;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.scrolls.Scroll;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.network.PartyQuests;
import com.bilboldev.pixeldungeonskills.levels.CavesBossLevel;
import com.bilboldev.pixeldungeonskills.levels.CavesLevel;
import com.bilboldev.pixeldungeonskills.levels.CityBossLevel;
import com.bilboldev.pixeldungeonskills.levels.CityLevel;
import com.bilboldev.pixeldungeonskills.levels.DeadEndLevel;
import com.bilboldev.pixeldungeonskills.levels.FrostLevel;
import com.bilboldev.pixeldungeonskills.levels.HallsBossLevel;
import com.bilboldev.pixeldungeonskills.levels.HallsLevel;
import com.bilboldev.pixeldungeonskills.levels.LastLevel;
import com.bilboldev.pixeldungeonskills.levels.LastShopLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.PrisonBossLevel;
import com.bilboldev.pixeldungeonskills.levels.PrisonLevel;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.SewerBossLevel;
import com.bilboldev.pixeldungeonskills.levels.SewerLevel;
import com.bilboldev.pixeldungeonskills.mechanics.ShadowCaster;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.BArray;
import com.bilboldev.pixeldungeonskills.windows.WndResurrect;
import com.bilboldev.utils.Bundlable;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.PathFinder;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.SparseArray;

public class Dungeon {

	public static int potionOfStrength;
	public static int scrollsOfUpgrade;
	public static int scrollsOfEnchantment;
	public static boolean dewVial;

	public static int challenges;

	public static Hero hero;
	public static Level level;

	public static int depth;
	public static int gold;
    public static int rebornEncounterSeed;
	public static boolean showGold;

    public static int difficulty;
    public static Difficulties currentDifficulty;

    private static int saveSlot = SaveSlots.FIRST;
    private static RunRules runRules = RunRules.CLASSIC;
    private static boolean playableRun = true;

    public static int saveSlot() {
        return saveSlot;
    }

    public static RunRules rules() {
        return runRules;
    }

    static Runnable enterGauntlet() {
        final int oldSlot = saveSlot, oldDifficulty = difficulty, oldChallenges = challenges, oldDepth = depth, oldGold = gold;
        final RunRules oldRules = runRules;
        final boolean oldPlayable = playableRun, oldNight = nightMode;
        final Difficulties oldCurrentDifficulty = currentDifficulty;
        saveSlot = SaveSlots.FIRST;
        runRules = RunRules.CLASSIC;
        playableRun = false;
        difficulty = Difficulties.NORMAL.ordinal();
        currentDifficulty = Difficulties.NORMAL;
        challenges = 0;
        nightMode = false;
        return new Runnable() {
            @Override
            public void run() {
                saveSlot = oldSlot;
                runRules = oldRules;
                playableRun = oldPlayable;
                difficulty = oldDifficulty;
                currentDifficulty = oldCurrentDifficulty;
                challenges = oldChallenges;
                depth = oldDepth;
                gold = oldGold;
                nightMode = oldNight;
            }
        };
    }

    public static void prepareNewRun(int slot, GameplayMode mode, int selectedDifficulty) {
        LocalCoop.clear();
        SaveSlots.validate(slot);
        RunRules selectedRules = RunRules.newRun(mode);
        int effectiveDifficulty = selectedRules.difficulty(selectedDifficulty);
        if (effectiveDifficulty < 0 || effectiveDifficulty >= Difficulties.values().length)
            throw new IllegalArgumentException("Invalid difficulty");
        saveSlot = slot;
        runRules = selectedRules;
        playableRun = true;
        hero = null;
        level = null;
        setDifficulty(effectiveDifficulty);
    }

    public static void setDifficulty(int selectedDifficulty) {
        difficulty = runRules.difficulty(selectedDifficulty);
        currentDifficulty = Difficulties.values()[difficulty];
        currentDifficulty.reset();

        if (runRules.mode == GameplayMode.REBORN) currentDifficulty.isNight = Difficulties.isNightOverwrite.DEFAULT;
    }

    private static void useSpecialRunRules() {
        LocalCoop.clear();
        saveSlot = SaveSlots.FIRST;
        runRules = RunRules.CLASSIC;
        playableRun = true;
    }

	public static String resultDescription;
	public static String resultCauseClassName;

	public static HashSet<Integer> chapters;

	public static boolean[] visible = new boolean[Level.LENGTH];

	public static boolean nightMode;

	public static SparseArray<ArrayList<Item>> droppedItems;

	public static void init() {
        rebornEncounterSeed = runRules.hasRebornEncounters() ? Random.Int(1000000) : 0;
        RivalEncounter.reset();
		playableRun = true;

		challenges = runRules.challenges(PixelDungeon.challenges());
		Skill.skillTrack = 0;
		Actor.clear();

		PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );

		Scroll.initLabels();
		Potion.initColors();
		Wand.initWoods();
		Ring.initGems();

		Statistics.reset();
		Journal.reset();

		depth = 0;
		gold = 0;

		droppedItems = new SparseArray<ArrayList<Item>>();

		potionOfStrength = 0;
		scrollsOfUpgrade = 0;
		scrollsOfEnchantment = 0;
		dewVial = true;

		chapters = new HashSet<Integer>();

		Ghost.Quest.reset();
		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();

		Room.shuffleTypes();

		QuickSlot.resetSlots();

		hero = new Hero();
        hero.difficulty = difficulty;
		hero.live();

		Badges.reset();

		StartScene.curClass.initHero( hero );
		LocalCoop.initialize();

	}

    public static void initOnline() {

        useSpecialRunRules();

        challenges = PixelDungeon.challenges();

        Actor.clear();

        PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );

        Scroll.initLabels();
        Potion.initColors();
        Wand.initWoods();
        Ring.initGems();

        Statistics.reset();
        Journal.reset();

        depth = 0;
        gold = 0;

        droppedItems = new SparseArray<ArrayList<Item>>();

        potionOfStrength = 0;
        scrollsOfUpgrade = 0;
        scrollsOfEnchantment = 0;
        dewVial = true;

        chapters = new HashSet<Integer>();

        Ghost.Quest.reset();
        Wandmaker.Quest.reset();
        Blacksmith.Quest.reset();
        Imp.Quest.reset();

        Room.shuffleTypes();

        QuickSlot.resetSlots();

        hero = new OnlineHero();
        hero.difficulty = difficulty;
        hero.live();

        Badges.reset();

        StartScene.curClass = HeroClass.ONLINE;
        StartScene.curClass.initHero( hero );

    }

    public static void initLegend() {

        useSpecialRunRules();

        challenges = PixelDungeon.challenges();

        Actor.clear();

        PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );

        Scroll.initLabels();
        Potion.initColors();
        Wand.initWoods();
        Ring.initGems();

        Statistics.reset();
        Journal.reset();

        depth = 0;
        gold = 0;

        droppedItems = new SparseArray<ArrayList<Item>>();

        potionOfStrength = 0;
        scrollsOfUpgrade = 0;
        scrollsOfEnchantment = 0;
        dewVial = true;

        chapters = new HashSet<Integer>();

        Ghost.Quest.reset();
        Wandmaker.Quest.reset();
        Blacksmith.Quest.reset();
        Imp.Quest.reset();

        Room.shuffleTypes();

        QuickSlot.resetSlots();

        hero = new Legend();
        hero.difficulty = difficulty;
        hero.live();

        Badges.reset();

        StartScene.curClass = HeroClass.HATSUNE;
        StartScene.curClass.initHero( hero );
        hero.heroSkills = CurrentSkills.HATSUNE;
        hero.heroSkills.init();

    }

	public static void initGauntlet() {

        if (!GauntletRun.active()) throw new IllegalStateException("Start an isolated Gauntlet session first");
		challenges = 0;
		Skill.skillTrack = 0;

		Actor.clear();

		PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );

		Scroll.initLabels();
		Potion.initColors();
		Wand.initWoods();
		Ring.initGems();

		Statistics.reset();
		Journal.reset();

		depth = 0;
		gold = 0;

		droppedItems = new SparseArray<ArrayList<Item>>();

		potionOfStrength = 0;
		scrollsOfUpgrade = 0;
		scrollsOfEnchantment = 0;
		dewVial = true;

		chapters = new HashSet<Integer>();

		Ghost.Quest.reset();
		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();

		Room.shuffleTypes();

		QuickSlot.resetSlots();

		hero = new GauntletHero();
		hero.difficulty = difficulty;
		hero.live();
		Badges.reset();

		StartScene.curClass.initHero( hero );
		new PotionOfHealing().identify().collect();
	}

	public static boolean isChallenged( int mask ) {
		return (challenges & mask) != 0;
	}

	public static Level newLevel() {

		Dungeon.level = null;
		Actor.clear();

		depth++;
		if (depth % ColdGirl.FROST_DEPTH > Statistics.deepestFloor) {
			Statistics.deepestFloor = depth;

			if (Statistics.qualifiedForNoKilling) {
				Statistics.completedWithNoKilling = true;
			} else {
				Statistics.completedWithNoKilling = false;
			}
		}

		Arrays.fill( visible, false );

		Level level;
		switch (depth) {
		case 1:
		case 2:
		case 3:
		case 4:
			level = new SewerLevel();
			break;
		case 5:
			level = new SewerBossLevel();
			break;
		case 6:
		case 7:
		case 8:
		case 9:
			level = new PrisonLevel();
			break;
		case 10:
			level = new PrisonBossLevel();
			break;
		case 11:
		case 12:
		case 13:
		case 14:
			level = new CavesLevel();
			break;
		case 15:
			level = new CavesBossLevel();
			break;
		case 16:
		case 17:
		case 18:
		case 19:
			level = new CityLevel();
			break;
		case 20:
			level = new CityBossLevel();
			break;
		case 21:
			level = new LastShopLevel();
			break;
		case 22:
		case 23:
		case 24:
			level = new HallsLevel();
			break;
		case 25:
			level = new HallsBossLevel();
			break;
		case 26:
			level = new LastLevel();
			break;
        case ColdGirl.FROST_DEPTH:
            level = new FrostLevel();
            break;
		default:
			level = new DeadEndLevel();
			Statistics.deepestFloor--;
		}

		level.create();

		Statistics.qualifiedForNoKilling = !bossLevel();

		return level;
	}

	public static void resetLevel() {

		Actor.clear();

		Arrays.fill( visible, false );

		level.reset();
		switchLevel( level, level.entrance );
	}

	public static boolean shopOnLevel() {
        return depth == 1 || depth == 6 || depth == 11 || depth == 16;
	}

	public static boolean bossLevel() {
		return bossLevel( depth );
	}

	public static boolean bossLevel( int depth ) {
		return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25 || depth == ColdGirl.FROST_DEPTH || depth == 0;
	}

	@SuppressWarnings("deprecation")
	public static void switchLevel( final Level level, int pos ) {

		nightMode = new Date().getHours() < 7;

        try
        {
            if(Dungeon.currentDifficulty.isNight == Difficulties.isNightOverwrite.ALWAYS_NIGHT)
                nightMode = true;
            if(Dungeon.currentDifficulty.isNight == Difficulties.isNightOverwrite.ALWAYS_DAY)
                nightMode = false;
        }
        catch (Exception e)
        {

        }

        if (runRules.hasRebornEncounters()) nightMode = false;
		Dungeon.level = level;
		if (LocalCoop.active()) LocalCoop.placeParty(level, pos != -1 ? pos : level.exit);
		Actor.init();

		Actor respawner = level.respawner();
		if (respawner != null) {
			Actor.add( level.respawner() );
		}

       if(hero.hiredMerc != null)
            hero.checkMerc = true;
        if(!LocalCoop.active() && depth != ColdGirl.FROST_DEPTH && depth != 0) {
            Actor mercRespawn = level.mercRespawner();
            if (mercRespawn != null) {
                Actor.add(mercRespawn);
            }
        }
        hero.pos = pos != -1 ? pos : level.exit;

		Light light = hero.buff( Light.class );
		hero.viewDistance = light == null ? level.viewDistance : Math.max( Light.DISTANCE, level.viewDistance );

		observe();
	}

	public static void dropToChasm( Item item ) {
		int depth = Dungeon.depth + 1;
		ArrayList<Item> dropped = (ArrayList<Item>)Dungeon.droppedItems.get( depth );
		if (dropped == null) {
			Dungeon.droppedItems.put( depth, dropped = new ArrayList<Item>() );
		}
		dropped.add( item );
	}

	public static boolean posNeeded() {
		int[] quota = {4, 2, 9, 4, 14, 6, 19, 8, 24, 9};
		return chance( quota, potionOfStrength );
	}

	public static boolean souNeeded() {
		int[] quota = {5, 3, 10, 6, 15, 9, 20, 12, 25, 13};
		return chance( quota, scrollsOfUpgrade );
	}

	public static boolean soeNeeded() {
		return Random.Int( 12 * (1 + scrollsOfEnchantment) ) < depth;
	}

	private static boolean chance( int[] quota, int number ) {

		for (int i=0; i < quota.length; i += 2) {
			int qDepth = quota[i];
			if (depth <= qDepth) {
				int qNumber = quota[i + 1];
				return Random.Float() < (float)(qNumber - number) / (qDepth - depth + 1);
			}
		}

		return false;
	}

	private static final String VERSION		= "version";
	private static final String CHALLENGES	= "challenges";
	private static final String HERO		= "hero";
	private static final String GOLD		= "gold";
	private static final String DEPTH		= "depth";
	private static final String LEVEL		= "level";
	private static final String DROPPED		= "dropped%d";
	private static final String POS			= "potionsOfStrength";
	private static final String SOU			= "scrollsOfEnhancement";
	private static final String SOE			= "scrollsOfEnchantment";
	private static final String DV			= "dewVial";
	private static final String CHAPTERS	= "chapters";
	private static final String QUESTS		= "quests";
	private static final String BADGES		= "badges";

	public static String gameFile( HeroClass cl ) {
		return SaveSlots.gameFile(cl, saveSlot);
	}

	public static void storePartyWorld(Bundle bundle) {
		runRules.store(bundle);
		bundle.put("reborn_encounter_seed", rebornEncounterSeed);
        RivalEncounter.store(bundle);
		bundle.put(CHALLENGES, challenges);
		bundle.put(GOLD, gold);
		bundle.put(DEPTH, depth);
		bundle.put(POS, potionOfStrength);
		bundle.put(SOU, scrollsOfUpgrade);
		bundle.put(SOE, scrollsOfEnchantment);
		bundle.put(DV, dewVial);
		int[] ids = new int[chapters.size()];
		int index = 0;
		for (int chapter : chapters) ids[index++] = chapter;
		bundle.put(CHAPTERS, ids);
		bundle.put("dropped_floors", droppedItems.keyArray());
		for (int floor : droppedItems.keyArray()) bundle.put(String.format(DROPPED, floor), droppedItems.get(floor));
		Bundle quests = new Bundle();
		Ghost.Quest.storeInBundle(quests);
		Wandmaker.Quest.storeInBundle(quests);
		Blacksmith.Quest.storeInBundle(quests);
		Imp.Quest.storeInBundle(quests);
		bundle.put(QUESTS, PartyQuests.encode(quests));
		Room.storeRoomsInBundle(bundle);
		Statistics.storeInBundle(bundle);
		Journal.storeInBundle(bundle);
		Scroll.save(bundle);
		Potion.save(bundle);
		Wand.save(bundle);
		Ring.save(bundle);
	}

	public static void restorePartyWorld(Bundle bundle) throws IOException {
		RunRules restored = RunRules.restore(bundle);
		if (restored.mode != GameplayMode.REBORN) throw new IOException("Party rules must be Reborn");
		runRules = restored;
		playableRun = true;
		setDifficulty(restored.difficulty(Difficulties.NORMAL.ordinal()));
		rebornEncounterSeed = bundle.getInt("reborn_encounter_seed");
        RivalEncounter.restore(bundle);
		challenges = restored.challenges(bundle.getInt(CHALLENGES));
		gold = bundle.getInt(GOLD);
		depth = bundle.getInt(DEPTH);
		potionOfStrength = bundle.getInt(POS);
		scrollsOfUpgrade = bundle.getInt(SOU);
		scrollsOfEnchantment = bundle.getInt(SOE);
		dewVial = bundle.getBoolean(DV);
		PathFinder.setMapSize(Level.WIDTH, Level.HEIGHT);
		Scroll.restore(bundle);
		Potion.restore(bundle);
		Wand.restore(bundle);
		Ring.restore(bundle);
		chapters = new HashSet<Integer>();
		for (int chapter : bundle.getIntArray(CHAPTERS)) chapters.add(chapter);
		Bundle quests = PartyQuests.decode(bundle.getBundle(QUESTS));
		Ghost.Quest.restoreFromBundle(quests);
		Wandmaker.Quest.restoreFromBundle(quests);
		Blacksmith.Quest.restoreFromBundle(quests);
		Imp.Quest.restoreFromBundle(quests);
		Room.restoreRoomsFromBundle(bundle);
		Statistics.restoreFromBundle(bundle);
		Journal.restoreFromBundle(bundle);
		droppedItems = new SparseArray<ArrayList<Item>>();
		for (int floor : bundle.getIntArray("dropped_floors")) {
			ArrayList<Item> items = new ArrayList<Item>();
			for (Bundlable item : bundle.getCollection(String.format(DROPPED, floor))) items.add((Item)item);
			droppedItems.put(floor, items);
		}
	}

	public static void saveGame( String fileName ) throws IOException {
		if (LocalCoop.active()) return;
		if (GauntletRun.active()) return;
		if (!playableRun) throw new IOException("Archived runs are read-only");
		try {
			Bundle bundle = new Bundle();

			bundle.put( VERSION, Game.version );
			runRules.store(bundle);
            if (runRules.hasRebornEncounters()) bundle.put("reborn_encounter_seed", rebornEncounterSeed);
            RivalEncounter.store(bundle);
			bundle.put( CHALLENGES, challenges );
			bundle.put( HERO, hero );
			bundle.put( GOLD, gold );
			bundle.put( DEPTH, depth );

			for (int d : droppedItems.keyArray()) {
				bundle.put( String.format( DROPPED, d ), droppedItems.get( d ) );
			}

			bundle.put( POS, potionOfStrength );
			bundle.put( SOU, scrollsOfUpgrade );
			bundle.put( SOE, scrollsOfEnchantment );
			bundle.put( DV, dewVial );

			int count = 0;
			int ids[] = new int[chapters.size()];
			for (Integer id : chapters) {
				ids[count++] = id;
			}
			bundle.put( CHAPTERS, ids );

			Bundle quests = new Bundle();
			Ghost		.Quest.storeInBundle( quests );
			Wandmaker	.Quest.storeInBundle( quests );
			Blacksmith	.Quest.storeInBundle( quests );
			Imp			.Quest.storeInBundle( quests );
			bundle.put( QUESTS, quests );

			Room.storeRoomsInBundle( bundle );

			Statistics.storeInBundle( bundle );
			Journal.storeInBundle( bundle );

			QuickSlot.save( bundle );

			Scroll.save( bundle );
			Potion.save( bundle );
			Wand.save( bundle );
			Ring.save( bundle );

			Bundle badges = new Bundle();
			Badges.saveLocal( badges );
			bundle.put( BADGES, badges );

			OutputStream output = Game.instance.openFileOutput( fileName, Game.MODE_PRIVATE );
			if (!Bundle.write(bundle, output)) throw new IOException("Unable to write game save");
			output.close();

		} catch (Exception e) {

			GamesInProgress.setUnknown(hero.heroClass, saveSlot);
			throw new IOException("Unable to save game", e);
		}
	}

	public static void saveLevel() throws IOException {
		if (LocalCoop.active()) {
			LocalCoop.rememberLevel(depth, level);
			return;
		}
		if (GauntletRun.active()) return;
		if (!playableRun || hero == null || Game.instance == null){
			return;
		}

		Bundle bundle = new Bundle();
		runRules.store(bundle);
		bundle.put( LEVEL, level );

		OutputStream output = Game.instance.openFileOutput(SaveSlots.levelFile(hero.heroClass, saveSlot, depth), Game.MODE_PRIVATE);

		if(output == null){
			return;
		}

		if (!Bundle.write(bundle, output)) throw new IOException("Unable to write floor save");
		output.close();
	}

	public static void saveAll() throws IOException {
		if (LocalCoop.active()) return;
		if (GauntletRun.active()) return;
		if (!playableRun || hero == null){
			return;
		}

		if (hero.isAlive()) {

			Actor.fixTime();
			saveGame( gameFile( hero.heroClass ) );
			saveLevel();

			GamesInProgress.setUnknown(hero.heroClass, saveSlot);

		} else if (WndResurrect.instance != null) {

			WndResurrect.instance.hide();
			Hero.reallyDie( WndResurrect.causeOfDeath );

		}
	}

	public static void loadGame( HeroClass cl ) throws IOException {
		loadGame(cl, saveSlot);
	}

	public static void loadGame(HeroClass cl, int slot) throws IOException {
		SaveSlots.validate(slot);
		Bundle bundle = gameBundle(SaveSlots.gameFile(cl, slot));
		RunRules restored = RunRules.restore(bundle);
		if (HeroClass.restoreInBundle(bundle.getBundle(HERO)) != cl)
			throw new IOException("Hero class does not match save slot");
		saveSlot = slot;
		playableRun = true;
		loadGame(bundle, true, restored);
	}

	public static void loadGame( String fileName ) throws IOException {
		loadGame( fileName, false );
	}

	public static void loadGame( String fileName, boolean fullLoad ) throws IOException {
		Bundle bundle = gameBundle( fileName );
		RunRules restored = RunRules.restore(bundle);

		playableRun = false;
		loadGame(bundle, fullLoad, restored);
	}

	private static void loadGame(Bundle bundle, boolean fullLoad, RunRules restored) throws IOException {
		LocalCoop.clear();
		runRules = restored;
        rebornEncounterSeed = bundle.getInt("reborn_encounter_seed");
        RivalEncounter.restore(bundle);

		Dungeon.challenges = runRules.challenges(bundle.getInt(CHALLENGES));

		Dungeon.level = null;
		Dungeon.depth = -1;

		if (fullLoad) {
			PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );
		}

		Scroll.restore( bundle );
		Potion.restore( bundle );
		Wand.restore( bundle );
		Ring.restore( bundle );

		potionOfStrength = bundle.getInt( POS );
		scrollsOfUpgrade = bundle.getInt( SOU );
		scrollsOfEnchantment = bundle.getInt( SOE );
		dewVial = bundle.getBoolean( DV );

		if (fullLoad) {
			chapters = new HashSet<Integer>();
			int ids[] = bundle.getIntArray( CHAPTERS );
			if (ids != null) {
				for (int id : ids) {
					chapters.add( id );
				}
			}

			Bundle quests = bundle.getBundle( QUESTS );
			if (!quests.isNull()) {
				Ghost.Quest.restoreFromBundle( quests );
				Wandmaker.Quest.restoreFromBundle( quests );
				Blacksmith.Quest.restoreFromBundle( quests );
				Imp.Quest.restoreFromBundle( quests );
			} else {
				Ghost.Quest.reset();
				Wandmaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
			}

			Room.restoreRoomsFromBundle( bundle );
		}

		Bundle badges = bundle.getBundle( BADGES );
		if (!badges.isNull()) {
			Badges.loadLocal( badges );
		} else {
			Badges.reset();
		}

		QuickSlot.restore( bundle );

		@SuppressWarnings("unused")
		String version = bundle.getString( VERSION );

		hero = null;
		hero = (Hero)bundle.get( HERO );
		Skill.skillTrack = 0;
		if (hero == null) throw new IOException("Unable to restore hero");

		gold = bundle.getInt( GOLD );
		depth = bundle.getInt( DEPTH );

		Statistics.restoreFromBundle( bundle );
		Journal.restoreFromBundle( bundle );

		droppedItems = new SparseArray<ArrayList<Item>>();
		for (int i=2; i <= Statistics.deepestFloor + 1; i++) {
			ArrayList<Item> dropped = new ArrayList<Item>();
			for (Bundlable b : bundle.getCollection( String.format( DROPPED, i ) ) ) {
				dropped.add( (Item)b );
			}
			if (!dropped.isEmpty()) {
				droppedItems.put( i, dropped );
			}
		}
	}

	public static Level loadLevel( HeroClass cl ) throws IOException {
		if (!playableRun) throw new IOException("Archived runs do not have playable floors");

		Dungeon.level = null;
		Actor.clear();

		Bundle bundle = gameBundle(SaveSlots.levelFile(cl, saveSlot, depth));
		RunRules levelRules = RunRules.restore(bundle);
		if (levelRules.mode != runRules.mode || levelRules.version != runRules.version)
			throw new IOException("Floor rules do not match this run");
		Level restored = (Level)bundle.get(LEVEL);
		if (restored == null) throw new IOException("Unable to restore floor");
		return restored;
	}

	public static void deleteGame( HeroClass cl, boolean deleteLevels ) {
		if (!playableRun) return;
		deleteGame(cl, saveSlot, deleteLevels);
	}

	public static void deleteGame(HeroClass cl, int slot, boolean deleteLevels) {
		if (LocalCoop.active()) return;
		if (GauntletRun.active()) return;

		Game.instance.deleteFile(SaveSlots.gameFile(cl, slot));

		if (deleteLevels) {

			for (String file : Game.instance.fileList())
				if (SaveSlots.isLevelFile(cl, slot, file)) Game.instance.deleteFile(file);
		}

		GamesInProgress.setUnknown(cl, slot);
	}

	public static Bundle gameBundle( String fileName ) throws IOException {

		try (InputStream input = Game.instance.openFileInput(fileName)) {
			Bundle bundle = Bundle.read(input);
			if (bundle == null) throw new IOException("Unreadable save data");
			return bundle;
		}
	}

	public static void preview( GamesInProgress.Info info, Bundle bundle ) throws IOException {
		info.rules = RunRules.restore(bundle);
		if (bundle.getBundle(HERO).isNull()) throw new IOException("Missing hero data");
		info.depth = bundle.getInt( DEPTH );
		info.challenges = info.rules.challenges(bundle.getInt(CHALLENGES)) != 0;
		if (info.depth == -1) {
			info.depth = bundle.getInt( "maxDepth" );
		}
		Hero.preview( info, bundle.getBundle( HERO ) );
	}

	private static String resultCauseClassName( Object cause ) {
		if (cause == null) {
			return null;
		}
		if (cause instanceof Class<?>) {
			return ((Class<?>) cause).getName();
		}
		return cause.getClass().getName();
	}

	public static void fail( String desc ) {
		fail( desc, null );
	}

	public static void fail( String desc, Object cause ) {
		if (LocalCoop.active()) return;
		if (GauntletRun.active()) return;
		resultDescription = desc;
		resultCauseClassName = resultCauseClassName( cause );
		if (hero.belongings.getItem( Ankh.class ) == null || Dungeon.depth == ColdGirl.FROST_DEPTH) {
            if(Dungeon.depth == ColdGirl.FROST_DEPTH)
                resultDescription = ColdGirl.TXT_DEATH;

			Rankings.INSTANCE.submit( false );
		}
	}

	public static void win( String desc ) {
		if (LocalCoop.active()) return;
		if (GauntletRun.active()) return;

		hero.belongings.identify();

		if (challenges != 0) {
			Badges.validateChampion();
		}

		resultDescription = desc;
		resultCauseClassName = null;
		Rankings.INSTANCE.submit( true );
	}

	public static void observe() {

		if (level == null || hero == null || LocalCoop.isRestoring()) {
			return;
		}

		level.updateFieldOfView( hero );
		System.arraycopy( Level.fieldOfView, 0, visible, 0, visible.length );

		if (LocalCoop.active()) {
			Hero observer = hero;
			boolean[] ownView = Level.fieldOfView.clone();
			for (Hero teammate : LocalCoop.heroes()) {
				if (teammate == observer || !teammate.isAlive()) continue;
				LocalCoop.withHero(teammate, () -> level.updateFieldOfView(teammate));
				BArray.or(visible, Level.fieldOfView, visible);
			}
			System.arraycopy(ownView, 0, Level.fieldOfView, 0, ownView.length);
		}

		BArray.or( level.visited, visible, level.visited );

		if (Difficulties.is3d && LocalCoop.active()) {
			GameScene.updateFog();
		} else if(Difficulties.is3d){
			int x = hero.pos % level.width();
			int y = hero.pos / level.width();

			int dist = ShadowCaster.MAX_DISTANCE+1;

			int l = Math.max( 0, x - dist );
			int r = Math.min( x + dist, level.width() - 1 );
			int t = Math.max( 0, y - dist );
			int b = Math.min( y + dist, level.height() - 1 );

			int width = r - l + 1;
			int height = b - t + 1;

			GameScene.updateFog(l, t, width, height);
		}

		GameScene.afterObserve();
		GauntletScene.afterObserve();
	}

	public static boolean[] passable = new boolean[Level.LENGTH];

	public static int findPath( Char ch, int from, int to, boolean pass[], boolean[] visible ) {

		if (Level.adjacent( from, to )) {
			return Actor.findChar( to ) == null && (pass[to] || Level.avoid[to]) ? to : -1;
		}

		if (ch.flying || ch.buff( Amok.class ) != null || ch.buff( Rage.class ) != null) {
			BArray.or( pass, Level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Level.LENGTH );
		}

		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}

		return PathFinder.getStep( from, to, passable );

	}

	public static int flee( Char ch, int cur, int from, boolean pass[], boolean[] visible ) {

		if (ch.flying) {
			BArray.or( pass, Level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Level.LENGTH );
		}

		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}
		passable[cur] = true;

		return PathFinder.getStepBack( cur, from, passable );

	}

}
