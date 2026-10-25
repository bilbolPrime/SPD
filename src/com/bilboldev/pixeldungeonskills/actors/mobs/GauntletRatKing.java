package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RatKing;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.GauntletRatKingSprite;
import com.bilboldev.utils.Random;

public class GauntletRatKing extends Mob {
    private int turns;
    {
        name = "rat king";
        HP = HT = 1;
        defenseSkill = EXP = 0;
        spriteClass = GauntletRatKingSprite.class;
        screams = false;
        noLoot();
    }

    @Override
    public String displayName() {
        return Messages.get(RatKing.class, "name");
    }

    @Override
    public com.bilboldev.pixeldungeonskills.messages.Message nameMessage() {
        return com.bilboldev.pixeldungeonskills.messages.Message.key(RatKing.class, "name");
    }

    @Override
    public String description() {
        return Messages.get("gauntlet.king_desc");
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return false;
    }

    @Override
    public int dr() {
        return 0;
    }

    @Override
    public int defenseSkill(Char enemy) {
        return 0;
    }

    @Override
    protected boolean act() {
        if (!Dungeon.hero.isAlive()) {
            spend(TICK);
            return true;
        }
        if (turns++ % 3 == 0) {
            if (sprite != null) sprite.showStatus(CharSprite.NEGATIVE, Message.key("gauntlet.taunt_" + Random.Int(5)));
        } else if (!rooted && !paralysed) {
            getFurther(Dungeon.hero.pos);
        }
        spend(TICK);
        return true;
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        if (GauntletRun.arena() != null) GauntletRun.arena().kingDied();
    }
}
