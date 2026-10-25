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

package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.RenderedText;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.utils.TextSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenderedTextMultiline extends Component {

	private int maxWidth = Integer.MAX_VALUE;
	public int nLines;

	private String text;
	private List<String> tokens;
	private ArrayList<RenderedText> words = new ArrayList<>();

	private int size;
	private float zoom;
	private int color = -1;
	private int highlightColor = 0xFFFF44;

	private static final RenderedText SPACE_MARKER = new RenderedText();
	private static final String NEWLINE = "\n";
	private static final String UNDERSCORE = "_";
	private static final String DOUBLE_ASTERISK = "**";

	public RenderedTextMultiline(int size){
		this.size = size;
	}

	public RenderedTextMultiline(String text, int size){
		this.size = size;
		text(text);
	}

	public void text(String text){
		text(text, true);
	}

	public void text(String text, boolean localize){
		this.text = localize ? Messages.maybeTranslate(text) : text;

		if (this.text == null || this.text.equals("")) {
			clear();
			words = new ArrayList<RenderedText>();
			width = 0;
			height = 0;
			return;
		}

		tokens = Arrays.asList(TextSupport.splitForTextBlock(this.text, true));
		build();
	}

	public void text(String text, int maxWidth){
		this.maxWidth = maxWidth;
		text(text);
	}

	public String text(){
		return text;
	}

	public void maxWidth(int maxWidth){
		if (this.maxWidth != maxWidth){
			this.maxWidth = maxWidth;
			layout();
		}
	}

	public int maxWidth(){
		return maxWidth;
	}

	private synchronized void build(){

		for (RenderedText word : words) {
			if (word != null && word != SPACE_MARKER) {
				word.destroy();
			}
		}
		clear();
		words = new ArrayList<>();
		boolean highlighting = false;
		for (String str : tokens){
			if (str.equals(UNDERSCORE) || str.equals(DOUBLE_ASTERISK)){
				highlighting = !highlighting;
			} else if (str.equals(NEWLINE)){
				words.add(null);
			} else if (str.equals(" ")) {
				words.add(SPACE_MARKER);
			} else {
				RenderedText word;
				if (str.startsWith(UNDERSCORE) && str.endsWith(UNDERSCORE)){
					word = new RenderedText(str.substring(1, str.length()-1), size, false);
					word.hardlight(highlightColor);
				} else {
					if (str.startsWith(UNDERSCORE)){
						highlighting = !highlighting;
						word = new RenderedText(str.substring(1, str.length()), size, false);
					} else if (str.endsWith(UNDERSCORE)) {
						word = new RenderedText(str.substring(0, str.length()-1), size, false);
					} else {
						word = new RenderedText(str, size, false);
					}
					if (highlighting) word.hardlight(highlightColor);
					else if (color != -1) word.hardlight(color);

					if (str.endsWith(UNDERSCORE)) highlighting = !highlighting;
				}
				word.scale.set(zoom);
				words.add(word);
				add(word);

			}
		}
		layout();
	}

	public synchronized void zoom(float zoom){
		if (this.zoom == zoom) return;
		this.zoom = zoom;
		for (RenderedText word : words) {
			if (word != null && word != SPACE_MARKER) word.scale.set(zoom);
		}
		layout();
	}

	public synchronized void hardlight(int color){
		this.color = color;
		for (RenderedText word : words) {
			if (word != null && word != SPACE_MARKER) word.hardlight( color );
		}
	}

	public synchronized void resetColor(){
		this.color = -1;
		for (RenderedText word : words) {
			if (word != null && word != SPACE_MARKER) {
				word.resetColor();
			}
		}
	}

	public synchronized void alpha(float value){
		for (RenderedText word : words) {
			if (word != null && word != SPACE_MARKER) {
				word.alpha(value);
			}
		}
	}

	public synchronized void setHighlightColor(int color){
		if (highlightColor != color) {
			highlightColor = color;
			if (text != null && !text.isEmpty()) build();
		}
	}

	public synchronized void invert(){
		if (words != null) {
			for (RenderedText word : words) {
				if (word != null && word != SPACE_MARKER) {
					word.ra = 0.77f;
					word.ga = 0.73f;
					word.ba = 0.62f;
					word.rm = -0.77f;
					word.gm = -0.73f;
					word.bm = -0.62f;
				}
			}
		}
	}

	@Override
	protected synchronized void layout() {
		super.layout();
		float x = this.x;
		float y = this.y;
		float lineHeight = 0;
		nLines = 1;
		width = 0;

		ArrayList<ArrayList<RenderedText>> lines = new ArrayList<ArrayList<RenderedText>>();
		ArrayList<RenderedText> curLine = new ArrayList<RenderedText>();
		lines.add(curLine);

		for (int i = 0; i < words.size(); i++) {
			RenderedText word = words.get(i);
			if (word == SPACE_MARKER) {
				x += 1.667f;
				continue;
			}

			if (word == null) {
				y += lineHeight + 2f;
				x = this.x;
				lineHeight = 0;
				nLines++;
				curLine = new ArrayList<RenderedText>();
				lines.add(curLine);
			} else {
				if (word.height() > lineHeight) {
					lineHeight = word.height();
				}

				float fullWidth = word.width();
				int j = i + 1;
				while (!TextSupport.isNoSpaceWrappingLanguage()
						&& j < words.size()
						&& words.get(j) != SPACE_MARKER
						&& words.get(j) != null) {
					fullWidth += words.get(j).width() - 0.667f;
					j++;
				}

				if ((x - this.x) + fullWidth - 0.001f > maxWidth && !curLine.isEmpty()) {
					y += lineHeight + 2f;
					x = this.x;
					lineHeight = word.height();
					nLines++;
					curLine = new ArrayList<RenderedText>();
					lines.add(curLine);
				}

				word.x = x;
				word.y = y;
				PixelScene.align2(word);
				x += word.width();
				curLine.add(word);
				x -= 0.667f;

				if ((x - this.x) > width) width = (x - this.x);

			}
		}
		this.height = (y - this.y) + lineHeight;

		for (ArrayList<RenderedText> line : lines) {
			for (RenderedText renderedText : line) {
				PixelScene.align2(renderedText);
			}
		}
	}
}
