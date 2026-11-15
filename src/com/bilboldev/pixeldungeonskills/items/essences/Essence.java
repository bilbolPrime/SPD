package com.bilboldev.pixeldungeonskills.items.essences;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.*;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;
import com.bilboldev.pixeldungeonskills.effects.CombatTextIcon;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Locale;

public abstract class Essence extends Item {
    public static final int MAX_GRADE = 10;
    public static final String AC_ABSORB = "ABSORB";
    public static final String AC_MERGE = "MERGE";
    private static final String[] GRADES = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    public enum Type {
        VITAL(Vital.class, 2, 5),
        ARCANE(Arcane.class, 2, 5),
        STONE(Stone.class, 1, 2),
        SHADE(Shade.class, 1, 2),
        WIND(Wind.class, 1, 3),
        MEMORY(Memory.class, 1, 2),
        EMBER(Ember.class, 2, 4),
        RIME(Rime.class, 2, 4),
        VENOM(Venom.class, 2, 4),
        STORM(Storm.class, 2, 4),
        BLOOD(Blood.class, 1, 2),
        IRON(Iron.class, 1, 2),
        SIGHT(Sight.class, 2, 4),
        WARD(Ward.class, 2, 4),
        HARVEST(Harvest.class, 2, 4);

        public final int min, max;
        public final Class<? extends Essence> itemClass;

        Type(Class<? extends Essence> itemClass, int min, int max) {
            this.itemClass = itemClass;
            this.min = min;
            this.max = max;
        }

        public String key() {
            return "essences." + name().toLowerCase(Locale.ROOT);
        }

        public static Type forItem(Class<?> itemClass) {
            for (Type type : values()) if (type.itemClass == itemClass) return type;
            throw new IllegalArgumentException("Unknown Essence class");
        }
    }

    public final Type type;
    private int grade = 1;
    private int bonus;
    private int skill = -1;
    private boolean throwing;

    protected Essence(Type type) {
        this.type = type;
        bonus = type.min;
        image = ItemSpriteSheet.ESSENCE_VITAL + type.ordinal();
        defaultAction = AC_ABSORB;
        levelKnown = cursedKnown = true;
    }

    public int grade() { return grade; }
    public String gradeLabel() { return GRADES[grade - 1]; }
    public int bonus() { return bonus; }
    public int skill() { return skill; }

    @Override
    public Item random() {
        if (Dungeon.rules().hasRebornEncounters()) {
            bonus = Random.IntRange(type.min, type.max);
            if (type == Type.MEMORY) skill = rollSkill(eligibleHeroes());
        }
        return this;
    }

    private static Hero[] eligibleHeroes() {
        Hero[] heroes = LocalCoop.heroes();
        return heroes.length == 0 && Dungeon.hero != null ? new Hero[]{Dungeon.hero} : heroes;
    }

    public static boolean memoryAvailable() {
        return !skills(eligibleHeroes()).isEmpty();
    }

    private static ArrayList<Talent> skills(Hero[] heroes) {
        ArrayList<Talent> choices = new ArrayList<>();
        for (Talent talent : Talent.values()) {
            if (talent.attributeWeight() <= 0 || talent.maxRank() == 1) continue;
            for (Hero hero : heroes) {
                if (hero.skillSystem == 4 && hero.talents() != null && talent.heroClass == hero.heroClass
                        && (talent.subclass == HeroSubClass.NONE || talent.subclass == hero.subClass)) {
                    choices.add(talent);
                    break;
                }
            }
        }
        return choices;
    }

    private static int rollSkill(Hero[] heroes) {
        ArrayList<Talent> choices = skills(heroes);
        if (choices.isEmpty()) return -1;
        float[] weights = new float[choices.size()];
        for (int i = 0; i < weights.length; i++) weights[i] = choices.get(i).attributeWeight();
        return choices.get(Random.chances(weights)).ordinal();
    }

