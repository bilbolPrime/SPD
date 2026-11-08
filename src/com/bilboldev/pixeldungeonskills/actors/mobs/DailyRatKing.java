package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.buffs.Vertigo;
import com.bilboldev.pixeldungeonskills.levels.DailyLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.RatKingSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public class DailyRatKing extends Mob {
    private boolean rallied;

    {
        name = "Rat King";
        spriteClass = RatKingSprite.class;
        HP = HT = 115;
        defenseSkill = 15;
        EXP = 0;
        maxLvl = 0;
        noLoot = true;
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || !isAlive() || sprite == null) {
            spend(TICK);
            return true;
        }
        if (!paralysed && buff(Sleep.class) == null && buff(Terror.class) == null
                && buff(Amok.class) == null && !rallied && HP * 2 <= HT) {
            rallied = true;
            yell(Message.key("daily.rat_king_rally"));
            ArrayList<Integer> cells = new ArrayList<>();
            for (int offset : Level.NEIGHBOURS8) {
                int cell = pos + offset;
                if (cell >= 0 && cell < Level.LENGTH && Level.adjacent(pos, cell)
                        && Level.passable[cell] && !Level.avoid[cell] && Actor.findChar(cell) == null) cells.add(cell);
            }
            for (int i = 0; i < 2 && !cells.isEmpty(); i++) {
                DailyFoe guard = new DailyFoe(7);
                guard.pos = cells.remove(Random.Int(cells.size()));
                guard.state = guard.HUNTING;
                guard.aggro(Dungeon.hero);
                GameScene.add(guard, TICK);
            }
            spend(TICK);
            return true;
        }
        if (state == HUNTING && enemy == Dungeon.hero && Dungeon.hero.isAlive()
                && Dungeon.hero.invisible == 0 && canAttack(Dungeon.hero) && canSwap(this)) {
            for (Mob mob : Dungeon.level.mobs) {
                if (!(mob instanceof DailyFoe) || !((DailyFoe)mob).bodyguard()
                        || !mob.isAlive() || mob.sprite == null || !Level.adjacent(pos, mob.pos)
                        || mob.canAttack(Dungeon.hero) || mob.isCharmedBy(Dungeon.hero) || !canSwap(mob)
                        || !Level.passable[pos] || !Level.passable[mob.pos]
                        || Level.avoid[pos] || Level.avoid[mob.pos]) continue;
                int from = pos;
                pos = mob.pos;
                mob.pos = from;
                Actor.occupyCell(this);
                Actor.occupyCell(mob);
                mob.aggro(Dungeon.hero);
                mob.state = mob.HUNTING;
                spend(1 / speed());
                sprite.visible = Dungeon.visible[pos];
                mob.sprite.visible = Dungeon.visible[mob.pos];
                moveSprite(from, pos);
                mob.moveSprite(pos, mob.pos);
                Dungeon.level.mobPress(this);
                if (mob.isAlive()) Dungeon.level.mobPress(mob);
                return true;
            }
        }
        return super.act();
    }

    private static boolean canSwap(Mob mob) {
        return !mob.sprite.isMoving && !mob.rooted && !mob.paralysed && mob.buff(Sleep.class) == null
                && mob.buff(Terror.class) == null && mob.buff(Amok.class) == null
                && mob.buff(Vertigo.class) == null;
    }

    @Override
    protected Char chooseEnemy() {
        return DailyAlly.targetFor(this, super.chooseEnemy());
    }

    @Override
    protected Char chooseNonHeroEnemy() {
        return DailyAlly.targetFor(this, super.chooseNonHeroEnemy());
    }

    @Override
    public void onAttackComplete() {
        if (!inCurrentRun() || !isAlive() || enemy == null || !enemy.isAlive()) next();
        else super.onAttackComplete();
    }

    @Override
    public void damage(int damage, Object source) {
        if (inCurrentRun()) super.damage(damage, source);
    }

    @Override
    public int attackSkill(Char target) {
        return 18;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(7, 12);
    }

    @Override
    public int dr() {
        return 3;
    }

    @Override
    public void die(Object cause) {
        if (!inCurrentRun()) return;
        destroy();
        if (sprite != null) sprite.die();
        DailyRun.finish(true);
    }

    @Override
    public Message nameMessage() {
        return Message.key("daily.foe_4");
    }

    @Override
    public String displayName() {
        return nameMessage().toString();
    }

    @Override
    public String description() {
        return Messages.get("daily.foe_desc_4");
    }

    @Override
    public String displayDescription() {
        return description();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("daily_rallied", rallied);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        rallied = bundle.getBoolean("daily_rallied");
    }
}
