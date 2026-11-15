package com.bilboldev.pixeldungeonskills.actors.skills.skill4;

import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import java.util.Locale;

public enum Talent {
    SMASH(HeroClass.WARRIOR, HeroSubClass.NONE, 0, 0, 0, -1, Effect.STRIKE, 17, 120, 10, 6, 5),
    ENDURANCE(HeroClass.WARRIOR, HeroSubClass.NONE, 1, 2, 0, -1, Effect.HEALTH, 1, 4, 4, 0, 0),
    FIRM_HAND(HeroClass.WARRIOR, HeroSubClass.NONE, 2, 1, 1, 0, Effect.ACCURACY, 10, 2, 2, 0, 0),
    REGENERATION(HeroClass.WARRIOR, HeroSubClass.NONE, 3, 2, 1, 1, Effect.REGENERATION, 2, 1, 1, 0, 0),
    TOUGHNESS(HeroClass.WARRIOR, HeroSubClass.NONE, 4, 2, 2, 3, Effect.RESISTANCE, 3, 2, 2, 0, 0),
    KNOCKBACK(HeroClass.WARRIOR, HeroSubClass.NONE, 5, 0, 1, 0, Effect.PUSH, 18, 110, 10, 8, 8),
    SECOND_WIND(HeroClass.WARRIOR, HeroSubClass.NONE, 6, 2, 3, 4, Effect.HEAL, 294, 8, 4, 10, 24),
    SURE_FOOTING(HeroClass.WARRIOR, HeroSubClass.NONE, 7, 0, 2, -1, Effect.TENACITY, 295, 5, 5, 0, 0),
    MASTERY(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 0, 0, 0, -1, Effect.MASTERY, 11, 1, 1, 0, 0),
    PARRY(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 1, 2, 0, -1, Effect.GUARD, 297, 25, 5, 6, 10),
    SMITE(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 2, 0, 1, 0, Effect.PIERCING_STRIKE, 20, 125, 10, 10, 8),
    RIPOSTE(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 3, 2, 1, 1, Effect.RIPOSTE, 299, 10, 10, 0, 0),
    FOOTWORK(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 4, 1, 1, 0, Effect.FOOTWORK, 300, 1, 1, 0, 0),
    DUELIST(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 5, 2, 2, 3, Effect.DUEL_EVASION, 301, 2, 2, 0, 0),
    DISARMING_STRIKE(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 6, 0, 2, 2, Effect.DISARM, 302, 110, 10, 10, 12),
    FIND_THE_GAP(HeroClass.WARRIOR, HeroSubClass.GLADIATOR, 7, 1, 3, 4, Effect.COMBO_PIERCE, 303, 1, 1, 0, 0),
    AGGRESSION(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 0, 0, 0, -1, Effect.LOW_HEALTH_DAMAGE, 9, 10, 10, 0, 0),
    CHARGE(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 1, 2, 0, -1, Effect.CHARGE, 305, 120, 10, 10, 10),
    RAMPAGE(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 2, 1, 1, 1, Effect.SWEEP, 19, 85, 10, 14, 12),
    BLOODLUST(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 3, 0, 1, 0, Effect.KILL_HASTE, 307, 3, 3, 0, 0),
    WAR_CRY(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 4, 2, 1, 1, Effect.WAR_CRY, 308, 3, 1, 12, 16),
    REND(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 5, 1, 2, 2, Effect.BLEED, 309, 1, 1, 0, 0),
    RELENTLESS(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 6, 2, 2, 4, Effect.KILL_COOLDOWN, 310, 1, 1, 0, 0),
    CORNERED(HeroClass.WARRIOR, HeroSubClass.BERSERKER, 7, 0, 3, 3, Effect.CROWDED_GUARD, 311, 4, 4, 0, 0),
    SPARK(HeroClass.MAGE, HeroSubClass.NONE, 0, 0, 0, -1, Effect.BOLT, 45, 7, 3, 6, 3),
    SPIRITUALITY(HeroClass.MAGE, HeroSubClass.NONE, 1, 2, 0, -1, Effect.MANA, 25, 5, 5, 0, 0),
    SORCERER(HeroClass.MAGE, HeroSubClass.NONE, 2, 0, 1, 0, Effect.WAND_DAMAGE, 34, 6, 6, 0, 0),
    SUMMON_RAT(HeroClass.MAGE, HeroSubClass.NONE, 3, 1, 1, -1, Effect.SUMMON_RAT, 41, 1, 1, 10, 18),
    MEDITATION(HeroClass.MAGE, HeroSubClass.NONE, 4, 2, 1, 1, Effect.MANA_REGENERATION, 26, 1, 1, 0, 0),
    SPIRIT_ARMOR(HeroClass.MAGE, HeroSubClass.NONE, 5, 2, 2, 4, Effect.MANA_GUARD, 27, 1, 1, 0, 0),
    WIZARD(HeroClass.MAGE, HeroSubClass.NONE, 6, 0, 2, 2, Effect.WAND_RECHARGE, 33, 5, 5, 0, 0),
    CHANNELING(HeroClass.MAGE, HeroSubClass.NONE, 7, 1, 3, 6, Effect.COOLDOWN, 319, 3, 3, 0, 0),
    CHARGED_STAFF(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 0, 0, 0, -1, Effect.STAFF_CHARGE, 320, 1, 1, 0, 0),
    ARCANE_WARD(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 1, 2, 0, -1, Effect.GUARD, 321, 25, 5, 7, 10),
    ARCANE_STRIKE(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 2, 0, 1, 0, Effect.PIERCING_STRIKE, 322, 100, 5, 9, 7),
    STEADY_CASTING(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 3, 2, 1, 1, Effect.ACCURACY, 323, 2, 2, 0, 0),
    CONDUCTIVE(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 4, 1, 1, 0, Effect.WAND_MELEE, 324, 1, 1, 0, 0),
    DISCHARGE(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 5, 2, 2, 1, Effect.BURST, 325, 7, 3, 14, 12),
    RIPPLING_FORCE(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 6, 0, 2, 2, Effect.PUSH_CHANCE, 326, 4, 4, 0, 0),
    SPELLWEAVE(HeroClass.MAGE, HeroSubClass.BATTLEMAGE, 7, 1, 3, 4, Effect.SPELLWEAVE, 327, 5, 5, 0, 0),
    DRAIN_LIFE(HeroClass.MAGE, HeroSubClass.WARLOCK, 0, 0, 0, -1, Effect.DRAIN, 179, 6, 2, 10, 9),
    RABID_MINIONS(HeroClass.MAGE, HeroSubClass.WARLOCK, 1, 2, 0, -1, Effect.MINION_DAMAGE, 134, 5, 5, 0, 0),
    SUMMON_SKELETON(HeroClass.MAGE, HeroSubClass.WARLOCK, 2, 1, 1, 1, Effect.SUMMON_SKELETON, 43, 1, 1, 14, 20),
    SOUL_HUNGER(HeroClass.MAGE, HeroSubClass.WARLOCK, 3, 0, 1, 0, Effect.KILL_MANA, 331, 1, 1, 0, 0),
    ENRAGE(HeroClass.MAGE, HeroSubClass.WARLOCK, 4, 2, 1, 1, Effect.ENRAGE, 136, 10, 10, 0, 0),
    SLOW(HeroClass.MAGE, HeroSubClass.WARLOCK, 5, 0, 2, -1, Effect.SLOW, 135, 3, 1, 8, 12),
    SUMMONER(HeroClass.MAGE, HeroSubClass.WARLOCK, 6, 2, 2, 2, Effect.SUMMON_LIMIT, 35, 1, 0, 0, 0),
    DARK_PACT(HeroClass.MAGE, HeroSubClass.WARLOCK, 7, 1, 3, 3, Effect.PACT, 335, 10, 4, 0, 24),
    DOUBLE_STRIKE(HeroClass.ROGUE, HeroSubClass.NONE, 0, 0, 0, -1, Effect.DOUBLE_STRIKE, 61, 70, 10, 8, 8),
    STEALTH(HeroClass.ROGUE, HeroSubClass.NONE, 1, 2, 0, -1, Effect.STEALTH, 50, 1, 1, 0, 0),
    EVASION(HeroClass.ROGUE, HeroSubClass.NONE, 2, 1, 1, 1, Effect.EVASION, 152, 2, 2, 0, 0),
    SMOKE_BOMB(HeroClass.ROGUE, HeroSubClass.NONE, 3, 2, 1, 1, Effect.SHADOW, 65, 8, 3, 10, 20),
    BANDIT(HeroClass.ROGUE, HeroSubClass.NONE, 4, 0, 1, -1, Effect.GOLD, 49, 10, 10, 0, 0),
    VENOM(HeroClass.ROGUE, HeroSubClass.NONE, 5, 0, 2, 4, Effect.VENOM, 57, 10, 10, 4, 5),
    LOCKSMITH(HeroClass.ROGUE, HeroSubClass.NONE, 6, 2, 2, 3, Effect.DISABLE_TRAP, 51, 15, 15, 0, 0),
    LIGHT_STEP(HeroClass.ROGUE, HeroSubClass.NONE, 7, 1, 3, 2, Effect.TENACITY, 343, 5, 5, 0, 0),
    DASH(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 0, 0, 0, -1, Effect.DASH, 344, 2, 0, 6, 8),
    HASTE(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 1, 2, 0, -1, Effect.HASTE, 345, 10, 5, 8, 16),
    SHADOW_CLONE(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 2, 1, 2, 0, Effect.CLONE, 67, 1, 1, 20, 30),
    ESCAPE(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 3, 2, 1, 1, Effect.CLEANSE, 347, 2, 1, 10, 18),
    TRAILBLAZER(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 4, 0, 1, 0, Effect.MOVE_SPEED, 348, 2, 2, 0, 0),
    SKIRMISH(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 5, 0, 2, 4, Effect.AFTER_MOVE, 349, 5, 5, 0, 0),
    KEEP_DISTANCE(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 6, 2, 2, 3, Effect.RANGED_EVASION, 350, 3, 3, 0, 0),
    SECOND_BREATH(HeroClass.ROGUE, HeroSubClass.FREERUNNER, 7, 1, 3, -1, Effect.HEAL, 351, 6, 3, 10, 24),
    AMBUSH(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 0, 0, 0, -1, Effect.UNSEEN_DAMAGE, 352, 5, 5, 0, 0),
    SCORPION(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 1, 2, 0, -1, Effect.POISON_DAMAGE, 58, 10, 10, 0, 0),
    SILENT_DEATH(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 2, 0, 3, 4, Effect.SILENT_KILL, 59, 10, 10, 0, 0),
    EXPOSE(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 3, 1, 1, 0, Effect.ARMOR_PIERCE, 355, 1, 1, 0, 0),
    EXECUTION(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 4, 0, 2, 0, Effect.WOUNDED_DAMAGE, 356, 5, 5, 0, 0),
    DEADLY_THROW(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 5, 2, 1, -1, Effect.RANGED_DAMAGE, 357, 4, 4, 0, 0),
    VITAL_STRIKE(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 6, 1, 2, 3, Effect.PIERCING_STRIKE, 358, 140, 10, 12, 12),
    SILENT_FINISH(HeroClass.ROGUE, HeroSubClass.ASSASSIN, 7, 2, 3, 5, Effect.KILL_HEALTH, 359, 1, 1, 0, 0),
    ACCURACY(HeroClass.HUNTRESS, HeroSubClass.NONE, 0, 0, 0, -1, Effect.RANGED_ACCURACY, 81, 2, 2, 0, 0),
    AWARENESS(HeroClass.HUNTRESS, HeroSubClass.NONE, 1, 2, 0, -1, Effect.RANGED_DODGE, 144, 5, 5, 0, 0),
    DOUBLE_SHOT(HeroClass.HUNTRESS, HeroSubClass.NONE, 2, 0, 1, 0, Effect.DOUBLE_SHOT, 90, 70, 10, 8, 10),
    HUNTING(HeroClass.HUNTRESS, HeroSubClass.NONE, 3, 2, 1, -1, Effect.HUNTING, 74, 1, 1, 0, 0),
    FLETCHING(HeroClass.HUNTRESS, HeroSubClass.NONE, 4, 1, 2, 3, Effect.FLETCHING, 73, 1, 1, 0, 0),
    KO_ARROW(HeroClass.HUNTRESS, HeroSubClass.NONE, 5, 0, 2, 2, Effect.READY_SLEEP, 60, 10, 10, 8, 10),
    WOODCRAFT(HeroClass.HUNTRESS, HeroSubClass.NONE, 6, 2, 2, 3, Effect.STEALTH, 366, 1, 1, 0, 0),
    QUICKDRAW(HeroClass.HUNTRESS, HeroSubClass.NONE, 7, 1, 3, 4, Effect.RANGED_SPEED, 367, 3, 3, 0, 0),
    BARKSKIN(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 0, 0, 0, -1, Effect.ARMOR, 368, 1, 1, 0, 0),
    NATURE_MEND(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 1, 2, 0, -1, Effect.HEAL, 369, 6, 3, 10, 24),
    ENTANGLE(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 2, 0, 1, 0, Effect.ROOT, 370, 2, 1, 10, 12),
    GROWTH(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 3, 1, 1, 0, Effect.HEALTH, 371, 4, 4, 0, 0),
    HERBALIST(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 4, 2, 1, -1, Effect.POISON_RESISTANCE, 372, 5, 5, 0, 0),
    DEW_KEEPER(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 5, 2, 2, 1, Effect.HEALING, 373, 1, 1, 0, 0),
    KNEE_SHOT(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 6, 0, 2, 2, Effect.CRIPPLE_CHANCE, 82, 10, 10, 0, 0),
    THICKET(HeroClass.HUNTRESS, HeroSubClass.WARDEN, 7, 1, 3, 0, Effect.GUARD, 375, 30, 5, 10, 12),
    AIMED_SHOT(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 0, 0, 0, -1, Effect.READY_SHOT, 93, 20, 5, 8, 10),
    LONG_SIGHT(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 1, 2, 0, -1, Effect.DISTANT_DAMAGE, 377, 4, 4, 0, 0),
    PIERCING_SHOT(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 2, 0, 1, 0, Effect.READY_PIERCE, 378, 2, 2, 10, 12),
    IRON_TIP(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 3, 1, 1, -1, Effect.ARROW_PENETRATION, 83, 1, 1, 0, 0),
    MARK_PREY(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 4, 2, 1, 1, Effect.MARK, 380, 10, 5, 8, 12),
    BOMBVOYAGE(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 5, 1, 2, 3, Effect.BOMB_ARROW, 91, 12, 6, 15, 30),
    FOLLOW_THROUGH(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 6, 2, 2, 4, Effect.KILL_AIM, 382, 5, 5, 0, 0),
    KILL_SHOT(HeroClass.HUNTRESS, HeroSubClass.SNIPER, 7, 0, 3, 2, Effect.READY_EXECUTE, 383, 30, 10, 14, 18),
    CURSE(HeroClass.NECROMANCER, HeroSubClass.NONE, 0, 0, 0, -1, Effect.CURSE, 182, 1, 1, 8, 10),
    NECRO_SPIRIT(HeroClass.NECROMANCER, HeroSubClass.NONE, 1, 2, 0, -1, Effect.MANA, 432, 5, 5, 0, 0),
    RAISE_SKELETON(HeroClass.NECROMANCER, HeroSubClass.NONE, 2, 1, 1, -1, Effect.RAISE_SKELETON, 178, 1, 1, 12, 18),
    SUMMON_GHOST(HeroClass.NECROMANCER, HeroSubClass.NONE, 3, 2, 1, 1, Effect.SUMMON_GHOST, 180, 1, 1, 14, 20),
    SOUL_BIND(HeroClass.NECROMANCER, HeroSubClass.NONE, 4, 1, 2, 2, Effect.MINION_DAMAGE, 388, 4, 4, 0, 0),
    NECRO_MEDITATION(HeroClass.NECROMANCER, HeroSubClass.NONE, 5, 2, 2, 1, Effect.MANA_REGENERATION, 433, 1, 1, 0, 0),
    NECRO_DRAIN_LIFE(HeroClass.NECROMANCER, HeroSubClass.NONE, 6, 0, 1, 0, Effect.DRAIN, 434, 8, 3, 10, 10),
    MASTER_OF_DEATH(HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, 7, 1, 3, 6, Effect.REVIVE, 181, 900, -100, 0, 0),
    SPIRIT_BOLT(HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, 0, 0, 0, -1, Effect.BOLT, 392, 8, 3, 8, 5),
    SPIRIT_ARMY(HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, 3, 2, 1, 1, Effect.SUMMON_LIMIT, 435, 1, 0, 0, 0),
    MEND_BONES(HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, 2, 1, 1, 1, Effect.MEND_SUMMONS, 394, 5, 3, 10, 16),
    GUARDIAN_BOND(HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, 1, 2, 0, -1, Effect.MINION_GUARD, 395, 4, 4, 0, 0),
    CORPSE_EXPLOSION(HeroClass.NECROMANCER, HeroSubClass.LICH, 8, 2, 2, -1, Effect.CORPSE_EXPLOSION, 184, 18, 6, 12, 12),
    RAISE_SKELETON_ARCHER(HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, 5, 2, 2, 3, Effect.RAISE_ARCHER, 183, 1, 1, 14, 20),
    LAST_RITES(HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, 6, 1, 2, 2, Effect.MINION_REPRIEVE, 398, 12, 12, 0, 0),
    SOUL_SHELTER(HeroClass.NECROMANCER, HeroSubClass.NONE, 7, 1, 3, -1, Effect.GUARD, 399, 35, 5, 12, 14),
    COLD_TOUCH(HeroClass.NECROMANCER, HeroSubClass.LICH, 0, 0, 0, -1, Effect.SLOW_STRIKE, 400, 110, 10, 8, 8),
    PHYLACTERY(HeroClass.NECROMANCER, HeroSubClass.LICH, 1, 2, 0, -1, Effect.HEALTH, 401, 4, 4, 0, 0),
    NECRO_SLOW(HeroClass.NECROMANCER, HeroSubClass.LICH, 2, 0, 1, 0, Effect.SLOW, 436, 3, 1, 8, 10),
    BONE_SPEAR(HeroClass.NECROMANCER, HeroSubClass.LICH, 3, 1, 1, 0, Effect.HEAVY_BOLT, 403, 10, 4, 12, 8),
    DEATH_FED(HeroClass.NECROMANCER, HeroSubClass.LICH, 4, 2, 1, -1, Effect.KILL_MANA, 404, 1, 1, 0, 0),
    CRUEL_STUDY(HeroClass.NECROMANCER, HeroSubClass.LICH, 5, 1, 2, 3, Effect.WAND_DAMAGE, 405, 5, 5, 0, 0),
    FROZEN_VEIL(HeroClass.NECROMANCER, HeroSubClass.LICH, 6, 2, 2, 4, Effect.GUARD, 406, 30, 5, 10, 14),
    WITHERING_WAVE(HeroClass.NECROMANCER, HeroSubClass.LICH, 7, 0, 3, 2, Effect.CHILL_BURST, 407, 8, 3, 16, 16),
    STEADY_AIM(HeroClass.MERCENARY, HeroSubClass.NONE, 0, 0, 0, -1, Effect.GUN_ACCURACY, 164, 2, 2, 0, 0),
    PACKRAT(HeroClass.MERCENARY, HeroSubClass.NONE, 1, 2, 0, -1, Effect.GUN_AMMO, 163, 1, 1, 0, 0),
    FAST_RELOAD(HeroClass.MERCENARY, HeroSubClass.NONE, 2, 2, 1, 1, Effect.RELOAD, 410, 4, 4, 0, 0),
    QUICK_DRAW(HeroClass.MERCENARY, HeroSubClass.NONE, 3, 0, 1, 0, Effect.GUN_SPEED, 165, 5, 5, 0, 0),
    VETERAN_COMPANY(HeroClass.MERCENARY, HeroSubClass.NONE, 4, 1, 1, -1, Effect.MERC_DAMAGE, 412, 6, 6, 0, 0),
    FIELD_MEDICINE(HeroClass.MERCENARY, HeroSubClass.NONE, 5, 1, 2, 4, Effect.HEAL, 413, 6, 3, 10, 24),
    POWDER(HeroClass.MERCENARY, HeroSubClass.NONE, 6, 0, 2, 3, Effect.GUN_DAMAGE, 414, 4, 4, 0, 0),
    STEADY_NERVES(HeroClass.MERCENARY, HeroSubClass.NONE, 7, 2, 3, 2, Effect.TENACITY, 415, 5, 5, 0, 0),
    VOLLEY(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 0, 0, 0, -1, Effect.READY_SHOT, 416, 20, 5, 8, 10),
    RALLY(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 1, 2, 0, -1, Effect.RALLY, 417, 4, 2, 12, 22),
    GUARD_ORDER(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 2, 1, 1, 1, Effect.MERC_GUARD, 418, 5, 5, 0, 0),
    DRILL(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 3, 0, 1, 0, Effect.ACCURACY, 419, 2, 2, 0, 0),
    SUPPRESS(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 4, 0, 2, 0, Effect.READY_WEAKEN, 420, 3, 1, 10, 12),
    SUPPLY(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 5, 2, 1, 1, Effect.GUN_AMMO, 421, 1, 1, 0, 0),
    COUNTER_FIRE(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 6, 1, 2, 2, Effect.HURT_AIM, 422, 5, 5, 0, 0),
    I_AM_THE_LAW(HeroClass.MERCENARY, HeroSubClass.MARSHAL, 7, 2, 3, 1, Effect.LAW, 167, 10, 10, 12, 60),
    HEADSHOT(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 0, 0, 0, -1, Effect.HEADSHOT, 166, 20, 10, 4, 6),
    INTIMIDATE(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 1, 2, 0, -1, Effect.FEAR, 425, 2, 1, 8, 14),
    WANTED(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 2, 1, 1, -1, Effect.BOUNTY, 162, 1, 1, 0, 0),
    NO_WITNESSES(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 3, 2, 1, 1, Effect.NO_WITNESSES, 169, 10, 10, 4, 6),
    BLOOD_PRICE(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 4, 1, 2, 2, Effect.KILL_HEALTH, 428, 1, 1, 0, 0),
    ARMOR_PIERCER(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 5, 0, 1, 0, Effect.RANGED_PIERCE, 429, 1, 1, 0, 0),
    EXECUTE(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 6, 0, 3, 5, Effect.EXECUTE, 168, 5, 3, 0, 0),
    FINAL_NOTICE(HeroClass.MERCENARY, HeroSubClass.EXECUTIONER, 7, 2, 2, 3, Effect.READY_PIERCE, 431, 3, 2, 14, 16);

