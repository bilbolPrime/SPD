package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.utils.Bundle;

public final class RivalEncounter {
    private static int checked, floor, chosen;

    private RivalEncounter() { }

    public static void reset() {
        checked = floor = chosen = 0;
    }

    public static Mob boss(int depth) {
        if (!Dungeon.rules().hasRebornBosses() || depth < 5 || depth > 20 || depth % 5 != 0)
            return Bestiary.mob(depth);
        if (PartySession.client()) throw new IllegalStateException("Only the host chooses a boss");
        int bit = 1 << (depth / 5 - 1);
        if ((checked & bit) == 0) {
            checked |= bit;
            if (floor == 0 && eligible(depth)) {
                java.util.Random roll = new java.util.Random(0x5EEDB055L ^ ((long)Dungeon.rebornEncounterSeed << 16));
                int chance = 0, hero = 0;

                for (int bossDepth = 5; bossDepth <= depth; bossDepth += 5) {
                    chance = roll.nextInt(4);
                    hero = 1 + roll.nextInt(6);
                }
                if (chance == 0) {
                    floor = depth;
                    chosen = hero;
                }
            }
        }
        if (floor != depth) return Bestiary.mob(depth);
        RivalBoss rival;
        switch (chosen) {
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
        if (boss instanceof RivalBoss) ((RivalBoss)boss).enter();
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
