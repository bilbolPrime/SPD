package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.RivalSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class RivalServant extends Mob {
    public int ownerId;
    private int rank = 1, role, turnsLeft;

    public RivalServant() {
        spriteClass = SkeletonSprite.class;
        state = HUNTING;
        EXP = 0;
        screams = false;
    }

    public int tier() {
        return rank;
    }

    @Override
    public Message nameMessage() {
        return Message.key(RivalServant.class, role == 2 ? "clone" : "name");
    }

    @Override
    public String displayName() {
        return nameMessage().toString();
    }
    @Override
    public String description() {
        return Messages.get(RivalServant.class, "desc");
    }
    @Override
    public String displayDescription() {
        return description();
    }

    public void configure(int owner, int tier, int role) {
        ownerId = owner;
        rank = tier;
        this.role = role;
        spriteClass = role == 2 ? RivalSprite.class : SkeletonSprite.class;
        HP = HT = role == 2 ? 1 : 8 * tier;
        defenseSkill = 4 * tier;
        range = role == 1 ? 5 : 1;
        turnsLeft = role == 2 ? 6 : 12;
    }

    private RivalBoss owner() {
        for (Mob mob : Dungeon.level.mobs)
            if (mob.id() == ownerId && mob instanceof RivalBoss && mob.isAlive()) return (RivalBoss)mob;
        return null;
    }

    @Override
    protected boolean act() {
        if (owner() == null || --turnsLeft <= 0) {
            dismiss();
            return true;
        }
        return super.act();
    }

    @Override
    public int attackSkill(Char target) {
        return 8 + rank * 4;
    }
    @Override
    public int damageRoll() {
        return Random.NormalIntRange(rank, rank * 2 + 1);
    }

    public void dismiss() {
        HP = 0;
        Actor.remove(this);
        Actor.freeCell(pos);
        Dungeon.level.mobs.remove(this);
        if (sprite != null) sprite.die();
    }

    @Override
    public void die(Object source) {
        RivalBoss owner = owner();
        if (owner instanceof RivalNecromancer) ((RivalNecromancer)owner).servantFell(pos);
        dismiss();
    }

    @Override
    public void destroy() {
        dismiss();
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("rival_owner", ownerId);
        b.put("rival_rank", rank);
        b.put("rival_role", role);
        b.put("rival_lifetime", turnsLeft);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        configure(b.getInt("rival_owner"), b.getInt("rival_rank"), b.getInt("rival_role"));
        super.restoreFromBundle(b);
        turnsLeft = b.getInt("rival_lifetime");
    }
}
