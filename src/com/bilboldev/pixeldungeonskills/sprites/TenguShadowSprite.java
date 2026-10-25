package com.bilboldev.pixeldungeonskills.sprites;

public class TenguShadowSprite extends TenguSprite {
    public TenguShadowSprite() {
        idle();
        hardlight(0x8768C7);
        alpha(0.65f);
    }

    @Override
    public void die() {
        paused = false;
        animCallback = null;
        super.die();
        alpha(0.65f);
    }

    @Override
    public void update() {
        float opacity = am;
        super.update();
        hardlight(0x8768C7);

        alpha(curAnim == die ? Math.min(opacity, 0.65f) : 0.65f);
    }
}