    public enum Effect { ACCURACY, AFTER_MOVE, ARMOR, ARMOR_PIERCE, ARROW_PENETRATION, BLEED, BLIND, BOLT, BOMB_ARROW, BOUNTY, BURST, CHARGE, CHILL_BURST, CLEANSE, CLONE, COMBO_PIERCE, COOLDOWN, CORPSE_EXPLOSION, CRIPPLE_CHANCE, CROWDED_GUARD, CURSE, DASH, DISABLE_TRAP, DISARM, DISTANT_DAMAGE, DODGE, DOUBLE_SHOT, DOUBLE_STRIKE, DRAIN, DUEL_EVASION, ENRAGE, EVASION, EXECUTE, FEAR, FLETCHING, FOOTWORK, GOLD, GUARD, GUN_ACCURACY, GUN_AMMO, GUN_DAMAGE, GUN_FINISH, GUN_SPEED, HASTE, HEADSHOT, HEAL, HEALING, HEALTH, HEAVY_BOLT, HUNTING, HURT_AIM, KILL_AIM, KILL_COOLDOWN, KILL_HASTE, KILL_HEALTH, KILL_MANA, LAW, LOW_HEALTH_DAMAGE, MANA, MANA_GUARD, MANA_REGENERATION, MARK, MASTERY, MELEE_DAMAGE, MELEE_SPEED, MEND_SUMMONS, MERC_DAMAGE, MERC_GUARD, MINION_DAMAGE, MINION_GUARD, MINION_REPRIEVE, MOVE_SPEED, NO_WITNESSES, PACT, PIERCING_STRIKE, POISON, POISON_CHANCE, POISON_DAMAGE, POISON_RESISTANCE, PUSH, PUSH_CHANCE, RAISE_ARCHER, RAISE_SKELETON, RALLY, RANGED_ACCURACY, RANGED_DAMAGE, RANGED_DODGE, RANGED_EVASION, RANGED_PIERCE, RANGED_SPEED, READY_EXECUTE, READY_PIERCE, READY_ROOT, READY_SHOT, READY_SLEEP, READY_SLOW, READY_WEAKEN, REGENERATION, RELOAD, RESISTANCE, RESTORE_MANA, REVIVE, RIPOSTE, ROOT, ROOT_STRIKE, SHADOW, SILENT_KILL, SLOW, SLOW_STRIKE, SPELLWEAVE, STAFF_CHARGE, STEALTH, STRIKE, SUMMON_ARCHER, SUMMON_GHOST, SUMMON_LIMIT, SUMMON_RAT, SUMMON_SKELETON, SWEEP, TENACITY, UNSEEN_DAMAGE, VENOM, WAND_DAMAGE, WAND_MELEE, WAND_RECHARGE, WAR_CRY, WEAKEN, WOUNDED_DAMAGE }

