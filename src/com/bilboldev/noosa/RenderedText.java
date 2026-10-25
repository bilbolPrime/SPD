/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.glwrap.Matrix;
import com.bilboldev.glwrap.Texture;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.utils.TextSupport;
import com.bilboldev.utils.RectF;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RenderedText extends Image {

	private static Canvas canvas = new Canvas();
	private static Paint painter = new Paint();

	private static LinkedHashMap<String, CachedText> textCache = new LinkedHashMap<>(700, 0.75f, true);

	private static int cachedChars = 0;

	private static final int GC_TRIGGER = 1250;
	private static final int GC_TARGET = 1000;

	private static void runGC(){
		Iterator<Map.Entry<String, CachedText>> it = textCache.entrySet().iterator();
		while (cachedChars > GC_TARGET && it.hasNext()){
			CachedText cached = it.next().getValue();
			if (cached.activeTexts.isEmpty()) {
				cachedChars -= cached.length;
				cached.texture.delete();
				it.remove();
			}
		}
	}

	private int size;
	private String text;
	private CachedText cache;

	private boolean needsRender = false;

	public RenderedText( ){
		text = null;
	}

	public RenderedText( int size ){
		text = null;
		this.size = size;
	}

	public RenderedText(String text, int size){
		this(text, size, true);
	}

	public RenderedText(String text, int size, boolean localize){
		this.size = size;
		setText(text, localize);
	}

	public void text( String text ){
		text(text, true);
	}

	public void text( String text, boolean localize ){
		setText(text, localize);
	}

	public String text(){
		return text;
	}

	private void setText(String value, boolean localize) {
		this.text = localize ? Messages.maybeTranslate(value) : value;

		needsRender = true;
		measure(this);
	}

	public void size( int size ){
		this.size = size;
		needsRender = true;
		measure(this);
	}

	public float baseLine(){
		return size * scale.y;
	}

	private static synchronized void measure(RenderedText r){

		if ( r.text == null || r.text.equals("") ) {
			r.text = "";
			r.width=r.height=0;
			r.visible = false;
			return;
		} else {
			r.visible = true;
		}

		painter.setTextSize(r.size);
		painter.setAntiAlias(true);
		TextSupport.TypefaceInfo typefaceInfo = TextSupport.resolveTypefaceInfo(r.text);
		painter.setTypeface(typefaceInfo.typeface == null ? Typeface.DEFAULT : typefaceInfo.typeface);

		painter.setARGB(0xff, 0, 0, 0);
		painter.setStyle(Paint.Style.STROKE);
		painter.setStrokeWidth(r.size / 5f);

		r.width = (painter.measureText(r.text)+ (r.size/5f));
		r.height = (-painter.ascent() + painter.descent()+ (r.size/5f));
	}

	private static synchronized void render(RenderedText r){
		r.needsRender = false;

		if (r.cache != null)
			r.cache.activeTexts.remove(r);

		TextSupport.TypefaceInfo typefaceInfo = TextSupport.resolveTypefaceInfo(r.text);
		String key = "text:" + typefaceInfo.cacheKey + ":" + r.size + " " + r.text;
		if (textCache.containsKey(key)){
			r.cache = textCache.get(key);
			r.texture = r.cache.texture;
			r.frame(r.cache.rect);
			r.cache.activeTexts.add(r);
		} else {

			measure(r);

			if (r.width == 0 || r.height == 0)
				return;

			Bitmap bitmap = Bitmap.createBitmap(Integer.highestOneBit((int)r.width)*2, Integer.highestOneBit((int)r.height)*2, Bitmap.Config.ARGB_4444);
			bitmap.eraseColor(0x00000000);

			canvas.setBitmap(bitmap);
			canvas.drawText(r.text, (r.size/10f), r.size, painter);

			painter.setARGB(0xff, 0xff, 0xff, 0xff);
			painter.setStyle(Paint.Style.FILL);

			canvas.drawText(r.text, (r.size/10f), r.size, painter);

			r.texture = new SmartTexture(bitmap, Texture.NEAREST, Texture.CLAMP, true);

			RectF rect = r.texture.uvRect(0, 0, r.width, r.height);
			r.frame(rect);

			r.cache = new CachedText();
			r.cache.rect = rect;
			r.cache.texture = r.texture;
			r.cache.length = r.text.length();
			r.cache.activeTexts = new HashSet<>();
			r.cache.activeTexts.add(r);

			cachedChars += r.cache.length;
			textCache.put(key, r.cache);

			if (cachedChars >= GC_TRIGGER){
				runGC();
			}
		}
	}

	@Override
	protected void updateMatrix() {
		super.updateMatrix();

		Matrix.translate( matrix, 0, -Math.round((baseLine()*0.15f)/scale.y) );
	}

	@Override
	public void draw() {
		if (needsRender)
			render(this);
		if (texture != null)
			super.draw();
	}

	@Override
	public void destroy() {
		synchronized (RenderedText.class) {
			if (cache != null) cache.activeTexts.remove(this);
		}
		super.destroy();
	}

	public static synchronized void clearCache(){
		for (CachedText cached : textCache.values()){
			cached.texture.delete();
		}
		cachedChars = 0;
		textCache.clear();
	}

	public static synchronized void reloadCache(){
		for (CachedText txt : textCache.values()){
			txt.texture.reload();
		}
	}

	public static synchronized void setFont(String asset){
		TextSupport.setBasicFontAsset(asset);
		clearCache();
	}

	public static Typeface getFont(){
		return TextSupport.getBasicTypeface();
	}

	private static class CachedText{
		public SmartTexture texture;
		public RectF rect;
		public int length;
		public HashSet<RenderedText> activeTexts;
	}
}
