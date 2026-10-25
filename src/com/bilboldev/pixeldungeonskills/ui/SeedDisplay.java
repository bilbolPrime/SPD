package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.SeedIcons;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.windows.WndSeedInfo;

public class SeedDisplay extends Button {
    private final RunSeed seed;
    private final int columns;
    private final BitmapText label;
    private final Image[] icons;
    private final ColorBlock[] border;

    public SeedDisplay(RunSeed seed) {
        this(seed, RunSeed.SYMBOLS, true);
    }

    public SeedDisplay(RunSeed seed, int columns, boolean showLabel) {
        this.seed = seed;
        this.columns = columns;
        label = showLabel ? PixelScene.createText(Messages.get(RunSeed.class, "title"), 8) : null;
        if (label != null) {
            label.measure();
            add(label);
        }
        border = new ColorBlock[seed != null && seed.custom ? 4 : 0];
        for (int i = 0; i < border.length; i++) {
            border[i] = new ColorBlock(1, 1, 0xFFBC9BEA);
            add(border[i]);
        }
        icons = new Image[seed == null ? 1 : RunSeed.SYMBOLS];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = seed == null ? new ItemSprite(ItemSpriteSheet.SMTH, null)
                    : SeedIcons.entry(seed.symbol(i)).image();
            if (seed == null) icons[i].tint(0xFFFFFF, .8f);
            add(icons[i]);
        }
    }

    @Override protected void layout() {
        super.layout();
        if (icons == null) return;
        boolean labelAbove = label != null && !Window.landscapeLayout();
        float labelWidth = label == null || labelAbove ? 0 : label.width() + 4;
        float labelHeight = labelAbove ? label.height() + 4 : 0;
        if (label != null) {
            label.x = x;
            label.y = PixelScene.align(labelAbove ? y : y + (height - label.baseLine()) / 2);
        }
        int rows = seed == null ? 1 : (icons.length + columns - 1) / columns;
        float cell = Math.min((height - labelHeight) / rows, (width - labelWidth) / (seed == null ? 1 : columns));
        float left = x + labelWidth + (width - labelWidth - columns * cell) / 2;
        float top = y + labelHeight + (height - labelHeight - rows * cell) / 2;
        if (border.length > 0) {
            for (int i = 0; i < border.length; i++) {
                boolean horizontal = i < 2;
                border[i].size(horizontal ? columns * cell : 1, horizontal ? 1 : rows * cell);
                border[i].x = PixelScene.align(left + (i == 3 ? columns * cell - 1 : 0));
                border[i].y = PixelScene.align(top + (i == 1 ? rows * cell - 1 : 0));
            }
        }
        for (int i = 0; i < icons.length; i++) {
            Image icon = icons[i];
            float size = Math.max(1, cell - 2);
            icon.scale.set(Math.min(size / icon.width, size / icon.height));
            icon.x = PixelScene.align(seed == null ? x + labelWidth + (width - labelWidth - icon.width()) / 2
                    : left + i % columns * cell + (cell - icon.width()) / 2);
            icon.y = PixelScene.align(top + i / columns * cell + (cell - icon.height()) / 2);
        }
    }

    public void useScrollPaneInput() {
        Touchscreen.event.remove(hotArea);
    }

    public void open() {
        Game.scene().add(new WndSeedInfo(seed));
    }

    @Override protected void onClick() {
        open();
    }
}