    public boolean carriedBy(Hero hero) {
        return Dungeon.rules().hasRebornEncounters() && hero != null && hero.isAlive()
                && hero.belongings.backpack.contains(this);
    }

    public static void inventoryChanged(Bag bag, Item item) {
        if (!Dungeon.rules().hasRebornEncounters() || !(item instanceof Essence || item instanceof Bag)) return;
        Hero owner = bag.owner instanceof Hero ? (Hero)bag.owner : null;
        for (Hero hero : eligibleHeroes()) {
            if (hero.belongings.backpack == bag || hero.belongings.backpack.contains(bag)) {
                owner = hero;
                break;
            }
        }
        if (owner != null) owner.refreshBonuses();
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = new ArrayList<>();
        if (!carriedBy(hero)) return actions;
        actions.addAll(super.actions(hero));
        actions.add(AC_ABSORB);
        if (grade < MAX_GRADE) actions.add(AC_MERGE);
        return actions;
    }

    @Override
    public String actionName(String action, Hero hero) {
        if (AC_ABSORB.equals(action)) return Messages.get("essences.ac_absorb");
        if (AC_MERGE.equals(action)) return Messages.get("essences.ac_merge");
        return super.actionName(action, hero);
    }

    @Override
    public void execute(Hero hero, String action) {
        if (AC_STORE_TAKE.equals(action) && Dungeon.rules().hasRebornEncounters() && hero != null && hero.isAlive()
                && !throwing && hero.pos == Dungeon.level.storage && hero.storage.backpack.contains(this)) {
            LocalCoop.withHero(hero, () -> super.execute(hero, action));
            return;
        }
        if (!carriedBy(hero) || throwing) return;
        LocalCoop.withHero(hero, () -> {
            curUser = hero;
            curItem = this;
            if (AC_ABSORB.equals(action)) absorb(hero);
            else if (AC_MERGE.equals(action)) chooseMerge(hero);
            else super.execute(hero, action);
        });
    }

    public boolean absorb(Hero hero) {
        if (!carriedBy(hero) || throwing) return false;
        if (type == Type.STORM && !(hero.belongings.weapon instanceof Wand)) {
            GLog.w("essences.no_wand");
            return false;
        }
        if (type == Type.ARCANE && hero.MMP <= 0) {
            GLog.w("essences.no_mana");
            return false;
        }
        detachAll(hero.belongings.backpack);
        switch (type) {
            case VITAL: heal(hero, 10 * grade); break;
            case ARCANE: mana(hero, 8 * grade); break;
            case STONE: Buff.affect(hero, Barkskin.class).level(5 * grade); break;
            case SHADE: Buff.prolong(hero, Invisibility.class, Math.min(40, 5 * grade)); break;
            case WIND: EssenceEffect.apply(hero, EssenceEffect.Kind.WIND, 0, Math.min(40, 5 * grade)); break;
            case MEMORY: Focus.apply(hero, Math.min(100, 10 * grade)); break;
            case EMBER:
                Buff.detach(hero, Burning.class);
                EssenceEffect.apply(hero, EssenceEffect.Kind.FIRE, 0, Math.min(100, 10 * grade));
                break;
            case RIME:
                Buff.detach(hero, Frost.class);
                Buff.detach(hero, Frostbite.class);
                EssenceEffect.apply(hero, EssenceEffect.Kind.COLD, 0, Math.min(100, 10 * grade));
                break;
            case VENOM:
                Buff.detach(hero, Poison.class);
                EssenceEffect.apply(hero, EssenceEffect.Kind.POISON, 0, Math.min(100, 10 * grade));
                break;
            case STORM:
                Wand wand = (Wand)hero.belongings.weapon;
                wand.curCharges = (int)Math.min(wand.maxCharges, (long)wand.curCharges + grade);
                wand.updateQuickslot();
                break;
            case BLOOD:
                Buff.detach(hero, Bleeding.class);
                heal(hero, 8 * grade);
                break;
            case IRON: EssenceEffect.apply(hero, EssenceEffect.Kind.IRON, 2 * grade, 10); break;
            case SIGHT: Buff.prolong(hero, MindVision.class, Math.min(60, 6 * grade)); break;
            case WARD: EssenceEffect.apply(hero, EssenceEffect.Kind.WARD, 10 * grade, 15); break;
            case HARVEST: Buff.affect(hero, Hunger.class).satisfy(75 * grade); break;
        }
        useAnimation(hero);
        hero.spendAndNext(1);
        return true;
    }

