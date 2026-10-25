package android.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Canvas {

    private Bitmap bitmap;

    public Canvas() {
    }

    public Canvas(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void drawPaint(Paint paint) {
        if (bitmap == null) {
            return;
        }

        Graphics2D graphics = begin(paint);
        try {
            graphics.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        } finally {
            graphics.dispose();
        }
    }

    public void drawText(String text, float x, float y, Paint paint) {
        if (bitmap == null || text == null || text.isEmpty()) {
            return;
        }

        Graphics2D graphics = begin(paint, text);
        try {
            GlyphVector glyphVector = paint.resolveFont(text).createGlyphVector(graphics.getFontRenderContext(), text);
            drawShape(graphics, glyphVector.getOutline(x, y), paint);
        } finally {
            graphics.dispose();
        }
    }

    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        if (bitmap == null) {
            return;
        }

        Graphics2D graphics = begin(paint);
        try {
            Ellipse2D.Float circle = new Ellipse2D.Float(cx - radius, cy - radius, radius * 2f, radius * 2f);
            drawShape(graphics, circle, paint);
        } finally {
            graphics.dispose();
        }
    }

    private void drawShape(Graphics2D graphics, Shape shape, Paint paint) {
        if (paint.getStyle() == Paint.Style.STROKE || paint.getStyle() == Paint.Style.FILL_AND_STROKE) {
            graphics.setStroke(new BasicStroke(Math.max(1f, paint.getStrokeWidth()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(shape);
        }
        if (paint.getStyle() == Paint.Style.FILL || paint.getStyle() == Paint.Style.FILL_AND_STROKE) {
            graphics.fill(shape);
        }
    }

    private Graphics2D begin(Paint paint) {
        BufferedImage image = bitmap.bufferedImage();
        Graphics2D graphics = image.createGraphics();
        paint.configureGraphics(graphics);
        setPaint(graphics, paint);
        return graphics;
    }

    private Graphics2D begin(Paint paint, String text) {
        BufferedImage image = bitmap.bufferedImage();
        Graphics2D graphics = image.createGraphics();
        paint.configureGraphics(graphics, text);
        setPaint(graphics, paint);
        return graphics;
    }

    private void setPaint(Graphics2D graphics, Paint paint) {
        Shader shader = paint.getShader();
        java.awt.Paint awtPaint = shader != null ? shader.toAwtPaint(bitmap) : null;
        if (awtPaint != null) {
            graphics.setPaint(awtPaint);
        } else {
            graphics.setPaint(new Color(paint.getColor(), true));
        }
    }
}
