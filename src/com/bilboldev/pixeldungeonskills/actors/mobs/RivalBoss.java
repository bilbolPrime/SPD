package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.*;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornCurse;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.ArmorKit;
import com.bilboldev.pixeldungeonskills.items.LloydsBeacon;
import com.bilboldev.pixeldungeonskills.items.TomeOfMastery;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfThorns;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.SewerBossLevel;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.RivalSprite;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public abstract class RivalBoss extends RebornBoss {
    private static final int[] ACCURACY = {12, 20, 28, 32};
    private static final int[] EVASION = {8, 14, 18, 24};
    private static final int[] ARMOR = {2, 4, 7, 10};
    private static final int[] MIN_DAMAGE = {4, 8, 13, 18}, MAX_DAMAGE = {8, 13, 20, 26};
    private static final int[] MIN_SPECIAL = {8, 14, 22, 30}, MAX_SPECIAL = {12, 20, 30, 40};
    private int floor = 5, turns, attacks, retreats, responseWaits;
    private int[] responseIds = new int[0], responseActions = new int[0];
    private boolean entered;
    protected int ability;

    public RivalBoss() {
        spriteClass = RivalSprite.class;
        state = HUNTING;
        screams = false;
    }

    public abstract HeroClass heroClass();
    protected abstract int[] health();
    protected abstract Skill abilityName();

    public boolean meleeWarning() {
        return false;
    }

    public final int tier() {
        return floor / 5;
    }

    public final void configure(int depth) {
        if (depth < 5 || depth > 20 || depth % 5 != 0) throw new IllegalArgumentException("Invalid rival floor");
        floor = depth;
        HP = HT = Math.round(health()[tier() - 1] * Dungeon.currentDifficulty.mobHPModifier());
        defenseSkill = EVASION[tier() - 1] + (heroClass() == HeroClass.ROGUE ? 2 : 0);
        EXP = tier() * 10;
        maxLvl = 30;
        range = preferredRange();
        loot = floor == 5 ? LloydsBeacon.class : floor == 15 ? RingOfThorns.class : null;
        lootChance = loot == null ? 0 : 0.333f;
    }

    @Override
    protected Class<? extends Mob> original() {
        return getClass();
    }

    @Override
    public Message nameMessage() {
        return Message.key(RivalBoss.class, "name", Message.heroClass(heroClass()));
    }

    @Override
    public String displayName() {
        return nameMessage().toString();
    }

    @Override
    public String description() {
        return Messages.get(RivalBoss.class, "desc", floor);
    }

    @Override
    public int attackSkill(Char target) {
        return ACCURACY[tier() - 1];
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(MIN_DAMAGE[tier() - 1], MAX_DAMAGE[tier() - 1]);
    }

    @Override
    public int dr() {
        if (opening()) return 0;
        int armor = ARMOR[tier() - 1];
        if (heroClass() == HeroClass.WARRIOR) armor += 2;
        if (heroClass() == HeroClass.MAGE || heroClass() == HeroClass.NECROMANCER) armor /= 2;
        return armor;
    }

    public final void enter() {
        if (entered || defeated) return;
        entered = true;
        int cell = pos;
        for (int offset : Level.NEIGHBOURS8) {
            int next = pos + offset;
            if (valid(next) && Level.adjacent(pos, next) && Level.passable[next]
                    && !Level.avoid[next] && Actor.findChar(next) == null) {
                cell = next;
                break;
            }
        }
        GameScene.bossRemains(floor, cell);
        PartySession.bossRemains(floor, cell);
        String[] taunts = {"glory", "first", "late"};
        yell(Message.key(RivalBoss.class, taunts[Random.Int(taunts.length)]));
        notice();
    }

    @Override
    protected boolean act() {
        if (floor == 5 && !entered) {
            spend(TICK);
            return true;
        }
        turns++;
        phase = Math.max(phase, HP * 3 <= HT ? 2 : HP * 3 <= HT * 2 ? 1 : 0);
        return super.act();
    }

    @Override
    protected int actionClock() {
        return turns;
    }

    protected final int nextAbility() {
        int count = phase == 0 ? 1 : phase == 1 ? Math.min(tier(), 2) : tier();
        return attacks % count;
    }

    protected final boolean aim(int skill, int[] cells) {
        ability = skill;
        if (!warn(DRILL, pos, cells)) return false;
        attacks++;
        Hero[] heroes = LocalCoop.active() ? LocalCoop.heroes() : new Hero[]{Dungeon.hero};
        responseIds = new int[heroes.length];
        responseActions = new int[heroes.length];
        for (int i = 0; i < heroes.length; i++) {

            responseIds[i] = heroes[i].id();
            responseActions[i] = heroes[i].rebornActions;
            heroes[i].interrupt();
            heroes[i].restoreHealth = false;
        }
        responseWaits = 0;
        return true;
    }

    @Override
    protected boolean awaitingResponse() {
        responseWaits++;
        Hero[] heroes = LocalCoop.active() ? LocalCoop.heroes() : new Hero[]{Dungeon.hero};
        for (Hero hero : heroes) {
            if (!hero.isAlive()) continue;
            for (int i = 0; i < responseIds.length; i++) {
                if (hero.id() == responseIds[i] && hero.rebornActions - responseActions[i] < responsesRequired()
                        && (!hero.paralysed || responseWaits <= 3)) return true;
            }
        }
        return false;
    }

    @Override
    protected Message warningMessage() {
        return Message.key(RivalBoss.class, meleeWarning() ? "melee_warning" : "warning",
                Message.actor(this), Message.skill(abilityName(), 0, false));
    }

    protected int responsesRequired() {
        return 1;
    }

    @Override
    protected void recover(int actions) {
        super.recover(actions);
        cooldown = Math.max(actions, cooldown - phase);
    }

    @Override
    protected void warningSound(int kind, int[] cells) {
        sound(Assets.SND_CHALLENGE, .4f, 1, cells);
    }

    @Override
    protected void skillSound(int kind, int[] cells) {
        sound(Assets.SND_HIT, .7f, 1, cells);
    }

    protected final void special(int[] cells, float scale, boolean ranged) {
        strike(cells, Math.round(MIN_SPECIAL[tier() - 1] * scale),
                Math.round(MAX_SPECIAL[tier() - 1] * scale), ranged);
    }

    protected final int spell(int[] cells) {
        int lost = 0;
        for (int cell : cells) {
            if (!valid(cell) || Level.solid[cell] || !lineClear(warningOrigin, cell)) continue;
            Char victim = Actor.findChar(cell);
            if (victim == null || victim == this || ally(victim)) continue;
            int damage = Math.round(Random.NormalIntRange(MIN_SPECIAL[tier() - 1], MAX_SPECIAL[tier() - 1]) * .75f);
            NecroWeakened weakened = buff(NecroWeakened.class);
            if (weakened != null) damage = Math.round(damage * weakened.outgoingDamageModifier());
            damage = RebornCurse.spellDamage(this, damage);
            if (victim instanceof Hero) damage = adjustHeroDamage((Hero)victim, damage);
            int hp = victim.HP;
            victim.damageFrom(damage, this, this);
            lost += Math.max(0, hp - Math.max(0, victim.HP));
            if (victim.sprite != null) victim.sprite.flash();
            if (victim instanceof Hero && !victim.isAlive())
                Dungeon.fail(Utils.format(ResultDescriptions.MOB, Utils.indefinite(displayName()), Dungeon.depth), this);
            if (!isAlive()) break;
        }
        return lost;
    }

    protected final RivalServant summon(int role) {
        if (!isAlive()) return null;
        for (int offset : Level.NEIGHBOURS8) {
            int cell = pos + offset;
            if (!valid(cell) || !Level.adjacent(pos, cell) || !movementFloor()[cell]
                    || Level.avoid[cell] || dangerous(cell) || Actor.findChar(cell) != null) continue;
            RivalServant servant = new RivalServant();
            servant.configure(id(), tier(), role);
            servant.pos = cell;
            GameScene.add(servant, TICK);
            burst(new int[]{cell}, Speck.RATTLE);
            return servant;
        }
        return null;
    }

    protected final int[] aimedLine(int reach) {
        int[] cells = ray(pos, Dungeon.hero.pos, reach);
        return contains(cells, Dungeon.hero.pos) ? cells : new int[0];
    }

    protected int preferredRange() {
        return 1;
    }

    @Override
    protected boolean doAttack(Char enemy) {
        if (preferredRange() > 1 && !rooted && retreats < 2
                && Level.distance(pos, enemy.pos) < preferredRange() - 1 && getFurther(enemy.pos)) {
            retreats++;
            spend(TICK);
            return true;
        }
        retreats = 0;
        return super.doAttack(enemy);
    }

    protected final void reposition() {
        if (rooted) return;
        int destination = pos;
        int distance = Level.distance(pos, Dungeon.hero.pos);
        for (int offset : Level.NEIGHBOURS8) {
            int cell = pos + offset;
            if (valid(cell) && Level.adjacent(pos, cell) && movementFloor()[cell]
                    && !Level.avoid[cell] && !dangerous(cell) && Actor.findChar(cell) == null
                    && Level.distance(cell, Dungeon.hero.pos) >= distance) {
                destination = cell;
                distance = Level.distance(cell, Dungeon.hero.pos);
            }
        }
        if (destination != pos) {
            int from = pos;
            move(destination);
            if (sprite != null) sprite.move(from, destination);
        }
    }

    @Override
    protected boolean ally(Char ch) {
        return super.ally(ch) || ch instanceof RivalServant && ((RivalServant)ch).ownerId == id();
    }

    @Override
    protected void clearSummons() {
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (mob instanceof RivalServant && ((RivalServant)mob).ownerId == id()) {
                ((RivalServant)mob).dismiss();
            }
        }
    }

    @Override
    public void die(Object cause) {
        if (defeated) return;
        if (Dungeon.level instanceof SewerBossLevel) ((SewerBossLevel)Dungeon.level).unseal();
        if (floor == 10) Dungeon.level.drop(new TomeOfMastery(), pos).sprite.drop();
        if (floor == 20) Dungeon.level.drop(new ArmorKit(), pos).sprite.drop();
        super.die(cause);
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("rival_floor", floor);
        b.put("rival_turns", turns);
        b.put("rival_attacks", attacks);
        b.put("rival_retreats", retreats);
        b.put("rival_entered", entered);
        b.put("rival_ability", ability);
        b.put("rival_responses", responseIds);
        b.put("rival_actions", responseActions);
        b.put("rival_waits", responseWaits);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        configure(b.getInt("rival_floor"));
        super.restoreFromBundle(b);
        turns = b.getInt("rival_turns");
        attacks = b.getInt("rival_attacks");
        retreats = b.getInt("rival_retreats");
        entered = b.getBoolean("rival_entered");
        ability = b.getInt("rival_ability");
        responseIds = b.getIntArray("rival_responses");
        responseActions = b.getIntArray("rival_actions");
        responseWaits = b.getInt("rival_waits");
    }
}
