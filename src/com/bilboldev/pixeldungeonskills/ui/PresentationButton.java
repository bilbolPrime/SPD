package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Presentation;

public final class PresentationButton extends RedButton {
    public PresentationButton() {
        super(Presentation.reborn() ? "C" : "R");
        setSize(icon != null ? 24 : 16, icon != null ? 24 : 16);
    }

    @Override
    protected void createChildren() {
        super.createChildren();
        if (Presentation.reborn()) {
            bg.visible = text.visible = false;
            icon = new Image(Assets.CLASSIC_MODE);
            add(icon);
        }
    }

    @Override
    protected void layout() {
        super.layout();
        if (icon != null) {
            icon.x = x;
            icon.y = y;
        }
    }

    @Override
    protected void onTouchDown() {
        super.onTouchDown();
        if (icon != null) icon.brightness(1.5f);
    }

    @Override
    protected void onTouchUp() {
        super.onTouchUp();
        if (icon != null) icon.resetColor();
    }

    @Override
    protected void onClick() {
        Presentation.switchTo(!Presentation.reborn());
    }
}