    public final HeroClass heroClass;
    public final HeroSubClass subclass;
    public final int slot, column, row, prerequisite, icon, base, step, mana, cooldown;
    public final Effect effect;

    Talent(HeroClass heroClass, HeroSubClass subclass, int slot, int column, int row, int prerequisite, Effect effect,
           int icon, int base, int step, int mana, int cooldown) {
        this.heroClass = heroClass;
        this.subclass = subclass;
        this.slot = slot;
        this.column = column;
        this.row = row;
        this.prerequisite = prerequisite;
        this.effect = effect;
        this.icon = icon;
        this.base = base;
        this.step = step;
        this.mana = mana;
        this.cooldown = cooldown;
    }

    public int amount(int rank) {
        if (rank <= 0) return 0;
        if (rank > 5) {
            switch (effect) {
                case REVIVE: return Math.max(1, (int)Math.ceil(2500.0 / rank));
                case GUARD: return diminishingAmount(rank, 85);
                case CROWDED_GUARD: case MERC_GUARD: case COOLDOWN: return diminishingAmount(rank, 40);
                case MINION_GUARD: case RESISTANCE: case TENACITY: return diminishingAmount(rank, 50);
                case POISON_RESISTANCE: return diminishingAmount(rank, 60);
                case RANGED_DODGE: return diminishingAmount(rank, 75);
                case MINION_REPRIEVE: case SILENT_KILL: return diminishingAmount(rank, 95);
                case CRIPPLE_CHANCE: case PUSH_CHANCE: case POISON_CHANCE:
                case VENOM: case READY_SLEEP: case DISABLE_TRAP: return diminishingAmount(rank, 100);
                case EXECUTE: return diminishingAmount(rank, 30);
                default: break;
            }
        }
        return (int)Math.max(0, Math.min(Integer.MAX_VALUE, base + (long)step * (rank - 1)));
    }

