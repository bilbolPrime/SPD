package android.graphics;

import java.awt.Color;
import java.awt.MultipleGradientPaint;
import java.awt.geom.Point2D;

public class LinearGradient extends Shader {

    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final int[] colors;
    private final float[] positions;
    private final TileMode tileMode;

    public LinearGradient(float x0, float y0, float x1, float y1, int[] colors, float[] positions, TileMode tileMode) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.colors = colors == null ? new int[]{0x00000000} : colors.clone();
        this.positions = positions == null ? null : positions.clone();
        this.tileMode = tileMode == null ? TileMode.CLAMP : tileMode;
    }

    @Override
    java.awt.Paint toAwtPaint(Bitmap bitmap) {
        if (colors.length == 0) {
            return new Color(0, true);
        }
        if (colors.length == 1) {
            return new Color(colors[0], true);
        }

        float[] fractions = positions;
        if (fractions == null || fractions.length != colors.length) {
            fractions = new float[colors.length];
            for (int i = 0; i < colors.length; i++) {
                fractions[i] = colors.length == 1 ? 0f : (float) i / (float) (colors.length - 1);
            }
        }

        Color[] awtColors = new Color[colors.length];
        for (int i = 0; i < colors.length; i++) {
            awtColors[i] = new Color(colors[i], true);
        }

        MultipleGradientPaint.CycleMethod cycleMethod;
        switch (tileMode) {
            case REPEAT:
                cycleMethod = MultipleGradientPaint.CycleMethod.REPEAT;
                break;
            case MIRROR:
                cycleMethod = MultipleGradientPaint.CycleMethod.REFLECT;
                break;
            case CLAMP:
            default:
                cycleMethod = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                break;
        }

        return new java.awt.LinearGradientPaint(
                new Point2D.Float(x0, y0),
                new Point2D.Float(x1, y1),
                fractions,
                awtColors,
                cycleMethod);
    }
}
