package com.bilboldev.pixeldungeonskills.ui;

import android.graphics.Bitmap;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Presentation;

public final class RebornHud {
    private RebornHud() {
    }

    public static SmartTexture texture(Object asset) {
        SmartTexture original = TextureCache.get(asset);
        if (!Presentation.reborn()) return original;

        String key = "reborn-hud:" + asset;
        if (TextureCache.contains(key)) return TextureCache.get(key);

        int width = original.width;
        int height = original.height;
        int[] pixels = new int[width * height];
        original.bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        boolean status = Assets.STATUS.equals(asset);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = pixels[i] & 0xFF000000;
            int rgb = pixels[i] & 0xFFFFFF;
            pixels[i] = alpha | color(rgb, status);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        SmartTexture texture = new SmartTexture(bitmap);
        TextureCache.add(key, texture);
        return texture;
    }

    private static int color(int rgb, boolean status) {
        switch (rgb) {
            case 0x393B35: return 0x1B1921;
            case 0x3B3D37: return 0x221F29;
            case 0x3E4039: return 0x2A2631;
            case 0x494C45: return 0x34303D;
            case 0x53564E: return 0x503D62;
            case 0x585B52: return 0x614875;
            case 0x63665C: return 0x785695;
            case 0x696E61: return 0x8962A4;
            case 0x7B8073: return 0x38333F;
            case 0x8C9184: return 0xA17DBB;
            case 0x979C8F: return 0xB991D1;
            case 0xA0A695: return 0xC5A1E5;
            case 0xB1B6A9: return 0xD0B0E0;
            case 0xCACFC2: return 0xE0CEE8;
            case 0x313329: return 0x24212B;
            case 0x52564C: return 0x674D7D;
            case 0xD0D2B2: return 0xDAC7E5;
            case 0x802635: return status ? 0x29232F : rgb;
            case 0x992E2E: return status ? 0x43374F : rgb;
            case 0xBF5039: return status ? 0xC5A1E5 : rgb;
            default: return rgb;
        }
    }
}
