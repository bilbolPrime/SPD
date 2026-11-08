package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.effects.DeathRay;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemapOld;
import com.bilboldev.utils.Callback;

public class DailyEyeSprite extends EyeSprite {
    private int beamFrom, beamTo;
    private Callback impact;

    public void beam(int from, int to, Callback callback) {
        beamFrom = from;
        beamTo = to;
        impact = callback;
        turnTo(from, to);
        play(attack);
    }

    @Override
    public void onComplete(Animation animation) {
        if (animation == attack) {
            if (impact == null) return;
            Callback callback = impact;
            impact = null;
            idle();
            if (parent != null && ch != null && ch.isAlive()) {
                parent.add(new DeathRay(center(), Difficulties.is3d
                        ? DungeonTilemap.raisedTileCenterToWorld(beamTo) : DungeonTilemapOld.tileCenterToWorld(beamTo)));
                MagicMissile.purpleLight(parent, beamFrom, beamTo, callback);
            } else callback.call();
        } else super.onComplete(animation);
    }
}
