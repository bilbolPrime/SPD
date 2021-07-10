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
package com.bilboldev.pixeldungeonskills.scenes;

import java.io.IOException;
import java.util.ArrayList;

import com.bilboldev.glwrap.Blending;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.NoosaScript;
import com.bilboldev.noosa.NoosaScriptNoLighting;
import com.bilboldev.noosa.SkinnedBlock;
import com.bilboldev.noosa.Visual;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.traps.Trap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTerrainTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTileSheet;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemapOld;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonWallsTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.FogOfWar;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.BannerSprites;
import com.bilboldev.pixeldungeonskills.effects.BlobEmitter;
import com.bilboldev.pixeldungeonskills.effects.EmoIcon;
import com.bilboldev.pixeldungeonskills.effects.FixedText;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.effects.FloatingText;
import com.bilboldev.pixeldungeonskills.effects.Ripple;
import com.bilboldev.pixeldungeonskills.effects.SpellSprite;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.MovieLevel;
import com.bilboldev.pixeldungeonskills.levels.RegularLevel;
import com.bilboldev.pixeldungeonskills.levels.features.Chasm;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.DiscardedItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.PlantSprite;
import com.bilboldev.pixeldungeonskills.thetiles.FogOfWarNew;
import com.bilboldev.pixeldungeonskills.thetiles.GridTileMap;
import com.bilboldev.pixeldungeonskills.thetiles.RaisedTerrainTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.TerrainFeaturesTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.WallBlockingTilemap;
import com.bilboldev.pixeldungeonskills.ui.AttackIndicator;
import com.bilboldev.pixeldungeonskills.ui.Banner;
import com.bilboldev.pixeldungeonskills.ui.BusyIndicator;
import com.bilboldev.pixeldungeonskills.ui.GameLog;
import com.bilboldev.pixeldungeonskills.ui.HealthIndicator;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.pixeldungeonskills.ui.Toast;
import com.bilboldev.pixeldungeonskills.ui.Toolbar;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndBag.Mode;
import com.bilboldev.pixeldungeonskills.windows.WndGame;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndStory;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.SparseArray;

public class GameScene extends PixelScene {
	
	protected static final String TXT_WELCOME			= "Welcome to the level %d of Pixel Dungeon!";
    protected static final String TXT_WELCOME_BACK	= "Welcome back to the level %d of Pixel Dungeon!";
    protected static final String TXT_NIGHT_MODE		= "Be cautious, since the dungeon is even more dangerous at night!";

    protected static final String TXT_CHASM	= "Your steps echo across the dungeon.";
    protected static final String TXT_WATER	= "You hear the water splashing around you.";
    protected static final String TXT_GRASS	= "The smell of vegetation is thick in the air.";
    protected static final String TXT_SECRETS	= "The atmosphere hints that this floor hides many secrets.";

    protected static final String TXT_WARN_DEGRADATION = "Your items will wear down with time. You can disable item degradation from settings.";

    protected static final String TXT_FROST			= "The portal spits you out in a cold domain...";

    protected static GameScene scene;

    protected SkinnedBlock water;
	protected DungeonTilemapOld tiles;
	private GridTileMap visualGrid;
	private TerrainFeaturesTilemap terrainFeatures;
	private RaisedTerrainTilemap raisedTerrain;
	private DungeonWallsTilemap walls;
	private WallBlockingTilemap wallBlocking;
	protected FogOfWar fog;
	protected FogOfWarNew fogNew;
	private HeroSprite hero;

    protected GameLog log;

    protected BusyIndicator busy;

    protected static CellSelector cellSelector;

    protected Group terrain;
    protected Group ripples;
    protected Group plants;
    protected Group heaps;
    protected Group mobs;
    protected Group emitters;
    protected Group effects;
    protected Group gases;
    protected Group spells;
    protected Group statuses;
    protected Group emoicons;

    protected Toolbar toolbar;
    protected Toast prompt;

    public void originalCreate()
    {
        super.create();
    }

