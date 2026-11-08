package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.network.PartySession;

public final class HeroSkins {
    public static int localChoice;
    public static int companionChoice;

    private HeroSkins() { }

    public static String sheet(HeroClass hero, int skin) {
        if (skin == 1) {
            switch (hero) {
                case WARRIOR: return "heroes/warrior_supporter.png";
                case MAGE: return "heroes/mage_supporter.png";
                case ROGUE: return "heroes/rogue_supporter.png";
                case HUNTRESS: return "heroes/huntress_supporter.png";
                case NECROMANCER: return "heroes/necromancer_supporter.png";
                case MERCENARY: return "heroes/mercenary_supporter.png";
                default: break;
            }
        }
        return hero.spritesheet();
    }

    public static void startRun() {
        Hero first = LocalCoop.primary();
        if (first == null) return;
        first.skin = SupporterEntitlements.ownsSkins() ? localChoice : 0;
        Hero second = LocalCoop.partner(first);
        if (second != null) {
            second.skin = PartySession.network() ? PartySession.partnerSkin()
                    : SupporterEntitlements.ownsSkins() ? companionChoice : 0;
        }
    }
}
