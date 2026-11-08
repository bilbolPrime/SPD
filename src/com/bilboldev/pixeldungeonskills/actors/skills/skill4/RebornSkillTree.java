package com.bilboldev.pixeldungeonskills.actors.skills.skill4;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.*;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.GhostSummonedPet;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillContainer;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillTree;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.CombatTextIcon;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.BlastParticle;
import com.bilboldev.pixeldungeonskills.effects.particles.SmokeParticle;
import com.bilboldev.pixeldungeonskills.items.Corpse;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Gun;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.MirrorSprite;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

import static com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent.Effect.*;

public final class RebornSkillTree extends SkillTree {
    public final Hero hero;
    private final ArrayList<TalentSkill> talents = new ArrayList<>();
    private int hits;
    private float lastHit = -100;
    private float lastKill = -100;
    private float riposteUntil, hasteUntil, aimUntil, counterUntil, spellUntil;
    private boolean moved;
    private boolean casting;
    private int refundedPoints;

    public RebornSkillTree(Hero hero) {
        super(0, 0);
        this.hero = hero;
        for (Talent talent : Talent.values()) {
            if (talent.heroClass == hero.heroClass) talents.add(new TalentSkill(this, talent));
        }
    }

    public TalentSkill skill(Talent talent) {
        for (TalentSkill skill : talents) if (skill.talent == talent) return skill;
        return null;
    }

    public ArrayList<TalentSkill> talents() {
        ArrayList<TalentSkill> result = new ArrayList<>();
        for (TalentSkill skill : talents) if (skill.talent.available()) result.add(skill);
        return result;
    }

    public int refundedPoints() { return refundedPoints; }

    public TalentSkill prerequisite(Talent talent) {
        if (talent.prerequisite < 0) return null;
        for (TalentSkill skill : talents) {
            if (skill.talent.subclass == talent.subclass && skill.talent.slot == talent.prerequisite) return skill;
        }
        return null;
    }

    public int rank(Talent talent) {
        TalentSkill skill = skill(talent);
        if (skill == null || !branchAvailable(talent)) return 0;
        if (skill.level == 0 && (EquipmentAttribute.bonus(hero, EquipmentAttribute.Type.SKILL, talent.ordinal()) == 0
                || !requirementsMet(talent))) return 0;
        return rank(talent, skill.level);
    }

    private int rank(Talent talent, int learned) {
        int bonus = EquipmentAttribute.bonus(hero, EquipmentAttribute.Type.SKILL, talent.ordinal());
        if (learned > 0) {
            bonus += EquipmentAttribute.bonus(hero, EquipmentAttribute.Type.CLASS_SKILLS, hero.heroClass.ordinal());
            if (talent.subclass != HeroSubClass.NONE) {
                bonus += EquipmentAttribute.bonus(hero, EquipmentAttribute.Type.TREE_SKILLS, talent.subclass.ordinal());
            }
        }
        return Math.min(talent.maxRank(), learned + Math.min(3, bonus));
    }

    private boolean requirementsMet(Talent talent) {
        if (!branchAvailable(talent) || hero.lvl < talent.requiredLevel()) return false;
        int points = 0;
        for (TalentSkill skill : talents) {
            if (skill.talent.subclass != talent.subclass) continue;
            points += skill.level;
        }
        TalentSkill previous = prerequisite(talent);
        return points >= talent.requiredPoints() && (previous == null || previous.level > 0);
    }

    public int nextRank(Talent talent) {
        return rank(talent, skill(talent).level + 1);
    }

    public int bonus(Talent.Effect effect) {
        int value = 0;
        for (TalentSkill skill : talents) if (skill.talent.effect == effect) value += skill.talent.amount(rank(skill.talent));
        return value;
    }

    private boolean branchAvailable(Talent talent) {
        return talent.available() && (talent.subclass == HeroSubClass.NONE || talent.subclass == hero.subClass);
    }

    public String requirement(Talent talent) {
        ArrayList<String> missing = new ArrayList<>();
        if (!branchAvailable(talent)) missing.add(Messages.get("skills4.requires_subclass", (Object)Message.subclass(talent.subclass, false).toString()));
        if (hero.lvl < talent.requiredLevel()) missing.add(Messages.get("skills4.requires_level", talent.requiredLevel()));
        int points = 0;
        for (TalentSkill skill : talents) if (skill.talent.subclass == talent.subclass) points += skill.level;
        if (points < talent.requiredPoints()) missing.add(Messages.get("skills4.requires_points", talent.requiredPoints() - points));
        TalentSkill previous = prerequisite(talent);
        if (previous != null && previous.level == 0) missing.add(Messages.get("skills4.requires_skill", (Object)previous.talent.title()));
        StringBuilder text = new StringBuilder();
        for (String requirement : missing) {
            if (text.length() > 0) text.append('\n');
            text.append(requirement);
        }
        return text.toString();
    }

    @Override public boolean canLevel(Skill skill) {
        return canLevel(skill, Skill.availableSkill);
    }

    public boolean canLevel(Skill skill, int points) {
        return skill instanceof TalentSkill && ((TalentSkill)skill).tree == this && hero.isAlive()
                && points > 0 && skill.level < ((TalentSkill)skill).talent.maxRank()
                && requirementsMet(((TalentSkill)skill).talent);
    }

    @Override public void advance(Skill skill) {
        if (!canLevel(skill)) return;
        skill.level++;
        Skill.availableSkill--;
        hero.refreshBonuses();
        QuickSlot.refresh();
    }

    @Override public void subclassChosen(HeroSubClass subclass) {
        for (TalentSkill skill : talents) {
            if (skill.talent.subclass == HeroSubClass.NONE || skill.talent.subclass == subclass) continue;
            Skill.availableSkill += skill.level;
            skill.level = 0;
            skill.active = false;
            skill.availableAfter = 0;
        }
        for (SkillEffect effect : hero.buffs(SkillEffect.class)) {
            if (effect.talent.subclass != HeroSubClass.NONE && effect.talent.subclass != subclass) effect.detach();
        }
        hero.refreshBonuses();
        QuickSlot.refresh();
    }
    @Override public ArrayList<ColorBlock> getBackground() { return new ArrayList<>(); }

