package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.tweeners.PosTweener;
import com.bilboldev.noosa.tweeners.Tweener;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Callback;

public class DailyRimehornSprite extends IceSprite.Horn {
    private Callback impact;

    public void charge(int from, int to, Callback callback) {
        interruptMotion();
        impact = callback;
        play(run);
        turnTo(from, to);
        motion = new PosTweener(this, worldToCamera(to), Math.max(0.12f, Level.distance(from, to) * 0.08f));
        motion.listener = this;
        isMoving = true;
        parent.add(motion);
    }

    @Override
    public void onComplete(Tweener tweener) {
        boolean arrived = tweener == motion;
        super.onComplete(tweener);
        if (arrived && impact != null) {
            Callback callback = impact;
            impact = null;
            callback.call();
        }
    }
}
