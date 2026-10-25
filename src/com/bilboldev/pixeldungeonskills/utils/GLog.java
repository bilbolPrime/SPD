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
package com.bilboldev.pixeldungeonskills.utils;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.utils.Signal;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import android.util.Log;

import com.bilboldev.pixeldungeonskills.network.PartySession;

public class GLog {

	public static final String TAG = "GAME";

	public static final String POSITIVE		= "++ ";
	public static final String NEGATIVE		= "-- ";
	public static final String WARNING		= "** ";
	public static final String HIGHLIGHT	= "@@ ";

	public static Signal<String> update = new Signal<String>();

	public static void localI( String text, Object... args ) {
		dispatch( "", text, args );
	}

	public static void localP( String text, Object... args ) {
		dispatch( POSITIVE, text, args );
	}

	public static void localN( String text, Object... args ) {
		dispatch( NEGATIVE, text, args );
	}

	public static void localW( String text, Object... args ) {
		dispatch( WARNING, text, args );
	}

	public static void localH( String text, Object... args ) {
		dispatch( HIGHLIGHT, text, args );
	}

	private static void dispatch( String prefix, String text, Object... args ) {
		String localized = args.length > 0 ? Messages.maybeTranslate( text, args ) : Messages.maybeTranslate( text );
		String output = prefix + localized;
		Log.i( TAG, output );
		update.dispatch( output );
	}

	public static void i(Message text) {
		dispatch("", text);
	}
	public static void p(Message text) {
		dispatch(POSITIVE, text);
	}
	public static void n(Message text) {
		dispatch(NEGATIVE, text);
	}
	public static void w(Message text) {
		dispatch(WARNING, text);
	}
	public static void h(Message text) {
		dispatch(HIGHLIGHT, text);
	}

	public static void i(String key, Object... args) {
		i(Message.key(key, args));
	}
	public static void p(String key, Object... args) {
		p(Message.key(key, args));
	}
	public static void n(String key, Object... args) {
		n(Message.key(key, args));
	}
	public static void w(String key, Object... args) {
		w(Message.key(key, args));
	}
	public static void h(String key, Object... args) {
		h(Message.key(key, args));
	}

	public static void i(Class<?> type, String field, Object... args) {
		i(Message.key(type, field, args));
	}
	public static void p(Class<?> type, String field, Object... args) {
		p(Message.key(type, field, args));
	}
	public static void n(Class<?> type, String field, Object... args) {
		n(Message.key(type, field, args));
	}
	public static void w(Class<?> type, String field, Object... args) {
		w(Message.key(type, field, args));
	}
	public static void h(Class<?> type, String field, Object... args) {
		h(Message.key(type, field, args));
	}

	private static void dispatch(String prefix, Message text) {
		PartySession.log(prefix, text);
		String output = prefix + text.toString();
		Log.i(TAG, output);
		update.dispatch(output);
	}
}
