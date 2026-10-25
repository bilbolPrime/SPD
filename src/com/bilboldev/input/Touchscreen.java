/*
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

package com.bilboldev.input;

import java.util.ArrayList;
import java.util.HashMap;

import com.bilboldev.utils.PointF;
import com.bilboldev.utils.Signal;

import android.view.MotionEvent;

public class Touchscreen {

	public static Signal<Touch> event = new Signal<Touch>( true );

	public static HashMap<Integer,Touch> pointers = new HashMap<Integer, Touch>();

	public static float x;
	public static float y;
	public static boolean touched;

	public static void processTouchDown( int pointerId, float x, float y ) {
		processTouchDown( pointerId, x, y, false );
	}

	public static void processTouchDown( int pointerId, float x, float y, boolean longClick ) {
		touched = true;
		Touch touch = new Touch( x, y, longClick );
		pointers.put( pointerId, touch );
		event.dispatch( touch );
	}

	public static void processLongClick( int pointerId, float x, float y ) {
		processTouchDown( pointerId, x, y, true );
		processTouchUp( pointerId );
	}

	public static void processTouchMove( int pointerId, float x, float y ) {
		Touch touch = pointers.get( pointerId );
		if (touch != null) {
			touch.update( x, y );
			event.dispatch( null );
		}
	}

	public static void processTouchUp( int pointerId ) {
		Touch touch = pointers.remove( pointerId );
		if (touch != null) {
			touched = !pointers.isEmpty();
			event.dispatch( touch.up() );
		}
	}

	public static void cancelTouch( int pointerId ) {
		Touch touch = pointers.remove( pointerId );
		if (touch != null) {
			touched = !pointers.isEmpty();
			event.dispatch( touch.cancel() );
		}
	}

	public static void cancelAllTouches() {
		if (pointers.isEmpty()) {
			touched = false;
			return;
		}

		ArrayList<Integer> pointerIds = new ArrayList<Integer>( pointers.keySet() );
		for (int pointerId : pointerIds) {
			cancelTouch( pointerId );
		}
	}

	public static void processTouchEvents( ArrayList<MotionEvent> events ) {

		try {
			int size = events.size();
			for (int i = 0; i < size; i++) {

				MotionEvent e = events.get(i);
				Touch touch;

				switch (e.getAction() & MotionEvent.ACTION_MASK) {

					case MotionEvent.ACTION_DOWN:
						processTouchDown(e.getPointerId(0), e.getX(0), e.getY(0));
						break;

					case MotionEvent.ACTION_POINTER_DOWN:
						int index = e.getActionIndex();
						processTouchDown(e.getPointerId(index), e.getX(index), e.getY(index));
						break;

					case MotionEvent.ACTION_MOVE:
						int count = e.getPointerCount();
						for (int j = 0; j < count; j++) {
							touch = pointers.get(e.getPointerId(j));
							if (touch != null) {
								touch.update(e.getX(j), e.getY(j));
							}
						}
						event.dispatch(null);
						break;

					case MotionEvent.ACTION_POINTER_UP:
						processTouchUp(e.getPointerId(e.getActionIndex()));
						break;

					case MotionEvent.ACTION_UP:
						processTouchUp(e.getPointerId(0));
						break;

				}

				e.recycle();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public static class Touch {

		public PointF start;
		public PointF current;
		public boolean down;
        public boolean cancelled;
        public boolean longClick;

		public Touch( float x, float y ) {
			this( x, y, false );
		}

		public Touch( float x, float y, boolean longClick ) {
			start = new PointF( x, y );
			current = new PointF( x, y );
			down = true;
			cancelled = false;
			this.longClick = longClick;
		}

		public Touch( MotionEvent e, int index ) {
			this( e.getX( index ), e.getY( index ) );
		}

		public void update( MotionEvent e, int index ) {
			update( e.getX( index ), e.getY( index ) );
		}

		public void update( float x, float y ) {
			current.set( x, y );
		}

		public Touch up() {
			down = false;
			cancelled = false;
			return this;
		}

		public Touch cancel() {
			down = false;
			cancelled = true;
			return this;
		}
	}

}
