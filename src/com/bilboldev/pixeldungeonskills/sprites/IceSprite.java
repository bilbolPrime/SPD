package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.mobs.IceMob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Iceclaw;
import com.bilboldev.pixeldungeonskills.actors.mobs.OldRime;
import com.bilboldev.utils.RectF;

public class IceSprite extends MobSprite {
    private boolean warningShown;
    private boolean preparing;
    private final Animation windup;

    protected IceSprite(String asset, int width, int height) {
        texture(asset);
        TextureFilm frames = new TextureFilm(texture, width, height);
        idle = new Animation(4, true);
        idle.frames(frames, 0, 1, 2, 3);
        run = new Animation(8, true);
        run.frames(frames, 4, 5, 6, 7);
        attack = new Animation(8, false);
        attack.frames(frames, 8, 9, 10, 11);
        zap = new Animation(6, false);
        zap.frames(frames, 12, 13, 14, 15);
        windup = new Animation(6, true);
        windup.frames(frames, 12, 13, 14, 15);
        die = new Animation(7, false);
        die.frames(frames, 20, 21, 22, 23);
        play(idle);
    }

    @Override
    public void update() {
        super.update();
        boolean pending = ch != null && ch.isAlive() && (ch instanceof IceMob && ((IceMob)ch).warningCells().length > 0
                || ch instanceof OldRime && ((OldRime)ch).warningCells().length > 0
                || ch instanceof Iceclaw && ((Iceclaw)ch).braced());
        if (pending && curAnim == idle) play(windup);
        else if (!pending && preparing && curAnim == windup) idle();
        preparing = pending;
        boolean warning = ch != null && ch.isAlive() && (ch instanceof IceMob && ((IceMob)ch).meleeWarning()
                || ch instanceof OldRime && ((OldRime)ch).meleeWarning());
        if (warning) showAlert();
        else if (warningShown) hideAlert();
        warningShown = warning;
        if (warning && emo != null) {
            emo.heightOffset = 8;
            emo.visible = visible;
        }
    }

    @Override public int blood() { return 0xFFB4DDE7; }

    public static class Bat extends IceSprite {
        public Bat() { super(Assets.RIME_BAT, 16, 16); }
    }

    public static class Claw extends IceSprite {
        public Claw() { super(Assets.ICECLAW, 16, 16); }
    }

    public static class Gnoll extends IceSprite {
        public Gnoll() { super(Assets.FROSTBACK, 16, 20); }
    }

    public static class Shard extends IceSprite {
        public Shard() { super(Assets.SHARDLING, 16, 16); }
    }

    public static class Miner extends IceSprite {
        public Miner() { super(Assets.HOLLOW_MINER, 16, 20); }
    }

    public static class Horn extends IceSprite {
        public Horn() { super(Assets.RIMEHORN, 24, 20); }
    }

    public static class Rime extends IceSprite {
        private static final String[] PHASES = {Assets.OLD_RIME, Assets.OLD_RIME_2, Assets.OLD_RIME_3};
        private int phase;

        public Rime() { super(Assets.OLD_RIME, 24, 22); }

        @Override
        public void onComplete(Animation animation) {
            if (animation == zap) idle();
            else super.onComplete(animation);
        }

        @Override
        public void update() {
            if (ch instanceof OldRime && phase != ((OldRime)ch).phase()) {
                phase = ((OldRime)ch).phase();
                RectF frame = frame();
                texture(PHASES[phase]);
                frame(frame);
            }
            super.update();
        }
    }
}
