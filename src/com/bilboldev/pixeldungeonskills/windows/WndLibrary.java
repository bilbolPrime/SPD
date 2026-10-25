package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.ui.RedButton;

public abstract class WndLibrary extends WndScrollable {
    private static final int[] FADE = {0xD0141416, 0x80141416, 0x00141416};
    private final ColorBlock titleShade = new ColorBlock(1, 1, FADE[0]);
    private final Image shade = new Image(TextureCache.createGradient(1, 128, FADE));

    protected WndLibrary() {
        shade.texture.filter(SmartTexture.LINEAR, SmartTexture.LINEAR);
        add(titleShade);
        add(shade);
    }

    protected final void layoutLibrary(int width, Component heading, Component content,
                                       float gap, RedButton... buttons) {
        layoutContent(width, heading, content, gap, buttons);
        float top = heading == null ? 0 : heading.bottom();
        titleShade.size(width, top);
        shade.y = top;
        shade.scale.set(width, Math.max(0, height - top) / shade.height);
    }
}