    @Override public ArrayList<SkillContainer> getSkills() {
        ArrayList<SkillContainer> result = new ArrayList<>();

        for (TalentSkill skill : talents) result.add(new SkillContainer(skill, skill.talent.column + 1, skill.talent.row + 1));
        return result;
    }

    @Override public void storeInBundle(Bundle bundle) {
        int[] ranks = new int[talents.size()];
        int[] cooldowns = new int[talents.size()];
        int active = 0;
        for (int i = 0; i < ranks.length; i++) {
            ranks[i] = talents.get(i).level;
            cooldowns[i] = Math.max(0, Math.round((talents.get(i).availableAfter - Skill.skillTrack) * 1000));
            if (talents.get(i).active && talents.get(i).talent.toggle()) active |= 1 << i;
        }
        bundle.put("skill4_ranks", ranks);
        bundle.put("skill4_cooldowns", cooldowns);
        bundle.put("skill4_active", active);
        bundle.put("skill4_hits", hits);
        bundle.put("skill4_last_hit", lastHit - Skill.skillTrack);
        bundle.put("skill4_last_kill", lastKill - Skill.skillTrack);
        bundle.put("skill4_riposte", Math.max(0, riposteUntil - Skill.skillTrack));
        bundle.put("skill4_haste", Math.max(0, hasteUntil - Skill.skillTrack));
        bundle.put("skill4_aim", Math.max(0, aimUntil - Skill.skillTrack));
        bundle.put("skill4_counter", Math.max(0, counterUntil - Skill.skillTrack));
        bundle.put("skill4_spell", Math.max(0, spellUntil - Skill.skillTrack));
        bundle.put("skill4_moved", moved);
    }

    @Override public void restoreSkillsFromBundle(Bundle bundle) {
        int[] ranks = bundle.getIntArray("skill4_ranks");
        int[] cooldowns = bundle.getIntArray("skill4_cooldowns");
        if (ranks.length != talents.size() || cooldowns.length != talents.size())
            throw new IllegalArgumentException("Incomplete skill tree");
        refundedPoints = 0;
        for (int i = 0; i < ranks.length; i++) {
            TalentSkill skill = talents.get(i);
            skill.level = Math.max(0, Math.min(skill.talent.maxRank(), ranks[i]));
            if (!skill.talent.available()) {
                refundedPoints += skill.level;
                skill.level = 0;
            }
            skill.availableAfter = Math.max(0, Math.min(1000000, cooldowns[i])) / 1000f;
            skill.active = skill.talent.toggle() && (bundle.getInt("skill4_active") & (1 << i)) != 0;
        }
        hits = Math.max(0, Math.min(3, bundle.getInt("skill4_hits")));
        lastHit = bundle.getFloat("skill4_last_hit");
        lastKill = bundle.getFloat("skill4_last_kill");
        riposteUntil = bundle.getFloat("skill4_riposte");
        hasteUntil = bundle.getFloat("skill4_haste");
        aimUntil = bundle.getFloat("skill4_aim");
        counterUntil = bundle.getFloat("skill4_counter");
        spellUntil = bundle.getFloat("skill4_spell");
        moved = bundle.getBoolean("skill4_moved");
    }

    public int cooldown(Talent talent) {
        float turns = talent.cooldown * (1 - Math.min(40, bonus(COOLDOWN)) / 100f);
        if (hero.buff(Focus.class) != null) turns = Focus.cooldown(turns);
        return Math.max(1, (int)Math.ceil(turns));
    }

    public void shiftClock(float offset) {
        for (TalentSkill skill : talents) skill.availableAfter += offset;
        lastHit += offset;
        lastKill += offset;
        riposteUntil += offset;
        hasteUntil += offset;
        aimUntil += offset;
        counterUntil += offset;
        spellUntil += offset;
    }

    public void use(TalentSkill skill) {
        if (skill.talent.toggle() || !usable(skill)) return;
        Level floor = Dungeon.level;
        if (skill.talent.effect == CLONE) {
            GameScene.show(new WndOptions(Message.skillName(skill), Message.key("skills4.clone_choice"),
                    Message.key("custom.generated.entry_one_strong_clone_5ad1f563de"),
                    Message.key("custom.generated.entry_three_weak_clones_abda350d7d")) {
                @Override protected void onSelect(int index) {
                    if (Dungeon.level != floor) return;
                    Runnable summon = () -> castClone(skill, index == 1 ? 3 : 1);
                    if (!LocalCoop.defer(hero, summon)) summon.run();
                }
            });
            return;
        }
        if (skill.talent.targeted()) {
            GameScene.selectCell(new CellSelector.Listener() {
                @Override public void onSelect(Integer cell) {
                    QuickSlot.cancel();
                    if (cell == null || Dungeon.level != floor) return;
                    Runnable cast = () -> {
                        if (!cast(skill, cell)) GLog.w("skills4.invalid_target");
                    };
                    if (!LocalCoop.defer(hero, cast)) cast.run();
                }
                @Override public Message prompt() { return Message.key("skills4.target"); }
            });
        } else cast(skill, hero.pos);
    }

    public boolean useAttackSkill(Char target) {
        Skill selected = hero.heroSkills.lastUsed;
        if (!(selected instanceof TalentSkill) || !selected.active) return false;
        TalentSkill skill = (TalentSkill)selected;
        if (!skill.talent.automatic() || readyShot(skill.talent.effect) || skill.talent.effect == VENOM) return false;
        return target != null && Level.adjacent(hero.pos, target.pos) && cast(skill, target.pos);
    }

    public void prepareShot(boolean gun) {
        Skill selected = hero.heroSkills.lastUsed;
        if (!(selected instanceof TalentSkill) || !selected.active) return;
        TalentSkill skill = (TalentSkill)selected;
        if (!readyShot(skill.talent.effect) || !usable(skill)
                || skill.talent.effect == DOUBLE_SHOT || skill.talent.effect == BOMB_ARROW
                || skill.talent.effect == HEADSHOT || skill.talent.effect == NO_WITNESSES) return;
        if (gun != (skill.talent.heroClass == HeroClass.MERCENARY)) return;
        if (gun ? !(hero.belongings.weapon instanceof Gun) : hero.belongings.bow == null) return;
        if (SkillEffect.find(hero, skill.talent.effect) != null) return;
        spend(skill);
        apply(skill.talent, skill.talent.amount(skill.rank()), hero);
    }

