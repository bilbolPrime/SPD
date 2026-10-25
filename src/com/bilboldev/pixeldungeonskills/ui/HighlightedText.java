package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.utils.TextSupport;
import com.bilboldev.utils.Highlighter;

public class HighlightedText extends Component {

	protected BitmapTextMultiline normal;
	protected BitmapTextMultiline highlighted;
	protected RenderedTextMultiline rendered;
	protected final int renderedSize;
	protected boolean renderedMode;
	private String sourceText;

	protected int nColor = 0xFFFFFF;
	protected int hColor = 0xFFFF44;

	public HighlightedText( float size ) {
		renderedSize = Math.max( 1, Math.round( size ) );
		normal = PixelScene.createMultiline( size );
		add( normal );

		highlighted = PixelScene.createMultiline( size );
		add( highlighted );

		rendered = PixelScene.renderMultiline( "", renderedSize );
		rendered.visible = false;
		add( rendered );

		setColor( 0xFFFFFF, 0xFFFF44 );
	}

	@Override
	protected void layout() {
		normal.x = highlighted.x = x;
		normal.y = highlighted.y = y;
		rendered.setPos( x, y );
	}

	public void text( String value, int maxWidth ) {
		text( value, maxWidth, true );
	}

	public void text( String value, int maxWidth, boolean localize ) {

		value = localize ? Messages.maybeTranslate( value ) : value;
		sourceText = value;
		Highlighter hl = new Highlighter( value );
		renderedMode = TextSupport.requiresRenderedGlyphs( normal.font(), hl.text );
		normal.mask = null;
		highlighted.mask = null;

		if (renderedMode) {
			rendered.visible = true;
			normal.visible = false;
			highlighted.visible = false;
			rendered.text( value, false );
			rendered.maxWidth( maxWidth );
			width = rendered.width();
			height = rendered.height();
			return;
		}

		rendered.visible = false;
		normal.visible = true;
		highlighted.visible = true;

		normal.text( hl.text, false );
		normal.maxWidth = maxWidth;
		normal.measure();

		if (hl.isHighlighted()) {
			normal.mask = hl.inverted();

			highlighted.text( hl.text, false );
			highlighted.maxWidth = maxWidth;
			highlighted.measure();

			highlighted.mask = hl.mask;
			highlighted.visible = true;
		} else {
			highlighted.visible = false;
		}

		width = normal.width();
		height = normal.height();
	}

	public String text() {
		return sourceText;
	}

	public void setColor( int n, int h ) {
		nColor = n;
		hColor = h;
		normal.hardlight( n );
		highlighted.hardlight( h );
		rendered.hardlight( n );
		rendered.setHighlightColor( h );
	}
}