    private void useAnimation(Hero hero) {
        if (hero.sprite != null) hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(type == Type.MEMORY ? Assets.SND_READ : Assets.SND_DEWDROP, .6f, .6f, 1f);
    }

    private void chooseMerge(Hero hero) {
        if (grade >= MAX_GRADE) {
            GLog.w("essences.over_cap");
            return;
        }
        boolean found = false;
        for (Item item : hero.belongings.backpack) {
            if (item != this && item.getClass() == getClass() && grade + ((Essence)item).grade <= MAX_GRADE) {
                found = true;
                break;
            }
        }
        if (!found) {
            GLog.w("essences.no_merge");
            return;
        }
        GameScene.selectItem(new WndBag.Listener() {
            private boolean selected;

            @Override
            public void onSelect(Item item) {
                if (selected) return;
                selected = true;
                if (item == null) return;
                LocalCoop.withHero(hero, () -> {
                    if (!canMerge(hero, item)) return;
                    Essence other = (Essence)item;
                    if (type == Type.MEMORY) {
                        confirmAction(hero, Message.key("essences.memory_confirm"), Message.key("essences.confirm"),
                                Message.key("essences.cancel"), () -> merge(hero, other));
                    } else merge(hero, other);
                });
            }
        }, WndBag.Mode.ALL, Message.key("essences.merge_prompt"));
    }

    private boolean canMerge(Hero hero, Item other) {
        if (!carriedBy(hero) || throwing || other == null || !hero.belongings.backpack.contains(other)) {
            GLog.w("essences.stale");
            return false;
        }
        if (other == this || other.getClass() != getClass() || ((Essence)other).throwing) {
            GLog.w("essences.incompatible");
            return false;
        }
        if (grade + ((Essence)other).grade > MAX_GRADE) {
            GLog.w("essences.over_cap");
            return false;
        }
        if (type == Type.MEMORY && skills(new Hero[]{hero}).isEmpty()) {
            GLog.w("essences.memory_inactive");
            return false;
        }
        return true;
    }

    public boolean merge(Hero hero, Essence other) {
        if (!canMerge(hero, other)) return false;
        grade += other.grade;
        bonus += other.bonus;
        if (type == Type.MEMORY) skill = rollSkill(new Hero[]{hero});

        other.detachAll(hero.belongings.backpack);
        hero.refreshBonuses();
        updateQuickslot();
        useAnimation(hero);
        hero.spendAndNext(1);
        return true;
    }

    @Override
    public void doThrow(Hero hero) {
        if (!carriedBy(hero) || throwing) return;
        GameScene.selectCell(new CellSelector.Listener() {
            private boolean selected;

            @Override
            public void onSelect(Integer target) {
                if (selected) return;
                selected = true;
                if (target != null) LocalCoop.withHero(hero, () -> cast(hero, target));
            }

            @Override
            public Message prompt() { return Message.key(Item.class, "prompt"); }
        });
    }

    private boolean helpful() {
        return type == Type.VITAL || type == Type.ARCANE || type == Type.MEMORY
                || type == Type.WARD || type == Type.HARVEST;
    }