    private static boolean readyShot(Talent.Effect effect) {
        switch (effect) {
            case READY_SHOT: case READY_ROOT: case READY_SLOW:
            case READY_PIERCE: case READY_EXECUTE: case READY_WEAKEN:
            case DOUBLE_SHOT: case READY_SLEEP: case BOMB_ARROW: case HEADSHOT: case NO_WITNESSES:
                return true;
            default:
                return false;
        }
    }

    private void spend(TalentSkill skill) {
        hero.spendSkillMana(skill.getManaCost());
        skill.availableAfter = Skill.skillTrack + cooldown(skill.talent);
        if (hero.sprite != null) {
            Message shout = skill.talent == Talent.SMASH
                    ? Message.key("custom.generated.entry_smash_f9f0ebb581") : Message.skillName(skill);
            hero.sprite.showStatus(CharSprite.NEUTRAL, shout);
        }
    }

    private boolean usable(TalentSkill skill) {
        return hero == Dungeon.hero && skill.tree == this && hero.isAlive() && !casting && !hero.paralysed && rank(skill.talent) > 0
                && !skill.talent.passive() && !skill.coolDown() && hero.canUseSkill(skill.getManaCost());
    }

    public boolean cast(TalentSkill skill, int cell) {
        if (skill.talent.effect == MANA_GUARD || skill.talent.effect == ENRAGE
                || skill.talent.effect == WAND_MELEE || !usable(skill)
                || Dungeon.level == null || cell < 0 || cell >= Level.LENGTH) return false;
        Talent talent = skill.talent;
        int power = talent.amount(rank(talent));
        Char target = Actor.findChar(cell);
        int destination = -1;
        boolean corpseSpell = talent.effect == RAISE_SKELETON || talent.effect == RAISE_ARCHER || talent.effect == CORPSE_EXPLOSION;
        if (corpseSpell) {
            if (!Dungeon.visible[cell] || corpseAt(cell) == null
                    || Ballistica.cast(hero.pos, cell, false, false) != cell) return false;
        } else if (talent.targeted() && talent.effect != DASH) {
            if (!(target instanceof Mob) || !((Mob)target).hostile || !target.isAlive()
                    || !Dungeon.visible[cell] || target.invisible > 0 || cell == hero.pos) return false;
            if (Ballistica.cast(hero.pos, cell, false, true) != cell) return false;
        }
        switch (talent.effect) {
            case BOLT: case HEAVY_BOLT: case DRAIN: case WEAKEN: case SLOW: case BLIND:
            case POISON: case ROOT: case FEAR: case MARK: case CURSE:
                if (!RebornChallenges.targetAllowed(hero, cell)) return false;
                break;
            case STRIKE: case PIERCING_STRIKE: case PUSH: case DISARM: case ROOT_STRIKE: case SLOW_STRIKE: case DOUBLE_STRIKE:
                if (!Level.adjacent(hero.pos, cell)) return false;
                break;
            case CHARGE:
                if (Level.distance(hero.pos, cell) > 4 || hero.rooted) return false;
                destination = rushDestination(cell, true);
                if (destination < 0) return false;
                break;
            case DASH:
                if (!Dungeon.visible[cell] || Level.distance(hero.pos, cell) > power || hero.rooted) return false;
                destination = rushDestination(cell, false);
                if (destination < 0) return false;
                break;
            case PACT:
                if (hero.HP <= 5) return false;
                break;
            case SUMMON_RAT: case SUMMON_SKELETON: case SUMMON_ARCHER: case SUMMON_GHOST:
                if (!summon(talent, power, hero.pos)) return false;
                break;
            case RAISE_SKELETON: case RAISE_ARCHER:
                if (!summon(talent, power, cell)) return false;
                consumeCorpse(cell);
                break;
            case CORPSE_EXPLOSION:
                if (!RebornChallenges.targetAllowed(hero, cell)) return false;
                consumeCorpse(cell);
                break;
            case CLONE:
                return castClone(skill, 1);
            case VENOM: case DOUBLE_SHOT: case BOMB_ARROW: case HEADSHOT: case NO_WITNESSES:
                return false;
            case LAW:
                if (SkillEffect.find(hero, LAW) != null) return false;
                break;
            case READY_SHOT: case READY_ROOT: case READY_SLOW: case READY_PIERCE: case READY_EXECUTE: case READY_WEAKEN: case READY_SLEEP:
                if (hero.heroClass == HeroClass.MERCENARY) {
                    if (!(hero.belongings.weapon instanceof Gun)) return false;
                } else if (hero.belongings.bow == null) return false;
                break;
            default: break;
        }
        casting = true;
        spend(skill);
        QuickSlot.remember(hero, skill);
        if (talent.aimsAtEnemy()) QuickSlot.target(null, target);
        hero.curAction = null;
        int from = hero.pos;
        if (destination >= 0) {
            hero.move(destination);
            Dungeon.observe();
            if (!hero.isAlive()) {
                casting = false;
                return true;
            }
        }
        hero.busy();
        Level floor = Dungeon.level;
        Callback impact = () -> finishCast(floor, talent, power, target, cell);
        if (destination >= 0 && from != destination && hero.sprite != null && hero.sprite.parent != null && hero.pos == destination) {
            hero.sprite.dash(from, destination, () -> {
                if (Dungeon.level != floor || !hero.isAlive()) {
                    casting = false;
                    return;
                }
                animateCast(talent, target, cell, impact);
            });
        } else animateCast(talent, target, cell, impact);
        QuickSlot.refresh();
        return true;
    }

