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

import java.io.FileNotFoundException;
import java.io.IOException;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.levels.Campaigns.Betrayal;
import com.bilboldev.pixeldungeonskills.levels.Campaigns.FirstWave;
import com.bilboldev.pixeldungeonskills.levels.Campaigns.SoulFury;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.MovieLevel;
import com.bilboldev.pixeldungeonskills.levels.Online.BetaRoomsLevel;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.online.OnlineSync;
import com.bilboldev.pixeldungeonskills.ui.GameLog;
import com.bilboldev.pixeldungeonskills.windows.WndError;
import com.bilboldev.pixeldungeonskills.windows.WndStory;

public class InterlevelScene extends PixelScene {

	private static final float TIME_TO_FADE = 0.3f;

    private static final String TXT_TELEPORTING		= "A weird portal sucks you in...";
    private static final String TXT_ONLINE		= "Connecting...";

	private static final String ERR_FILE_NOT_FOUND	= "File not found. For some reason.";
	private static final String ERR_GENERIC			= "Something went wrong..."	;

	public static enum Mode {
		DESCEND, ASCEND, CONTINUE, RESURRECT, RETURN, FALL, NONE, TELEPORT, TELEPORT_BACK, MOVIE, MOVIE_OUT, MISSION, PLAY_ONLINE, GUANTLET, GUANTLET_NEXT
	};
	public static Mode mode;

	public static int returnDepth;
	public static int returnPos;

	public static boolean noStory = false;

	public static boolean fallIntoPit;
	public static Hero fallingHero;

	private enum Phase {
		FADE_IN, STATIC, FADE_OUT
	};
	private Phase phase;
	private float timeLeft;

	private BitmapText message;

	private Thread thread;
	private String error = null;

	public static int missionChosen = 0;

	@Override
	public void create() {
		super.create();
		if (mode != Mode.FALL) fallingHero = null;

		String text = "";
		switch (mode) {
		case DESCEND:
			text = Messages.get( Mode.class, "descend" );
			break;
		case ASCEND:
			text = Messages.get( Mode.class, "ascend" );
			break;
		case CONTINUE:
			text = Messages.get( Mode.class, "continue" );
			break;
		case RESURRECT:
			text = Messages.get( Mode.class, "resurrect" );
			break;
		case RETURN:
			text = Messages.get( Mode.class, "return" );
			break;
		case FALL:
			text = Messages.get( Mode.class, "fall" );
			break;
        case TELEPORT:
        case TELEPORT_BACK:
            text = TXT_TELEPORTING;
            break;
        case MOVIE:
            text = "10 years ago...";
            break;
        case MOVIE_OUT:
            text = "The true story has yet to begin";
            break;
        case MISSION:
             text = "10 years ago...";
             break;
            case PLAY_ONLINE:
                text = TXT_ONLINE;
                break;
			case GUANTLET:
				text = Messages.get("gauntlet.entering");
				break;
			case GUANTLET_NEXT:
				text = Messages.maybeTranslate( "You survived..." );
				break;
		default:
		}

		message = PixelScene.createText( text, 9 );
		message.measure();
		message.x = (Camera.main.width - message.width()) / 2;
		message.y = (Camera.main.height - message.height()) / 2;
		add( message );

		phase = Phase.FADE_IN;
		timeLeft = TIME_TO_FADE;

        if(mode == Mode.MOVIE || mode == Mode.MOVIE_OUT || mode == Mode.MISSION)
            timeLeft = 10 * TIME_TO_FADE;

		thread = new Thread() {
			@Override
			public void run() {

				try {

					if (mode != Mode.GUANTLET && mode != Mode.GUANTLET_NEXT) Generator.reset();

					switch (mode) {
                    case MISSION:
                        runMission(missionChosen);
                         break;
						case GUANTLET:
						case GUANTLET_NEXT:
							runGauntlet();
							break;
                    case MOVIE:
                        runMovie();
                        break;
                    case MOVIE_OUT:
                       endMovie();
                        break;
					case DESCEND:
						descend();
						break;
					case ASCEND:
						ascend();
						break;
					case CONTINUE:
						restore();
						break;
					case RESURRECT:
						resurrect();
						break;
					case RETURN:
						returnTo();
						break;
					case FALL:
						fall();
						break;
                    case TELEPORT:
                        teleport();
                     break;
                    case TELEPORT_BACK:
                        teleport_back();
                         break;
                    case PLAY_ONLINE:
                        runOnline();
                        break;
					default:
					}

					if ((Dungeon.depth % 5) == 0) {
						Sample.INSTANCE.load( Assets.SND_BOSS );
					}

				} catch (FileNotFoundException e) {

					error = ERR_FILE_NOT_FOUND;

				} catch (Exception e ) {

					error = ERR_GENERIC + " in " + mode + "\n" + e;

				}

				if (phase == Phase.STATIC && error == null) {
					phase = Phase.FADE_OUT;
					timeLeft = TIME_TO_FADE;
				}
			}
		};
		thread.start();
	}