	@Override
	public void create() {
        if(Dungeon.depth != 0 && Dungeon.depth != ColdGirl.FROST_DEPTH) {
            Music.INSTANCE.play(Assets.TUNE, true);
            Music.INSTANCE.volume(1f);
        }
        else
        {
            Music.INSTANCE.play(Assets.TUNE_SPECIAL, true);
            Music.INSTANCE.volume(1f);
        }
		
		PixelDungeon.lastClass( Dungeon.hero.heroClass.ordinal() );
		
		super.create();
		Camera.main.zoom( defaultZoom + PixelDungeon.zoom() );
		
		scene = this;

		terrain = new Group();
		add( terrain );

		water = new SkinnedBlock(
				Dungeon.level.WIDTH * DungeonTilemap.SIZE,
				Dungeon.level.HEIGHT * DungeonTilemap.SIZE,
				Dungeon.level.waterTex());
		terrain.add( water );
		
		ripples = new Group();
		terrain.add( ripples );

		DungeonTileSheet.setupVariance(Dungeon.level.map.length, Random.Int(1000));

		if(Difficulties.is3d)
			tiles = new DungeonTerrainTilemap();
		else
			tiles = new DungeonTilemapOld();

		terrain.add( tiles );

		Dungeon.level.addVisuals( this );
		
		plants = new Group();
		add( plants );
		
		int size = Dungeon.level.plants.size();
		for (int i=0; i < size; i++) {
			addPlantSprite( Dungeon.level.plants.valueAt( i ) );
		}


		terrainFeatures = new TerrainFeaturesTilemap(Dungeon.level.plants, new SparseArray<Trap>());
		//if(Difficulties.is3d)
		//	terrain.add(terrainFeatures);


		heaps = new Group();
		add( heaps );
		
		size = Dungeon.level.heaps.size();
		for (int i=0; i < size; i++) {
			addHeapSprite( Dungeon.level.heaps.valueAt( i ) );
		}

		emitters = new Group();
		effects = new Group();
		emoicons = new Group();

		mobs = new Group();
		add( mobs );

		for (Mob mob : Dungeon.level.mobs) {
			addMobSprite( mob );
			if (Statistics.amuletObtained) {
				mob.beckon( Dungeon.hero.pos );
			}
		}

		raisedTerrain = new RaisedTerrainTilemap();

		if(Difficulties.is3d)
			add( raisedTerrain );

		walls = new DungeonWallsTilemap();
		if(Difficulties.is3d)
			add(walls);

		add( emitters );
		add( effects );
		
		gases = new Group();
		add( gases );
		
		for (Blob blob : Dungeon.level.blobs.values()) {
			blob.emitter = null;
			addBlobSprite( blob );
		}

		if(!Difficulties.is3d)
		{
			fog = new FogOfWar( Level.WIDTH, Level.HEIGHT );
			fog.updateVisibility( Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped );
			add( fog );
		}
		else {
			fogNew = new FogOfWarNew( Level.WIDTH, Level.HEIGHT );
			//fog.updateVisibility( Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped );
			add( fogNew );
		}

		
		brightness( PixelDungeon.brightnessNew() );


		spells = new Group();
		add( spells );

		statuses = new Group();
		add( statuses );

		add( emoicons );

		hero = new HeroSprite();
		hero.place( Dungeon.hero.pos );
		hero.updateArmor();
		mobs.add( hero );

		add( new HealthIndicator() );

		add( cellSelector = new CellSelector( tiles ) );

		StatusPane sb = new StatusPane();
		sb.camera = uiCamera;
		sb.setSize( uiCamera.width, 0 );
		add( sb );

		toolbar = new Toolbar();
		toolbar.secondQuickslot(PixelDungeon.secondQuickSlot());
		toolbar.camera = uiCamera;
		toolbar.setRect( 0,uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height() );
		add( toolbar );

		AttackIndicator attack = new AttackIndicator();
		attack.camera = uiCamera;
		attack.setPos(
			uiCamera.width - attack.width(),
			toolbar.top() - attack.height() );
		add( attack );

		log = new GameLog();
		log.camera = uiCamera;
		log.setRect( 0, toolbar.top(), attack.left(),  0 );
		add( log );

		busy = new BusyIndicator();
		busy.camera = uiCamera;
		busy.x = 1;
		busy.y = sb.bottom() + 1;
		add( busy );

		switch (InterlevelScene.mode) {
		case RESURRECT:
			WandOfBlink.appear( Dungeon.hero, Dungeon.level.entrance );
			new Flare( 8, 32 ).color( 0xFFFF66, true ).show( hero, 2f ) ;
			break;
		case RETURN:
			WandOfBlink.appear(  Dungeon.hero, Dungeon.hero.pos );
			break;
		case FALL:
			Chasm.heroLand();
			break;
		case DESCEND:
			switch (Dungeon.depth) {
			case 1:
				WndStory.showChapter( WndStory.ID_SEWERS );
                if(PixelDungeon.itemDeg() == false)
                    WndStory.showStory( TXT_WARN_DEGRADATION );
				break;
			case 6:
				WndStory.showChapter( WndStory.ID_PRISON );
				break;
			case 11:
				WndStory.showChapter( WndStory.ID_CAVES );
				break;
			case 16:
				WndStory.showChapter( WndStory.ID_METROPOLIS );
				break;
			case 22:
				WndStory.showChapter( WndStory.ID_HALLS );
				break;
			}
			if (Dungeon.hero.isAlive() && Dungeon.depth != 22) {
				Badges.validateNoKilling();
			}
			break;
		default:
		}
		
		ArrayList<Item> dropped = Dungeon.droppedItems.get( Dungeon.depth );
		if (dropped != null) {
			for (Item item : dropped) {
				int pos = Dungeon.level.randomRespawnCell();
				if (item instanceof Potion) {
					((Potion)item).shatter( pos );
				} else if (item instanceof Plant.Seed) {
					Dungeon.level.plant( (Plant.Seed)item, pos );
				} else {
					Dungeon.level.drop( item, pos );
				}
			}
			Dungeon.droppedItems.remove( Dungeon.depth );
		}
		
		Camera.main.target = hero;

		if (InterlevelScene.mode != InterlevelScene.Mode.NONE && Dungeon.depth != 0) {
			if (Dungeon.depth < Statistics.deepestFloor) {
				GLog.h( TXT_WELCOME_BACK, Dungeon.depth );
			} else {
                if(Dungeon.depth != ColdGirl.FROST_DEPTH) {
                    GLog.h(TXT_WELCOME, Dungeon.depth);
                    Sample.INSTANCE.play(Assets.SND_DESCEND);
                }
                else
                {
                    GLog.h(TXT_FROST);
                    Sample.INSTANCE.play(Assets.SND_TELEPORT);
                }
			}
			switch (Dungeon.level.feeling) {
				case CHASM:
					GLog.w( TXT_CHASM );
					break;
				case WATER:
					GLog.w( TXT_WATER );
					break;
				case GRASS:
					GLog.w( TXT_GRASS );
					break;
				default:
			}
			if (Dungeon.level instanceof RegularLevel &&
					((RegularLevel) Dungeon.level).secretDoors > Random.IntRange( 3, 4 )) {
				GLog.w( TXT_SECRETS );
			}
			if (Dungeon.nightMode && !Dungeon.bossLevel()) {
				GLog.w( TXT_NIGHT_MODE );
			}

			InterlevelScene.mode = InterlevelScene.Mode.NONE;

			fadeIn();
		}
	}
	
