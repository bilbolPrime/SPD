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

package com.bilboldev.utils;

import java.util.Collection;
import java.util.HashMap;

public class Random {

	private static final ThreadLocal<java.util.Random> source = new ThreadLocal<>();

	public static final class Scope implements AutoCloseable {
		private final java.util.Random previous;

		private Scope(long seed) {
			previous = source.get();
			source.set(new java.util.Random(seed));
		}

		@Override public void close() {
			if (previous == null) source.remove();
			else source.set(previous);
		}
	}

	public static Scope use(long seed) {
		return new Scope(seed);
	}

	public static boolean seeded() {
		return source.get() != null;
	}

	public static double Double() {
		java.util.Random random = source.get();
		return random == null ? Math.random() : random.nextDouble();
	}

	public static long Long() {
		java.util.Random random = source.get();
		return random == null ? java.util.concurrent.ThreadLocalRandom.current().nextLong() : random.nextLong();
	}

	public static float Float( float min, float max ) {
		return (float)(min + Double() * (max - min));
	}

	public static float Float( float max ) {
		return (float)(Double() * max);
	}

	public static float Float() {
		return (float)Double();
	}

	public static int Int( int max ) {
		return max > 0 ? (int)(Double() * max) : 0;
	}

	public static int Int( int min, int max ) {
		return min + (int)(Double() * (max - min));
	}

	public static int IntRange( int min, int max ) {
		return min + (int)(Double() * (max - min + 1));
	}

	public static int NormalIntRange( int min, int max ) {
		return min + (int)((Double() + Double()) * (max - min + 1) / 2f);
	}

	public static int chances( float[] chances ) {

		int length = chances.length;

		float sum = chances[0];
		for (int i=1; i < length; i++) {
			sum += chances[i];
		}

		float value = Float( sum );
		sum = chances[0];
		for (int i=0; i < length; i++) {
			if (value < sum) {
				return i;
			}
			sum += chances[i + 1];
		}

		return 0;
	}

	@SuppressWarnings("unchecked")
	public static <K> K chances( HashMap<K,Float> chances ) {

		int size = chances.size();

		Object[] values = chances.keySet().toArray();
		float[] probs = new float[size];
		float sum = 0;
		for (int i=0; i < size; i++) {
			probs[i] = chances.get( values[i] );
			sum += probs[i];
		}

		float value = Float( sum );

		sum = probs[0];
		for (int i=0; i < size; i++) {
			if (value < sum) {
				return (K)values[i];
			}
			sum += probs[i + 1];
		}

		return null;
	}

	public static int index( Collection<?> collection ) {
		return (int)(Double() * collection.size());
	}

	@SafeVarargs
	public static<T> T oneOf( T... array ) {
		return array[(int)(Double() * array.length)];
	}

	public static<T> T element( T[] array ) {
		return element( array, array.length );
	}

	public static<T> T element( T[] array, int max ) {
		return array[(int)(Double() * max)];
	}

	@SuppressWarnings("unchecked")
	public static<T> T element( Collection<? extends T> collection ) {
		int size = collection.size();
		return size > 0 ?
			(T)collection.toArray()[Int( size )] :
			null;
	}

	public static<T> void shuffle( T[] array ) {
		for (int i=0; i < array.length - 1; i++) {
			int j = Int( i, array.length );
			if (j != i) {
				T t = array[i];
				array[i] = array[j];
				array[j] = t;
			}
		}
	}

	public static<U,V> void shuffle( U[] u, V[]v ) {
		for (int i=0; i < u.length - 1; i++) {
			int j = Int( i, u.length );
			if (j != i) {
				U ut = u[i];
				u[i] = u[j];
				u[j] = ut;

				V vt = v[i];
				v[i] = v[j];
				v[j] = vt;
			}
		}
	}
}
