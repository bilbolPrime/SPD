package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RoomProp;

public class RoomPropSprite extends MobSprite {
    public RoomPropSprite() {
        super();
        texture(Assets.ITEMS);
        animation(ItemSpriteSheet.CRYSTAL_CHEST);
    }

    private void animation(int frame) {
        idle = new Animation(1, true);
        idle.frames(new TextureFilm(texture, 16, 16), frame);
        run = idle.clone();
        attack = idle.clone();
        attack.looped = false;
        die = attack.clone();
        play(idle);
    }

    @Override
    public void link(Char ch) {
        super.link(ch);
        animation(((RoomProp)ch).image());
    }
}