	@Override
	public void update() {
		super.update();

		float p = timeLeft / TIME_TO_FADE;
        if(mode == Mode.MOVIE || mode == Mode.MOVIE_OUT || mode == Mode.MISSION)
            p /= 10;

		switch (phase) {

		case FADE_IN:
			message.alpha( 1 - p );
			if ((timeLeft -= Game.elapsed) <= 0) {
				if (!thread.isAlive() && error == null) {
					phase = Phase.FADE_OUT;
					timeLeft = TIME_TO_FADE;
				} else {
					phase = Phase.STATIC;
				}
			}
			break;

		case FADE_OUT:
             message.alpha( p );

			if (mode == Mode.CONTINUE || (mode == Mode.DESCEND && Dungeon.depth == 1)) {
				Music.INSTANCE.volume( p );
			}
			if ((timeLeft -= Game.elapsed) <= 0) {
                if(mode == Mode.MOVIE || mode == Mode.MISSION)

                    Game.switchScene( MissionScene.class );
                else if(mode == Mode.PLAY_ONLINE)
                    Game.switchScene( OnlineScene.class );
                else if(mode == Mode.MOVIE_OUT)
                    Game.switchScene(TitleScene.class);
                else if (mode == Mode.GUANTLET){
					GauntletScene.level = 1;
					Game.switchScene(GauntletScene.class);
				}
				else if (mode == Mode.GUANTLET_NEXT){
					Game.switchScene(GauntletScene.class);
				}
                else
				    Game.switchScene( GameScene.class );
			}
			break;

		case STATIC:
			if (error != null) {
                if (mode != Mode.MOVIE && mode != Mode.MOVIE_OUT) {
                    add(new WndError(error) {
                        public void onBackPressed() {
                            super.onBackPressed();
                            Game.switchScene(StartScene.class);
                        }

                        ;
                    });
                    error = null;
                }
                else
                {
                    add(new WndError("Something went wrong with your movie... but the game continues") {
                        public void onBackPressed() {
                            super.onBackPressed();
                            InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                            Game.switchScene(InterlevelScene.class);
                        }

                        ;
                    });
                    error = null;
                }
            }
			break;
		}
	}

    private void runMission(int missionChosen) throws Exception {

        try {
            GameLog.wipe();
        }
        catch (Exception ex)
        {

        }

        if (Dungeon.hero == null)
            Dungeon.initLegend();

        MissionScene.scenePause = true;
        Level level = new FirstWave();
        if(missionChosen == 1){
			level = new Betrayal();
		}

		if(missionChosen == 2){
			level = new SoulFury();
		}
        Dungeon.level = level;
        level.create();
        Dungeon.switchLevel( level, level.randomRespawnCell() );
    }

	private void runGauntlet() throws Exception {
		com.bilboldev.pixeldungeonskills.GauntletRun.prepare();
	}

    private void runOnline() throws Exception {

        try {
            GameLog.wipe();
        }
        catch (Exception ex)
        {

        }

        if (Dungeon.hero == null)
            Dungeon.initOnline();

        OnlineSync.Reset();
        Level level = new BetaRoomsLevel();
        Dungeon.level = level;
        level.create();
        Dungeon.switchLevel( level, level.randomRespawnCell() );
    }

    private void runMovie() throws Exception {

        try {
            GameLog.wipe();
        }
        catch (Exception ex)
        {

        }

        if (Dungeon.hero == null)
            Dungeon.initLegend();

        Level level = new MovieLevel();
        Dungeon.level = level;
        level.create();
        Dungeon.switchLevel( level, level.randomRespawnCell() );
    }

    private void endMovie() throws Exception {

    }

	private void descend() throws Exception {

		Actor.fixTime();
		if (Dungeon.hero == null) {
			Dungeon.init();
			if (noStory) {
				Dungeon.chapters.add( WndStory.ID_SEWERS );
				noStory = false;
			}
			GameLog.wipe();
		} else {
			saveLevel();
		}

		Level level;
		if (newFloorBelow()) {
			level = Dungeon.newLevel();
		} else {
			Dungeon.depth++;
			level = loadLevel();
		}
		Dungeon.switchLevel( level, level.entrance );
	}

