package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.sprites.BarbecueKingSprite;
import com.bilboldev.utils.Bundle;

public class BarbecueKing extends RoomProp {
    public int grill;
    private boolean dinnerTaken;
    {
        spriteClass = BarbecueKingSprite.class;
    }

    public boolean dinnerTaken() {
        return dinnerTaken;
    }

    public void takeDinner() {
        if (dinnerTaken) return;
        dinnerTaken = true;
        yell(Message.of(this, "stolen"));
    }

    @Override
    public void interact() {
        if (available()) yell(Message.of(this, dinnerTaken ? "after" : "greeting"));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("grill", grill);
        bundle.put("dinner_taken", dinnerTaken);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        grill = bundle.getInt("grill");
        dinnerTaken = bundle.getBoolean("dinner_taken");
    }
}
