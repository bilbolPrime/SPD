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
package com.bilboldev.pixeldungeonskills.ui;

import android.content.Intent;
import android.net.Uri;
import java.util.Locale;
import java.util.ArrayList;
import java.net.URI;
import com.bilboldev.input.Keys;
import com.bilboldev.input.Keys.Key;
import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.Visual;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.effects.ShadowBox;
import com.bilboldev.pixeldungeonskills.network.PartyWindows;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.utils.Signal;

public class Window extends Group implements Signal.Listener<Key> {

	private static final boolean DESKTOP_RUNTIME = detectDesktopRuntime();
	private static final float DESKTOP_OVERLAY_TEXT_SCALE = 0.75f;
	private static final ArrayList<Window> WINDOWS = new ArrayList<>();

	protected int width;
	protected int height;

	protected TouchArea blocker;
	protected ShadowBox shadow;
	protected NinePatch chrome;
	private ColorBlock modalDim;
	private boolean partyWindow;
	protected MenuModel menuModel;

	public MenuModel menuModel() {
		return menuModel;
	}

	public static final int TITLE_COLOR = 0xFFFF44;
	public static int titleColor() {
		return Presentation.reborn() ? 0xD6BE98 : TITLE_COLOR;
	}

	public static boolean landscapeLayout() {
		return Presentation.reborn() ? Game.width >= Game.height : com.bilboldev.pixeldungeonskills.PixelDungeon.landscape();
	}

	protected RebornChrome.Material rebornMaterial() {
		return RebornChrome.Material.STONE;
	}

	public Window() {
		this( 0, 0, Chrome.get( Chrome.Type.WINDOW ) );
	}

	public Window( int width, int height ) {
		this( width, height, Chrome.get( Chrome.Type.WINDOW ) );
	}

	public Window( int width, int height, NinePatch chrome ) {
		super();

		blocker = new TouchArea( 0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height ) {
			@Override
			protected void onClick( Touch touch ) {
				if (!Window.this.chrome.overlapsScreenPoint(
					(int)touch.current.x,
					(int)touch.current.y )) {

					onBackPressed();
				}
			}
		};
		blocker.camera = PixelScene.uiCamera;
		add( blocker );

		this.chrome = chrome;
		if (chrome instanceof RebornChrome) {
			((RebornChrome)chrome).material(rebornMaterial());
			modalDim = new ColorBlock(PixelScene.uiCamera.width, PixelScene.uiCamera.height, 0x76060910);
			modalDim.camera = PixelScene.uiCamera;
			addToBack(modalDim);
		}

		this.width = width;
		this.height = height;

		shadow = new ShadowBox();
		shadow.am = 0.5f;
		shadow.camera = PixelScene.uiCamera.visible ?
			PixelScene.uiCamera : Camera.main;
		add( shadow );

		chrome.x = -chrome.marginLeft();
		chrome.y = -chrome.marginTop();
		chrome.size(
			this.width - chrome.x + chrome.marginRight(),
			this.height - chrome.y + chrome.marginBottom() );
		add( chrome );

		camera = new Camera( 0, 0,
			(int)chrome.width,
			(int)chrome.height,
			PixelScene.defaultZoom );
		camera.x = (int)(Game.width - camera.width * camera.zoom) / 2;
		camera.y = (int)(Game.height - camera.height * camera.zoom) / 2;
		camera.scroll.set( chrome.x, chrome.y );
		Camera.add( camera );

		shadow.boxRect(
			camera.x / camera.zoom,
			camera.y / camera.zoom,
			chrome.width(), chrome.height );

		Keys.event.add( this );
		WINDOWS.add( this );
	}

	@Override
	public void draw() {

		if (modalDim != null) modalDim.visible = chrome.visible && topWindow() == this;
		super.draw();
	}

	public void resize( int w, int h ) {
		this.width = w;
		this.height = h;

		chrome.size(
			width + chrome.marginHor(),
			height + chrome.marginVer() );

		camera.resize( (int)chrome.width, (int)chrome.height );
		camera.x = (int)(Game.width - camera.screenWidth()) / 2;
		camera.y = (int)(Game.height - camera.screenHeight()) / 2;

		shadow.boxRect( camera.x / camera.zoom, camera.y / camera.zoom, chrome.width(), chrome.height );
	}