    private int diminishingAmount(int rank, int limit) {
        int trained = base + 4 * step;
        return limit - (int)Math.ceil((limit - trained) * 5.0 / rank);
    }

    public int maxRank() {
        if (step == 0) return 1;
        switch (effect) {
            case SUMMON_RAT: case SUMMON_SKELETON: case SUMMON_ARCHER:
            case SUMMON_GHOST: case RAISE_SKELETON: case RAISE_ARCHER: return 10;
            default: return 5;
        }
    }

    public int attributeWeight() {
        if (!available() || maxRank() == 1) return 0;
        switch (effect) {
            case SUMMON_RAT: case SUMMON_SKELETON: case SUMMON_ARCHER:
            case SUMMON_GHOST: case RAISE_SKELETON: case RAISE_ARCHER: return 4;
            case REVIVE: case SILENT_KILL: case MINION_REPRIEVE: case MASTERY: return 1;
            default: return 2;
        }
    }

    public boolean summonsCreature() {
        switch (effect) {
            case SUMMON_RAT: case SUMMON_SKELETON: case SUMMON_ARCHER:
            case SUMMON_GHOST: case RAISE_SKELETON: case RAISE_ARCHER: return true;
            default: return false;
        }
    }

    public int summonHealth(int rank, boolean lich) {
        if (rank <= 0) return 0;
        long health = effect == Effect.SUMMON_RAT
                ? 6 + 3L * rank + Math.max(0, rank - 5)
                : 12 + 4L * rank + Math.max(0, rank - 5);
        if (effect == Effect.SUMMON_GHOST || effect == Effect.RAISE_ARCHER || effect == Effect.SUMMON_ARCHER)
            health = health * 3 / 4;
        return summonValue(health, lich);
    }

