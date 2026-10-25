package android.opengl;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public final class GLUtils {

    private GLUtils() {
    }

    public static void texImage2D(int target, int level, Bitmap bitmap, int border) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        for (int color : pixels) {
            buffer.put((byte) ((color >> 16) & 0xFF));
            buffer.put((byte) ((color >> 8) & 0xFF));
            buffer.put((byte) (color & 0xFF));
            buffer.put((byte) ((color >> 24) & 0xFF));
        }
        buffer.position(0);

        GLES20.glTexImage2D(target, level, GLES20.GL_RGBA, width, height, border, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
    }
}
