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

import com.bilboldev.input.ControlSettings;
import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.DailyReminders;

import com.bilboldev.pixeldungeonskills.DailyRun;

import com.bilboldev.pixeldungeonskills.effects.DeathBombs;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.network.PartyEffects;

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Scene;
import com.bilboldev.noosa.NoosaScript;
import com.bilboldev.noosa.SkinnedBlock;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.effects.BossRemains;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.JourneyRun;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.levels.traps.Trap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTerrainTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTileSheet;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemapOld;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonWallsTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.FogOfWar;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.BannerSprites;
import com.bilboldev.pixeldungeonskills.effects.DeathDrops;
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
import com.bilboldev.pixeldungeonskills.ui.ResumeButton;
import com.bilboldev.pixeldungeonskills.ui.Banner;
import com.bilboldev.pixeldungeonskills.ui.BusyIndicator;
import com.bilboldev.pixeldungeonskills.ui.GameLog;
import com.bilboldev.pixeldungeonskills.ui.HealthIndicator;
import com.bilboldev.pixeldungeonskills.ui.ItemDropCards;
import com.bilboldev.pixeldungeonskills.thetiles.WallShadows;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.pixeldungeonskills.ui.Toast;
import com.bilboldev.pixeldungeonskills.ui.Toolbar;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndBag.Mode;
import com.bilboldev.pixeldungeonskills.windows.WndGame;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndError;
import com.bilboldev.pixeldungeonskills.windows.WndPartnerActions;
import com.bilboldev.pixeldungeonskills.windows.WndStory;
import com.bilboldev.pixeldungeonskills.windows.WndSkillsNew;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.SparseArray;
import com.bilboldev.utils.RectF;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.network.PartyWindows;
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
    WallShadows wallShadows;
    DungeonLighting lighting;
	protected FogOfWar fog;
	protected FogOfWarNew fogNew;
	protected HeroSprite hero;
	private EmoIcon.PartyTurn partyTurn;

    protected GameLog log;

    protected BusyIndicator busy;

    protected static CellSelector cellSelector;

    protected Group terrain;
    protected Group ripples;
    protected Group plants;
    protected Group heaps;
    protected Group mobs;
	private boolean spritesDrawnWithGrass;
    protected Group emitters;
    protected Group effects;
    protected Group gases;
    protected Group spells;
    protected Group statuses;
    protected Group emoicons;

    protected Toolbar toolbar;
    private StatusPane statusPane;
    protected Toast prompt;

    private static WeakReference<Hero> perspectiveHero = new WeakReference<>(null);
    private static WeakReference<Level> terrainLevel = new WeakReference<>(null);
    private static long terrainSeed;
    private static int sessionViewMode = 2;
    private FirstPersonView firstPersonView;
    private DesktopKeyHints keyHints;
    com.bilboldev.pixeldungeonskills.effects.TenguWarnings tenguWarnings;
    com.bilboldev.pixeldungeonskills.effects.BossWarnings bossWarnings;
    private FirstPersonButton firstPersonButton;
    private ResumeButton resume;
    private boolean partyStateReady = true;
    ItemDropCards itemDrops;
    private final RectF dropObstacle = new RectF();

    public void originalCreate()
    {
        super.create();
    }

	@Override
	public void create() {
		partyStateReady = PartySession.prepareScene();
        if (!partyStateReady) return;
        if (perspectiveHero.get() != Dungeon.hero) {
            perspectiveHero = new WeakReference<>(Dungeon.hero);
            if (!PartySession.client()) {
                sessionViewMode = GauntletRun.active() || PixelDungeon.enabled3d() ? 2 : 3;
            }
        }
        if (PartySession.splitControls() && sessionViewMode < 2) {
            sessionViewMode = PixelDungeon.enabled3d() ? 2 : 3;
        }
        Difficulties.is3d = sessionViewMode != 3;
        com.bilboldev.pixeldungeonskills.DungeonMusic.enter();

		if (!com.bilboldev.pixeldungeonskills.GauntletRun.active() && !DailyRun.active() && !TutorialRun.active())
			PixelDungeon.lastClass( Dungeon.hero.heroClass.ordinal() );

		super.create();
		Camera.main.zoom(Math.max(minZoom, Math.min(maxZoom, defaultZoom + PixelDungeon.zoom())));

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

		if (terrainLevel.get() != Dungeon.level) {
			terrainLevel = new WeakReference<>(Dungeon.level);
			terrainSeed = Dungeon.rules().hasRebornEncounters()
					? 31L * Dungeon.rebornEncounterSeed + Dungeon.depth : Random.Int(1000);
		}
		DungeonTileSheet.setupVariance(Dungeon.level.map.length, terrainSeed);

		if(Difficulties.is3d)
			tiles = new DungeonTerrainTilemap();
		else
			tiles = createFlatTilemap();

		terrain.add( tiles );
        if (Dungeon.rules().hasRebornEncounters()) {
            wallShadows = new WallShadows(Dungeon.level);
            terrain.add(wallShadows);
        }

		Dungeon.level.addVisuals( this );

		plants = new Group();
		add( plants );

		int size = Dungeon.level.plants.size();
		for (int i=0; i < size; i++) {
			addPlantSprite( Dungeon.level.plants.valueAt( i ) );
		}

		terrainFeatures = new TerrainFeaturesTilemap(Dungeon.level.plants, new SparseArray<Trap>());

		heaps = new Group() {
			@Override
			public void draw() {
				spritesDrawnWithGrass = false;
				if (walls != null) {
					boolean overhead = Difficulties.is3d && firstPersonView == null
							&& walls.exists && walls.visible;
					spritesDrawnWithGrass = walls.drawSprites(overhead ? this : null,
							overhead && mobs.exists && mobs.visible ? mobs : null);
				}
				if (!spritesDrawnWithGrass) super.draw();
			}
		};
		add( heaps );

		size = Dungeon.level.heaps.size();
		for (int i=0; i < size; i++) {
			addHeapSprite( Dungeon.level.heaps.valueAt( i ) );
		}

		emitters = new Group();
		effects = new Group();
		emoicons = new Group();

		mobs = new Group() {

            @Override
            public void update() {
                super.update();
                for (Gizmo member : members) {
                    if (member instanceof CharSprite && ((CharSprite)member).ch instanceof BossRemains)
                        ((BossRemains)((CharSprite)member).ch).update();
                }
            }

			@Override
			public void draw() {
				if (spritesDrawnWithGrass) walls.drawEffects(this);
				else super.draw();
			}
		};
		add( mobs );
        add(new DeathBombs());

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

        if (wallShadows != null) {
            lighting = new DungeonLighting(Dungeon.level, wallShadows);
            add(lighting);
        }

		add( emitters );
        if (Dungeon.level instanceof com.bilboldev.pixeldungeonskills.levels.IceCavesLevel
                || Dungeon.level instanceof com.bilboldev.pixeldungeonskills.levels.DailyHuntLevel
                || Dungeon.level instanceof com.bilboldev.pixeldungeonskills.levels.IceBossLevel)
            com.bilboldev.pixeldungeonskills.effects.IceDust.addTo(emitters, Dungeon.level, wallShadows);
		add( effects );
        if (Dungeon.rules().hasRebornEncounters()) {
            tenguWarnings = new com.bilboldev.pixeldungeonskills.effects.TenguWarnings();
            add(tenguWarnings);
            bossWarnings = new com.bilboldev.pixeldungeonskills.effects.BossWarnings();
            add(bossWarnings);
        }

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

			add( fogNew );
		}

		brightness( PixelDungeon.brightnessNew() );

		spells = new Group();
		add( spells );

		statuses = new Group();

		hero = new HeroSprite();
		hero.place( Dungeon.hero.pos );
		hero.updateArmor();
		mobs.add( hero );
		if (LocalCoop.active()) {
			for (Hero teammate : LocalCoop.heroes()) {
				if (teammate == Dungeon.hero) continue;
				if (!teammate.isAlive() && (!(teammate.sprite instanceof HeroSprite)
						|| !((HeroSprite)teammate.sprite).onCurrentLevel())) continue;
				HeroSprite sprite = new HeroSprite(teammate);
				sprite.place(teammate.pos);
				mobs.add(sprite);
			}
			partyTurn = new EmoIcon.PartyTurn();
		}

		add( new HealthIndicator(mobs) );
		add( emoicons );
		add( statuses );

		add( cellSelector = new CellSelector( tiles ) );

        if (Dungeon.rules().mode == com.bilboldev.pixeldungeonskills.GameplayMode.REBORN) {
            itemDrops = new ItemDropCards(this);
            add(itemDrops);
        }

		StatusPane sb = statusPane = new StatusPane();
		sb.camera = uiCamera;
		sb.setSize( uiCamera.width, 0 );
		add( sb );

		toolbar = new Toolbar();
		toolbar.camera = uiCamera;
		toolbar.setRect( 0,uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height() );
		add( toolbar );

		AttackIndicator attack = new AttackIndicator();
		attack.camera = uiCamera;
		attack.setPos(
			uiCamera.width - attack.width(),
			toolbar.top() - attack.height() );
		add( attack );

		resume = new ResumeButton();
		resume.camera = uiCamera;
		resume.setPos(attack.right() - resume.width(), attack.top() - resume.height() - 2);
		sb.add(resume);

		log = new GameLog();
		log.camera = uiCamera;
		log.setRect( 0, toolbar.top(), attack.left(),  0 );
		add( log );

		busy = new BusyIndicator();
		busy.camera = uiCamera;
		busy.x = 1;
		busy.y = sb.bottom() + 1;
		add( busy );

        if (!(Dungeon.level instanceof MovieLevel)) {
            firstPersonButton = new FirstPersonButton();
            firstPersonButton.camera = uiCamera;
            toolbar.layoutViewButton(firstPersonButton);
            add(firstPersonButton);
        }
		bringToFront(toolbar);

		switch (InterlevelScene.mode) {
		case RESURRECT:
			WandOfBlink.appear( Dungeon.hero, LocalCoop.active() ? Dungeon.hero.pos : Dungeon.level.entrance );
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

		ArrayList<Item> dropped = PartySession.client()
                ? null : Dungeon.droppedItems.get( Dungeon.levelKey() );
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
			Dungeon.droppedItems.remove( Dungeon.levelKey() );
		}

		Camera.main.target = hero;

		if (InterlevelScene.mode != InterlevelScene.Mode.NONE && Dungeon.depth != 0) {
			if (Dungeon.depth < Statistics.deepestFloor) {
				GLog.h(this.getClass(), "return", Dungeon.depth);
			} else {
                if(Dungeon.depth != ColdGirl.FROST_DEPTH) {
					GLog.h(this.getClass(), "descend", Dungeon.depth);
                    Sample.INSTANCE.play(Assets.SND_DESCEND);
                }
                else
                {
                    GLog.h("custom.generated.entry_the_portal_spits_you_out_in_a_cold_domain_91766f5139");
                    Sample.INSTANCE.play(Assets.SND_TELEPORT);
                }
			}
			switch (Dungeon.level.feeling) {
				case CHASM:
					GLog.w("levels.level$feeling.chasm_desc");
					break;
				case WATER:
					GLog.w("custom.generated.entry_you_hear_the_water_splashing_around_you_34048c37ef");
					break;
				case GRASS:
					GLog.w("levels.level$feeling.grass_desc");
					break;
				default:
			}
			if (Dungeon.level instanceof RegularLevel &&
					((RegularLevel) Dungeon.level).secretDoors > Random.IntRange( 3, 4 )) {
				GLog.w("levels.level$feeling.secrets_desc");
			}
			if (Dungeon.nightMode && !Dungeon.bossLevel()) {
				GLog.w("custom.generated.entry_be_cautious_since_the_dungeon_is_even_more_dange_f9c1d7bfac");
			}

			InterlevelScene.mode = InterlevelScene.Mode.NONE;

			fadeIn();
		}

		restoreGameplayInput();
        if (firstPersonButton != null) setViewMode(sessionViewMode);
        PartySession.sceneReady();
	}

    @Override
    public Gizmo add(Gizmo child) {
        if (PartySession.client() && child instanceof WndSkillsNew && !PartyWindows.displays((Window)child)) {
            PartySession.openSkills();
            child.destroy();
            return child;
        }
        if (child instanceof Window
                && PartyWindows.capture((Window)child)) return child;
        return super.add(child);
    }

    protected DungeonTilemapOld createFlatTilemap() {
        return new DungeonTilemapOld();
    }

    @Override
    public boolean canLeave(Class<? extends Scene> next) {
        if (JourneyRun.dying() && (next != InterlevelScene.class
                || InterlevelScene.mode != InterlevelScene.Mode.JOURNEY_CAMP)) return false;
        if (JourneyRun.active() && next == TitleScene.class) {
            try {
                JourneyRun.prepareLeave();
            } catch (IOException failure) {
                Game.cancelSceneChange();
                show(new WndError(Messages.get("journey.save_failed")));
                return false;
            }
        }
        if (next != getClass() || PartySession.client() || Dungeon.hero == null || !Dungeon.hero.isAlive()) return true;

        Actor current = Actor.current();
        return current == null || current instanceof Hero && ((Hero)current).ready;
    }

	public void destroy() {

        PartyPresentation.detachDeaths(this);
        PartyVisuals.detach(this);
        closeFirstPerson();
		if (keyHints != null) keyHints.destroy();

		scene = null;
		Badges.saveGlobal();

		super.destroy();
	}

    public static boolean isFirstPerson() {
        return isPerspectiveView() && !scene.firstPersonView.isThirdPerson();
    }

    public static void ignoreItemDrop(Heap heap, Item item, int quantity) {
        if (scene != null && scene == Game.scene() && scene.itemDrops != null
                && Dungeon.level != null && Dungeon.level.heaps.get(heap.pos) == heap) {
            scene.itemDrops.ignore(item, quantity);
        }
    }

    public boolean itemDropBounds(Heap heap, RectF bounds) {
        int cell = heap.pos;
        if (Dungeon.visible == null || cell < 0 || cell >= Dungeon.visible.length || !Dungeon.visible[cell]
                || heap.sprite == null || !heap.sprite.exists || !heap.sprite.visible) return false;
        if (firstPersonView != null) return firstPersonView.itemBounds(heap.sprite, bounds);
        screenBounds(heap.sprite, bounds);
        return bounds.right > 0 && bounds.left < uiCamera.width && bounds.bottom > 38 && bounds.top < itemDropBottom();
    }

    public boolean terrainCardBounds(int cell, RectF bounds) {
        if (Dungeon.visible == null || cell < 0 || cell >= Dungeon.visible.length || !Dungeon.visible[cell]) return false;
        if (firstPersonView != null) return firstPersonView.cellBounds(cell, bounds);
        screenBounds((cell % Dungeon.level.width()) * 16, (cell / Dungeon.level.width()) * 16, 16, 16, bounds);
        return bounds.right > 0 && bounds.left < uiCamera.width && bounds.bottom > 38 && bounds.top < itemDropBottom();
    }

    public boolean mobCardBounds(Mob mob, RectF bounds) {
        if (Dungeon.visible == null || mob.pos < 0 || mob.pos >= Dungeon.visible.length || !Dungeon.visible[mob.pos]
                || mob.sprite == null || !mob.sprite.exists || !mob.sprite.visible) return false;
        if (firstPersonView != null) return firstPersonView.spriteCardBounds(mob.sprite, bounds);
        screenBounds(mob.sprite, bounds);
        return bounds.right > 0 && bounds.left < uiCamera.width && bounds.bottom > 38 && bounds.top < itemDropBottom();
    }

    private void screenBounds(com.bilboldev.noosa.Visual sprite, RectF bounds) {
        screenBounds(sprite.x, sprite.y, sprite.width(), sprite.height(), bounds);
    }

    private void screenBounds(float worldX, float worldY, float width, float height, RectF bounds) {
        Camera world = Camera.main;
        float x = ((worldX - world.scroll.x) * world.zoom + world.x - uiCamera.x) / uiCamera.zoom + uiCamera.scroll.x;
        float y = ((worldY - world.scroll.y) * world.zoom + world.y - uiCamera.y) / uiCamera.zoom + uiCamera.scroll.y;
        float scale = world.zoom / uiCamera.zoom;
        bounds.set(x, y, x + width * scale, y + height * scale);
    }

    public float itemDropBottom() {
        return toolbar == null ? uiCamera.height - 48 : toolbar.top() - 24;
    }

    public boolean itemDropObstructed(RectF bounds) {
        if (bounds.left < 25 && bounds.top < 102) return true;
        if (resume != null && resume.visible && bounds.left < resume.right() && bounds.right > resume.left()
                && bounds.top < resume.bottom() && bounds.bottom > resume.top()) return true;
        if (firstPersonView != null) return firstPersonView.itemCardObstructed(bounds);
        if (hero != null) {
            screenBounds(hero, dropObstacle);
            if (bounds.left < dropObstacle.right && bounds.right > dropObstacle.left
                    && bounds.top < dropObstacle.bottom && bounds.bottom > dropObstacle.top) return true;
        }
        for (Hero member : LocalCoop.heroes()) {
            if (member.sprite == null || !member.sprite.visible) continue;
            screenBounds(member.sprite, dropObstacle);
            if (bounds.left < dropObstacle.right && bounds.right > dropObstacle.left
                    && bounds.top < dropObstacle.bottom && bounds.bottom > dropObstacle.top) return true;
        }
        return false;
    }

    public static boolean isThirdPerson() {
        return isPerspectiveView() && scene.firstPersonView.isThirdPerson();
    }

    public static boolean isPerspectiveView() {
        return scene != null && scene == Game.scene() && scene.firstPersonView != null;
    }

    public static int viewMode() {
        if (isFirstPerson()) return 0;
        if (isThirdPerson()) return 1;
        return Difficulties.is3d ? 2 : 3;
    }

    static FirstPersonView firstPersonView() {
        return isPerspectiveView() ? scene.firstPersonView : null;
    }

    public static Object perspectiveContext() {
        return firstPersonView();
    }

    public static boolean canLookAround() {
        return isPerspectiveView() && canAcceptGameplayInput() && Window.topWindow() == null && !Toolbar.menuOpen() &&
                Dungeon.hero != null && Dungeon.hero.isAlive() && cellSelector != null &&
                cellSelector.exists && cellSelector.parent == scene && cellSelector.listener != null;
    }

    public static boolean isPerspectiveTargeting() {
        return canLookAround() && cellSelector.listener != defaultCellListener;
    }

    public static void drawPerspectiveTargetCursor(int screenX, int screenY) {
        if (!isPerspectiveTargeting()) return;
        try {
            scene.firstPersonView.drawTargetCursor(screenX, screenY);
        } catch (RuntimeException | LinkageError failure) {
            scene.firstPersonFailed(failure);
        }
    }

    public static void lookAround(double radians) {
        lookAround(radians, 0);
    }

    public static void lookAround(double radians, double tilt) {
        if (canLookAround()) scene.firstPersonView.lookBy(radians, tilt);
    }

    public static void perspectiveMouseState(boolean captured, boolean available, boolean awaitingClick) {
        if (isPerspectiveView()) scene.firstPersonView.mouseState(captured, available, awaitingClick);
    }

    public static void perspectiveControllerState(boolean active, boolean looking) {
        if (isPerspectiveView()) scene.firstPersonView.controllerState(active, looking);
    }

    public static int[] perspectiveStep(int x, int y) {
        return isPerspectiveView() ? scene.firstPersonView.movementStep(x, y) : new int[]{x, y};
    }

    public static void cycleView() {
        if (scene == null || scene != Game.scene() || scene.firstPersonButton == null || Window.topWindow() != null) return;
        if (scene instanceof GauntletScene && !com.bilboldev.pixeldungeonskills.GauntletRun.acceptsInput()) return;
        Toolbar.closeTrays();
        scene.setViewMode(PartySession.splitControls() ? (viewMode() == 2 ? 3 : 2) : (viewMode() + 1) % 4);
        if (cellSelector != null) cellSelector.resetViewGesture();
    }

    public static void updateButtonLayout() {
        if (scene == null) return;
        scene.statusPane.refreshButtonLayout();
        if (scene.firstPersonButton != null) scene.toolbar.layoutViewButton(scene.firstPersonButton);
    }

    public static String perspectiveHelp() {
        return FirstPersonButton.helpText();
    }

    protected void setViewMode(int mode) {
        if (PartySession.splitControls() && mode < 2) mode = PixelDungeon.enabled3d() ? 2 : 3;
        sessionViewMode = mode;
        if (mode >= 2 && !GauntletRun.active() && PixelDungeon.enabled3d() != (mode == 2))
            PixelDungeon.enabled3d(mode == 2);
        if (Difficulties.is3d != (mode != 3)) {
            Hero hero = Dungeon.hero;
            Level level = Dungeon.level;
            CellSelector.Listener listener = cellSelector.listener;
            PixelScene.noFade = true;
            Game.resetScene(() -> {
                if (Dungeon.hero == hero && Dungeon.level == level && listener != null)
                    selectCell(listener);
            });
            return;
        }
        try {
            if (mode >= 2) closeFirstPerson();
            else {
                if (firstPersonView == null) firstPersonView = new FirstPersonView(this, uiCamera);
                if (mode == 1) firstPersonView.thirdPerson();
            }
            if (firstPersonButton != null) firstPersonButton.viewMode(mode);
        } catch (RuntimeException | LinkageError failure) {
            firstPersonFailed(failure);
        }
    }

    private void closeFirstPerson() {
        FirstPersonView old = firstPersonView;
        firstPersonView = null;
        if (firstPersonButton != null) firstPersonButton.viewMode(Difficulties.is3d ? 2 : 3);
        if (old != null) {
            try {
                old.destroy();
            }
            catch (RuntimeException failure) {
                failure.printStackTrace();
            }
        }
    }

    private void firstPersonFailed(Throwable failure) {
        sessionViewMode = 2;
        closeFirstPerson();
        failure.printStackTrace();
        GLog.w(FirstPersonView.class, "unavailable");
    }

    @Override
    public void draw() {
        spritesDrawnWithGrass = false;
        if (walls != null && (heaps == null || !heaps.exists || !heaps.visible)) {
            walls.drawSprites(null, null);
        }
        if (tenguWarnings != null) tenguWarnings.refresh();
        if (bossWarnings != null) bossWarnings.refresh();
        if (firstPersonView == null) {
            super.draw();
            drawKeyHints();
            return;
        }
        Camera original = Camera.main;
        try {
            firstPersonView.draw();

            Camera.main = firstPersonView.beginRadar();
            try {
                NoosaScript.get().resetCamera();
                for (int i = 0; i < length; i++) {
                    Gizmo g = members.get(i);
                    if (g != null && g.exists && g.visible && (g.camera() == Camera.main || g.camera() == original)) g.draw();
                }
            } finally {
                firstPersonView.endRadar();
                Camera.main = original;
            }
            firstPersonView.drawRadar();
            firstPersonView.drawMapHeading();
        } catch (RuntimeException | LinkageError failure) {
            firstPersonFailed(failure);
        } finally {
            Camera.main = original;
            NoosaScript.get().resetCamera();
            NoosaScript.get().camera(uiCamera);
        }
        if (firstPersonView == null) {
            super.draw();
            drawKeyHints();
            return;
        }
        firstPersonView.drawHint();

        for (int i = 0; i < length; i++) {
            Gizmo g = members.get(i);
            if (g != null && g.exists && g.visible && g.camera() != original) g.draw();
        }
        drawKeyHints();
    }

    private void drawKeyHints() {
        if (!ControlSettings.keyboardHints || ControlSettings.backend == null) return;
        if (keyHints == null) keyHints = new DesktopKeyHints(uiCamera);
        keyHints.draw();
    }

	@Override
	public synchronized void pause() {
		if (!partyStateReady) return;
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
		} catch (IOException e) {

		}
	}

	@Override
	public synchronized void update() {
		if (!partyStateReady || Dungeon.hero == null) {
			return;
		}

		try
		{
			super.update();
		}
		catch (Exception e){

		}

		DailyReminders.showRequested(true);
		water.offset( 0, -5 * Game.elapsed );
		PartySession.beforeActors();

		if (!PartySession.client()
				&& !PartyWindows.busy()
				&& (!LocalCoop.active() || Window.topWindow() == null)
				&& (!GauntletRun.active() || GauntletRun.acceptsInput())
                && !JourneyRun.dying()
                && (!TutorialRun.active()
                    || (TutorialRun.acceptsInput() && Window.topWindow() == null))
                && (!DailyRun.active()
                    || (!DailyRun.finished() && Window.topWindow() == null))) {
			LocalCoop.updateFollowing();
			Actor.process();
		}
		if (partyTurn != null) partyTurn.update();
        if (JourneyRun.active() || !DailyRun.active() && !GauntletRun.active() && Dungeon.bossLevel())
            Music.INSTANCE.boss(com.bilboldev.pixeldungeonskills.DungeonMusic.hasBoss());
		if (LocalCoop.active()) {
			Hero focus = cameraHero();
			if (focus != null && (LocalCoop.following(focus) || LocalCoop.following(LocalCoop.partner(focus)))) {
				Camera.main.target = focus.sprite;
			}
		}

        if (firstPersonView != null) {
            try {
                firstPersonView.update();
            }
            catch (RuntimeException | LinkageError failure) {
                firstPersonFailed(failure);
            }
        }

		if (Dungeon.hero.ready && !Dungeon.hero.paralysed) {
			log.newLine();
		}

		try
		{
			cellSelector.enabled = !DailyRun.finished() && (LocalCoop.active() ? LocalCoop.acceptsInput() : Dungeon.hero.ready);
		}
		catch (Exception e){

		}
	}

	@Override
	protected void onBackPressed() {
        if(Dungeon.depth == 0 && Dungeon.level instanceof MovieLevel)
        {
            com.bilboldev.pixeldungeonskills.Presentation.applyAudio();
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
		if (LocalCoop.active() ? LocalCoop.acceptsInput() : Dungeon.hero.ready) {
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

    public static void bossRemains(int depth, int cell) {
        if (scene == null) return;
        BossRemains dummy = new BossRemains(depth, cell);
        scene.addMobSprite(dummy);
        dummy.play();
    }

    static List<CharSprite> takeBossRemains() {
        ArrayList<CharSprite> result = new ArrayList<>();
        if (scene == null) return result;
        for (int i = 0; i < scene.mobs.length; i++) {
            Gizmo member = scene.mobs.memberAt(i);
            if (member instanceof CharSprite && ((CharSprite)member).ch instanceof BossRemains && member.exists)
                result.add((CharSprite)member);
        }
        for (CharSprite sprite : result) scene.mobs.erase(sprite);
        return result;
    }

    static void restoreBossRemains(List<CharSprite> sprites) {
        for (CharSprite sprite : sprites) scene.mobs.add(sprite);
    }

    protected void prompt( String text ) {

		if (prompt != null) {
			prompt.killAndErase();
			prompt = null;
		}

		if (text != null) {
				prompt = new Toast( Messages.maybeTranslate( text ) ) {
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

    public static void deathDrops(Heap remains, Iterable<Item> items) {
        if (scene == null) return;
        if (remains.sprite == null) scene.addHeapSprite(remains);
        else remains.sprite.link(remains);
        DeathDrops drops = new DeathDrops(remains.pos, items);
        if (drops.length > 0) {
            remains.sprite.alpha(0);
            effect(drops);
        } else {
            remains.sprite.drop();
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

	public static void effect( Gizmo effect ) {
		scene.effects.add( effect );
	}

    public static void spellLight(Emitter missile, int color) {
        if (scene != null && scene.lighting != null && color != 0) scene.lighting.follow(missile, color);
    }

    public static void updateDungeonEffects() {
        if (scene != null && scene.lighting != null) scene.lighting.enabled(Presentation.dungeonEffects());
    }

    public static void updateHelperCards() {
        if (scene != null && scene.itemDrops != null) scene.itemDrops.settingsChanged();
    }

    public static void explosionLight(float x, float y) {
        if (scene == null || scene.lighting == null || !Presentation.dungeonEffects()) return;
        int cx = (int)(x / 16), cy = (int)(y / 16);
        if (cx < 0 || cy < 0 || cx >= Level.WIDTH || cy >= Level.HEIGHT || !Dungeon.visible[cx + cy * Level.WIDTH]) return;
        scene.lighting.burst(x, y, 4, 0xFFD5A0);
        if (scene.firstPersonView != null) scene.firstPersonView.shake();
        else Camera.main.shake(.6f, .18f);
    }

	public static Ripple ripple( int pos ) {
		PartyEffects.ripple(pos);
		Ripple ripple = (Ripple)scene.ripples.recycle( Ripple.class );
		ripple.reset( pos );
		return ripple;
	}

	public static SpellSprite spellSprite() {
		return (SpellSprite)scene.spells.recycle( SpellSprite.class );
	}

	public static Emitter emitter() {
		if (scene != null) {
			Emitter emitter = (Emitter)scene.emitters.recycle(
					com.bilboldev.pixeldungeonskills.network.PartyParticles.BurstEmitter.class );
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

	public static void updateMap() {
		if (scene != null) {
			scene.tiles.updateMap();
            if (scene.wallShadows != null) scene.wallShadows.updateMap();

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
            if (scene.wallShadows != null) scene.wallShadows.updateMapCell(cell);

			if(scene.terrainFeatures != null)
				scene.terrainFeatures.updateMapCell(cell);

			if(scene.raisedTerrain != null)
				scene.raisedTerrain.updateMapCell( cell );

			if(scene.walls != null)
				scene.walls.updateMapCell( cell );

			updateFog( cell, 1 );
		}
	}

	public static void updateFog(){
		if (scene != null) {
			if(scene.fogNew != null){
				scene.fogNew.updateFog();
			}

		}
	}

	public static void updateFog(int x, int y, int w, int h){
		if (scene != null) {
			if(scene.fogNew != null){
				scene.fogNew.updateFogArea(x, y, w, h);
			}

		}
	}

	public static void updateFog( int cell, int radius ){
		if (scene != null) {
			if(scene.fogNew != null) {
				scene.fogNew.updateFog(cell, radius);
			}

		}
	}

	public static void discoverTile( int pos, int oldValue ) {
		if (scene != null) {
			scene.tiles.discover( pos, oldValue );
		}
	}

	public static void show( Window wnd ) {
		if (PartyWindows.capture(wnd)) return;
		Toolbar.closeTrays();
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
		JourneyRun.bossSlain();
		Music.INSTANCE.boss(false);
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

	public static boolean canAcceptGameplayInput() {
		if (!(Game.scene() instanceof GameScene) || !Game.scene().isActive() ||
				!Game.scene().isVisible()) return false;
		if (com.bilboldev.pixeldungeonskills.GauntletRun.active())
			return Game.scene() instanceof GauntletScene && com.bilboldev.pixeldungeonskills.GauntletRun.acceptsInput();
        if (TutorialRun.active())
            return Game.scene() instanceof TutorialScene && TutorialRun.acceptsInput();
		if (Dungeon.level instanceof MovieLevel) return false;
		return !(Game.scene() instanceof MissionScene || Game.scene() instanceof GauntletScene ||
				Game.scene() instanceof OnlineScene) || !MissionScene.scenePause;
	}

	public static boolean canAcceptMovement() {
		return canAcceptGameplayInput() && cellSelector != null && cellSelector.exists &&
				cellSelector.parent == Game.scene() && cellSelector.isActive() && cellSelector.enabled &&
				cellSelector.listener == defaultCellListener;
	}

    public static boolean hasCellSelection() {
        return cellSelector != null && cellSelector.listener != null && cellSelector.listener != defaultCellListener;
    }

	public static Object movementContext() {
		return cellSelector != null && cellSelector.parent == Game.scene() ? cellSelector.listener : null;
	}

	public static boolean zoomByAmount( float amount ) {
		if (!(Game.scene() instanceof GameScene) || cellSelector == null || amount == 0) {
			return false;
		}
		cellSelector.zoomByAmount( amount );
		return true;
	}

	public static void selectCell( CellSelector.Listener listener ) {
        if (PartySession.captureTarget(listener, listener == defaultCellListener)) return;
        Toolbar.targeting(listener, listener != defaultCellListener);
    	try
		{
			cellSelector.listener = listener;
			scene.prompt(listener.prompt() == null ? null : listener.prompt().toString());
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

	public static WndBag selectItem( WndBag.Listener listener, WndBag.Mode mode, Message title ) {
		if (PartySession.client()
                && listener == null && mode == WndBag.Mode.ALL) {
            PartySession.openInventory();
            return null;
        }
		if (!PartySession.remoteContext()) cancelCellSelector();

		WndBag wnd = mode == Mode.SEED ?
			WndBag.seedPouch( listener, mode, title ) :
			WndBag.lastBag( listener, mode, title );
		scene.add( wnd );

		return wnd;
	}

	public static boolean cancel() {
    	try {
            if (Toolbar.closeTrays()) return true;
            if (LocalCoop.wantsToFollow(Dungeon.hero)) {
                if (!PartySession.input(Dungeon.hero, "unfollow", 0)) LocalCoop.stopFollowing(Dungeon.hero);
                return true;
            }
            if (PartySession.client()) {
                if (cancelCellSelector()) return true;
                if (LocalCoop.hasCommand(Dungeon.hero) || Dungeon.hero.curAction != null || Dungeon.hero.restoreHealth) {
                    PartySession.input(Dungeon.hero, "cancel", 0);
                    return true;
                }
                return false;
            }
			if (LocalCoop.hasCommand(Dungeon.hero)) {
				LocalCoop.cancelCommand(Dungeon.hero);
				return true;
			}
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
		Lore.inventory();
		selectCell( defaultCellListener );
		QuickSlot.cancel();
	}

	public static void coopHeroChanged() {
		coopHeroChanged(true);
	}

	public static Hero cameraHero() {
		Hero hero = PartySession.network() ? PartySession.localHero() : Dungeon.hero;
		if (!PartySession.network() && LocalCoop.following(hero)) {
			Hero leader = LocalCoop.partner(hero);
			if (leader != null && leader.isAlive()) return leader;
		}
		return hero;
	}

	public static void coopHeroChanged(boolean focusCamera) {
		if (!LocalCoop.active() || scene == null || Game.scene() != scene
				|| Dungeon.hero == null || !(Dungeon.hero.sprite instanceof HeroSprite)) return;
		Hero focus = cameraHero();
		if (focusCamera || LocalCoop.following(focus) || LocalCoop.following(LocalCoop.partner(focus))
				|| scene.coopCameraNeedsFocus()) Camera.main.target = focus.sprite;
		if (scene.hero == Dungeon.hero.sprite) return;
		scene.hero = (HeroSprite)Dungeon.hero.sprite;
		QuickSlot.cancel();
		QuickSlot.refresh();
		BuffIndicator.refreshHero();
		AttackIndicator.updateState();
	}

	private boolean coopCameraNeedsFocus() {
		if (PartySession.network() || firstPersonView != null) return true;
		Hero next = Dungeon.hero;
		Hero partner = LocalCoop.partner(next);
		if (next.isAlive() && partner != null && !partner.isAlive()) return true;
		if (!next.isAlive() || !LocalCoop.isTurn(next) || next.curAction != null
				|| next.restoreHealth || LocalCoop.hasCommand(next)) return false;
		Camera camera = Camera.main;
		if (!(camera.target instanceof HeroSprite)) return true;
		Char current = ((HeroSprite)camera.target).ch;
		if (!LocalCoop.contains(current) || !current.isAlive() || Level.distance(current.pos, next.pos) > 3) return true;
		CharSprite sprite = next.sprite;
		float bottom = camera.scroll.y + camera.height - toolbar.height() * uiCamera.zoom / camera.zoom;
		return sprite.x < camera.scroll.x || sprite.x + sprite.width() > camera.scroll.x + camera.width
				|| sprite.y < camera.scroll.y || sprite.y + sprite.height() > bottom;
	}

	protected void restoreGameplayInput() {
		ready();
		if (cellSelector != null && Dungeon.hero != null) {
			cellSelector.enabled = Dungeon.hero.ready;
		}
	}

	private static final CellSelector.Listener defaultCellListener = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			if (cell == null) return;
			if (LocalCoop.active()) {
				Char target = Actor.findChar(cell);
				if (target instanceof Hero && target != Dungeon.hero && LocalCoop.contains(target)) {
					show(new WndPartnerActions());
					return;
				}
			}
			if (Dungeon.hero.handle( cell )) {
				Dungeon.hero.next();
			}
		}
		@Override
		public Message prompt() {
			return null;
		}
	};
}
