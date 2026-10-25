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

import java.util.ArrayList;

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.utils.Point;
import com.bilboldev.utils.PointF;

public class ScrollPane extends Component {

	private static final ArrayList<ScrollPane> DESKTOP_SCROLL_PANES = new ArrayList<ScrollPane>();
	private static final float DESKTOP_WHEEL_STEP_FACTOR = 0.25f;
	private static final float DESKTOP_SCROLLBAR_HIT_WIDTH = 8f;

	protected static final int THUMB_COLOR		= 0xFF7b8073;
	protected static final float THUMB_ALPHA	= 0.5f;

	protected TouchController controller;
	protected Component content;
	protected ColorBlock thumb;

	protected float minX;
	protected float minY;
	protected float maxX;
	protected float maxY;

	public ScrollPane( Component content ) {
		super();

		this.content = content;
		addToBack( content );

		width = content.width();
		height = content.height();

		content.camera = new Camera( 0, 0, 1, 1, PixelScene.defaultZoom );
		Camera.add( content.camera );
		DESKTOP_SCROLL_PANES.add( this );
	}

	@Override
	public void destroy() {
		DESKTOP_SCROLL_PANES.remove( this );
		super.destroy();
		Camera.remove( content.camera );
	}

	public void scrollTo( float x, float y ) {
		content.camera.scroll.set( x, y );
		clampScroll();
		updateThumbPosition();
	}

	public static boolean processMouseWheel( int screenX, int screenY, float amountY ) {
		if (amountY == 0f || !Window.isDesktopRuntime()) {
			return false;
		}

		Window modal = Window.topWindow();
		for (int i = DESKTOP_SCROLL_PANES.size() - 1; i >= 0; i--) {
			ScrollPane pane = DESKTOP_SCROLL_PANES.get( i );
			if (pane != null
					&& pane.isActive()
					&& pane.isVisible()
					&& pane.owningWindow() == modal
					&& pane.containsScreenPoint( screenX, screenY )) {
				pane.scrollByMouseWheel( amountY );

				return true;
			}
		}

		return modal != null;
	}

	private Window owningWindow() {
		for (Gizmo ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
			if (ancestor instanceof Window) return (Window)ancestor;
		}
		return null;
	}

	@Override
	protected void createChildren() {
		controller = createTouchController();
		add( controller );

		thumb = new ColorBlock( 1, 1, com.bilboldev.pixeldungeonskills.Presentation.reborn() ? 0xFFC0A57D : THUMB_COLOR );
		thumb.am = THUMB_ALPHA;
		add( thumb );
	}

	protected TouchController createTouchController() {
		return new TouchController();
	}

	@Override
	protected void layout() {

		content.setPos( 0, 0 );
		controller.x = x;
		controller.y = y;
		controller.width = width;
		controller.height = height;

		Point p = camera().cameraToScreen( x, y );
		Camera cs = content.camera;
		cs.x = p.x;
		cs.y = p.y;
		cs.zoom = camera().zoom;
		cs.resize( (int)width, (int)height );
		clampScroll();

		thumb.visible = height < content.height();
		if (thumb.visible) {
			thumb.scale.set( 2, height * height / content.height() );
			thumb.x = right() - thumb.width();
			updateThumbPosition();
		}
	}

	public Component content() {
		return content;
	}

	public void onClick( float x, float y ) {
	}

	private boolean canScrollVertically() {
		return content != null && content.height() > height;
	}

	private float maxScrollY() {
		return Math.max( 0f, content.height() - height );
	}

	private float thumbTravel() {
		return Math.max( 0f, height - thumb.height() );
	}

	private boolean scrollByMouseWheel( float amountY ) {
		if (!canScrollVertically()) {
			return false;
		}

		float previousY = content.camera.scroll.y;
		float scrollStep = Math.max( 12f, height * DESKTOP_WHEEL_STEP_FACTOR );
		scrollTo( content.camera.scroll.x, previousY + amountY * scrollStep );
		return content.camera.scroll.y != previousY;
	}

	private boolean containsScreenPoint( int screenX, int screenY ) {
		Camera paneCamera = camera();
		if (paneCamera == null) {
			return false;
		}

		PointF point = paneCamera.screenToCamera( screenX, screenY );
		return inside( point.x, point.y );
	}

