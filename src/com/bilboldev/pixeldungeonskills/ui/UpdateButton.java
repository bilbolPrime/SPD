package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.GameUpdates;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.windows.WndUpdateAvailable;
import com.bilboldev.pixeldungeonskills.windows.WndUpdateHistory;

public class UpdateButton extends Button {
    private Image scroll;
    private Emitter sparks;
    private float motion;
    private float restingY;

    @Override
    protected void createChildren() {
        super.createChildren();
        GameUpdates.check();
        scroll = new Image(Assets.CHROME, 32, 32, 32, 32);
        scroll.scale.set(.5f);
        scroll.origin.set(16, 16);
        add(scroll);
        sparks = new Emitter();
        sparks.autoKill = false;
        add(sparks);
        sparks.pour(Speck.factory(Speck.LIGHT), .3f);
        sparks.on = GameUpdates.available() || Presentation.lastReadUpdate() < Game.versionBuild;
    }

    @Override
    protected void layout() {
        super.layout();
        float left = PixelScene.align(x + (width - scroll.width()) / 2);

        scroll.x = left - scroll.origin.x * (1 - scroll.scale.x);
        restingY = y - scroll.origin.y * (1 - scroll.scale.y);
        scroll.y = restingY;
        sparks.pos(left - 2, y - 2, scroll.width() + 4, scroll.height() + 4);
    }

    @Override
    protected void onTouchDown() {
        scroll.brightness(1.3f);
        Sample.INSTANCE.play(Assets.SND_CLICK);
    }

    @Override
    protected void onTouchUp() {
        scroll.resetColor();
    }

    @Override
    protected void onClick() {
        Game.scene().add(GameUpdates.available() ? new WndUpdateAvailable() : new WndUpdateHistory());
    }

    @Override
    public void update() {
        boolean available = GameUpdates.available();
        sparks.on = available || Presentation.lastReadUpdate() < Game.versionBuild;
        super.update();
        motion = available ? (motion + Math.min(Game.elapsed, .1f)) % 4f : 0;
        float bounce = motion < 1 ? (float)Math.sin(motion * Math.PI) : 0;
        float zoom = camera().zoom;
        scroll.y = restingY - 1.5f * bounce / zoom;
        scroll.angle = motion < 1 ? 6 * bounce * (float)Math.sin(motion * Math.PI * 6) / zoom : 0;
    }
}
