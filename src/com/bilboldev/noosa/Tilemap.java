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

package com.bilboldev.noosa;

import java.nio.FloatBuffer;
import java.util.Arrays;

import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.glwrap.Quad;
import com.bilboldev.glwrap.Vertexbuffer;
import com.bilboldev.utils.Rect;
import com.bilboldev.utils.RectF;


public class Tilemap extends Visual {

	protected SmartTexture texture;
	protected TextureFilm tileset;

	protected int[] data;
	protected int mapWidth;
	protected int mapHeight;
	protected int size;

	private float cellW;
	private float cellH;

	protected float[] vertices;
	protected FloatBuffer quads;
	protected Vertexbuffer buffer;

	private volatile Rect updated;
	private boolean fullUpdate;
	private Rect updating;
	private int topLeftUpdating;
	private int bottomRightUpdating;

	public Tilemap( Object tx, TextureFilm tileset ) {

		super( 0, 0, 0, 0 );

		this.texture = TextureCache.get( tx );
		this.tileset = tileset;

		RectF r = tileset.get( 0 );
		cellW = tileset.width( r );
		cellH = tileset.height( r );

		vertices = new float[16];

		updated = new Rect();
	}

	public void map( int[] data, int cols ) {

		this.data = data;

		mapWidth = cols;
		mapHeight = data.length / cols;
		size = mapWidth * mapHeight;

		width = cellW * mapWidth;
		height = cellH * mapHeight;

		quads = Quad.createSet( size );

		updateMap();
	}

	//forces a full update, including new buffer
	public synchronized void updateMap(){
		updated.set( 0, 0, mapWidth, mapHeight );
		fullUpdate = true;
	}

	public synchronized void updateMapCell(int cell){
		updated.union( cell % mapWidth, cell / mapWidth );
	}

	private synchronized void moveToUpdating(){
		updating = new Rect(updated);
		updated.setEmpty();
	}

	protected void updateVertices() {

		moveToUpdating();

		float x1, y1, x2, y2;
		int pos;
		RectF uv;

		y1 = cellH * updating.top;
		y2 = y1 + cellH;

		for (int i=updating.top; i < updating.bottom; i++) {

			x1 = cellW * updating.left;
			x2 = x1 + cellW;

			pos = i * mapWidth + updating.left;

			for (int j=updating.left; j < updating.right; j++) {

				if (topLeftUpdating == -1)
					topLeftUpdating = pos;

				bottomRightUpdating = pos + 1;

				quads.position(pos*16);

				uv = tileset.get(data[pos]);

				if (needsRender(pos) && uv != null) {

					vertices[0] = x1;
					vertices[1] = y1;

					vertices[2] = uv.left;
					vertices[3] = uv.top;

					vertices[4] = x2;
					vertices[5] = y1;

					vertices[6] = uv.right;
					vertices[7] = uv.top;

					vertices[8] = x2;
					vertices[9] = y2;

					vertices[10] = uv.right;
					vertices[11] = uv.bottom;

					vertices[12] = x1;
					vertices[13] = y2;

					vertices[14] = uv.left;
					vertices[15] = uv.bottom;

				} else {

					//If we don't need to draw this tile simply set the quad to size 0 at 0, 0.
					// This does result in the quad being drawn, but we are skipping all
					// pixel-filling. This is better than fully skipping rendering as we
					// don't need to manage a buffer of drawable tiles with insertions/deletions.
					Arrays.fill(vertices, 0);
				}

				quads.put(vertices);

				pos++;
				x1 = x2;
				x2 += cellW;

			}

			y1 = y2;
			y2 += cellH;
		}

	}

	private int camX, camY, camW, camH;
	private int topLeft, bottomRight, length;

	@Override
	public void draw() {

		super.draw();

		NoosaScript script = NoosaScript.get();

		texture.bind();

		script.uModel.valueM4( matrix );
		script.lighting(
				rm, gm, bm, am,
				ra, ga, ba, aa );

		if (!updated.isEmpty()) {
			updateVertices();
		}

		script.camera( camera );
		script.drawQuadSet( quads, size );

	}

	@Override
	public void destroy() {
		super.destroy();
		if (buffer != null)
			buffer.delete();
	}

	protected boolean needsRender(int pos){
		return true;
	}
}