    public int summonDamageMin(int rank, boolean lich) {
        if (rank <= 0) return 0;
        return summonValue(effect == Effect.SUMMON_RAT ? 1 + (rank + 1L) / 2 : 2L + rank, lich);
    }

    public int summonDamageMax(int rank, boolean lich) {
        if (rank <= 0) return 0;
        return summonValue(effect == Effect.SUMMON_RAT ? 3L + rank : 5 + 2L * rank, lich);
    }

    public int summonAccuracy(int rank) {
        if (rank <= 0) return 0;
        int base = effect == Effect.SUMMON_RAT ? 2 : effect == Effect.SUMMON_GHOST ? 7 : 3;
        return (int)Math.min(Integer.MAX_VALUE, base + 2L * rank);
    }

    private int summonValue(long value, boolean lich) {
        if (lich && (effect == Effect.RAISE_SKELETON || effect == Effect.RAISE_ARCHER)) value = value * 5 / 4;
        return (int)Math.min(Integer.MAX_VALUE, value);
    }
    public int requiredLevel() { return 1 + 4 * row; }
    public int requiredPoints() { return 2 * row; }

    public boolean available() {
        switch (this) {
            case STEADY_CASTING: case RIPPLING_FORCE: case SECOND_BREATH:
            case VITAL_STRIKE: case SILENT_FINISH: case GROWTH: case KILL_SHOT:
            case SPIRIT_BOLT: case PHYLACTERY: case FROZEN_VEIL:
            case DRILL: case SUPPLY: case BLOOD_PRICE:
                return false;
            default:
                return true;
        }
    }
    public boolean passive() { return cooldown == 0 && !toggle(); }

