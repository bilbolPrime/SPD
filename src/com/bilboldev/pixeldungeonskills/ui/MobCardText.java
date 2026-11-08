package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.pixeldungeonskills.LibraryCreatureStats;
import com.bilboldev.pixeldungeonskills.actors.buffs.Champ;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyFoe;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDemon;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDefender;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyRaider;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyCityGuard;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyQuarry;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyAlly;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyRatKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDM300;
import com.bilboldev.pixeldungeonskills.actors.mobs.GauntletRatKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.messages.Messages;

final class MobCardText {
    static String description(Mob mob) {
        String description = abilities(mob);
        String traits = LibraryCreatureStats.traits(mob);
        return traits.isEmpty() ? description : description + "\n" + traits;
    }

    private static String abilities(Mob mob) {
        if (mob instanceof DailyAlly) return mob.description();
        if (mob instanceof DailyDemon) return mob.description();
        if (mob instanceof DailyDefender || mob instanceof DailyRaider || mob instanceof DailyCityGuard) return mob.description();
        if (mob instanceof DailyRatKing) return Messages.get("daily.rat_king_card");
        if (mob instanceof DailyDM300) return Messages.get("daily.dm300_card");
        if (mob instanceof DailyQuarry) return mob.description();
        if (mob instanceof DailyFoe && ((DailyFoe)mob).escapeCaptain()) return Messages.get("daily.captain_card");
        if (mob instanceof DailyFoe) return mob.description();
        StringBuilder text = new StringBuilder();
        if (Bestiary.isBoss(mob) || mob instanceof GauntletRatKing) {
            String boss = Messages.get(mob, "card_boss");
            text.append(Messages.NO_TEXT_FOUND.equals(boss) ? Messages.get("ui.itemdropcards.boss") : boss);
        } else {
            String description = Messages.get(mob, "card_desc");
            if (!Messages.NO_TEXT_FOUND.equals(description)) text.append(description);
            else if (mob.range > 1) text.append(Messages.get("ui.itemdropcards.ranged"));
            if (mob.champ > 0) {
                if (text.length() > 0) text.append('\n');
                text.append(Messages.get("ui.itemdropcards.champion"));
                RebornChampion champion = mob.buff(RebornChampion.class);
                if (champion != null) {
                    for (RebornChampion.Skill skill : champion.learnedSkills()) {
                        text.append('\n').append(Messages.get("champions.knows", (Object)skill.title(), champion.rank(skill)));
                    }
                } else {
                    String effect = mob.champ == Champ.CHAMP_VAMPERIC ? "vampiric"
                            : mob.champ == Champ.CHAMP_CURSED ? "weakening"
                            : mob.champ == Champ.CHAMP_FOUL ? "poisonous" : null;
                    if (effect != null) text.append('\n').append(Messages.get("ui.itemdropcards." + effect));
                }
            }
            if (text.length() == 0) text.append(Messages.get("ui.itemdropcards.mob"));
        }
        return text.toString();
    }
}
