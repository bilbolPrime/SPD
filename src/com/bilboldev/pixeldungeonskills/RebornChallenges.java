package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornBoss;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornTengu;
import com.bilboldev.pixeldungeonskills.actors.mobs.Swarm;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.items.ArmorKit;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.TomeOfMastery;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.SewerBossLevel;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;

public final class RebornChallenges {
    public static final int LAST_STAND = 1 << 7;
    public static final int BLOOD_MAGIC = 1 << 8;
    public static final int OATHBOUND = 1 << 9;
    public static final int UNCHAINED = 1 << 10;
    public static final int DEATH_THROES = 1 << 11;
    public static final int SURVIVAL = 1 << 12;
    public static final int OUTNUMBERED = 1 << 13;
    public static final int DOUBLE_TROUBLE = 1 << 14;
    public static final int POINT_BLANK = 1 << 15;
    public static final int PLAIN_STEEL = 1 << 16;
    public static final int ALL = (1 << 17) - (1 << 7);
    public static final int[] MASKS = {LAST_STAND, BLOOD_MAGIC, OATHBOUND, UNCHAINED,
            DEATH_THROES, SURVIVAL, OUTNUMBERED, DOUBLE_TROUBLE, POINT_BLANK, PLAIN_STEEL};
    private static final String[] KEYS = {"last_stand", "blood_magic", "oathbound", "unchained",
            "death_throes", "survival", "outnumbered", "double_trouble", "point_blank", "plain_steel"};
    private static final int[] ICONS = {ItemSpriteSheet.SKULL, ItemSpriteSheet.POTION_CRIMSON,
            ItemSpriteSheet.LOCKED_CHEST, ItemSpriteSheet.SCROLL_WIPE_OUT, ItemSpriteSheet.BOMB,
            ItemSpriteSheet.ARMOR_WARRIOR, ItemSpriteSheet.BONES, ItemSpriteSheet.SKELETON_KEY,
            ItemSpriteSheet.KNUCKLEDUSTER, ItemSpriteSheet.SWORD};

    private RebornChallenges() { }

    public static boolean active(int challenge) {
        return Dungeon.rules().hasChallenges() && (Dungeon.challenges & challenge) != 0;
    }

    public static boolean enraged(Char enemy) {
        return active(LAST_STAND) && enemy instanceof Mob
                && ((Mob)enemy).hostile
                && !(enemy instanceof NPC)
                && !Bestiary.isBoss(enemy)
                && !(enemy instanceof RebornBoss)
                && enemy.HP > 0 && enemy.HP * 4 < enemy.HT;
    }

    public static boolean equipmentBound() {
        int region = Math.max(0, Math.min(24, Statistics.deepestFloor - 1) / 5);
        return active(OATHBOUND) && (Dungeon.challengeBosses & (1 << region)) == 0;
    }

    public static boolean targetAllowed(Hero hero, int cell) {
        if (!active(POINT_BLANK) || cell == hero.pos
                || Level.adjacent(hero.pos, cell)) return true;
        GLog.w(RebornChallenges.class, "too_far");
        showInfo(POINT_BLANK);
        return false;
    }

    public static void showInfo(int challenge) {
        for (int i = 0; i < MASKS.length; i++) {
            if (MASKS[i] != challenge) continue;
            WndOptions window = new WndOptions(new ItemSprite(ICONS[i], null),
                    Message.key(RebornChallenges.class, KEYS[i]),
                    Message.key(RebornChallenges.class, KEYS[i] + "_desc"));
            window.menuModel().image = ICONS[i];
            GameScene.show(window);
            return;
        }
    }

    public static void promote(Mob mob, java.util.Random random) {
        if (!active(SURVIVAL) || mob.champ > 0 || !mob.hostile
                || mob instanceof NPC
                || mob instanceof RebornBoss
                || Bestiary.isBoss(mob)
                || mob instanceof Swarm) return;
        int chance = Math.min(100, 25 * (1 + (Dungeon.depth - 1) / 5));
        if (random.nextInt(100) >= chance) return;
        RebornChampion.grant(mob, true);
        mob.challengeChampion = true;
    }

    public static boolean doubleBoss() {
        return active(DOUBLE_TROUBLE) && Dungeon.depth >= 5 && Dungeon.depth <= 20 && Dungeon.depth % 5 == 0;
    }

    public static boolean canWarn(Mob owner) {
        if (!doubleBoss()) return true;
        for (Mob mob : Dungeon.level.mobs) {
            if (mob == owner || !mob.isAlive()) continue;
            if (mob instanceof RebornBoss && ((RebornBoss)mob).warningCells().length > 0) return false;
            if (mob instanceof RebornTengu
                    && ((RebornTengu)mob).warningCells().length > 0) return false;
        }
        return true;
    }

    public static void bossDefeated(Mob boss) {
        for (Mob other : Dungeon.level.mobs) {
            if (other != boss && other.isAlive() && (other instanceof RebornBoss
                    && !(other instanceof RebornKing.Retainer)
                    || other instanceof RebornTengu)) return;
        }
        int bit = 1 << (Dungeon.depth / 5 - 1);
        if ((Dungeon.challengeBosses & bit) != 0) return;
        Dungeon.challengeBosses |= bit;
        if (Dungeon.level instanceof SewerBossLevel) {
            ((SewerBossLevel)Dungeon.level).unseal();
        }
        if (Dungeon.depth == 10) {
            Dungeon.level.drop(new TomeOfMastery(), boss.pos, boss).sprite.drop();
            IceCaves.dropScroll(boss);
        }
        if (Dungeon.depth == 20) {
            Dungeon.level.drop(new ArmorKit(), boss.pos, boss).sprite.drop();
        }
        Dungeon.level.drop(new SkeletonKey(), boss.pos, boss).sprite.drop();
        GameScene.bossSlain();
        Badges.validateBossSlain();
    }

    public static void plain(Item item) {
        if (!active(PLAIN_STEEL)) return;
        if (item instanceof Weapon) {
            item.weaponEffect = null;
            ((Weapon)item).enchant(null);
        } else if (item instanceof Armor) {
            Armor armor = (Armor)item;
            armor.weaponEffect = null;
            armor.prefix = null;
            armor.glyph = null;
        }
    }

    public static boolean valid(int selection) {
        return selection >= 0 && (selection & ~ALL) == 0;
    }

    public static String title() {
        return Messages.get(RebornChallenges.class, "title");
    }

    public static String name(int index) {
        return Messages.get(RebornChallenges.class, KEYS[index]);
    }

    public static String description(int index) {
        return Messages.get(RebornChallenges.class, KEYS[index] + "_desc");
    }

    public static int icon(int index) {
        return ICONS[index];
    }

    public static String summary(int selection) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < MASKS.length; i++) {
            if ((selection & MASKS[i]) == 0) continue;
            if (names.length() > 0) names.append(", ");
            names.append(name(i));
        }
        return names.length() == 0 ? Messages.get(RebornChallenges.class, "none") : names.toString();
    }
}
