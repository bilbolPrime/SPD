package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.utils.Bundle;

public final class RivalEncounter {
    private static int checked, floor, chosen;

    private RivalEncounter() { }

    public static void reset() {
        checked = floor = chosen = 0;
    }

    public static Mob boss(int depth) {
        if (RebornChallenges.doubleBoss() || !Dungeon.rules().hasRebornBosses() || depth < 5 || depth > 20 || depth % 5 != 0)
            return Bestiary.mob(depth);
        if (PartySession.client()) throw new IllegalStateException("Only the host chooses a boss");
        int bit = 1 << (depth / 5 - 1);
        if ((checked & bit) == 0) {
            checked |= bit;
            java.util.Random roll = new java.util.Random(0x5EEDB055L ^ ((long)Dungeon.rebornEncounterSeed << 16));
            int chance = 0, hero = 0;

            for (int bossDepth = 5; bossDepth <= depth; bossDepth += 5) {
                chance = roll.nextInt(4);
                hero = 1 + roll.nextInt(6);
            }
            if (floor == 0 && chance == 0 && eligible(depth)) {
                floor = depth;
                chosen = hero;
            }
        }
        if (floor != depth) return Bestiary.mob(depth);
        return rival(chosen, depth);
    }

    private static RivalBoss rival(int hero, int depth) {
        RivalBoss rival;
        switch (hero) {
            case 1: rival = new RivalWarrior(); break;
            case 2: rival = new RivalMage(); break;
            case 3: rival = new RivalRogue(); break;
            case 4: rival = new RivalHuntress(); break;
            case 5: rival = new RivalNecromancer(); break;
            case 6: rival = new RivalMercenary(); break;
            default: throw new IllegalStateException("Invalid rival class");
        }
        rival.configure(depth);
        return rival;
    }

    private static boolean eligible(int depth) {
        if (LocalCoop.active()) return true;
        Badges.Badge[] bosses = {Badges.Badge.BOSS_SLAIN_1, Badges.Badge.BOSS_SLAIN_2,
                Badges.Badge.BOSS_SLAIN_3, Badges.Badge.BOSS_SLAIN_4};
        return Badges.isUnlocked(bosses[depth / 5 - 1]);
    }

    public static void enter(Mob boss) {
        if (boss instanceof RivalBoss) {
            ((RivalBoss)boss).enter();
        } else if (RebornChallenges.doubleBoss() && !Dungeon.level.doubleBossSpawned) {
            int cell = RebornBossSpawn.companion(Dungeon.level, boss.pos, Dungeon.hero.pos);
            if (cell < 0) return;
            Dungeon.level.doubleBossSpawned = true;
            java.util.Random roll = new java.util.Random(0xD0B1EB055L ^ Dungeon.rebornEncounterSeed ^ ((long)Dungeon.depth << 32));
            RivalBoss rival = rival(1 + roll.nextInt(6), Dungeon.depth);
            rival.pos = cell;
            rival.challengeExtra = true;
            GameScene.add(rival);
            rival.enter();
        }
    }

    public static void store(Bundle bundle) {
        bundle.put("rival_checked", checked);
        bundle.put("rival_floor", floor);
        bundle.put("rival_class", chosen);
    }

    public static void restore(Bundle bundle) {
        checked = bundle.getInt("rival_checked");
        floor = bundle.getInt("rival_floor");
        chosen = bundle.getInt("rival_class");
    }
}
