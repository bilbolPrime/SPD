package android.graphics;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Paint {

    public enum Style {
        FILL,
        STROKE,
        FILL_AND_STROKE
    }

    private static final BufferedImage METRICS_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private int color = 0xFF000000;
    private float strokeWidth = 1f;
    private float textSize = 12f;
    private boolean antiAlias;
    private Style style = Style.FILL;
    private Typeface typeface = Typeface.DEFAULT;
    private Shader shader;
    private String measurementText;

    public void setARGB(int alpha, int red, int green, int blue) {
        color = ((alpha & 0xFF) << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | (blue & 0xFF);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setStyle(Style style) {
        this.style = style == null ? Style.FILL : style;
    }

    public Style getStyle() {
        return style;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface == null ? Typeface.DEFAULT : typeface;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public Shader getShader() {
        return shader;
    }

    public float measureText(String text) {
        if (text == null || text.isEmpty()) {
            measurementText = null;
            return 0f;
        }
        measurementText = text;
        Graphics2D graphics = METRICS_IMAGE.createGraphics();
        try {
            configureGraphics(graphics);
            FontMetrics metrics = graphics.getFontMetrics(resolveFont(text));
            return metrics.stringWidth(text);
        } finally {
            graphics.dispose();
        }
    }

    public float ascent() {
        Graphics2D graphics = METRICS_IMAGE.createGraphics();
        try {
            configureGraphics(graphics);
            FontMetrics metrics = graphics.getFontMetrics(resolveFont(measurementText));
            return -metrics.getAscent();
        } finally {
            graphics.dispose();
        }
    }

    public float descent() {
        Graphics2D graphics = METRICS_IMAGE.createGraphics();
        try {
            configureGraphics(graphics);
            FontMetrics metrics = graphics.getFontMetrics(resolveFont(measurementText));
            return metrics.getDescent();
        } finally {
            graphics.dispose();
        }
    }

    Font resolveFont() {
        return typeface.toFont(textSize);
    }

    Font resolveFont(String text) {
        if (typeface != null && typeface != Typeface.DEFAULT && !typeface.canDisplay(text, textSize)) {
            return Typeface.DEFAULT.toFont(textSize);
        }
        return resolveFont();
    }

    void configureGraphics(Graphics2D graphics) {
        graphics.setFont(resolveFont());
        configureRenderingHints(graphics);
    }

    void configureGraphics(Graphics2D graphics, String text) {
        graphics.setFont(resolveFont(text));
        configureRenderingHints(graphics);
    }

    private void configureRenderingHints(Graphics2D graphics) {
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    }
}