    @Override
    public void cast(Hero hero, int destination) {
        if (!carriedBy(hero) || throwing || hero.sprite == null || hero.sprite.parent == null
                || destination < 0 || destination >= Level.LENGTH || !RebornChallenges.targetAllowed(hero, destination)) return;
        int cell = Ballistica.cast(hero.pos, destination, false, true);
        if (helpful() && (destination == hero.pos || cell == hero.pos || Actor.findChar(cell) == hero)) {
            GLog.w("essences.self_target");
            return;
        }
        final Level level = Dungeon.level;
        final Object scene = Game.scene();
        throwing = true;
        hero.busy();
        hero.sprite.zap(cell);
        QuickSlot.target(this, Actor.findChar(cell));
        Sample.INSTANCE.play(Assets.SND_MISS, .6f, .6f, 1.5f);
        ((MissileSprite)hero.sprite.parent.recycle(MissileSprite.class)).reset(hero.pos, cell, this, () -> {
            if (!throwing) return;
            throwing = false;
            if (Dungeon.level != level || Game.scene() != scene || !hero.isAlive() || !Actor.all().contains(hero)) return;
            if (!carriedBy(hero)) {
                hero.curAction = null;
                hero.next();
                return;
            }
            LocalCoop.withHero(hero, () -> {
                detachAll(hero.belongings.backpack);
                Char target = Actor.findChar(cell);
                if (target != null && target.isAlive() && (!helpful() || target != hero)) impact(hero, target);
                hero.spendAndNext(1);
            });
        });
    }

    @Override
    public void castSPD(Hero hero, int destination, int skip) {
        cast(hero, destination);
    }

    private void impact(Hero hero, Char target) {
        switch (type) {
            case VITAL: heal(target, 25 * grade); break;
            case ARCANE:
                if (target instanceof Hero) mana((Hero)target, 20 * grade);
                break;
            case STONE: Buff.prolong(target, Roots.class, Math.min(20, 3 * grade)); break;
            case SHADE: Buff.prolong(target, Blindness.class, Math.min(20, 3 * grade)); break;
            case WIND: push(hero, target, Math.min(3, grade)); break;
            case MEMORY:
                if (target instanceof Hero) Focus.apply((Hero)target, Math.min(100, 20 * grade));
                break;
            case EMBER: Buff.affect(target, Burning.class).reignite(target, Math.min(20, 3 * grade)); break;
            case RIME: Buff.prolong(target, Frost.class, Math.min(10, 2 * grade)); break;
            case VENOM: Buff.affect(target, Poison.class).extendItemDuration(3 * grade); break;
            case STORM: target.damageFrom(6 * grade, this, hero, CombatTextIcon.LIGHTNING); break;
            case BLOOD: Buff.affect(target, Bleeding.class).set(3 * grade); break;
            case IRON: target.damageFrom(6 * grade, this, hero, CombatTextIcon.PHYSICAL); break;
            case SIGHT: Buff.prolong(target, Blindness.class, Math.min(30, 4 * grade)); break;
            case WARD: EssenceEffect.apply(target, EssenceEffect.Kind.WARD, 25 * grade, 15); break;
            case HARVEST:
                if (target instanceof Hero) Buff.affect(target, Hunger.class).satisfy(150 * grade);
                break;
        }
    }

    private static void push(Hero hero, Char target, int cells) {
        int dx = Integer.signum(target.pos % Level.WIDTH - hero.pos % Level.WIDTH);
        int dy = Integer.signum(target.pos / Level.WIDTH - hero.pos / Level.WIDTH);
        if (target == hero || dx == 0 && dy == 0) return;
        int sourceX = target.pos % Level.WIDTH - dx;
        int sourceY = target.pos / Level.WIDTH - dy;
        if (sourceX < 0 || sourceX >= Level.WIDTH || sourceY < 0 || sourceY >= Level.HEIGHT) return;
        int distance = cells;
        if (dx != 0 && dy != 0) {
            distance = 0;
            for (int step = 0; step < cells; step++) {
                int x = target.pos % Level.WIDTH + dx * step;
                int y = target.pos / Level.WIDTH + dy * step;
                if (x <= 0 || x >= Level.WIDTH - 1 || y <= 0 || y >= Level.HEIGHT - 1) break;
                int cell = x + y * Level.WIDTH;
                if (!Level.passable[cell + dx] || !Level.passable[cell + dy * Level.WIDTH]) break;
                distance++;
            }
        }
        if (distance > 0) Pushing.knockBack(sourceX + sourceY * Level.WIDTH, target, distance);
        Dungeon.observe();
    }

