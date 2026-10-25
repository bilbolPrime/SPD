package com.bilboldev.pixeldungeonskills.actors;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.BlastParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.SmokeParticle;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class DeathBomb extends Actor {
    public int pos;
    public int turns = 3;

    public static void leave(Mob mob) {
        if (!RebornChallenges.active(RebornChallenges.DEATH_THROES) || !mob.hostile
                || mob instanceof NPC || Dungeon.level == null || Level.pit[mob.pos]
                || !Dungeon.level.mobs.contains(mob)) return;
        DeathBomb bomb = new DeathBomb();
        bomb.pos = mob.pos;
        Dungeon.level.deathBombs.add(bomb);

        Actor current = Actor.current();
        float actionEnds = current == null ? Actor.partyTime() : Math.max(Actor.partyTime(), current.time);
        Actor.addDelayed(bomb, actionEnds - Actor.partyTime() + TICK);
    }

    @Override
    public boolean act() {
        if (--turns > 0) {
            spend(TICK);
        } else {
            Actor.remove(this);
            Dungeon.level.deathBombs.remove(this);
            explode();
        }
        return true;
    }

    private void explode() {
        Sample.INSTANCE.play(Assets.SND_BLAST, 2);
        if (Dungeon.visible[pos]) CellEmitter.center(pos).burst(BlastParticle.FACTORY, 30);
        boolean changed = false;
        for (int offset : Level.NEIGHBOURS9) {
            int cell = pos + offset;
            if (cell < 0 || cell >= Level.LENGTH || Level.distance(pos, cell) > 1) continue;
            if (Dungeon.visible[cell]) CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 4);
            if (Level.flamable[cell]) {
                Dungeon.level.destroy(cell);
                GameScene.updateMap(cell);
                changed = true;
            }
            Char target = Actor.findChar(cell);
            if (target == null) continue;
            int damage = Random.Int(1 + Dungeon.depth, 10 + Dungeon.depth * 2) - Random.Int(target.dr());
            if (damage <= 0) continue;
            target.damage(damage, this);
            if (target instanceof Hero && !target.isAlive()) {
                Dungeon.fail(Messages.get(RebornChallenges.class, "bomb_death", Dungeon.depth), DeathBomb.class);
            }
        }
        if (changed) Dungeon.observe();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("pos", pos);
        bundle.put("turns", turns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        pos = bundle.getInt("pos");
        turns = bundle.getInt("turns");
    }
}
