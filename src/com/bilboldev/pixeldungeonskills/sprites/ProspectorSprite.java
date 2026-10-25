package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;

public class ProspectorSprite extends MobSprite {
    public ProspectorSprite() {
        texture(Assets.PROSPECTOR);
        TextureFilm frames = new TextureFilm(texture, 16, 16);
        idle = new Animation(4, true);
        idle.frames(frames, 0, 0, 0, 1, 1, 2, 3, 0);
        run = new Animation(4, true);
        run.frames(frames, 0, 1);
        die = new Animation(4, false);
        die.frames(frames, 0);
        play(idle);
    }
}
