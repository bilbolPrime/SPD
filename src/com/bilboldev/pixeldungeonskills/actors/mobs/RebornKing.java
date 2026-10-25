package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.items.ArmorKit;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.*;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public class RebornKing extends RebornBoss {
    private boolean calling = true;
    private int nextRole, rallyUntil;
    {
        name = "King of Dwarves";
        spriteClass = KingSprite.class;
        HP = HT = 525;
        defenseSkill = 25;
        EXP = 40;
        state = HUNTING;
    }

    @Override
    protected Class<? extends Mob> original() {
        return King.class;
    }

    @Override
    public int attackSkill(Char target) {
        return 32;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 32);
    }

    @Override
    public int dr() {
        return opening() ? 0 : 12;
    }

    @Override
    protected boolean act() {
        phase = HP * 3 <= HT ? 2 : HP * 3 <= HT * 2 ? Math.max(phase, 1) : phase;
        return super.act();
    }

    public ArrayList<Retainer> court() {
        ArrayList<Retainer> result = new ArrayList<>();
        for (Mob mob : Dungeon.level.mobs) if (mob instanceof Retainer && ((Retainer)mob).ownerId == id() && mob.isAlive())
            result.add((Retainer)mob);
        return result;
    }

    public boolean rallied() {
        if (rallyUntil <= RebornBoss.heroActions()) return false;
        for (Retainer guard : court()) if (guard.role == 2) return true;
        return false;
    }

    public boolean summoning() {
        return calling && warning.length > 0;
    }

    @Override
    public boolean showsWarning() {
        return false;
    }

    @Override
    protected boolean prepareSkill() {
        int cap = phase == 0 ? 3 : 4;
        int size = court().size();
        if (calling && size < cap) {
            ArrayList<Integer> choices = new ArrayList<>(), cells = new ArrayList<>();
            for (int cell = 0; cell < Level.LENGTH; cell++)
                if (Level.passable[cell] && !Level.avoid[cell] && Actor.findChar(cell) == null
                        && Level.distance(cell, Dungeon.hero.pos) >= 3 && Level.distance(pos, cell) <= 5
                        && lineClear(pos, cell) && !dangerous(cell)) choices.add(cell);
            while (!choices.isEmpty() && cells.size() < Math.min(2, cap - size)) {
                int cell = Random.element(choices);
                choices.remove(Integer.valueOf(cell));
                cells.add(cell);
            }
            if (!cells.isEmpty()) return warn(ORDER, pos, array(cells));
        }
        calling = false;
        return warn(ORDER, pos, phase == 2 ? cone(pos, Dungeon.hero.pos, 3) : ray(pos, Dungeon.hero.pos, 10));
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (calling) {
            if (sprite != null && sprite.visible) {
                sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.4f, 2);
                Sample.INSTANCE.play(Assets.SND_CHALLENGE);
            }
            for (int cell : cells) if (Level.passable[cell] && Actor.findChar(cell) == null
                    && Level.distance(cell, Dungeon.hero.pos) >= 2 && court().size() < (phase == 0 ? 3 : 4)) {
                Retainer retainer = new Retainer();
                retainer.ownerId = id();

                int role = nextRole++ % 4;
                if (role == 2) for (Retainer other : court()) if (other.role == 2) {
                    role = 0;
                    break;
                }
                retainer.role(role);
                retainer.pos = cell;
                GameScene.add(retainer);

                if (retainer.sprite != null && retainer.sprite.visible) {
                    retainer.sprite.alpha(0);
                    retainer.sprite.parent.add(new AlphaTweener(retainer.sprite, 1, 0.4f));
                    retainer.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3);
                    new Flare(3, 32).color(0x000000, false).show(retainer.sprite, 2f);
                    Sample.INSTANCE.play(Assets.SND_TELEPORT);
                }
            }
            calling = false;
            recover(1);
        } else {
            strike(cells, phase == 2 ? 25 : 18, phase == 2 ? 35 : 26);
            if (!isAlive()) return;
            burst(cells, ShadowParticle.CURSE);
            rallyUntil = RebornBoss.heroActions() + 4;
            calling = true;
            recover(2);
        }
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return phase == 2 && super.canAttack(enemy);
    }

    @Override
    protected boolean getCloser(int target) {
        if (phase < 2 && Level.distance(pos, target) <= 2) return super.getFurther(target);
        return super.getCloser(target);
    }

    @Override
    public void interruptSkill() {
        super.interruptSkill();
        disruptCourt();
    }

    private void disruptCourt() {
        rallyUntil = 0;
        for (Retainer retainer : court()) {
            retainer.warning = new int[0];
            retainer.recover(2);
        }
    }

    public void heraldFell() {
        if (defeated) return;
        warning = new int[0];
        disruptCourt();
        recover(3);
        yell(Message.of(this, "herald_fell"));
    }

    @Override
    protected void clearSummons() {
        for (Retainer retainer : court()) retainer.dismiss();
    }

    @Override
    public void die(Object source) {
        if (defeated) return;
        Dungeon.level.drop(new ArmorKit(), pos).sprite.drop();
        super.die(source);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("calling", calling);
        b.put("next_role", nextRole);
        b.put("rally_until", rallyUntil);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        calling = b.getBoolean("calling");
        nextRole = b.getInt("next_role");
        rallyUntil = b.getInt("rally_until");
    }

    public static class Retainer extends RebornBoss {
        public int ownerId, role;
        public Retainer() {
            role(0);
            EXP = 0;
            state = HUNTING;
            cooldown = 3;
        }

        public void role(int value) {
            role = value >= 0 && value <= 3 ? value : 0;
            name = "royal retainer";
            spriteClass = RebornUndeadSprite.class;
            HP = HT = role == 0 ? 44 : 30;
            defenseSkill = 20;
        }

        public RebornKing king() {
            for (Mob mob : Dungeon.level.mobs) if (mob instanceof RebornKing && mob.id() == ownerId && mob.isAlive()) return (RebornKing)mob;
            return null;
        }

        @Override
        protected Class<? extends Mob> original() {
            return King.Undead.class;
        }

        @Override
        public String displayName() {
            return Messages.get(this, "name_" + role);
        }

        @Override
        public String description() {
            return Messages.get(this, "desc_" + role);
        }

        @Override
        public boolean showsWarning() {
            return false;
        }

        @Override
        public int attackSkill(Char target) {
            return 28;
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange(10, 17) + (king() != null && king().rallied() ? 4 : 0);
        }

        @Override
        public int dr() {
            return opening() ? 0 : role == 0 ? 5 : 2;
        }

        @Override
        protected boolean act() {
            if (king() == null) {
                dismiss();
                return true;
            }
            return super.act();
        }

        @Override
        protected boolean prepareSkill() {
            if (role != 1 && role != 3) return false;
            int[] cells = ray(pos, Dungeon.hero.pos, role == 3 ? 7 : 9);
            return contains(cells, Dungeon.hero.pos) && warn(ORDER, pos, cells);
        }

        @Override
        protected void skillSound(int kind, int[] cells) {
            sound(role == 3 ? Assets.SND_MISS : Assets.SND_ZAP, .85f, 1, cells);
        }

        @Override
        protected void resolveSkill(int kind, int[] cells) {
            int end = warningOrigin;
            for (int cell : cells) {
                if (!valid(cell) || Level.solid[cell] || !lineClear(warningOrigin, cell)) break;
                end = cell;
                if (role == 3 && Actor.findChar(cell) != null) break;
            }
            if (sprite != null && sprite.parent != null && end != warningOrigin && visibleEffect(cells)) {
                if (role == 3) {
                    ((MissileSprite)sprite.parent.recycle(MissileSprite.class))
                            .reset(warningOrigin, end, ItemSpriteSheet.Arrow, null, null);
                } else {

                    MagicMissile.shadow(sprite.parent, warningOrigin, end, () -> {});
                }
            }
            if (role == 3) strike(new int[]{end}, 10, 17, true);
            else {
                strike(cells, 14, 21, true);
                burst(cells, ShadowParticle.CURSE);
            }
            if (!isAlive()) return;
            recover(2);
            if (sprite != null && sprite.visible && end != warningOrigin) sprite.zap(end);
        }

        @Override
        protected boolean getCloser(int target) {
            if ((role == 1 || role == 3) && Level.distance(pos, target) <= 3) return super.getFurther(target);
            if (role == 2 && king() != null && Level.distance(pos, king().pos) > 2) return super.getCloser(king().pos);
            return super.getCloser(target);
        }

        @Override
        public void die(Object source) {
            if (defeated) return;
            RebornKing king = king();
            dismiss();
            if (role == 2 && king != null) king.heraldFell();
        }

        @Override
        public void storeInBundle(Bundle b) {
            super.storeInBundle(b);
            b.put("king", ownerId);
            b.put("role", role);
        }

        @Override
        public void restoreFromBundle(Bundle b) {
            role(b.getInt("role"));
            super.restoreFromBundle(b);
            ownerId = b.getInt("king");
        }
    }
}
