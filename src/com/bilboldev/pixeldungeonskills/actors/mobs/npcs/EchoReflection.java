package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.mobs.EchoDuel;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.RebornSideRooms;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.EchoSprite;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Bundle;

public class EchoReflection extends RoomProp {
    public Room chamber;
    public int power, armorTier;
    {
        spriteClass = EchoSprite.class;
    }

    public void configure(Room room, int cell) {
        chamber = room;
        pos = cell;
        armorTier = Dungeon.hero.tier();
    }

    @Override
    public void interact() {
        if (!available() || !chamber.inside(Dungeon.hero.pos)) return;
        GameScene.show(new WndOptions(Message.actor(this), Message.of(this, "challenge"), Message.of(this, "accept"), Message.key(RebornSideRooms.class, "leave")) {
            @Override
            protected void onSelect(int index) {
                if (index == 0) challenge();
            }
        });
    }

    public EchoDuel challenge() {
        if (!available() || !chamber.inside(Dungeon.hero.pos)) return null;
        if (power == 0) power = Math.max(1, Math.min(30, Dungeon.hero.lvl));
        armorTier = Dungeon.hero.tier();
        EchoDuel duel = new EchoDuel(this);
        consume();
        GameScene.add(duel);
        Dungeon.hero.spendAndNext(1f);
        return duel;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("chamber", chamber);
        bundle.put("power", power);
        bundle.put("armor_tier", armorTier);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        chamber = (Room)bundle.get("chamber");
        power = bundle.getInt("power");
        armorTier = bundle.getInt("armor_tier");
    }
}