    private void animateCast(Talent talent, Char target, int cell, Callback impact) {
        if (hero.sprite != null && hero.sprite.parent != null && target != null && target != hero) {
            switch (talent.effect) {
                case STRIKE: case PIERCING_STRIKE: case PUSH: case DISARM: case ROOT_STRIKE: case SLOW_STRIKE: case CHARGE: case DOUBLE_STRIKE:
                    hero.sprite.attack(cell, impact);
                    break;
                default:
                    hero.sprite.zap(cell);
                    Sample.INSTANCE.play(Assets.SND_ZAP);
                    if (talent.heroClass == HeroClass.NECROMANCER)
                        MagicMissile.shadow(hero.sprite.parent, hero.pos, cell, impact);
                    else MagicMissile.blueLight(hero.sprite.parent, hero.pos, cell, impact);
                    break;
            }
        } else {
            impact.call();
        }
    }

    private void finishCast(Level floor, Talent talent, int power, Char target, int cell) {
        if (!casting) return;
        try {
            if (Dungeon.level != floor || !hero.isAlive()) return;
            if (talent.effect == CORPSE_EXPLOSION) explodeCorpse(cell, power);
            else if (target == null || target == hero || target.isAlive() && floor.mobs.contains(target))
                apply(talent, power, target);
            if (!hero.isAlive()) return;
            Buff.detach(hero, Invisibility.class);
            if (talent.effect == SHADOW) Buff.prolong(hero, Invisibility.class, power);
            hero.spend(talent == Talent.SMITE ? 2f : 1f);
            if (hero.sprite != null) hero.sprite.idle();
            hero.next();
            QuickSlot.refresh();
        } finally {
            casting = false;
        }
    }

