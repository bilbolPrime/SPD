package com.bilboldev.pixeldungeonskills.scenes;

import android.graphics.Bitmap;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.noosa.Image;

final class FirstPersonFlares {
    private static final int SIZE = 64;
    private final Image[] shapes = new Image[17];

    Image image(int rays) {
        if (rays < 2 || rays >= shapes.length) return null;
        if (shapes[rays] != null) return shapes[rays];
        int[] pixels = new int[SIZE * SIZE];
        double step = Math.PI * 2 / rays, half = step * 0.5;
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
            double px = (x + 0.5 - SIZE * 0.5) / (SIZE * 0.5);
            double py = (y + 0.5 - SIZE * 0.5) / (SIZE * 0.5);
            double angle = (Math.atan2(py, px) + Math.PI * 2) % step;
            if (angle > half) continue;
            double edge = Math.cos(half * 0.5) / Math.cos(angle - half * 0.5);
            int alpha = (int)(255 * Math.max(0, 1 - Math.sqrt(px * px + py * py) / edge));
            pixels[x + y * SIZE] = (alpha << 24) | 0xFFFFFF;
        }
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE);
        Image image = new Image(new SmartTexture(bitmap));
        shapes[rays] = image;
        return image;
    }

    void destroy() {
        for (int i = 0; i < shapes.length; i++) if (shapes[i] != null) {
            shapes[i].texture.delete();
            shapes[i].destroy();
            shapes[i] = null;
        }
    }
}