	public void destroy() {
		
		scene = null;
		Badges.saveGlobal();
		
		super.destroy();
	}
	
	@Override
	public synchronized void pause() {
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
		} catch (IOException e) {
			//
		}
	}
	
	@Override
	public synchronized void update() {
		if (Dungeon.hero == null) {
			return;
		}

		try
		{
			super.update();
		}
		catch (Exception e){

		}
		
		water.offset( 0, -5 * Game.elapsed );
		
		Actor.process();
		
		if (Dungeon.hero.ready && !Dungeon.hero.paralysed) {
			log.newLine();
		}

		try
		{
			cellSelector.enabled = Dungeon.hero.ready;
		}
		catch (Exception e){

		}
	}
	
	@Override
	protected void onBackPressed() {
        if(Dungeon.depth == 0 && Dungeon.level instanceof MovieLevel)
        {
            Music.INSTANCE.enable(PixelDungeon.music());
            InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
            Game.switchScene(InterlevelScene.class);
            Dungeon.observe();
        }
		else if (!cancel()) {
			add( new WndGame() );
		}
	}
	
	@Override
	protected void onMenuPressed() {
		if (Dungeon.hero.ready) {
			selectItem( null, WndBag.Mode.ALL, null );
		}
	}
	
	public void brightness( boolean value ) {
    	if(!Difficulties.is3d){
			water.rm = water.gm = water.bm =
					tiles.rm = tiles.gm = tiles.bm =
							value ? 1.5f : 1.0f;
			if (value) {
				fog.am = +2f;
				fog.aa = -1f;
			} else {
				fog.am = +1f;
				fog.aa =  0f;
			}
		}
	}

	public void brightness( int value ) {
		if(!Difficulties.is3d){
			water.rm = water.gm = water.bm =
					tiles.rm = tiles.gm = tiles.bm =
							1.0f + 0.125f * ( 2 + value);

			fog.am = +1f + (2 + value) * 0.25f;
			fog.aa = 0f - (2 + value) * 0.25f;
			//if (value) {
			//	fog.am = +2f;
			//	fog.aa = -1f;
			//} else {
			//	fog.am = +1f;
			//	fog.aa =  0f;
			//}
		}
		else {
			if(scene.fogNew != null)
				scene.fogNew.updateFog();
		}
	}

    protected void addHeapSprite( Heap heap ) {
		ItemSprite sprite = heap.sprite = (ItemSprite)heaps.recycle( ItemSprite.class );
		sprite.revive();
		sprite.link( heap );
		heaps.add( sprite );
	}

    protected void addDiscardedSprite( Heap heap ) {
		heap.sprite = (DiscardedItemSprite)heaps.recycle( DiscardedItemSprite.class );
		heap.sprite.revive();
		heap.sprite.link( heap );
		heaps.add( heap.sprite );
	}

    protected void addPlantSprite( Plant plant ) {
		(plant.sprite = (PlantSprite)plants.recycle( PlantSprite.class )).reset( plant );
	}

    protected void addBlobSprite( final Blob gas ) {
		if (gas.emitter == null) {
			gases.add( new BlobEmitter( gas ) );
		}
	}

    protected void addMobSprite( Mob mob ) {
		CharSprite sprite = mob.sprite();
		sprite.visible = Dungeon.visible[mob.pos];
		mobs.add( sprite );
		sprite.link( mob );
	}

    protected void prompt( String text ) {
		
		if (prompt != null) {
			prompt.killAndErase();
			prompt = null;
		}
		
		if (text != null) {
			prompt = new Toast( text ) {
				@Override
				protected void onClose() {
					cancel();
				}
			};
			prompt.camera = uiCamera;
			prompt.setPos( (uiCamera.width - prompt.width()) / 2, uiCamera.height - 60 );
			add( prompt );
		}
	}

    protected void showBanner( Banner banner ) {
		banner.camera = uiCamera;
		banner.x = align( uiCamera, (uiCamera.width - banner.width) / 2 );
		banner.y = align( uiCamera, (uiCamera.height - banner.height) / 3 );
		add( banner );
	}
	
	// -------------------------------------------------------
	
	public static void add( Plant plant ) {
		if (scene != null) {
			scene.addPlantSprite( plant );
		}
	}
	
	public static void add( Blob gas ) {
		Actor.add( gas );
		if (scene != null) {
			scene.addBlobSprite( gas );
		}
	}
	
	public static void add( Heap heap ) {
		if (scene != null) {
			scene.addHeapSprite( heap );
		}
	}
	
	public static void discard( Heap heap ) {
		if (scene != null) {
			scene.addDiscardedSprite( heap );
		}
	}
	
	public static void add( Mob mob ) {
		Dungeon.level.mobs.add( mob );
		Actor.add( mob );
		Actor.occupyCell( mob );
		scene.addMobSprite( mob );
	}
	
	public static void add( Mob mob, float delay ) {
		Dungeon.level.mobs.add( mob );
		Actor.addDelayed( mob, delay );
		Actor.occupyCell( mob );
		scene.addMobSprite( mob );
	}


	
	public static void add( EmoIcon icon ) {
    	try
		{
			scene.emoicons.add( icon );
		}
		catch (Exception e){}
	}
	
	public static void effect( Visual effect ) {
		scene.effects.add( effect );
	}
	
	public static Ripple ripple( int pos ) {
		Ripple ripple = (Ripple)scene.ripples.recycle( Ripple.class );
		ripple.reset( pos );
		return ripple;
	}
	
	public static SpellSprite spellSprite() {
		return (SpellSprite)scene.spells.recycle( SpellSprite.class );
	}
	
	public static Emitter emitter() {
		if (scene != null) {
			Emitter emitter = (Emitter)scene.emitters.recycle( Emitter.class );
			emitter.revive();
			return emitter;
		} else {
			return null;
		}
	}
	
	public static FloatingText status() {
		return scene != null ? (FloatingText)scene.statuses.recycle( FloatingText.class ) : null;
	}

    public static FixedText fixedStatus() {
        return scene != null ? (FixedText)scene.statuses.recycle( FixedText.class ) : null;
    }
	
	public static void pickUp( Item item ) {
		scene.toolbar.pickup( item );
	}

	//updates the whole map
	public static void updateMap() {
		if (scene != null) {
			scene.tiles.updateMap();
		//	scene.visualGrid.updateMap();
	//		scene.terrainFeatures.updateMap();
			if(scene.raisedTerrain != null)
				scene.raisedTerrain.updateMap();

			if(scene.terrainFeatures != null)
				scene.terrainFeatures.updateMap();

			if(scene.walls != null)
				scene.walls.updateMap();

			updateFog();
		}
	}

	public static void updateMap( int cell ) {
		if (scene != null) {
			scene.tiles.updateMapCell( cell );
//			scene.visualGrid.updateMapCell( cell );
//			scene.terrainFeatures.updateMapCell( cell );

			if(scene.terrainFeatures != null)
				scene.terrainFeatures.updateMapCell(cell);


			if(scene.raisedTerrain != null)
				scene.raisedTerrain.updateMapCell( cell );

			if(scene.walls != null)
				scene.walls.updateMapCell( cell );
			//update adjacent cells too
			updateFog( cell, 1 );
		}
	}


	public static void updateFog(){
		if (scene != null) {
			if(scene.fogNew != null){
				scene.fogNew.updateFog();
			}

			//scene.wallBlocking.updateMap();
		}
	}

	public static void updateFog(int x, int y, int w, int h){
		if (scene != null) {
			if(scene.fogNew != null){
				scene.fogNew.updateFogArea(x, y, w, h);
			}

			//scene.wallBlocking.updateArea(x, y, w, h);
		}
	}

	public static void updateFog( int cell, int radius ){
		if (scene != null) {
			if(scene.fogNew != null) {
				scene.fogNew.updateFog(cell, radius);
			}
			//scene.wallBlocking.updateArea( cell, radius );
		}
	}


	public static void discoverTile( int pos, int oldValue ) {
		if (scene != null) {
			scene.tiles.discover( pos, oldValue );
		}
	}
	
	public static void show( Window wnd ) {
		cancelCellSelector();
		scene.add( wnd );
	}
	
	public static void afterObserve() {
		if (scene != null) {
			if(scene.fog != null)
				scene.fog.updateVisibility( Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped );

			for (Mob mob : Dungeon.level.mobs) {
				if (mob.sprite != null)
					mob.sprite.visible = Dungeon.visible[mob.pos];
			}
		}
	}


	
	public static void flash( int color ) {
		scene.fadeIn( 0xFF000000 | color, true );
	}
	
	public static void gameOver() {
		Banner gameOver = new Banner( BannerSprites.get( BannerSprites.Type.GAME_OVER ) );
		gameOver.show( 0x000000, 1f );
		scene.showBanner( gameOver );
		
		Sample.INSTANCE.play( Assets.SND_DEATH );
	}
	
	public static void bossSlain() {
		if (Dungeon.hero.isAlive()) {
			Banner bossSlain = new Banner( BannerSprites.get( BannerSprites.Type.BOSS_SLAIN ) );
			bossSlain.show( 0xFFFFFF, 0.3f, 5f );
			scene.showBanner( bossSlain );
			
			Sample.INSTANCE.play( Assets.SND_BOSS );
		}
	}
	
	public static void handleCell( int cell ) {
		cellSelector.select( cell );
	}
	
	public static void selectCell( CellSelector.Listener listener ) {
    	try
		{
			cellSelector.listener = listener;
			scene.prompt( listener.prompt() );
		}
		catch (Exception e) {}
	}
	
	private static boolean cancelCellSelector() {
		if (cellSelector.listener != null && cellSelector.listener != defaultCellListener) {
			cellSelector.cancel();
			return true;
		} else {
			return false;
		}
	}
	
	public static WndBag selectItem( WndBag.Listener listener, WndBag.Mode mode, String title ) {
		cancelCellSelector();
		
		WndBag wnd = mode == Mode.SEED ?
			WndBag.seedPouch( listener, mode, title ) :
			WndBag.lastBag( listener, mode, title );
		scene.add( wnd );
		
		return wnd;
	}

	static boolean cancel() {
    	try {
			if (Dungeon.hero.curAction != null || Dungeon.hero.restoreHealth) {

				Dungeon.hero.curAction = null;
				Dungeon.hero.restoreHealth = false;
				return true;

			} else {

				return cancelCellSelector();

			}
		}
		catch (Exception e){
    		return  true;
		}
	}
	
	public static void ready() {
		selectCell( defaultCellListener );
		QuickSlot.cancel();
	}
	
	private static final CellSelector.Listener defaultCellListener = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			if (Dungeon.hero.handle( cell )) {
				Dungeon.hero.next();
			}
		}
		@Override
		public String prompt() {
			return null;
		}
	};
}
