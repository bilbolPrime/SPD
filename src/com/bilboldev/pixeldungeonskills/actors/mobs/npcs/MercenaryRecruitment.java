package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc.MERC_TYPES;

public final class MercenaryRecruitment {
    private MercenaryRecruitment() {}

    public static boolean campFloor(int depth) {
        return depth >= 3 && depth <= 23 && depth % 5 == 3;
    }

    public static String noMercMessage() {
        return Messages.get(MercenaryRecruitment.class, "no_merc");
    }

    public static boolean maidenAvailable() {
        return HiredMerc.archerMaidenUnlocked;
    }

    public static String displayName(MERC_TYPES type) {
        return Messages.maybeTranslate(type == MERC_TYPES.ArcherMaiden ? "Archer Maiden" : type.getName());
    }

    public static Message nameText(MERC_TYPES type) {
        switch (type) {
            case Brute: return Message.key("custom.generated.entry_brute_fd383641d0");
            case Wizard: return Message.key("custom.generated.entry_wizard_127a661b8e");
            case Thief: return Message.key("custom.generated.entry_thief_8ac2de7b44");
            case Archer: return Message.key("custom.generated.entry_archer_f75aedc160");
            case ArcherMaiden: return Message.key("custom.generated.entry_archer_maiden_760746f689");
            default: throw new IllegalArgumentException("Unknown mercenary");
        }
    }

    public static boolean eligible(HeroClass hero, MERC_TYPES type, boolean maiden) {
        switch (type) {
            case Brute: return hero != HeroClass.WARRIOR && hero != HeroClass.MERCENARY;
            case Wizard: return hero != HeroClass.MAGE && hero != HeroClass.NECROMANCER;
            case Thief: return hero != HeroClass.ROGUE;
            case Archer: return hero != HeroClass.HUNTRESS && !maiden;
            case ArcherMaiden: return hero != HeroClass.HUNTRESS && maiden;
            default: return false;
        }
    }

    public static int cost(MERC_TYPES type, int level) {
        switch (type) {
            case Brute: return 100 + level * 25;
            case Wizard: return 80 + level * 20;
            case Thief: return 75 + level * 15;
            case Archer: return 90 + level * 20;
            case ArcherMaiden: return 90 + level * 25;
            default: throw new IllegalArgumentException("Unknown mercenary");
        }
    }

    public static boolean atRecruiter(MercenaryCamp camp) {
        return Dungeon.rules().hasRebornEncounters() && camp != null && Dungeon.level != null
                && Dungeon.hero != null && camp.isAlive() && Dungeon.level.mobs.contains(camp)
                && Level.adjacent(Dungeon.hero.pos, camp.pos);
    }

    public static Message hire(MERC_TYPES type, MercenaryCamp camp) {
        return hire(type, camp, null);
    }

    public static Message hire(MERC_TYPES type, MercenaryCamp camp, HiredMerc confirmedReplacement) {
        if (Dungeon.hero == null) return Message.key(MercenaryRecruitment.class, "no_hero");
        HiredMerc previous = Dungeon.hero.hiredMerc;
        if (previous != confirmedReplacement) return Message.key(MercenaryRecruitment.class, "changed");
        if (previous != null && (camp == null || !Dungeon.level.mobs.contains(previous)
                || Dungeon.hero.checkMerc)) return Message.key(MercenaryRecruitment.class, "wait");
        if (!Dungeon.rules().allowsOnDemandHiring() && !atRecruiter(camp))
            return Message.key(MercenaryRecruitment.class, "no_merc");
        if (camp != null && (!atRecruiter(camp) || type != camp.offer())) return Message.key(MercenaryRecruitment.class, "no_merc");
        if (Dungeon.rules().hasRebornEncounters()
                && !eligible(Dungeon.hero.heroClass, type, maidenAvailable()))
            return Message.key(MercenaryRecruitment.class, "unavailable");
        if (type == MERC_TYPES.ArcherMaiden && !maidenAvailable()) return Message.key("custom.generated.entry_please_consider_donating_to_unlock_this_feature_5b14e9ead8");
        int price = cost(type, Dungeon.hero.lvl);
        if (Dungeon.gold < price) return Message.key("custom.generated.entry_insufficient_gold_df0ae40068");
        int cell = camp == null ? -1 : camp.pos;
        for (int offset : Level.NEIGHBOURS8) {
            if (cell != -1) break;
            int p = Dungeon.hero.pos + offset;
            if (p >= 0 && p < Level.LENGTH && Level.adjacent(p, Dungeon.hero.pos)
                    && Actor.findChar(p) == null && (Level.passable[p] || Level.avoid[p])) {
                cell = p;
                break;
            }
        }
        if (cell == -1) return Message.key(MercenaryRecruitment.class, "no_space");
        HiredMerc merc = new HiredMerc(type);
        merc.spawn(Dungeon.hero.lvl);
        merc.HP = type.getHealth(Dungeon.hero.lvl);
        type.setEquipment(merc);
        merc.pos = cell;

        if (previous != null) previous.disband();
        if (camp != null) {
            camp.destroy();
            if (camp.sprite != null) camp.sprite.killAndErase();
        }
        Dungeon.gold -= price;
        Dungeon.hero.hiredMerc = merc;
        GameScene.add(merc);
        ((MercSprite) merc.sprite).updateArmor();
        WandOfBlink.appear(merc, cell);
        Dungeon.hero.spend(1 / Dungeon.hero.speed());
        return null;
    }
}
