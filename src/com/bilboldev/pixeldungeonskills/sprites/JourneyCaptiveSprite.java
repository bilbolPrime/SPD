package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;

public class JourneyCaptiveSprite extends MobSprite {
    public JourneyCaptiveSprite() {
        texture(Assets.TROLL);
        TextureFilm frames = new TextureFilm(texture, 13, 16);
        idle = new Animation(1, true);
        idle.frames(frames, 0);
        run = idle.clone();
        die = idle.clone();
        play(idle);
    }
}