	public void hide() {
		try{
			parent.erase( this );
			destroy();
		}
		catch (Exception e){

		}
	}

	public void detachForParty() {
		partyWindow = true;
		WINDOWS.remove(this);
		Keys.event.remove(this);
	}

	public boolean isPartyWindow() {
		return partyWindow;
	}

	@Override
	public Gizmo add(Gizmo child) {
		if (partyWindow && child instanceof Window
				&& PartyWindows.capture((Window)child)) return child;
		super.add(child);
		if (child instanceof Window) bringToFront(child);
		return child;
	}

	@Override
	public void destroy() {
		WINDOWS.remove( this );
		super.destroy();

		Camera.remove( camera );
		Keys.event.remove( this );
	}

	@Override
	public void onSignal( Key key ) {
		if (key.pressed) {
			switch (key.code) {
			case Keys.BACK:
				onBackPressed();
				break;
			case Keys.MENU:
				onMenuPressed();
				break;
			}
		}

		Keys.event.cancel();
	}

	public void onBackPressed() {
		hide();
	}

	public void onMenuPressed() {
	}

	public static boolean isDesktopRuntime() {
		return DESKTOP_RUNTIME;
	}

	public static Window topWindow() {
		for (int i = WINDOWS.size() - 1; i >= 0; i--) {
			Window window = WINDOWS.get(i);
			if (!window.exists || !window.isActive() || !window.isVisible()) continue;
			for (Gizmo ancestor = window.parent; ancestor != null; ancestor = ancestor.parent) {
				if (ancestor == Game.scene()) return window;
			}
		}
		return null;
	}

	public static boolean openUrl( String url ) {
		try {
			if (url == null || url.isEmpty()) {
				return false;
			}

			if (isDesktopRuntime()) {
				Class<?> desktopClass = Class.forName( "java.awt.Desktop" );
				Object desktop = desktopClass.getMethod( "getDesktop" ).invoke( null );
				Class<?> actionClass = Class.forName( "java.awt.Desktop$Action" );
				Object browseAction = actionClass.getField( "BROWSE" ).get( null );
				boolean supported = (Boolean) desktopClass.getMethod( "isSupported", actionClass )
					.invoke( desktop, browseAction );
				if (!supported) {
					return false;
				}
				desktopClass.getMethod( "browse", URI.class ).invoke( desktop, new URI( url ) );
				return true;
			}

			Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
			Game.instance.startActivity( intent );
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	protected boolean usesDesktopHeightConstraint() {
		return false;
	}

	protected final boolean shouldLimitOverlayHeight() {
		return false;
	}

	protected final int maxOverlayHeight() {
		return Integer.MAX_VALUE;
	}

	protected final int maxContentHeight() {
		return Integer.MAX_VALUE;
	}

	protected final boolean shouldConstrainContentHeight( int requestedContentHeight ) {
		return false;
	}

	protected final boolean shouldConstrainOverlayHeight( int requestedOverlayHeight ) {
		return false;
	}

	protected final int constrainContentHeight( int requestedHeight ) {
		return requestedHeight;
	}

	protected float overlayTextScale() {
		return 1f;
	}

	protected final float overlayTextSize( float size ) {
		return isDesktopRuntime() ? size * overlayTextScale() : size;
	}

	protected final float overlayTextDrawScale() {
		return isDesktopRuntime() ? overlayTextScale() : 1f;
	}

	protected final void scaleOverlayText( Visual visual ) {
		float scale = overlayTextDrawScale();
		if (scale != 1f) {
			visual.scale.set( visual.scale.x * scale, visual.scale.y * scale );
		}
	}

	protected final int overlayTextSize( int size ) {
		return Math.max( 1, Math.round( overlayTextSize( (float)size ) ) );
	}

	protected final float desktopOverlayTextScale() {
		return DESKTOP_OVERLAY_TEXT_SCALE;
	}

	private static boolean detectDesktopRuntime() {
		String runtimeInfo = (
			System.getProperty( "java.vm.name", "" ) + " " +
			System.getProperty( "java.runtime.name", "" ) + " " +
			System.getProperty( "java.vendor", "" )
		).toLowerCase( Locale.ENGLISH );

		return runtimeInfo.indexOf( "android" ) < 0 && runtimeInfo.indexOf( "dalvik" ) < 0;
	}
}