    public boolean automatic() {
        switch (effect) {
            case STRIKE: case PIERCING_STRIKE: case PUSH: case DISARM:
            case ROOT_STRIKE: case SLOW_STRIKE: case SWEEP: case DOUBLE_STRIKE: case VENOM:
            case READY_SHOT: case READY_ROOT: case READY_SLOW:
            case READY_PIERCE: case READY_EXECUTE: case READY_WEAKEN:
            case DOUBLE_SHOT: case READY_SLEEP: case BOMB_ARROW: case HEADSHOT: case NO_WITNESSES:
                return true;
            default:
                return false;
        }
    }

    public boolean toggle() {
        return automatic() || effect == Effect.MANA_GUARD || effect == Effect.ENRAGE || effect == Effect.WAND_MELEE;
    }

    public boolean targeted() {
        switch (effect) {
            case STRIKE: case PIERCING_STRIKE: case PUSH: case DISARM: case CHARGE: case DOUBLE_STRIKE:
            case ROOT_STRIKE: case SLOW_STRIKE: case BOLT: case HEAVY_BOLT: case DRAIN:
            case WEAKEN: case SLOW: case BLIND: case POISON: case ROOT: case FEAR: case MARK: case DASH:
            case CURSE: case RAISE_SKELETON: case RAISE_ARCHER: case CORPSE_EXPLOSION:
                return true;
            default:
                return false;
        }
    }

