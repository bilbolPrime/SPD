package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.windows.WndLoreNotices;

public final class LoreButton extends Button {
    private Image icon;
    private Emitter light;

    @Override protected void createChildren() {
        super.createChildren();
        icon = Icons.LORE.get();
        add(icon);
        light = new Emitter();
        light.pour(Speck.factory(Speck.LIGHT), 1.2f);
        add(light);
    }

    @Override protected void layout() {
        super.layout();
        icon.x = PixelScene.align(x + (width - icon.width()) / 2);
        icon.y = PixelScene.align(y + (height - icon.height()) / 2);
        light.pos(icon.x + 12, icon.y + 3, 1, 1);
    }

    @Override protected void onClick() {
        Game.scene().add(new WndLoreNotices());
    }
}
