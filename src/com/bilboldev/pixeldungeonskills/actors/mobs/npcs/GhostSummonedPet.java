package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

public class GhostSummonedPet extends SummonedPet {

    public GhostSummonedPet() {
        super(PET_TYPES.SKELETON);
    }

    @Override
    public void spawn(int level) {
        super.spawn(Math.max(1, level));
    }
}
