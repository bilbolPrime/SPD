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

import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;

import com.bilboldev.pixeldungeonskills.messages.Languages;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.AndroidNetworkAccess;
import com.bilboldev.pixeldungeonskills.network.NetworkAccess;
import com.bilboldev.pixeldungeonskills.network.PartyPrompts;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.online.OnlineSync;
import com.bilboldev.pixeldungeonskills.online.ServerConnection;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;

import java.util.Locale;

public class PixelDungeon extends Game {
	private AndroidNetworkAccess networkAccess;
	private boolean resumed, windowFocused;

	@Override
	protected Class<? extends Scene> resolveScene(Class<? extends Scene> sceneClass) {
		return Presentation.menuScene(sceneClass);
	}

	public PixelDungeon() {
		super( TitleScene.class );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade.class,
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfEnhancement" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfHealth.class,
			"com.watabou.pixeldungeon.actors.blobs.Light" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.rings.RingOfMending.class,
			"com.watabou.pixeldungeon.items.rings.RingOfRejuvenation" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.wands.WandOfReach.class,
			"com.watabou.pixeldungeon.items.wands.WandOfTelekenesis" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.actors.blobs.Foliage.class,
			"com.watabou.pixeldungeon.actors.blobs.Blooming" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.actors.buffs.Shadows.class,
			"com.watabou.pixeldungeon.actors.buffs.Rejuvenation" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfPsionicBlast.class,
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfNuclearBlast" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.actors.hero.Hero.class,
			"com.watabou.pixeldungeon.actors.Hero" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Shopkeeper.class,
			"com.watabou.pixeldungeon.actors.mobs.Shopkeeper" );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.quest.DriedRose.class,
			"com.watabou.pixeldungeon.items.DriedRose" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MirrorImage.class,
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfMirrorImage$MirrorImage" );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.rings.RingOfElements.class,
			"com.watabou.pixeldungeon.items.rings.RingOfCleansing" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.rings.RingOfElements.class,
			"com.watabou.pixeldungeon.items.rings.RingOfResistance" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.weapon.missiles.Boomerang.class,
			"com.watabou.pixeldungeon.items.weapon.missiles.RangersBoomerang" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.rings.RingOfPower.class,
			"com.watabou.pixeldungeon.items.rings.RingOfEnergy" );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.plants.Dreamweed.class,
			"com.watabou.pixeldungeon.plants.Blindweed" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.plants.Dreamweed.Seed.class,
			"com.watabou.pixeldungeon.plants.Blindweed$Seed" );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Shock.class,
			"com.watabou.pixeldungeon.items.weapon.enchantments.Piercing" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Shock.class,
			"com.watabou.pixeldungeon.items.weapon.enchantments.Swing" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfEnchantment.class,
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade" );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfEnchantment.class,
			"com.watabou.pixeldungeon.items.Stylus" );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.actors.mobs.FetidRat.class,
			"com.watabou.pixeldungeon.actors.mobs.npcs.Ghost$FetidRat" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.plants.Rotberry.class,
			"com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker$Rotberry" );
		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.plants.Rotberry.Seed.class,
			"com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker$Rotberry$Seed" );

		com.bilboldev.utils.Bundle.addAlias(
			com.bilboldev.pixeldungeonskills.items.wands.WandOfReach.class,
			"com.watabou.pixeldungeon.items.wands.WandOfTelekinesis" );
	}

	@Override
	public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
		super.onRequestPermissionsResult(request, permissions, grants);
		if (networkAccess != null) networkAccess.onRequestPermissionsResult(request, permissions, grants);
	}

	@Override
	public void onDestroy() {
		PartySession.setForeground(false);
		if (networkAccess != null) networkAccess.close();
		PartySession.cancelLobby();
		super.onDestroy();
	}

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		PartySession.setForeground(false);
		super.onCreate( savedInstanceState );
		networkAccess = new AndroidNetworkAccess(this);
		NetworkAccess.install(networkAccess);
		PartyPrompts.install(networkAccess);
		Messages.setup( language() );

		updateImmersiveMode();

		DisplayMetrics metrics = new DisplayMetrics();
		instance.getWindowManager().getDefaultDisplay().getMetrics( metrics );
		boolean landscape = metrics.widthPixels > metrics.heightPixels;

		if (Preferences.INSTANCE.getBoolean( Preferences.KEY_LANDSCAPE, false ) != landscape) {
			landscape( !landscape );
		}

		Presentation.applyAudio();

		Sample.INSTANCE.load(
			Assets.SND_CLICK,
			Assets.SND_BADGE,
			Assets.SND_GOLD,

			Assets.SND_DESCEND,
			Assets.SND_STEP,
			Assets.SND_WATER,
			Assets.SND_OPEN,
			Assets.SND_UNLOCK,
			Assets.SND_ITEM,
			Assets.SND_DEWDROP,
			Assets.SND_HIT,
			Assets.SND_MISS,
			Assets.SND_EAT,
			Assets.SND_READ,
			Assets.SND_LULLABY,
			Assets.SND_DRINK,
			Assets.SND_SHATTER,
			Assets.SND_ZAP,
			Assets.SND_LIGHTNING,
			Assets.SND_LEVELUP,
			Assets.SND_DEATH,
			Assets.SND_CHALLENGE,
			Assets.SND_CURSED,
			Assets.SND_EVOKE,
			Assets.SND_TRAP,
			Assets.SND_TOMB,
			Assets.SND_ALERT,
			Assets.SND_MELD,
			Assets.SND_BOSS,
			Assets.SND_BLAST,
			Assets.SND_PLANT,
			Assets.SND_RAY,
			Assets.SND_BEACON,
			Assets.SND_TELEPORT,
			Assets.SND_CHARMS,
			Assets.SND_MASTERY,
			Assets.SND_PUFF,
			Assets.SND_ROCKS,
			Assets.SND_BURNING,
			Assets.SND_FALLING,
			Assets.SND_GHOST,
			Assets.SND_SECRET,
			Assets.SND_BONES,
			Assets.SND_BEE,
			Assets.SND_DEGRADE,
			Assets.SND_MIMIC );
	}

	@Override
	public void onResume() {
		resumed = true;
		PartySession.setForeground(windowFocused);
		super.onResume();
	}

	@Override
	public void onPause() {
		resumed = false;
		PartySession.setForeground(false);
		super.onPause();
	}

	@Override
	protected void onStop() {
		PartySession.setForeground(false);
		super.onStop();
	}

	@Override
	public void onWindowFocusChanged( boolean hasFocus ) {
		windowFocused = hasFocus;
		PartySession.setForeground(resumed && hasFocus);

		super.onWindowFocusChanged( hasFocus );

		if (hasFocus) {
			updateImmersiveMode();
		}
	}

	public static void switchNoFade( Class<? extends PixelScene> c ) {
		PixelScene.noFade = true;
		switchScene( c );
	}

	public static void landscape( boolean value ) {
		Game.instance.setRequestedOrientation( value ?
			ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
			ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
		Preferences.INSTANCE.put( Preferences.KEY_LANDSCAPE, value );
		Game.instance.reloadCache();
	}

	public static boolean landscape() {
		return width > height;
	}

	private static boolean immersiveModeChanged = false;

	@SuppressLint("NewApi")
	public static void immerse( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_IMMERSIVE, value );

		instance.runOnUiThread( new Runnable() {
			@Override
			public void run() {
				updateImmersiveMode();
				immersiveModeChanged = true;
			}
		} );
	}

	@Override
	public void onSurfaceChanged( GL10 gl, int width, int height ) {
		super.onSurfaceChanged( gl, width, height );

		if (immersiveModeChanged) {
			requestedReset = true;
			immersiveModeChanged = false;
		}
	}

	@SuppressLint("NewApi")
	public static void updateImmersiveMode() {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			try {

				instance.getWindow().getDecorView().setSystemUiVisibility(
					immersed() ?
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
					View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
					View.SYSTEM_UI_FLAG_FULLSCREEN |
					View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					:
					0 );
			} catch (Exception e) {
				reportException( e );
			}
		}
	}

	public static boolean immersed() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_IMMERSIVE, true );
	}

	public static void uiScale(int value) {
		Preferences.INSTANCE.put(Presentation.reborn() ? "reborn_ui_scale" : "ui_scale", value);
	}

	public static int uiScale() {
		int value = Preferences.INSTANCE.getInt("ui_scale", 0);
		return Presentation.reborn() ? Presentation.initialSetting("reborn_ui_scale", value) : value;
	}

	public static void zoom( int value ) {
		Preferences.INSTANCE.put( Presentation.reborn() ? "reborn_zoom" : Preferences.KEY_ZOOM, value );
	}

	public static int zoom() {
		int value = Preferences.INSTANCE.getInt( Preferences.KEY_ZOOM, 0 );
		return Presentation.reborn() ? Presentation.initialSetting("reborn_zoom", value) : value;
	}

	public static void music( boolean value ) {
		Presentation.musicVolume(value ? 100 : 0);
	}

	public static boolean titleAnimation() {
		return Preferences.INSTANCE.getBoolean("title_animation", true);
	}

	public static void titleAnimation(boolean enabled) {
		Preferences.INSTANCE.put("title_animation", enabled);
	}

	public static boolean music() {
		return Presentation.musicVolume() > 0;
	}

	public static void soundFx( boolean value ) {
		Presentation.soundVolume(value ? 100 : 0);
	}

	public static boolean soundFx() {
		return Presentation.soundVolume() > 0;
	}

	public static void language( Languages value ) {
		Preferences.INSTANCE.put( Preferences.KEY_LANGUAGE, value.code() );
		Messages.setup( value );
	}

	public static Languages language() {
		String code = Preferences.INSTANCE.getString( Preferences.KEY_LANGUAGE, null );
		if (code == null) {
			return Languages.matchLocale( Locale.getDefault() );
		}
		return Languages.matchCode( code );
	}

    public static void itemDeg( boolean value ) {
        Preferences.INSTANCE.put( Preferences.KEY_DEGRADATION, value );
    }

    public static boolean itemDeg() {
        return Preferences.INSTANCE.getBoolean( Preferences.KEY_DEGRADATION, true );
    }

	public static boolean skillFontSize() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_SKILL_FONT_SIZE, true );
	}

	public static void skillFontSize(boolean value) {
		Preferences.INSTANCE.put( Preferences.KEY_SKILL_FONT_SIZE, value );
	}

	public static void brightness( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_BRIGHTNESS, value );
		if (scene() instanceof GameScene) {
			((GameScene)scene()).brightness( value );
		}
	}

	public static boolean brightness() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_BRIGHTNESS, false );
	}

	public static void brightnessNew( int value ) {
		Preferences.INSTANCE.put( Preferences.KEY_BRIGHTNESS_NEW, value );
		if (scene() instanceof GameScene) {
			((GameScene)scene()).brightness( value );
		}
	}

	public static int brightnessNew() {
		int toReturn = Preferences.INSTANCE.getInt( Preferences.KEY_BRIGHTNESS_NEW, -10 );
		if(toReturn == -10){
			return brightness() ? 2 : -2;
		}

		return toReturn;
	}

	public static boolean secondQuickSlot() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_SECOND_QUICK_SLOT, false );
	}

	public static void secondQuickSlot( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_SECOND_QUICK_SLOT, value );
	}

	public static boolean supportsWindowedMode() {
		return false;
	}

	public static boolean supportsBorderlessWindowedMode() {
		return false;
	}

	public static void windowedMode(boolean value) {
	}

	public static boolean windowedMode() {
		return false;
	}

	public static void borderlessWindowedMode(boolean value) {
	}

	public static boolean borderlessWindowedMode() {
		return false;
	}

	public static void enabled3d( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_ENABLED_3D, value );
	}

	public static boolean enabled3d() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_ENABLED_3D, true );
	}

	public static void donated( int value ) {
		Preferences.INSTANCE.put( Preferences.KEY_DONATED, value );
	}

	public static int donated() {
		return Preferences.INSTANCE.getInt( Preferences.KEY_DONATED, 0 );
	}

    public static void maidenUnlocked( Boolean value ) {
        Preferences.INSTANCE.put( Preferences.KEY_ARCHER_MAIDEN, value );
    }

    public static boolean maidenUnlocked() {
        return Preferences.INSTANCE.getBoolean( Preferences.KEY_ARCHER_MAIDEN, false );
    }

    public static void disableChampionsUnlocked( Boolean value ) {
        Preferences.INSTANCE.put( Preferences.KEY_DISABLE_CHAMPIONS, value );
    }

    public static boolean disableChampionsUnlocked() {
        return Preferences.INSTANCE.getBoolean( Preferences.KEY_DISABLE_CHAMPIONS, false );
    }

	public static void lastClass( int value ) {
		Preferences.INSTANCE.put( Preferences.KEY_LAST_CLASS, value );
	}

	public static int lastClass() {
		return Preferences.INSTANCE.getInt( Preferences.KEY_LAST_CLASS, 0 );
	}

	public static void challenges( int value ) {
		Preferences.INSTANCE.put( Preferences.KEY_CHALLENGES, value );
	}

	public static int challenges() {
		return Preferences.INSTANCE.getInt( Preferences.KEY_CHALLENGES, 0 );
	}

	public static void intro( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_INTRO, value );
	}

	public static boolean intro() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_INTRO, true );
	}

	public static void reportException( Throwable tr ) {
		Log.e( "PD", Log.getStackTraceString( tr ) );
	}
}