    public boolean aimsAtEnemy() {
        switch (effect) {
            case BOLT: case HEAVY_BOLT: case DRAIN: case WEAKEN: case SLOW: case BLIND:
            case POISON: case ROOT: case FEAR: case MARK: case CURSE:
                return true;
            default:
                return false;
        }
    }
    public String key() { return "skills4." + name().toLowerCase(Locale.ROOT); }
    public String title() { return Messages.get(key() + ".name"); }
    public String rankDescription(int rank) {
        return rankDescription(rank, HeroSubClass.NONE);
    }

    public String rankDescription(int rank, HeroSubClass subclass) {
        if (summonsCreature()) return Messages.get("skills4.rank.summon_stats",
                summonHealth(rank, subclass == HeroSubClass.LICH),
                summonDamageMin(rank, subclass == HeroSubClass.LICH),
                summonDamageMax(rank, subclass == HeroSubClass.LICH), summonAccuracy(rank));
        if (effect == Effect.MASTERY) return Messages.get("skills4.rank.mastery", amount(rank), 5L * amount(rank));
        if (effect == Effect.CLONE) return Messages.get("skills4.rank.clone",
                Math.min(Integer.MAX_VALUE, 7 + 14L * rank), Math.min(Integer.MAX_VALUE, 5 + 4L * rank));
        if (effect == Effect.ENRAGE) return Messages.get("skills4.rank.enrage", amount(rank), Math.min(50, amount(rank)));
        if (effect == Effect.ARROW_PENETRATION)
            return Messages.get(rank == 1 ? "skills4.rank.arrow_penetration_one" : "skills4.rank.arrow_penetration",
                    100L * rank / (rank + 1L), rank);
        return Messages.get("skills4.rank." + effect.name().toLowerCase(Locale.ROOT), displayAmount(rank));
    }
    private int displayAmount(int rank) {
        int value = amount(rank);
        if (effect == Effect.REGENERATION) return healthRecoveryInterval(rank);
        if (effect == Effect.MANA_REGENERATION) return manaRecoveryInterval(rank);
        if (effect == Effect.CURSE) return (int)Math.min(80, 5L * value);
        if (effect == Effect.FLETCHING || effect == Effect.HUNTING) value = productionInterval(rank);
        return value;
    }

    public static int healthRecoveryInterval(int rank) {
        if (rank <= 0) return 0;
        return rank <= 5 ? 120 - 20 * rank : Math.max(1, (int)Math.ceil(100.0 / rank));
    }

    public static int manaRecoveryInterval(int rank) {
        if (rank <= 0) return 0;
        if (rank <= 3) return 11 - rank;
        return rank == 4 ? 6 : Math.max(1, (int)Math.ceil(25.0 / rank));
    }

    public static int productionInterval(int rank) {
        if (rank <= 0) return 0;
        return rank <= 5 ? 100 - 5 * rank : Math.max(1, (int)Math.ceil(375.0 / rank));
    }
}
