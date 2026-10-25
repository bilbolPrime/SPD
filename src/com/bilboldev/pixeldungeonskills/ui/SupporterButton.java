package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.SupporterEntitlements;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.windows.WndHeroAppearance;

public final class SupporterButton extends Button {
    private SupporterIcon icon;
    private float phase;

    @Override protected void createChildren() {
        super.createChildren();
        icon = new SupporterIcon();
        icon.scale.set(.5f);
        add(icon);
    }

    @Override public void update() {
        super.update();
        phase += Game.elapsed;
        icon.visible = true;
        int style = SupporterEntitlements.selectedAura();
        if (style == 0) style = 1;
        icon.hardlight(SupporterIcon.color(style, phase));
        icon.alpha(SupporterEntitlements.ownsAura(style) ? 1 : .5f);
    }

    @Override protected void layout() {
        super.layout();
        icon.x = PixelScene.align(x + (width - icon.width()) / 2);
        icon.y = PixelScene.align(y + (height - icon.height()) / 2);
    }

    @Override protected void onClick() {
        Game.scene().add(new WndHeroAppearance(PartySession.network() ? PartySession.localHero() : Dungeon.hero));
    }
}