    private static void heal(Char target, int amount) {
        int restored = (int)Math.min(amount, Math.max(0L, (long)target.HT - target.HP));
        target.HP += restored;
        if (restored > 0) target.showRecovery(restored, false);
    }

    private static void mana(Hero hero, int amount) {
        int restored = (int)Math.min(amount, Math.max(0L, (long)hero.MMP - hero.MP));
        hero.MP += restored;
        if (restored > 0) hero.showRecovery(restored, true);
    }

    public static int reduceDamage(Char target, int damage, CombatTextIcon type) {
        if (!Dungeon.rules().hasRebornEncounters() || damage <= 0) return damage;
        Type element = type == CombatTextIcon.FIRE ? Type.EMBER
                : type == CombatTextIcon.COLD ? Type.RIME : type == CombatTextIcon.POISON ? Type.VENOM : null;
        if (element != null && EssenceEffect.protectedFrom(target, element)) return 0;
        if (target instanceof Hero) {
            Hero hero = (Hero)target;
            double multiplier = element == null ? 1 : 1 - Math.min(75, hero.essenceBonus(element)) / 100.0;
            if (type == CombatTextIcon.MAGIC || type == CombatTextIcon.FIRE
                    || type == CombatTextIcon.COLD || type == CombatTextIcon.LIGHTNING) {
                multiplier *= 1 - Math.min(50, hero.essenceBonus(Type.WARD)) / 100.0;
            }
            damage = Math.max(1, (int)Math.ceil(damage * multiplier));
        }
        return EssenceEffect.absorb(target, damage);
    }

    public static void lifeSteal(Hero hero, Char enemy, int damage) {
        if (!Dungeon.rules().hasRebornEncounters() || !hero.isAlive() || damage <= 0
                || !(enemy instanceof Mob) || !((Mob)enemy).hostile) return;
        hero.essenceLifeSteal(damage, Math.min(50, hero.essenceBonus(Type.BLOOD)));
    }

    @Override public String name() { return Messages.get("essences.name", (Object)Messages.get(type.key() + ".name"), GRADES[grade - 1]); }
    @Override public String coreName() { return Messages.get(type.key() + ".name"); }
    @Override public String desc() { return ""; }

    public String passiveInfo() {
        return type == Type.MEMORY
                ? Messages.get(type.key() + ".passive", bonus, skill < 0 ? Messages.get("essences.memory_skill") : Talent.values()[skill].title())
                : Messages.get(type.key() + ".passive", bonus);
    }

    public String cardInfo() {
        return passiveInfo() + "\n" + effects();
    }

    @Override
    public String info() {
        String result = passiveInfo() + "\n\n" + effects() + "\n\n" + Messages.get("essences.rules");
        if (type == Type.MEMORY && (Dungeon.hero == null || Dungeon.hero.skillSystem != 4)) {
            result += "\n\n" + Messages.get("essences.memory_inactive");
        }
        return result;
    }

    public String loreInfo() {
        String range = type.min + "\u2013" + type.max;
        String passive = type == Type.MEMORY ? Messages.get(type.key() + ".passive", (Object)range, Messages.get("essences.memory_skill"))
                : Messages.get(type.key() + ".passive", (Object)range);
        return passive + "\n\n" + effects() + "\n\n" + Messages.get("essences.rules");
    }