	private void clampScroll() {
		Camera c = content.camera;
		float maxScrollX = Math.max( 0f, content.width() - width );
		float maxScrollY = maxScrollY();

		if (c.scroll.x > maxScrollX) {
			c.scroll.x = maxScrollX;
		}
		if (c.scroll.x < 0f) {
			c.scroll.x = 0f;
		}
		if (c.scroll.y > maxScrollY) {
			c.scroll.y = maxScrollY;
		}
		if (c.scroll.y < 0f) {
			c.scroll.y = 0f;
		}
	}

	private void updateThumbPosition() {
		if (thumb == null || !thumb.visible) {
			return;
		}

		float maxScrollY = maxScrollY();
		float thumbTravel = thumbTravel();
		thumb.y = y + (maxScrollY == 0f ? 0f : thumbTravel * content.camera.scroll.y / maxScrollY);
	}

	private boolean isInsideScrollbarTrack( float localX, float localY ) {
		if (!thumb.visible) {
			return false;
		}

		float trackLeft = right() - Math.max( thumb.width(), DESKTOP_SCROLLBAR_HIT_WIDTH );
		return localX >= trackLeft && localX < right() && localY >= y && localY < bottom();
	}

	private boolean isInsideThumb( float localX, float localY ) {
		if (!thumb.visible) {
			return false;
		}

		return localX >= thumb.x
				&& localX < thumb.x + thumb.width()
				&& localY >= thumb.y
				&& localY < thumb.y + thumb.height();
	}

	private void scrollThumbToLocalY( float localY ) {
		float maxScrollY = maxScrollY();
		float thumbTravel = thumbTravel();
		if (maxScrollY <= 0f || thumbTravel <= 0f) {
			return;
		}

		float thumbTop = Math.max( y, Math.min( localY - thumb.height() / 2f, y + thumbTravel ) );
		float ratio = (thumbTop - y) / thumbTravel;
		scrollTo( content.camera.scroll.x, maxScrollY * ratio );
	}

	private void dragThumbByScreenDelta( float screenDeltaY ) {
		float maxScrollY = maxScrollY();
		float thumbTravel = thumbTravel();
		Camera paneCamera = camera();
		if (maxScrollY <= 0f || thumbTravel <= 0f || paneCamera == null) {
			return;
		}

		float cameraDeltaY = screenDeltaY / paneCamera.zoom;
		float scrollDeltaY = cameraDeltaY * maxScrollY / thumbTravel;
		scrollTo( content.camera.scroll.x, content.camera.scroll.y + scrollDeltaY );
	}

	public class TouchController extends TouchArea {

		private float dragThreshold;
		private boolean draggingThumb = false;

		public TouchController() {
			super( 0, 0, 0, 0 );
			dragThreshold = PixelScene.defaultZoom * 8;
		}

		@Override
		protected void onTouchDown( Touch touch ) {
			if (!Window.isDesktopRuntime() || touch.longClick || !thumb.visible) {
				return;
			}

			Camera paneCamera = camera();
			if (paneCamera == null) {
				return;
			}

			PointF point = paneCamera.screenToCamera( (int)touch.start.x, (int)touch.start.y );
			if (!isInsideScrollbarTrack( point.x, point.y )) {
				return;
			}

			dragging = true;
			draggingThumb = true;
			lastPos.set( touch.current );
			thumb.am = 1f;

			if (!isInsideThumb( point.x, point.y )) {
				scrollThumbToLocalY( point.y );
			}
		}

		@Override
		protected void onClick( Touch touch ) {
			if (dragging) {

				dragging = false;
				draggingThumb = false;
				thumb.am = THUMB_ALPHA;

			} else {

				PointF p = content.camera.screenToCamera( (int)touch.current.x, (int)touch.current.y );
				ScrollPane.this.onClick( p.x, p.y );

			}
		}

		private boolean dragging = false;
		private PointF lastPos = new PointF();

		protected boolean isDragging() {
			return dragging;
		}

		@Override
		protected void onDrag( Touch t ) {
			if (dragging) {
				if (draggingThumb) {
					dragThumbByScreenDelta( t.current.y - lastPos.y );
					lastPos.set( t.current );
					return;
				}

				Camera c = content.camera;

				c.scroll.offset( PointF.diff( lastPos, t.current ).invScale( c.zoom ) );
				clampScroll();
				updateThumbPosition();

				lastPos.set( t.current );

			} else if (PointF.distance( t.current, t.start ) > dragThreshold) {

				dragging = true;
				draggingThumb = false;
				lastPos.set( t.current );
				thumb.am = 1;

			}
		}
	}
}