    private void apply(Talent talent, int power, Char target) {
        switch (talent.effect) {
            case STRIKE: case PIERCING_STRIKE: case PUSH: case DISARM: case ROOT_STRIKE:
            case SLOW_STRIKE: case CHARGE: case DOUBLE_STRIKE: case SWEEP:
                Sample.INSTANCE.play(Assets.SND_HIT);
                break;
            case DRAIN: case WEAKEN: case SLOW: case BLIND: case ROOT: case POISON: case FEAR:
                Sample.INSTANCE.play(Assets.SND_CURSED);
                break;
            case HEAL: case RESTORE_MANA: case MEND_SUMMONS: case RALLY:
                Sample.INSTANCE.play(Assets.SND_DRINK);
                break;
            case SUMMON_RAT: case SUMMON_SKELETON: case SUMMON_ARCHER: case SUMMON_GHOST:
            case RAISE_SKELETON: case RAISE_ARCHER:
                Sample.INSTANCE.play(Assets.SND_READ);
                break;
            case SHADOW: case DASH:
                Sample.INSTANCE.play(Assets.SND_PUFF);
                break;
            case BURST: Sample.INSTANCE.play(Assets.SND_LIGHTNING); break;
            case CHILL_BURST: Sample.INSTANCE.play(Assets.SND_SHATTER); break;
            case WAR_CRY:
                Sample.INSTANCE.play(Assets.SND_CHALLENGE, 1, 1, .85f);
                if (hero.sprite != null && hero.sprite.parent != null)
                    hero.sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), .18f, 3);
                break;
            case GUARD: case DODGE: case HASTE: case CLEANSE: case LAW: case PACT:
                Sample.INSTANCE.play(Assets.SND_EVOKE);
                break;
            default: break;
        }
        switch (talent.effect) {
            case DOUBLE_STRIKE:
                for (int i = 0; i < 2 && target.isAlive(); i++) {
                    hit(target, Math.max(0, hero.damageRoll() * power / 100 - Random.IntRange(0, Math.max(0, target.dr()))), CombatTextIcon.PHYSICAL);
                }
                break;
            case STRIKE: case PIERCING_STRIKE: case PUSH: case DISARM: case ROOT_STRIKE: case SLOW_STRIKE: case CHARGE:
                int armor = Random.IntRange(0, Math.max(0, target.dr()));
                if (talent.effect == PIERCING_STRIKE) armor /= 2;
                hit(target, Math.max(0, hero.damageRoll() * power / 100 - armor), CombatTextIcon.PHYSICAL);
                if (!target.isAlive()) break;
                if (talent.effect == PUSH) push(target);
                if (talent.effect == DISARM) Buff.prolong(target, NecroWeakened.class, 3).setLevel(5);
                if (talent.effect == ROOT_STRIKE) Buff.prolong(target, Roots.class, 2);
                if (talent.effect == SLOW_STRIKE) Buff.prolong(target, Slow.class, 3);
                if (talent.effect == CHARGE && bonus(BLEED) > 0) Buff.affect(target, Bleeding.class).set(bonus(BLEED));
                break;
            case BOLT: case HEAVY_BOLT:
                hit(target, power + (talent == Talent.SPARK && hero.subClass == HeroSubClass.WARLOCK ? 5 : 0));
                if (target.isAlive() && talent == Talent.SPARK && hero.subClass == HeroSubClass.BATTLEMAGE && Random.Int(2) == 0)
                    Buff.prolong(target, Blindness.class, 3);
                break;
            case DRAIN:
                int health = target.HP;
                hit(target, power);
                recover(hero, Math.max(0, health - Math.max(0, target.HP)) / 2, false);
                break;
            case HEAL: recover(hero, power, false); break;
            case RESTORE_MANA: recover(hero, power, true); break;
            case PACT: hero.damage(5, this); recover(hero, power, true); break;
            case LAW:
                int extraHealth = Math.max(1, Math.min(500, hero.HT * power / 100));
                SkillEffect.grant(hero, talent, extraHealth, 20);
                hero.refreshBonuses();
                recover(hero, extraHealth, false);
                break;
            case SHADOW:
                CellEmitter.get(hero.pos).burst(SmokeParticle.FACTORY, 8);
                break;
            case GUARD: case DODGE: SkillEffect.grant(hero, talent, power, 4); break;
            case HASTE: SkillEffect.grant(hero, talent, power, 5); break;
            case CLEANSE:
                Buff.detach(hero, Roots.class);
                Buff.detach(hero, Cripple.class);
                SkillEffect.grant(hero, talent, power, 4);
                break;
            case READY_SHOT: case READY_ROOT: case READY_SLOW: case READY_PIERCE: case READY_EXECUTE: case READY_WEAKEN: case READY_SLEEP:
                for (SkillEffect effect : hero.buffs(SkillEffect.class)) if (effect.talent.effect.name().startsWith("READY_")) effect.detach();
                SkillEffect.grant(hero, talent, power, 8);
                break;
            case WEAKEN: Buff.prolong(target, NecroWeakened.class, power).setLevel(5); break;
            case CURSE:
                Buff.prolong(target, NecroWeakened.class, 10).setLevel(power);
                Sample.INSTANCE.play(Assets.SND_CURSED);
                break;
            case SLOW: Buff.prolong(target, Slow.class, power); break;
            case BLIND: Buff.prolong(target, Blindness.class, power); break;
            case ROOT: Buff.prolong(target, Roots.class, power); break;
            case POISON: Buff.affect(target, Poison.class).set(power); break;
            case FEAR:
                if (!Bestiary.isBoss(target)) Buff.prolong(target, Terror.class, power).object = hero.id();
                break;
            case MARK: SkillEffect.grant(hero, talent, power, 6).enemy = target.id(); break;
            case SWEEP: case BURST: case CHILL_BURST: case WAR_CRY:
                for (Mob mob : new ArrayList<>(Dungeon.level.mobs)) {
                    if (!mob.hostile || !mob.isAlive() || !Dungeon.visible[mob.pos]
                            || Level.distance(hero.pos, mob.pos) > (talent.effect == WAR_CRY ? 2 : 1)) continue;
                    if (talent.effect == WAR_CRY) Buff.prolong(mob, NecroWeakened.class, power).setLevel(5);
                    else {
                        hit(mob, talent.effect == SWEEP ? Math.max(0, hero.damageRoll() * power / 100 - Random.IntRange(0, mob.dr())) : power,
                                talent.effect == SWEEP ? CombatTextIcon.PHYSICAL : talent.effect == CHILL_BURST ? CombatTextIcon.COLD : CombatTextIcon.MAGIC);
                        if (mob.isAlive() && talent.effect == CHILL_BURST) Buff.prolong(mob, Slow.class, 3);
                        if (mob.isAlive() && talent.effect == SWEEP && bonus(BLEED) > 0) Buff.affect(mob, Bleeding.class).set(bonus(BLEED));
                    }
                }
                break;
            case MEND_SUMMONS: case RALLY:
                if (talent.effect == RALLY) recover(hero, power, false);
                for (Mob mob : Dungeon.level.mobs) {
                    boolean owned = mob instanceof NPC && ((NPC)mob).owner() == hero;
                    if (owned && !mob.hostile && mob.isAlive() && Dungeon.visible[mob.pos]
                            && (talent.effect == MEND_SUMMONS ? mob instanceof SummonedPet : Level.distance(hero.pos, mob.pos) <= 2)) recover(mob, power, false);
                }
                Hero partner = LocalCoop.partner(hero);
                if (talent.effect == RALLY && partner != null && partner.isAlive() && Level.distance(hero.pos, partner.pos) <= 2) recover(partner, power, false);
                break;
            default: break;
        }
    }

    private void hit(Char target, int damage) {
        hit(target, damage, CombatTextIcon.MAGIC);
    }

    private void hit(Char target, int damage, CombatTextIcon type) {
        int before = target.HP;
        target.damageFrom(damage, this, hero, type);
        if (target.sprite != null) target.sprite.flash();
        if (before > 0 && !target.isAlive()) killed(target);
    }

    public static void recover(Char target, int amount, boolean mana) {
        if (!target.isAlive() || amount <= 0) return;
        int before = mana ? target.MP : target.HP;
        if (mana) target.MP = Math.min(target.MMP, target.MP + amount);
        else target.HP = Math.min(target.HT, target.HP + amount);
        target.showRecovery((mana ? target.MP : target.HP) - before, mana);
    }

    private int rushDestination(int cell, boolean attack) {
        if (cell == hero.pos || Ballistica.cast(hero.pos, cell, false, true) != cell) return -1;
        int destination = hero.pos;
        for (int i = 1; i < Ballistica.distance && Ballistica.trace[i] != cell; i++) {
            int next = Ballistica.trace[i];
            if (!Level.passable[next] || Actor.findChar(next) != null) return -1;
            destination = next;
        }
        if (!attack) {
            if (!Level.passable[cell] || Actor.findChar(cell) != null) return -1;
            destination = cell;
        }
        if (attack && !Level.adjacent(destination, cell)) return -1;
        return destination;
    }

    private boolean summon(Talent talent, int power, int origin) {
        int count = 0;
        for (Mob mob : Dungeon.level.mobs) if (mob instanceof SummonedPet && ((SummonedPet)mob).owner() == hero && mob.isAlive()) count++;
        if (count >= SummonedPet.SUMMONED_PETS_LIMIT + summoningLimitBonus()) return false;
        int cell = Actor.findChar(origin) == null && Level.passable[origin] ? origin : -1;
        for (int offset : Level.NEIGHBOURS8) {
            if (cell >= 0) break;
            int next = origin + offset;
            if (next >= 0 && next < Level.LENGTH && Level.adjacent(origin, next) && Level.passable[next] && Actor.findChar(next) == null) {
                cell = next;
                break;
            }
        }
        if (cell < 0) return false;
        SummonedPet.PET_TYPES type = talent.effect == SUMMON_RAT ? SummonedPet.PET_TYPES.RAT
                : talent.effect == SUMMON_ARCHER || talent.effect == RAISE_ARCHER ? SummonedPet.PET_TYPES.SKELETON_ARCHER : SummonedPet.PET_TYPES.SKELETON;
        SummonedPet pet = talent.effect == SUMMON_GHOST ? new GhostSummonedPet() : new SummonedPet(type);
        pet.partyOwner(hero);
        pet.spawn(hero.subClass == HeroSubClass.LICH && (talent.effect == RAISE_SKELETON || talent.effect == RAISE_ARCHER) ? power * 2 : power);
        if (talent.effect == RAISE_SKELETON && hero.subClass == HeroSubClass.SPIRIT_BINDER) pet.setLifetimeMultiplier(2);
        pet.pos = cell;
        GameScene.add(pet);
        return true;
    }

    private void push(Char target) {
        if (Bestiary.isBoss(target) || !Level.adjacent(hero.pos, target.pos)) return;
        int next = target.pos + target.pos - hero.pos;
        if (next < 0 || next >= Level.LENGTH || !Level.adjacent(target.pos, next) || !Level.passable[next] || Actor.findChar(next) != null) return;
        target.move(next);
        if (target.sprite != null) target.sprite.place(next);
        if (target instanceof Mob) Dungeon.level.mobPress((Mob)target);
    }

    private int adjacentEnemies() {
        if (Dungeon.level == null) return 0;
        int count = 0;
        for (Mob mob : Dungeon.level.mobs) if (mob.hostile && mob.isAlive() && Level.adjacent(hero.pos, mob.pos)) count++;
        return count;
    }

    public void moved() { moved = true; }
    public int armorBonus() { return bonus(ARMOR); }
    public int healthBonus() { return bonus(HEALTH) + SkillEffect.power(hero, LAW); }
    public int manaBonus() { return bonus(MANA); }
    public float movement() { return 1 + (bonus(MOVE_SPEED) + SkillEffect.power(hero, HASTE)) / 100f; }
    public float missileSpeed() { return 1 + bonus(RANGED_SPEED) / 100f; }
    public int evasion(Char enemy) { return enemy != null && Level.distance(hero.pos, enemy.pos) > 1 ? bonus(RANGED_EVASION) : 0; }

    public int meleeDefense(Char enemy, int damage) {
        if (enemy == null || !Level.adjacent(hero.pos, enemy.pos)) return damage;
        SkillEffect guard = SkillEffect.find(hero, GUARD);
        if (guard != null) {
            damage = damage * (100 - Math.min(85, guard.power)) / 100;
            guard.detach();
            riposteUntil = Skill.skillTrack + 4;
        }
        if (adjacentEnemies() >= 2) damage = damage * (100 - Math.min(40, bonus(CROWDED_GUARD))) / 100;
        if (damage > 0) counterUntil = Skill.skillTrack + 4;
        return damage;
    }

    public int attack(Char target, int damage, boolean ranged) {
        int percent = 0;
        if (!ranged) {
            if (hero.invisible > 0) percent += bonus(UNSEEN_DAMAGE);
            if (moved) percent += bonus(AFTER_MOVE);
            if (Skill.skillTrack < riposteUntil) percent += bonus(RIPOSTE);
            if (Skill.skillTrack - lastHit > 3 + bonus(FOOTWORK)) hits = 0;
            hits = Math.min(3, hits + 1);
            lastHit = Skill.skillTrack;
            moved = false;
            riposteUntil = 0;
            spellUntil = Skill.skillTrack + 3;
            if (hero.belongings.weapon instanceof Wand) {
                recover(hero, bonus(STAFF_CHARGE), true);
                Wand wand = (Wand)hero.belongings.weapon;
                if (chargedStrikeActive() && wand.curCharges > 0) {
                    damage += wand.curCharges * bonus(WAND_MELEE);
                    wand.curCharges--;
                    wand.updateQuickslot();
                }
            }
            TalentSkill venom = selected(VENOM);
            if (venom != null && usable(venom) && Random.Int(100) < venom.talent.amount(venom.rank())) {
                spend(venom);
                Buff.affect(target, Poison.class).set(3 * venomDamageModifier());
            }
            if (Random.Int(100) < bonus(POISON_CHANCE)) Buff.affect(target, Poison.class).set(3);
            if (Random.Int(100) < bonus(PUSH_CHANCE)) push(target);
        } else {
            if (Level.distance(hero.pos, target.pos) >= 4) percent += bonus(DISTANT_DAMAGE);
            if (Skill.skillTrack < aimUntil) percent += bonus(KILL_AIM);
            if (Skill.skillTrack < counterUntil) percent += bonus(HURT_AIM);
            for (SkillEffect effect : hero.buffs(SkillEffect.class)) {
                switch (effect.talent.effect) {
                    case READY_SHOT: percent += effect.power; break;
                    case READY_EXECUTE: if (target.HP < target.HT / 2) percent += effect.power; break;
                    case READY_PIERCE: break;
                    case READY_ROOT: Buff.prolong(target, Roots.class, effect.power); break;
                    case READY_SLOW: Buff.prolong(target, Slow.class, effect.power); break;
                    case DOUBLE_SHOT: damage = damage * effect.power / 100; continue;
                    case READY_WEAKEN: Buff.prolong(target, NecroWeakened.class, effect.power).setLevel(5); break;
                    default: continue;
                }
                effect.detach();
            }
            aimUntil = 0;
            counterUntil = 0;
        }
        SkillEffect mark = SkillEffect.find(hero, MARK);
        if (mark != null && mark.enemy == target.id()) percent += mark.power;
        if (target.HP < target.HT / 4) percent += bonus(WOUNDED_DAMAGE) + (ranged ? bonus(GUN_FINISH) : 0);
        return Math.max(0, damage * (100 + percent) / 100);
    }

    public int armorAfterPiercing(int armor, boolean ranged) {
        if (ranged && hero.subClass == HeroSubClass.SNIPER) return 0;
        int pierce;
        if (ranged) {
            pierce = bonus(RANGED_PIERCE) + SkillEffect.power(hero, READY_PIERCE);
        } else {
            int nextHits = Skill.skillTrack - lastHit > 3 + bonus(FOOTWORK) ? 1 : hits + 1;
            pierce = bonus(ARMOR_PIERCE) + (nextHits >= 3 ? bonus(COMBO_PIERCE) : 0);
        }
        return Math.max(0, armor - pierce);
    }

    public void killed(Char enemy) {
        if (!(enemy instanceof Mob) || !((Mob)enemy).hostile) return;
        recover(hero, bonus(KILL_HEALTH), false);
        recover(hero, bonus(KILL_MANA), true);
        hasteUntil = Skill.skillTrack + 3;
        aimUntil = Skill.skillTrack + 4;
        if (lastKill != Skill.skillTrack) {
            for (Talent talent : new Talent[]{Talent.CHARGE, Talent.WAR_CRY}) {
                TalentSkill skill = skill(talent);
                if (skill != null) skill.availableAfter = Math.max(Skill.skillTrack, skill.availableAfter - bonus(KILL_COOLDOWN));
            }
        }
        lastKill = Skill.skillTrack;
    }

    @Override public int getWeaponLevelBonus() { return (bonus(MASTERY) + 1) / 2; }
    @Override public float getMeleeModifier() { return 1 + (bonus(MELEE_DAMAGE) + (hero.subClass == HeroSubClass.BERSERKER ? 10 : 0)
            + bonus(LOW_HEALTH_DAMAGE) * (1 - hero.HP / (float)Math.max(1, hero.HT))) / 100f; }
    @Override public float getDamageReductionModifier() { return 1 - Math.min(50, bonus(RESISTANCE) + (hero.subClass == HeroSubClass.BERSERKER ? 10 : 0)) / 100f; }
    @Override public int getMeleeAttackSkillBonus() { return bonus(ACCURACY) + (hero.subClass == HeroSubClass.GLADIATOR ? 3 : 0); }
    @Override public float getMeleeSpeedBonus() { return 1 + (bonus(MELEE_SPEED) + 5 * bonus(MASTERY)
            + (hero.subClass == HeroSubClass.GLADIATOR ? 10 : 0) + (Skill.skillTrack < hasteUntil ? bonus(KILL_HASTE) : 0)) / 100f; }
    @Override public int getStealthBonus() { return bonus(STEALTH) + (hero.subClass == HeroSubClass.ASSASSIN ? 1 : 0); }
    @Override public int evasionDefenceBonus() { return bonus(EVASION) + SkillEffect.power(hero, DODGE) + SkillEffect.power(hero, CLEANSE) + (adjacentEnemies() == 1 ? bonus(DUEL_EVASION) : 0); }
    @Override public int getRangedAttackSkillBonus() { return bonus(RANGED_ACCURACY); }
    @Override public float rangedDamageModifier() { return 1 + (bonus(RANGED_DAMAGE) + (hero.subClass == HeroSubClass.SNIPER ? 20 : 0)) / 100f; }
    @Override public float getWandDamageModifier() { return 1 + (bonus(WAND_DAMAGE) + (Skill.skillTrack < spellUntil ? bonus(SPELLWEAVE) : 0)) / 100f; }
    @Override public float getWandRechargeSpeedModifier() { return 1 / (1 + bonus(WAND_RECHARGE) / 100f); }
    @Override public int SpiritArmorAbsorb(int damage) {
        TalentSkill armor = skill(Talent.SPIRIT_ARMOR);
        if (armor == null || !armor.active || damage <= 0) return 0;
        int absorbed = Math.max(0, Math.min(Math.min(hero.MP, damage), bonus(MANA_GUARD)));
        hero.MP -= absorbed;
        return absorbed;
    }
    @Override public int summoningLimitBonus() { return bonus(SUMMON_LIMIT) + (hero.subClass == HeroSubClass.WARLOCK || hero.subClass == HeroSubClass.LICH ? 1 : 0); }
    @Override public float getMinionDamageModifier() { return 1 + (bonus(MINION_DAMAGE) + enrage()) / 100f; }
    @Override public float getMinionIncomingDamageModifier() { return 1 + (enrage() - Math.min(50, bonus(MINION_GUARD))) / 100f; }
    @Override public int getGunAmmoBonus() { return bonus(GUN_AMMO) + (hero.subClass == HeroSubClass.MARSHAL ? 5 : 0); }
    @Override public int getGunAttackSkillBonus() { return bonus(GUN_ACCURACY) + (hero.subClass == HeroSubClass.MARSHAL ? 1 : 0); }
    @Override public float getGunDamageModifier() { return 1 + bonus(GUN_DAMAGE) / 100f; }
    @Override public float getGunAttackSpeedModifier() { return (1 + bonus(GUN_SPEED) / 100f)
            * (hero.subClass == HeroSubClass.EXECUTIONER && hero.HP < hero.HT * .4f ? 1.5f : 1); }
    @Override public float getGunRearmSpeedModifier() { return 1 + bonus(RELOAD) / 100f + (hero.subClass == HeroSubClass.EXECUTIONER ? .25f : 0); }
    @Override public GunShotData prepareGunShot(Hero hero) {
        GunShotData shot = new GunShotData();
        shot.attackSkillBonus = getGunAttackSkillBonus();
        TalentSkill skill = selected(HEADSHOT);
        if (skill == null) skill = selected(NO_WITNESSES);
        if (skill != null && usable(skill)) {
            spend(skill);
            shot.damageModifier = 1 + skill.talent.amount(skill.rank()) / 100f;
            if (skill.talent.effect == HEADSHOT) shot.attackSkillBonus += skill.rank();
            else shot.flee = true;
        }
        return shot;
    }
    @Override public int getLootBonus(int gold) { return gold * bonus(GOLD) / 100; }
    @Override public int getFletching() { return bonus(FLETCHING) == 0 ? 0 : Math.max(40, 100 - 5 * bonus(FLETCHING)); }
    @Override public int hunting() { return 5 * bonus(HUNTING); }
    @Override public int getWantedGoldBonus() { return bonus(BOUNTY); }
    @Override public float getGunExecuteThreshold() { return Math.min(30, bonus(EXECUTE)) / 100f; }
    @Override public boolean rangedDodgeChance() { return Random.Int(100) < bonus(RANGED_DODGE); }
    @Override public boolean rangedCripple() { return Random.Int(100) < bonus(CRIPPLE_CHANCE); }
    @Override public boolean disableTrap() { return Random.Int(100) < bonus(DISABLE_TRAP); }
    @Override public boolean instantKill() { return Random.Int(100) < bonus(SILENT_KILL); }
    @Override public float venomDamageModifier() { return 1 + bonus(POISON_DAMAGE) / 100f; }
    @Override public boolean canMasterOfDeathRevive() {
        TalentSkill skill = skill(Talent.MASTER_OF_DEATH);
        return skill != null && rank(skill.talent) > 0 && !skill.coolDown();
    }
    @Override public void triggerMasterOfDeathCooldown() {
        TalentSkill skill = skill(Talent.MASTER_OF_DEATH);
        skill.availableAfter = Skill.skillTrack + skill.talent.amount(skill.rank());
    }

    private int enrage() {
        TalentSkill skill = skill(Talent.ENRAGE);
        return skill != null && skill.active ? bonus(ENRAGE) : 0;
    }

    public boolean chargedStrikeActive() {
        TalentSkill skill = skill(Talent.CONDUCTIVE);
        return skill != null && skill.active && skill.rank() > 0;
    }

    public int arrowPenetration() {
        int rank = bonus(ARROW_PENETRATION);
        TalentSkill bomb = selected(BOMB_ARROW);
        if (rank == 0 || bomb != null && usable(bomb)) return 0;
        return Random.Int(rank + 1) > 0 ? rank : 0;
    }

    private TalentSkill selected(Talent.Effect effect) {
        Skill skill = hero.heroSkills.lastUsed;
        return skill instanceof TalentSkill && skill.active && ((TalentSkill)skill).talent.effect == effect
                ? (TalentSkill)skill : null;
    }

    @Override public boolean doubleShot(boolean moreArrows) {
        TalentSkill skill = selected(DOUBLE_SHOT);
        if (!moreArrows || skill == null || !usable(skill)) return false;
        spend(skill);
        SkillEffect.grant(hero, skill.talent, skill.talent.amount(skill.rank()), 2);
        return true;
    }

    public void arrowLanded() {
        SkillEffect extraShot = SkillEffect.find(hero, DOUBLE_SHOT);
        if (extraShot != null) extraShot.detach();
        SkillEffect sleep = SkillEffect.find(hero, READY_SLEEP);
        if (sleep != null) sleep.detach();
    }

    @Override public boolean rangedSleep() {
        SkillEffect sleep = SkillEffect.find(hero, READY_SLEEP);
        return sleep != null && Random.Int(100) < sleep.power;
    }
    @Override public boolean rangedSlow() { return SkillEffect.find(hero, READY_SLEEP) != null; }

    @Override public int aimedShot() { return SkillEffect.find(hero, READY_SHOT) == null ? 0 : 2 * rank(Talent.AIMED_SHOT); }

    @Override public boolean arrowToBomb() {
        TalentSkill skill = selected(BOMB_ARROW);
        if (skill == null || !usable(skill)) return false;
        spend(skill);
        return true;
    }
    @Override public int arrowToBombDamage() { return bonus(BOMB_ARROW); }
    @Override public float arrowToBombParalysis() { return 1 + rank(Talent.BOMBVOYAGE) * .2f; }

    private Corpse corpseAt(int cell) {
        Heap heap = Dungeon.level.heaps.get(cell);
        if (heap != null) for (Item item : heap.items) if (item instanceof Corpse) return (Corpse)item;
        return null;
    }

    private void consumeCorpse(int cell) {
        Heap heap = Dungeon.level.heaps.get(cell);
        Corpse corpse = corpseAt(cell);
        if (corpse.quantity() > 1) corpse.quantity(corpse.quantity() - 1);
        else heap.items.remove(corpse);
        if (heap.items.isEmpty()) heap.destroy();
        else if (heap.sprite != null) heap.sprite.view(heap.image(), heap.glowing());
    }

    private void explodeCorpse(int cell, int power) {
        Sample.INSTANCE.play(Assets.SND_BLAST);
        CellEmitter.center(cell).burst(BlastParticle.FACTORY, 24);
        for (int offset : Level.NEIGHBOURS9) {
            int next = cell + offset;
            if (next < 0 || next >= Level.LENGTH || Level.distance(cell, next) > 1) continue;
            Char target = Actor.findChar(next);
            if (target != null) hit(target, Math.max(0, power - Random.IntRange(0, Math.max(0, target.dr()))), CombatTextIcon.PHYSICAL);
        }
    }

    private boolean castClone(TalentSkill skill, int count) {
        if (!usable(skill) || Dungeon.level == null) return false;
        ArrayList<Integer> cells = new ArrayList<>();
        for (int offset : Level.NEIGHBOURS8) {
            int cell = hero.pos + offset;
            if (cell >= 0 && cell < Level.LENGTH && Level.adjacent(hero.pos, cell)
                    && Level.passable[cell] && Actor.findChar(cell) == null) cells.add(cell);
        }
        if (cells.size() < count) {
            GLog.w("skills4.no_room");
            return false;
        }
        for (Mob mob : new ArrayList<>(Dungeon.level.mobs)) {
            if (mob instanceof SummonedPet && ((SummonedPet)mob).owner() == hero && mob.spriteClass == MirrorSprite.class) mob.die(this);
        }
        int rank = skill.rank();
        for (int i = 0; i < count; i++) {
            SummonedPet clone = new SummonedPet(MirrorSprite.class);
            clone.partyOwner(hero);
            clone.nameText(Message.key(count == 1 ? "actors.hero.abilities.rogue.shadowclone.name" : "custom.generated.entry_weak_shadow_clone_1c63622f3a"));
            clone.level = rank;
            clone.HP = clone.HT = Math.max(1, (7 + 14 * rank) / count);
            clone.defenseSkill = Math.max(1, (5 + 4 * rank) / count);
            clone.pos = cells.get(i);
            clone.screams = false;
            GameScene.add(clone);
            CellEmitter.get(clone.pos).burst(SmokeParticle.FACTORY, 4);
        }
        spend(skill);
        QuickSlot.remember(hero, skill);
        Buff.detach(hero, Invisibility.class);
        Sample.INSTANCE.play(Assets.SND_PUFF);
        hero.spendAndNext(1f);
        QuickSlot.refresh();
        return true;
    }
}