    private String effects() {
        int absorb, thrown;
        switch (type) {
            case VITAL: absorb = 10 * grade; thrown = 25 * grade; break;
            case ARCANE: absorb = 8 * grade; thrown = 20 * grade; break;
            case STONE: absorb = 5 * grade; thrown = Math.min(20, 3 * grade); break;
            case SHADE: absorb = Math.min(40, 5 * grade); thrown = Math.min(20, 3 * grade); break;
            case WIND: absorb = Math.min(40, 5 * grade); thrown = Math.min(3, grade); break;
            case MEMORY: absorb = Math.min(100, 10 * grade); thrown = Math.min(100, 20 * grade); break;
            case EMBER: absorb = Math.min(100, 10 * grade); thrown = Math.min(20, 3 * grade); break;
            case RIME: absorb = Math.min(100, 10 * grade); thrown = Math.min(10, 2 * grade); break;
            case VENOM: absorb = Math.min(100, 10 * grade); thrown = 3 * grade; break;
            case STORM: absorb = grade; thrown = 6 * grade; break;
            case BLOOD: absorb = 8 * grade; thrown = 3 * grade; break;
            case IRON: absorb = 2 * grade; thrown = 6 * grade; break;
            case SIGHT: absorb = Math.min(60, 6 * grade); thrown = Math.min(30, 4 * grade); break;
            case WARD: absorb = 10 * grade; thrown = 25 * grade; break;
            default: absorb = 75 * grade; thrown = 150 * grade; break;
        }
        String key = type.key() + ".effects";
        if (grade == 1 && (type == Type.WIND || type == Type.STORM)) key += "_one";
        return Messages.get(key, absorb, thrown);
    }

    @Override public boolean isUpgradable() { return false; }
    @Override public boolean isIdentified() { return true; }
    @Override public Item upgrade() { return this; }
    @Override public Item degrade() { return this; }
    @Override public int price() { return 20 * grade; }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("essence_grade", grade);
        bundle.put("essence_bonus", bonus);
        bundle.put("essence_skill", skill);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        grade = Math.max(1, Math.min(MAX_GRADE, bundle.getInt("essence_grade")));
        bonus = Math.max(type.min * grade, Math.min(type.max * grade, bundle.getInt("essence_bonus")));
        skill = bundle.contains("essence_skill") ? bundle.getInt("essence_skill") : -1;
        if (skill < 0 || skill >= Talent.values().length || Talent.values()[skill].attributeWeight() <= 0
                || Talent.values()[skill].maxRank() == 1 || type != Type.MEMORY) skill = -1;
        quantity = 1;
        level = 0;
        stackable = cursed = false;
        levelKnown = cursedKnown = true;
        weaponEffect = null;
        attributes = EquipmentAttribute.NONE;
    }

    public static class Vital extends Essence { public Vital() { super(Type.VITAL); } }
    public static class Arcane extends Essence { public Arcane() { super(Type.ARCANE); } }
    public static class Stone extends Essence { public Stone() { super(Type.STONE); } }
    public static class Shade extends Essence { public Shade() { super(Type.SHADE); } }
    public static class Wind extends Essence { public Wind() { super(Type.WIND); } }
    public static class Memory extends Essence { public Memory() { super(Type.MEMORY); } }
    public static class Ember extends Essence { public Ember() { super(Type.EMBER); } }
    public static class Rime extends Essence { public Rime() { super(Type.RIME); } }
    public static class Venom extends Essence { public Venom() { super(Type.VENOM); } }
    public static class Storm extends Essence { public Storm() { super(Type.STORM); } }
    public static class Blood extends Essence { public Blood() { super(Type.BLOOD); } }
    public static class Iron extends Essence { public Iron() { super(Type.IRON); } }
    public static class Sight extends Essence { public Sight() { super(Type.SIGHT); } }
    public static class Ward extends Essence { public Ward() { super(Type.WARD); } }
    public static class Harvest extends Essence { public Harvest() { super(Type.HARVEST); } }
}