	private void fall() throws Exception {
		Hero fallen = fallingHero;
		fallingHero = null;
		if (LocalCoop.contains(fallen)) LocalCoop.activate(fallen);

		Actor.fixTime();
		saveLevel();

		Level level;
		if (newFloorBelow()) {
			level = Dungeon.newLevel();
		} else {
			Dungeon.depth++;
			level = loadLevel();
		}
		int landing = fallIntoPit ? level.pitCell() : level.randomRespawnCell();
		if (LocalCoop.active() && !canLand(level, landing)) {
			landing = level.entrance;
			if (!canLand(level, landing)) {
				landing = -1;
				for (int cell = 0; cell < Level.LENGTH; cell++) {
					if (canLand(level, cell)) {
						landing = cell;
						break;
					}
				}
				if (landing == -1) throw new IOException("No safe place to land on this floor.");
			}
		}
		Dungeon.switchLevel(level, landing);
	}

	private boolean canLand(Level level, int cell) {
		if (cell < 0 || cell >= Level.LENGTH || !Level.passable[cell] || Level.pit[cell]) return false;
		for (Mob mob : level.mobs) {
			if (mob.isAlive() && mob.pos == cell) return false;
		}
		return true;
	}

    private void teleport() throws Exception {

        Actor.fixTime();
        saveLevel();

        Dungeon.depth = ColdGirl.FROST_DEPTH - 1;
        Level level = Dungeon.newLevel();
        int pos = level.randomRespawnCell();
        Dungeon.switchLevel(level, level.randomRespawnCell() );
    }

    private void teleport_back() throws Exception {
        Actor.fixTime();

        saveLevel();
        Dungeon.depth = ColdGirl.cameFrom;
        Level level = loadLevel();
        Dungeon.switchLevel( level, ColdGirl.cameFromPos );
    }

	private void ascend() throws Exception {
		Actor.fixTime();

		saveLevel();
		Dungeon.depth--;
		Level level = loadLevel();
		Dungeon.switchLevel( level, level.exit );
	}

	private void returnTo() throws Exception {

		Actor.fixTime();

		saveLevel();
		Dungeon.depth = returnDepth;
		Level level = loadLevel();
		Dungeon.switchLevel( level, Level.resizingNeeded ? level.adjustPos( returnPos ) : returnPos );
	}

	public static int continueSlot = com.bilboldev.pixeldungeonskills.SaveSlots.FIRST;

	private void restore() throws Exception {
		if (LocalCoop.active()) throw new IOException("Co-op expeditions cannot load solo saves.");

		Actor.fixTime();

		GameLog.wipe();
		Dungeon.loadGame(StartScene.curClass, continueSlot);
		if (Dungeon.depth == -1) {
			Dungeon.depth = Statistics.deepestFloor;
			Dungeon.switchLevel( Dungeon.loadLevel( StartScene.curClass ), -1 );
		} else {
			Level level = Dungeon.loadLevel( StartScene.curClass );
			Dungeon.switchLevel( level, Level.resizingNeeded ? level.adjustPos( Dungeon.hero.pos ) : Dungeon.hero.pos );
		}
	}

	private boolean newFloorBelow() {
		return LocalCoop.active() ? LocalCoop.level(Dungeon.depth + 1) == null
				: Dungeon.depth >= Statistics.deepestFloor;
	}

	private void saveLevel() throws IOException {
		if (LocalCoop.active()) {
			LocalCoop.rememberLevel(Dungeon.depth, Dungeon.level);
		} else {
			Dungeon.saveLevel();
		}
	}

	private Level loadLevel() throws IOException {
		if (!LocalCoop.active()) return Dungeon.loadLevel(Dungeon.hero.heroClass);
		Level level = LocalCoop.level(Dungeon.depth);
		if (level == null) throw new IOException("This co-op floor has not been visited.");
		Dungeon.level = null;
		Actor.clear();
		level.rebuildFlags();
		return level;
	}

	private void resurrect() throws Exception {

		Actor.fixTime();
		if (LocalCoop.active()) {
			Actor.clear();
			Dungeon.hero.resurrect(-1);
			Dungeon.hero.time = 0;
			Dungeon.hero.curAction = null;
			Dungeon.hero.restoreHealth = false;
			LocalCoop.cancelCommand(Dungeon.hero);
			Actor.init();
			Actor respawner = Dungeon.level.respawner();
			if (respawner != null) Actor.add(respawner);
			Dungeon.observe();
			return;
		}

		if (Dungeon.bossLevel()) {
			Dungeon.hero.resurrect( Dungeon.depth );
			Dungeon.depth--;
			Level level = Dungeon.newLevel();
			Dungeon.switchLevel( level, level.entrance );
		} else {
			Dungeon.hero.resurrect( -1 );
			Dungeon.resetLevel();
		}
	}

	@Override
	protected void onBackPressed() {

	}
}
