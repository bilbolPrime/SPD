package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.mobs.ChampionSkills;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.CombatTextIcon;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.particles.SparkParticle;
import com.bilboldev.pixeldungeonskills.levels.traps.LightningTrap;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class RebornChampion extends Buff {
    public static final int CHAMP_REBORN = 5;
    private boolean applied;
    private boolean pair;
    private Skill[] learned = new Skill[0];
    private int[] ranks = new int[0];
    private int recovery;
    private Char attackTarget;
    private int attackHP;
    private boolean effectApplied;
    private Skill skill = Skill.NONE;

    public enum Skill {
        NONE(-1, true), SMASH(440, false), SMITE(441, false), KNOCKBACK(442, false),
        VENOM(443, false), DOUBLE_STRIKE(444, false), DOUBLE_SHOT(445, false),
        KNEE_SHOT(446, false), CURSE(447, false), SLOW(448, false),
        SHELL(449, true), EVASION(450, true), ACCURACY(451, true), HASTE(452, true),
        WARD(453, true), LEECH(454, false), FROST(455, false), ROOT(456, false),
        BLEED(457, false), AGGRESSION(458, true), BRACE(459, true), SHOCK(460, false), FIRE(461, false);

        public final int icon;
        public final boolean passive;

        Skill(int icon, boolean passive) {
            this.icon = icon;
            this.passive = passive;
        }

        public String key() {
            return "champions." + name().toLowerCase(Locale.ROOT);
        }

        public String title() {
            return Messages.get(key() + ".name");
        }

        public int amount(int rank) {
            switch (this) {
                case SMASH: return 10 + rank * 5;
                case SMITE: return 50 + rank * 10;
                case DOUBLE_STRIKE: case DOUBLE_SHOT: return 60 + rank * 5;
                case KNEE_SHOT: return 10 + rank;
                case SLOW: case ROOT: return 1 + rank / 2;
                case CURSE: case VENOM: case FROST: return 2 + rank;
                case EVASION: case ACCURACY: case BRACE: return rank * 10;
                case HASTE: case WARD: return rank * 4;
                case LEECH: return rank * 2;
                case BLEED: case SHOCK: case FIRE: return rank + 1;
                case AGGRESSION: return rank * 8;
                default: return rank;
            }
        }

        public String description(int rank) {
            int amount = amount(rank);
            String form = "";
            switch (this) {
                case VENOM: case KNEE_SHOT: case CURSE: case SLOW: case FROST: case ROOT:
                    form = durationForm(amount);
                    break;
                default: break;
            }
            return Messages.get(key() + ".desc" + form, amount);
        }

        private static String durationForm(int turns) {
            switch (Messages.lang()) {
                case RUSSIAN: case BELARUSIAN: case UKRAINIAN: case POLISH: case CZECH:
                    return turns == 1 ? ".one" : turns >= 2 && turns <= 4 ? ".few" : "";
                case ENGLISH: case SPANISH: case PORTUGUESE: case FRENCH: case GERMAN:
                case ITALIAN: case DUTCH: case SWEDISH: case GREEK: case ESPERANTO:
                    return turns == 1 ? ".one" : "";
                default: return "";
            }
        }
    }

    public static void grant(Mob mob, boolean pair) {

        long seed = Dungeon.seed == null ? Dungeon.rebornEncounterSeed
                : Dungeon.seed.value(RunSeed.CHAMPION, Dungeon.levelKey());
        seed ^= (long)mob.pos * 0x9E3779B97F4A7C15L ^ mob.rebornPack;
        seed = (seed ^ seed >>> 30) * 0xBF58476D1CE4E5B9L;
        grant(mob, pair, new java.util.Random(seed ^ seed >>> 27));
    }

    public static void grant(Mob mob, boolean pair, java.util.Random random) {
        if (mob.buff(RebornChampion.class) != null) return;
        Skill[] pool = skillsFor(mob.getClass());
        if (pool.length == 0) return;
        RebornChampion buff = new RebornChampion();
        buff.pair = pair;
        buff.roll(pool, random, Dungeon.depth);
        buff.attachTo(mob);
    }

    private void roll(Skill[] pool, java.util.Random random, int depth) {
        ArrayList<Skill> choices = new ArrayList<>(Arrays.asList(pool).subList(0,
                Math.min(pool.length, ChampionSkills.poolSize(depth))));
        int count = (pair ? 1 : 2) + (random.nextInt(100) < 35 ? 1 : 0);
        learned = new Skill[Math.min(count, choices.size())];
        ranks = new int[learned.length];
        int minimum = ChampionSkills.minRank(depth);
        ArrayList<Skill> attacks = new ArrayList<>();
        for (Skill ability : choices) if (!ability.passive) attacks.add(ability);
        for (int i = 0; i < learned.length; i++) {
            ArrayList<Skill> available = i == 0 && !pair && !attacks.isEmpty() ? attacks : choices;
            learned[i] = available.get(random.nextInt(available.size()));
            choices.remove(learned[i]);
            ranks[i] = minimum + random.nextInt(ChampionSkills.maxRank(depth) - minimum + 1);
        }
    }

    public boolean isPair() {
        return pair;
    }

    public Skill[] learnedSkills() {
        return learned.clone();
    }

    public int rank(Skill ability) {
        for (int i = 0; i < learned.length; i++) if (learned[i] == ability) return ranks[i];
        return 0;
    }

    public static Skill[] skillsFor(Class<? extends Mob> type) {
        return ChampionSkills.pool(type);
    }

    @Override
    public boolean attachTo(Char target) {
        if (!(target instanceof Mob)) return false;
        if (target.buff(RebornChampion.class) != null) return target.buff(RebornChampion.class) == this;
        if (!super.attachTo(target)) return false;
        target.champ = CHAMP_REBORN;
        if (!target.name.startsWith("Reborn ")) target.name = "Reborn " + target.name;
        if (!applied) {
            target.HT *= pair ? 2 : 3;
            target.HP = target.HT;
            applied = true;
        }
        updateSpriteState();
        return true;
    }

    @Override
    public boolean act() {
        diactivate();
        return true;
    }

    public void beginAttack(Char enemy) {
        attackTarget = enemy;
        attackHP = enemy == null ? 0 : enemy.HP;
        effectApplied = false;
        skill = Skill.NONE;
        if (enemy == null || !enemy.isAlive() || !target.isAlive()) return;
        if (recovery > 0 && !RebornChallenges.enraged(target)) {
            recovery--;
            return;
        }
        if (Random.Int(2) != 0 && !RebornChallenges.enraged(target)) return;
        int eligible = 0;
        for (Skill candidate : learned) if (!candidate.passive && eligible(candidate, enemy)) eligible++;
        if (eligible == 0) return;
        int choice = Random.Int(eligible);
        for (Skill candidate : learned) {
            if (!candidate.passive && eligible(candidate, enemy) && choice-- == 0) {
                skill = candidate;
                break;
            }
        }
        recovery = 2;
        if (target.sprite != null && target.sprite.visible) {
            target.sprite.showStatus(CharSprite.NEUTRAL, Message.key(skill.key() + ".name"));
        }
    }

    public void ensureAttack(Char enemy) {
        if (attackTarget != enemy) beginAttack(enemy);
    }

    public void finishAttack() {
        attackTarget = null;
        skill = Skill.NONE;
    }

    public float delayMultiplier() {
        return skill == Skill.SMITE ? 1.5f : 1f;
    }

    public boolean doubleAttack() {
        return skill == Skill.DOUBLE_STRIKE || skill == Skill.DOUBLE_SHOT;
    }

    public boolean using(Skill ability) {
        return skill == ability;
    }

    public int damage(int damage) {
        float multiplier = 1f;
        int rank = rank(skill);
        if (skill == Skill.SMASH || skill == Skill.SMITE) multiplier += skill.amount(rank) / 100f;
        if (doubleAttack()) multiplier = skill.amount(rank) / 100f;
        if (target.HT > 0) multiplier += Skill.AGGRESSION.amount(rank(Skill.AGGRESSION))
                * (1f - Math.max(0, target.HP) / (float)target.HT) / 100f;
        return Math.max(0, Math.round(damage * multiplier) + (skill == Skill.KNOCKBACK ? rank : 0));
    }

    public int reduceDamage(int damage, CombatTextIcon type) {
        if (type == CombatTextIcon.PHYSICAL && rank(Skill.SHELL) > 0) {
            damage -= Random.IntRange(0, rank(Skill.SHELL));
        }
        if (type == CombatTextIcon.MAGIC || type == CombatTextIcon.FIRE || type == CombatTextIcon.COLD
                || type == CombatTextIcon.LIGHTNING) damage = Math.round(damage * (1f - .04f * rank(Skill.WARD)));
        return Math.max(0, damage);
    }

    public float accuracy() {
        return 1f + .1f * rank(Skill.ACCURACY);
    }

    public float evasion() {
        return 1f + .1f * rank(Skill.EVASION);
    }

    public float movement() {
        return 1f + .04f * rank(Skill.HASTE);
    }

    private boolean eligible(Skill candidate, Char enemy) {
        switch (candidate) {
            case KNOCKBACK: return Pushing.knockbackCell(target, enemy) != -1;
            case VENOM: return canDebuff(enemy, Poison.class);
            case KNEE_SHOT: return canDebuff(enemy, Cripple.class);
            case CURSE: return canDebuff(enemy, NecroWeakened.class) && canDebuff(enemy, Weakness.class)
                    && canDebuff(enemy, RebornCurse.class);
            case SLOW: return canDebuff(enemy, Slow.class);
            case FROST: return canDebuff(enemy, Frostbite.class) && !enemy.immunities().contains(Frost.class);
            case ROOT: return !enemy.flying && canDebuff(enemy, Roots.class);
            case BLEED: return canDebuff(enemy, Bleeding.class);
            case LEECH: return target.HP < target.HT;
            case SHOCK: return !enemy.immunities().contains(LightningTrap.Electricity.class);
            case FIRE: return !enemy.immunities().contains(Burning.class);
            default: return true;
        }
    }

    private static boolean canDebuff(Char enemy, Class<? extends Buff> type) {
        return enemy.buff(type) == null && !enemy.immunities().contains(type);
    }

    public void onHit(Char enemy) {
        if (enemy != attackTarget || !target.isAlive() || effectApplied) return;
        effectApplied = true;
        int amount = skill.amount(rank(skill));
        if (skill == Skill.LEECH) {
            int healing = Math.min(amount, Math.min(target.HT - target.HP, attackHP - Math.max(0, enemy.HP)));
            if (healing > 0) {
                target.HP += healing;
                target.showRecovery(healing, false);
            }
        }
        if (!enemy.isAlive() || !eligible(skill, enemy)) return;
        switch (skill) {
            case KNOCKBACK: Pushing.knockBack(target, enemy); break;
            case VENOM:
                Poison poison = new Poison();
                poison.setChampionDuration(amount * Poison.durationFactor(enemy));
                poison.attachTo(enemy);
                break;
            case KNEE_SHOT: Buff.affect(enemy, Cripple.class, amount); break;
            case CURSE: Buff.affect(enemy, RebornCurse.class, amount); break;
            case SLOW: Buff.affect(enemy, Slow.class, amount); break;
            case FROST: Buff.affect(enemy, Frostbite.class, amount); break;
            case ROOT: Buff.affect(enemy, Roots.class, amount); break;
            case BLEED: Buff.affect(enemy, Bleeding.class).set(amount); break;
            case SHOCK:
                enemy.damageFrom(amount, LightningTrap.LIGHTNING, target, CombatTextIcon.LIGHTNING);
                if (enemy.sprite != null && enemy.sprite.visible) {
                    enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 4);
                }
                break;
            case FIRE:
                enemy.damageFrom(amount, Fire.class, target, CombatTextIcon.FIRE);
                if (enemy.isAlive()) Buff.affect(enemy, Burning.class).reignite(enemy);
                break;
            default: break;
        }
    }

    public void updateSpriteState() {
        if (target == null || !target.isAlive()) return;
        CharSprite sprite = target.sprite;
        if (sprite != null && (sprite.rebornAura == null || !sprite.rebornAura.exists)) {
            sprite.add(CharSprite.State.CHAMPREBORN);
        }
    }

    public String displayName(String name) {
        if (name.startsWith("Reborn ")) name = name.substring("Reborn ".length());
        return Messages.get(RebornChampion.class, "name", Messages.maybeTranslate(name));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("reborn_applied", applied);
        bundle.put("reborn_pair", pair);
        String[] names = new String[learned.length];
        for (int i = 0; i < names.length; i++) names[i] = learned[i].name();
        bundle.put("champion_skills", names);
        bundle.put("champion_ranks", ranks);
        bundle.put("champion_recovery", recovery);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        applied = bundle.getBoolean("reborn_applied");
        pair = bundle.getBoolean("reborn_pair");
        learned = new Skill[0];
        ranks = new int[0];
        finishAttack();
        recovery = Math.max(0, Math.min(2, bundle.getInt("champion_recovery")));
        if (!bundle.contains("champion_skills")) return;
        String[] names = bundle.getStringArray("champion_skills");
        int[] savedRanks = bundle.getIntArray("champion_ranks");
        if (names == null || savedRanks == null || names.length != savedRanks.length || names.length > 3) return;
        ArrayList<Skill> valid = new ArrayList<>();
        ArrayList<Integer> levels = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if (names[i] == null) continue;
            Skill ability;
            try {
                ability = Skill.valueOf(names[i]);
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (ability == Skill.NONE || valid.contains(ability) || savedRanks[i] < 1 || savedRanks[i] > 5) continue;
            valid.add(ability);
            levels.add(savedRanks[i]);
        }
        learned = valid.toArray(new Skill[0]);
        ranks = new int[learned.length];
        for (int i = 0; i < ranks.length; i++) ranks[i] = levels.get(i);
    }
}
