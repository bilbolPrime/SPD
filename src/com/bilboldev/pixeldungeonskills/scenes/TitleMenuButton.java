package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Assets;

abstract class TitleMenuButton extends Button {
    private ColorBlock border, fill, accent;
    private Image icon;
    private BitmapText label, arrow;
    private boolean primary, hovered;
    private float labelScaleX, labelScaleY;

    TitleMenuButton(String text, int index, boolean primary) {
        this.primary = primary;
        icon.frame(icon.texture.uvRect(index * 32, 0, (index + 1) * 32, 32));
        label.text(text);
        label.measure();
        labelScaleX = label.scale.x;
        labelScaleY = label.scale.y;
        palette(false);
    }

    @Override
    protected void createChildren() {
        super.createChildren();
        add(border = new ColorBlock(1, 1, 0xFFFFFFFF));
        add(fill = new ColorBlock(1, 1, 0xFFFFFFFF));
        add(accent = new ColorBlock(1, 1, 0xFFFFFFFF));
        add(icon = new Image(Assets.DASHBOARD));
        add(label = PixelScene.createText(10));
        add(arrow = PixelScene.createText(9));
        arrow.text(">");
        arrow.measure();
    }

    @Override
    protected void layout() {
        super.layout();
        border.x = x;
        border.y = y;
        border.size(width, height);
        fill.x = x + 1;
        fill.y = y + 1;
        fill.size(width - 2, height - 2);
        accent.x = x + 1;
        accent.y = y + 1;
        accent.size(primary ? 2 : 1, height - 2);
        float iconSize = Math.min(22, height - 6);
        icon.scale.set(iconSize / 32);
        icon.x = PixelScene.align(x + 8);
        icon.y = PixelScene.align(y + (height - iconSize) / 2);
        label.scale.set(labelScaleX, labelScaleY);
        float fit = Math.min(1, (width - 53) / Math.max(1, label.width()));
        label.scale.set(labelScaleX * fit, labelScaleY * fit);
        label.x = PixelScene.align(x + 38);
        label.y = PixelScene.align(y + (height - label.baseLine()) / 2 - 1);
        arrow.x = PixelScene.align(x + width - arrow.width() - 9);
        arrow.y = PixelScene.align(y + (height - arrow.baseLine()) / 2 - 1);
    }

    void opacity(float value) {
        border.alpha(value * 0.94f);
        fill.alpha(value * 0.94f);
        accent.alpha(value);
        icon.alpha(value);
        label.alpha(value);
        arrow.alpha(value * 0.7f);
        active = value >= 0.99f;
        hotArea.active = active;
        visible = value > 0;
    }

    private void palette(boolean down) {
        border.hardlight(down ? 0xB9A5EC : primary ? 0x8C76BC : 0x4C4A5F);
        fill.hardlight(down ? 0x3A304E : primary ? 0x282239 : 0x15151F);
        accent.hardlight(primary ? 0xBA9EEB : 0x706184);
        label.hardlight(primary ? 0xF5EFFD : 0xD6D2DF);
        arrow.hardlight(0xBAA6D8);
        icon.brightness(down ? 1.25f : 1);
    }

    void hover(boolean value) {
        if (hovered != value) {
            hovered = value;
            palette(pressed || hovered);
        }
    }

    @Override
    protected void onTouchDown() {
        palette(true);
        Sample.INSTANCE.play(Assets.SND_CLICK, 0.7f, 0.7f, 0.8f);
    }

    @Override
    protected void onTouchUp() {
        palette(hovered);
    }
}
