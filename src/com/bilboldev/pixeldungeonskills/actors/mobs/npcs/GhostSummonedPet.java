package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.WraithSprite;

public class GhostSummonedPet extends SummonedPet {

    public GhostSummonedPet() {
        super(PET_TYPES.SKELETON);
    }

    @Override
    public void spawn(int level) {
        super.spawn(Math.max(1, level));
        spriteClass = WraithSprite.class;
        nameText(Message.key("custom.generated.necro_merc.summoned_ghost"));
    }

    @Override public String description() {
        return Messages.get("actors.mobs.npcs.ghostsummonedpet.desc");
    }
}
