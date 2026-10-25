package com.bilboldev.pixeldungeonskills.actors.hero;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.windows.WndGauntletDie;
import com.bilboldev.utils.Bundle;

public class GauntletHero extends Hero {
    private boolean dead;
    private int hitsTaken;

    public int hitsTaken() {
        return hitsTaken;
    }

    @Override
    public void damage(int damage, Object source) {
        int previousHP = HP;
        super.damage(damage, source);
        if (HP < previousHP) hitsTaken++;
    }

    @Override
    public boolean isAlive() {
        return !dead && super.isAlive();
    }

    @Override
    public void ready() {
        super.ready();
    }

    @Override
    public void live() {
        super.live();
        Buff.detach(this, Hunger.class);
    }

    @Override
    public boolean act() {
        if (!GauntletRun.acceptsInput()) {
            ready = false;
            return false;
        }
        Buff.detach(this, Hunger.class);
        return super.act();
    }

    @Override
    protected boolean getCloser(int target) {
        int oldPos = pos;
        boolean moved = super.getCloser(target);
        if (moved && pos != oldPos) GauntletRun.arena().heroStepped();
        return moved;
    }

    @Override
    protected boolean actAscend(HeroAction.Ascend action) {

        if (getCloser(action.dst)) return true;
        ready();
        return false;
    }

    @Override
    public void die(Object cause) {
        if (dead) return;
        dead = true;
        interrupt();
        curAction = null;
        ready = false;
        destroy();
        if (sprite != null) sprite.die();
        GauntletRun.arena().heroDied();
        Sample.INSTANCE.play(Assets.SND_DEATH);
        GameScene.show(new WndGauntletDie());
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        throw new IllegalStateException("Gauntlet heroes cannot be saved");
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        throw new IllegalStateException("Gauntlet heroes cannot be loaded");
    }
}
