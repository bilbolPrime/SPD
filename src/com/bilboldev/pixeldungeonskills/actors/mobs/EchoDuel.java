package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.EchoReflection;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfSkill;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bullet;
import com.bilboldev.pixeldungeonskills.levels.*;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.EchoSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;

public class EchoDuel extends Mob {
    public Room chamber;
    public int power, armorTier;
    private HeroClass guise = HeroClass.WARRIOR;
    {
        name = "your reflection";
        spriteClass = EchoSprite.class;
        EXP = 0;
        state = HUNTING;
    }

    public EchoDuel() {}
    public EchoDuel(EchoReflection reflection) {
        chamber = reflection.chamber;
        pos = reflection.pos;
        power = reflection.power;
        armorTier = reflection.armorTier;
        guise = Dungeon.hero.heroClass;

        HP = HT = ((guise == HeroClass.HUNTRESS ? 15 : 20) + 5*(power-1))*3/5;
        defenseSkill = 4 + power;
    }

    private boolean present() {
        return Dungeon.level != null && Dungeon.level.mobs.contains(this);
    }

    private boolean inDuel() {
        return present() && chamber != null && Dungeon.hero != null && Dungeon.hero.isAlive()
                && chamber.inside(Dungeon.hero.pos) && chamber.inside(pos);
    }

    private boolean ranged() {
        return guise != HeroClass.WARRIOR && guise != HeroClass.ROGUE;
    }

    @Override
    public int attackSkill(Char target) {
        return 8 + power;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(2 + power/3, 4 + power/2);
    }

    @Override
    public int dr() {
        return power/4;
    }

    @Override
    protected float attackDelay() {
        return guise == HeroClass.ROGUE ? .85f : ranged() ? 1.3f : 1f;
    }

    @Override
    protected boolean act() {
        if (!inDuel()) {
            withdraw();
            return true;
        }
        return super.act();
    }

    @Override
    protected Char chooseEnemy() {
        Char chosen = super.chooseEnemy();
        return chosen != null && chamber.inside(chosen.pos) ? chosen : Dungeon.hero;
    }

    @Override
    protected boolean canAttack(Char target) {
        return inDuel() && target != null && chamber.inside(target.pos) && !isCharmedBy(target)
                && Level.distance(pos, target.pos) <= (ranged() ? 4 : 1)
                && Ballistica.cast(pos, target.pos, false, true) == target.pos;
    }

    @Override
    protected boolean getCloser(int target) {
        if (rooted || !inDuel()) return false;
        boolean[] allowed = new boolean[Level.LENGTH];
        for (int cell = 0; cell < allowed.length; cell++) allowed[cell] = chamber.inside(cell) && Level.passable[cell];
        int step = Dungeon.findPath(this, pos, target, allowed, Level.fieldOfView);
        if (step < 0) return false;
        move(step);
        return true;
    }

    @Override
    protected boolean getFurther(int target) {
        return false;
    }

    @Override
    public boolean canBeKnockedBackInto(int cell) {
        return chamber != null && chamber.inside(cell) && super.canBeKnockedBackInto(cell);
    }

    @Override
    public void move(int step) {
        if (chamber != null && chamber.inside(step)) super.move(step);
    }

    @Override
    public void beckon(int cell) {}

    @Override
    protected boolean doAttack(final Char target) {
        if (Level.adjacent(pos, target.pos)) return super.doAttack(target);
        spend(attackDelay());
        if (sprite != null && Dungeon.visible[pos]) {
            sprite.turnTo(pos, target.pos);
            Callback impact = new Callback() {
                @Override
                public void call() {
                    if (isAlive() && target.isAlive() && canAttack(target)) attack(target);
                    next();
                }
            };
            if (guise == HeroClass.MAGE || guise == HeroClass.NECROMANCER)
                MagicMissile.purpleLight(sprite.parent, pos, target.pos, impact);
            else ((MissileSprite)sprite.parent.recycle(MissileSprite.class)).reset(pos, target.pos,
                    guise == HeroClass.MERCENARY ? new Bullet() : new Arrow(), impact);
            return false;
        }
        attack(target);
        return true;
    }

    @Override
    public void damage(int damage, Object source) {
        if (!inDuel()) {
            withdraw();
            return;
        }
        super.damage(damage, source);
    }

    @Override
    public void die(Object source) {
        if (!present()) return;
        if (!inDuel()) {
            withdraw();
            return;
        }
        RebornSideRooms.remove(this);
        if (sprite != null) sprite.die();
        Dungeon.level.drop(new ScrollOfSkill().identify(), pos);
        GLog.p(EchoReflection.class, "victory");
    }

    private void withdraw() {
        if (!present()) return;
        EchoReflection invitation = new EchoReflection();
        int cell = pos;
        if (!chamber.inside(cell)) {
            cell = -1;
            for (int y = chamber.top+1; y < chamber.bottom && cell < 0; y++)
                for (int x = chamber.left+1; x < chamber.right; x++) {
                    int p = x+y*Level.WIDTH;
                    if (Level.passable[p] && Actor.findChar(p) == null) {
                        cell = p;
                        break;
                    }
                }
            if (cell < 0) {
                spend(TICK);
                return;
            }
        }
        invitation.configure(chamber, cell);
        invitation.power = power;
        invitation.armorTier = armorTier;
        RebornSideRooms.remove(this);
        if (sprite != null) sprite.killAndErase();
        RebornSideRooms.add(invitation);
        GLog.i(EchoReflection.class, "withdrawn");
    }

    @Override
    public boolean reset() {
        withdraw();
        return true;
    }

    @Override
    public boolean attack(Char target) {
        return canAttack(target) && super.attack(target);
    }

    @Override
    public String displayName() {
        return Messages.get(EchoReflection.class, "name");
    }

    @Override
    public Message nameMessage() {
        return Message.key(EchoReflection.class, "name");
    }

    @Override
    public String description() {
        return Messages.get(EchoReflection.class, "desc");
    }

    @Override
    public String displayDescription() {
        return description();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("chamber", chamber);
        bundle.put("power", power);
        bundle.put("armor_tier", armorTier);
        bundle.put("guise", guise.name());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        chamber = (Room)bundle.get("chamber");
        power = bundle.getInt("power");
        armorTier = bundle.getInt("armor_tier");
        guise = HeroClass.valueOf(bundle.getString("guise"));
        defenseSkill = 4 + power;
    }
}
