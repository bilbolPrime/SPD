package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.RebornSkillTree;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.TalentSkill;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.*;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.bags.Keyring;
import com.bilboldev.pixeldungeonskills.items.bags.PotionHolder;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.*;
import com.bilboldev.pixeldungeonskills.items.rings.*;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfRecharging;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfTeleportation;
import com.bilboldev.pixeldungeonskills.items.wands.*;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Gun;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Handgun;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.*;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Boomerang;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public final class DailyLoadout {
    public final Hero hero = new Hero();
    public final int freePoints;

    DailyLoadout(long day, long seed) {
        try (Random.Scope ignored = Random.use(seed)) {
            int tier = DailyRun.armorTier(day) - 2;
            hero.heroClass = DailyRun.hero(day);
            hero.subClass = DailyRun.subclass(day);
            hero.skillSystem = 4;
            hero.skillTree = new RebornSkillTree(hero);
            hero.startAtLevel(DailyRun.startingLevel(day));
            hero.STR = 12 + tier;
            hero.HP = hero.HT = tier == 3 ? 120 : tier == 2 ? 100 : tier == 1 ? 80 : 65;
            hero.MP = hero.MMP = tier == 3 ? 95 : tier == 2 ? 80 : tier == 1 ? 65 : 55;
            hero.belongings.armor = tier == 3 ? new PlateArmor() : tier == 2 ? new ScaleArmor()
                    : tier == 1 ? new MailArmor() : new LeatherArmor();
            hero.belongings.armor.upgrade(1 + Random.Int(2));
            switch (hero.heroClass) {
                case WARRIOR:
                    hero.belongings.weapon = Random.Int(2) == 0 ? new ShortSword() : new Spear();
                    break;
                case ROGUE: case HUNTRESS:
                    hero.belongings.weapon = Random.Int(2) == 0 ? new Dagger() : new ShortSword();
                    break;
                case MERCENARY:
                    hero.belongings.weapon = new Handgun();
                    break;
                default:
                    hero.belongings.weapon = Random.Int(2) == 0 ? new Knuckles() : new Quarterstaff();
                    break;
            }
            hero.belongings.weapon.upgrade(2 + tier);
            hero.belongings.ring1 = hero.heroClass == HeroClass.ROGUE ? new RingOfShadows()
                    : Random.Int(2) == 0 ? new RingOfAccuracy() : new RingOfEvasion();
            hero.belongings.ring1.upgrade();
            add(new Keyring());
            PotionHolder potions = new PotionHolder();
            add(potions);
            PotionOfHealing healing = new PotionOfHealing();
            healing.quantity(2);
            potions.items.add(healing);
            PotionOfMana mana = new PotionOfMana();
            mana.quantity(3);
            potions.items.add(mana);
            Food food = new Food();
            food.quantity(2);
            add(food);
            if (hero.heroClass == HeroClass.MAGE || hero.heroClass == HeroClass.NECROMANCER) {
                Wand wand = new WandOfMagicMissile();
                if (tier > 0) {
                    int choice = Random.Int(3);
                    if (choice == 1) wand = new WandOfFirebolt();
                    else if (choice == 2) wand = new WandOfLightning();
                }
                wand.upgrade(2 + tier);
                add(wand);
            }
            if (hero.heroClass == HeroClass.HUNTRESS) {
                hero.belongings.bow = new Bow();
                hero.belongings.bow.upgrade(2 + tier);
                add(new Arrow(tier == 0 ? 30 : 60));
                add(new Boomerang());
            }
            ArrayList<Item> supplies = new ArrayList<>();
            supplies.add(new PotionOfInvisibility());
            supplies.add(new PotionOfLevitation());
            supplies.add(new PotionOfPurity());
            supplies.add(new PotionOfLiquidFlame());
            supplies.add(new ScrollOfRecharging());
            supplies.add(new ScrollOfTeleportation());
            for (int i = 0; i < 2; i++) {
                Item item = supplies.remove(Random.Int(supplies.size()));
                if (item instanceof Potion) potions.items.add(item);
                else add(item);
            }
            for (Item item : hero.belongings) item.levelKnown = item.cursedKnown = true;
            if (hero.belongings.bow != null) hero.belongings.bow.levelKnown = hero.belongings.bow.cursedKnown = true;

            int points = 8 + tier * 4;
            for (int spent = 0; spent < points / 2; spent++) {
                ArrayList<TalentSkill> available = new ArrayList<>();
                for (TalentSkill skill : hero.talents().talents()) {
                    if (hero.talents().canLevel(skill, points - spent)) available.add(skill);
                }
                if (available.isEmpty()) throw new IllegalStateException("No legal daily skill assignment");
                available.get(Random.Int(available.size())).level++;
            }
            freePoints = points / 2 - 3;
            hero.refreshBonuses();
            hero.HP = hero.HT;
            hero.MP = hero.MMP;
        }
    }

    private void add(Item item) {
        hero.belongings.backpack.items.add(item);
        if (item instanceof Bag) ((Bag)item).owner = hero;
    }

    void equip(Hero target) {
        for (Ring.RingBuff buff : target.buffs(Ring.RingBuff.class)) buff.detach();
        for (Item item : target.belongings) if (item instanceof Wand) ((Wand)item).stopCharging();
        target.belongings.backpack.items.clear();
        target.belongings.weapon = hero.belongings.weapon;
        target.belongings.armor = hero.belongings.armor;
        target.belongings.ring1 = hero.belongings.ring1;
        target.belongings.ring2 = null;
        target.belongings.bow = hero.belongings.bow;
        target.belongings.backpack.items.addAll(hero.belongings.backpack.items);
        for (Item item : target.belongings) {
            item.identify();
            if (item instanceof Bag) ((Bag)item).owner = target;
            if (item instanceof Wand) ((Wand)item).charge(target);
        }
        if (target.belongings.bow != null) target.belongings.bow.identify();
        target.belongings.ring1.activate(target);
        target.startAtLevel(hero.lvl);
        target.STR = hero.STR;
        target.subClass = hero.subClass;

        target.HT = hero.HT - hero.talents().healthBonus();
        target.MMP = hero.MMP - hero.talents().manaBonus();
        for (TalentSkill skill : hero.talents().talents()) target.talents().skill(skill.talent).level = skill.level;
        Skill.availableSkill = freePoints;
        target.refreshBonuses();
        target.HP = target.HT;
        target.MP = target.MMP;
        QuickSlot.resetSlots();
        int slot = 0;
        if (target.belongings.weapon instanceof Gun) QuickSlot.values[slot++] = target.belongings.weapon;
        if (slot < QuickSlot.visibleSlots()) QuickSlot.values[slot++] = PotionOfHealing.class;
        ArrayList<TalentSkill> skills = target.talents().talents();
        skills.sort(java.util.Comparator.comparingInt((TalentSkill skill) -> skill.talent.subclass.ordinal())
                .thenComparingInt(skill -> skill.talent.row).thenComparingInt(skill -> skill.talent.column));
        for (TalentSkill skill : skills) {
            if (slot >= QuickSlot.visibleSlots()) break;
            if (QuickSlot.canAssign(skill)) QuickSlot.bind(skill, slot++);
        }
    }
}
