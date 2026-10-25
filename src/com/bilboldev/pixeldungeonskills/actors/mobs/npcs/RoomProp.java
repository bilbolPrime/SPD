package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.RebornSideRooms;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.RoomPropSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public abstract class RoomProp extends NPC {
    {
        rooted = true;
        spriteClass = RoomPropSprite.class;
    }

    public int image() {
        return 0;
    }

    @Override
    protected boolean act() {
        spend(TICK);
        return true;
    }

    @Override
    public boolean attack(Char enemy) {
        return false;
    }

    protected boolean available() {
        return Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.level != null
                && isAlive() && Dungeon.level.mobs.contains(this) && Level.adjacent(pos, Dungeon.hero.pos);
    }

    protected void consume() {
        RebornSideRooms.remove(this);
        if (sprite != null) sprite.killAndErase();
    }

    @Override
    public void damage(int damage, Object source) {}

    @Override
    public void die(Object source) {}

    @Override
    public void add(Buff buff) {}

    @Override
    public void move(int step) {}

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public String displayName() {
        return Messages.get(getClass(), "name");
    }

    @Override
    public String description() {
        return Messages.get(getClass(), "desc");
    }

    @Override
    public String displayDescription() {
        return description();
    }

    @Override
    public void yell(Message text) {
        GLog.n("custom.generated.entry_s_s_3ac152a1c1", Message.actor(this), text);
    }
}
