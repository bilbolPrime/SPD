package com.bilboldev.pixeldungeonskills.ui;

import java.nio.FloatBuffer;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.glwrap.Quad;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.NoosaScript;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.utils.RectF;

public final class RebornChrome extends NinePatch {
    public enum Material {
        STONE(0xBBA2D6, 0x1E1F24, 0, 64),
        IRON(0xA5B7CA, 0x252A32, 0, 0),
        PARCHMENT(0xD1B48A, 0x27201B, 0, 64),
        ARCANE(0xC2A5E3, 0x272431, 0, 0),
        EMBER(0xC9A08C, 0x2B1D1D, 0, 64);

        final int rim, surface, tileX, tileY;
        Material(int rim, int surface, int tileX, int tileY) {
            this.rim = rim;
            this.surface = surface;
            this.tileX = tileX;
            this.tileY = tileY;
        }
    }

    private final Chrome.Type type;
    private final SmartTexture stone;
    private Material material;
    private FloatBuffer surfaceVertices, outlineVertices;
    private int tiles, borders;
    private boolean outlineOnly;
    private boolean highlighted;

    public RebornChrome(Chrome.Type type) {
        this(type, region(type));
    }

    private RebornChrome(Chrome.Type type, int[] r) {
        super(Assets.CHROME, r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]);
        this.type = type;
        stone = TextureCache.get(Assets.TILES_CITY_3D);
        material(type == Chrome.Type.SCROLL ? Material.PARCHMENT : Material.STONE);
    }

    public RebornChrome material(Material value) {
        if (material != value) {
            material = value;
            rebuild();
        }
        return this;
    }

    public Material material() {
        return material;
    }

    public RebornChrome outlineOnly() {
        outlineOnly = true;
        return this;
    }

    public void highlight(boolean value) {
        highlighted = value;
    }

    @Override
    public void size(float width, float height) {
        if (this.width == width && this.height == height) return;
        super.size(width, height);
        if (material != null) rebuild();
    }

    private void rebuild() {
        boolean selectedTab = type == Chrome.Type.TAB_SELECTED;
        borders = selectedTab ? 10 : 8;
        if (outlineVertices == null) outlineVertices = Quad.createSet(borders);
        outlineVertices.position(0);
        float[] quad = new float[16];
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            verticesBuffer.position(i * 16);
            verticesBuffer.get(quad);
            outlineVertices.put(quad);
        }
        verticesBuffer.position(0);

        float left = marginLeft(), top = selectedTab ? 0 : marginTop();
        float right = Math.max(left, width - marginRight());
        float bottom = Math.max(top, height - marginBottom());
        if (selectedTab) {

            RectF bevel = texture.uvRect(69, 7, 71, 8);
            Quad.fill(quad, left, left + 2, top, bottom, bevel.left, bevel.right, bevel.top, bevel.bottom);
            outlineVertices.put(quad);
            bevel = texture.uvRect(79, 7, 81, 8);
            Quad.fill(quad, right - 2, right, top, bottom, bevel.left, bevel.right, bevel.top, bevel.bottom);
            outlineVertices.put(quad);
            left += 2;
            right = Math.max(left, right - 2);
        }
        tiles = (int)Math.ceil((right - left) / 16) * (int)Math.ceil((bottom - top) / 16);
        if (tiles == 0) return;
        if (surfaceVertices == null || surfaceVertices.capacity() < tiles * 16)
            surfaceVertices = Quad.createSet(tiles);
        surfaceVertices.position(0);
        for (float y = top; y < bottom; y += 16) {
            for (float x = left; x < right; x += 16) {
                float w = Math.min(16, right - x), h = Math.min(16, bottom - y);
                RectF uv = stone.uvRect(material.tileX, material.tileY,
                        material.tileX + w, material.tileY + h);
                Quad.fill(quad, x, x + w, y, y + h, uv.left, uv.right, uv.top, uv.bottom);
                surfaceVertices.put(quad);
            }
        }
    }

    @Override
    public void draw() {

        updateMatrix();
        NoosaScript script = NoosaScript.get();
        script.camera(camera());
        script.uModel.valueM4(matrix);
        float emphasis = type == Chrome.Type.TAB_UNSELECTED ? .60f : 1f;
        int rim = highlighted ? 0xD1B48A : material.rim;
        texture.bind();
        light(script, rim, emphasis);
        script.drawQuadSet(outlineVertices, borders);
        if (!outlineOnly && tiles > 0) {
            stone.bind();
            int surface = material.surface;
            if (type == Chrome.Type.BUTTON) surface = highlighted ? 0x3A3228 : 0x302D36;
            light(script, surface, emphasis);
            script.drawQuadSet(surfaceVertices, tiles);
        }
    }

    private void light(NoosaScript script, int color, float strength) {
        script.lighting(rm * ((color >> 16) & 255) / 255f * strength,
                gm * ((color >> 8) & 255) / 255f * strength,
                bm * (color & 255) / 255f * strength, am, ra, ga, ba, aa);
    }

    private static int[] region(Chrome.Type type) {
        switch (type) {
            case WINDOW: return new int[]{0, 0, 22, 22, 7, 7, 7, 7};
            case TAB_SET: return new int[]{64, 0, 22, 22, 7, 7, 7, 7};
            case TAB_SELECTED: return new int[]{64, 22, 10, 14, 4, 7, 4, 6};
            case TAB_UNSELECTED: return new int[]{74, 22, 10, 14, 4, 7, 4, 6};

            case BUTTON: return new int[]{22, 0, 18, 18, 2, 2, 2, 2};
            case SCROLL: return new int[]{32, 32, 32, 32, 5, 11, 5, 11};
            case TAG: return new int[]{22, 18, 16, 14, 3, 3, 3, 3};
            case TOAST_TR: return new int[]{40, 0, 18, 18, 5, 5, 5, 5};
            default: return new int[]{22, 0, 18, 18, 5, 5, 5, 5};
        }
    }
}
