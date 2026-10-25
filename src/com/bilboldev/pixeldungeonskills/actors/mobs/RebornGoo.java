package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Ooze;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.LloydsBeacon;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.SewerBossLevel;
import com.bilboldev.pixeldungeonskills.sprites.GooSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public class RebornGoo extends RebornBoss {
    private int healedAt = -1;
    private boolean waterKnockback;
    {
        name = "Goo";
        spriteClass = GooSprite.class;
        HP = HT = 100;
        defenseSkill = 12;
        EXP = 10;
        loot = LloydsBeacon.class;
        lootChance = 0.333f;
    }

    @Override
    protected Class<? extends Mob> original() {
        return Goo.class;
    }

    @Override
    public int attackSkill(Char target) {
        return 12;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(4, 11);
    }

    @Override
    public int dr() {
        return opening() ? 0 : 3;
    }

    @Override
    protected boolean act() {
        if (defeated || !isAlive()) return true;
        if (healedAt != RebornBoss.heroActions()) {
            healedAt = RebornBoss.heroActions();
            if (Level.water[pos] && HP < HT) {
                HP++;
                if (sprite != null) sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
            }
        }
        phase = HP * 2 <= HT ? 1 : phase;
        return super.act();
    }

    @Override
    protected boolean prepareSkill() {
        if (Level.distance(pos, Dungeon.hero.pos) > 2) return false;
        waterKnockback = Level.water[Dungeon.hero.pos];
        int[] cells;
        if (waterKnockback) {

            if (!Level.adjacent(pos, Dungeon.hero.pos)) return false;
            cells = new int[]{Dungeon.hero.pos};
        } else cells = dryCells(cone(pos, Dungeon.hero.pos, 2));
        if (!warn(SLIME, pos, cells)) return false;
        if (sprite instanceof GooSprite) ((GooSprite)sprite).pumpUp();
        return true;
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (!waterKnockback) cells = dryCells(cells);
        int heroCell = Dungeon.hero.pos, gooCell = pos;
        strike(cells, 10, 18);
        if (!isAlive()) return;
        burst(cells, Speck.BUBBLE);
        if (waterKnockback) {
            if (Dungeon.hero.isAlive() && Dungeon.hero.pos == heroCell && contains(cells, heroCell)) {
                Pushing.knockBack(gooCell, Dungeon.hero, 3);
                if (isAlive() && pos == gooCell) Pushing.knockBack(heroCell, this, 1);
            }
        } else if (phase > 0) leaveResidue(cells, SLIME, 3, 5);
        recover(2);
    }

    private int[] dryCells(int[] cells) {
        ArrayList<Integer> dry = new ArrayList<>();
        for (int cell : cells)
            if (valid(cell) && (Level.passable[cell] || Level.avoid[cell])
                    && !Level.solid[cell] && !Level.pit[cell] && !Level.water[cell]) dry.add(cell);
        return array(dry);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (Random.Int(4) == 0) Buff.affect(enemy, Ooze.class);
        return super.attackProc(enemy, damage);
    }

    @Override
    public void move(int cell) {
        if (Dungeon.level instanceof SewerBossLevel) ((SewerBossLevel)Dungeon.level).seal();
        super.move(cell);
    }

    @Override
    public void die(Object cause) {
        if (defeated) return;
        if (Dungeon.level instanceof SewerBossLevel) ((SewerBossLevel)Dungeon.level).unseal();
        super.die(cause);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("healed_at", healedAt);
        b.put("water_knockback", waterKnockback);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        healedAt = b.getInt("healed_at");
        waterKnockback = b.getBoolean("water_knockback");
    }
}
