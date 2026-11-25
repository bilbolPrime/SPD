package com.bilboldev.pixeldungeonskills.items.weapon.gun;

import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;

import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.EssenceEffect;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.particles.SmokeParticle;
import com.bilboldev.pixeldungeonskills.effects.CombatTextIcon;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillTree;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.essences.Essence;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bullet;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public abstract class Gun extends MeleeWeapon {

    public static final String AC_FIRE = "FIRE";

    public int curCharges;
    public int maxCharges;
    private Charger charger;

    private static final String BUNDLE_CUR  = "curCharges";
    private static final String BUNDLE_MAX  = "maxCharges";

    public Gun( int tier ) {
        super( tier, 1.0f, 1.5f );
        maxCharges = Math.max(10, initialCharges());
        curCharges = maxCharges;
        defaultAction = AC_FIRE;
    }

    protected abstract int initialCharges();

    protected int shotsPerFire() {
        return 1;
    }

    public int projectilesPerShot() {
        return shotsPerFire();
    }

    public float firingDelay(Hero hero) {
        return DLY / Math.max(0.1f, hero.skillTree.getGunAttackSpeedModifier());
    }

    @Override
    public float acuracyFactor(Hero hero) {
        int requirement = STR - hero.skillTree.getWeaponLevelBonus();
        int reduction = hero.skillTree.getWeaponStrengthReduction();
        if (reduction > 0) requirement = (int)Math.max(0L, (long)requirement - reduction);
        int encumbrance = requirement - hero.STR();
        return (encumbrance > 0 ? (float)(ACU / Math.pow(1.5, encumbrance)) : ACU)
                * (imbue == Imbue.ACCURACY ? 1.5f : 1f);
    }

    @Override
    protected int min0() {
        return tier + 2;
    }

    @Override
    protected int max0() {
        return 12 + 2 * (tier - 1) - (tier >= 3 ? 2 : 0);
    }

    @Override
    public int min() {
        return min0() + (tier >= 3 && level() > 0 ? level() / 2 : level());
    }

    @Override
    public int max() {
        return max0() + level() * Math.max(1, (tier + 1) / 3);
    }

    protected float turnsPerCharge() {
        return Math.max(5f, 20f - tier * 2f - level() * 0.5f);
    }

    public int ammoCapacity() {
        return effectiveMaxAmmo();
    }

    public void restoreAmmo(int amount) {
        curCharges = Math.min(ammoCapacity(), curCharges + Math.max(0, amount));
        updateQuickslot();
    }

    private int effectiveMaxAmmo() {
        return effectiveMaxAmmo(Dungeon.hero);
    }

    private int effectiveMaxAmmo(Hero viewer) {
        int bonus = 0;
        if (viewer != null && viewer.skillTree != null) {
            bonus = viewer.skillTree.getGunAmmoBonus();
        }
        return Math.max(10, maxCharges + bonus);
    }

    private void ensureRearming( Hero hero ) {
        if (hero != null && ownedBy(hero) && charger == null) {
            (charger = new Charger()).attachTo(hero);
        }
    }

    private boolean ownedBy(Hero hero) {
        return isEquipped(hero) || hero.belongings.backpack.contains(this);
    }

    private boolean canFire(Hero hero) {
        if (!hero.isAlive() || !ownedBy(hero) || !IceWeapon.canUse(this, hero)) {
            QuickSlot.cancel();
            return false;
        }
        if (curCharges <= 0) {
            QuickSlot.cancel();
            GLog.w("custom.generated.necro_merc.gun_rearm");
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions(hero);
        ensureRearming(hero);
        if (ownedBy(hero) && curCharges > 0) {
            actions.add(AC_FIRE);
        }
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action.equals(AC_FIRE)) {
            if (!canFire(hero)) return;
            ensureRearming(hero);
            curUser = hero;
            curItem = this;
            selectTarget(hero);
        } else {
            super.execute(hero, action);
        }
    }

    private void selectTarget(final Hero hero) {
        GameScene.selectCell(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null || LocalCoop.contains(Actor.findChar(cell))) return;
                LocalCoop.withHero(hero, () -> fire(hero, cell));
            }

            @Override
            public Message prompt() {
                return Message.key("custom.generated.necro_merc.shoot_prompt");
            }
        });
    }

    private void fire(final Hero hero, final int cell) {
        if (!canFire(hero)) return;
        if (!RebornChallenges.targetAllowed(hero, cell)) return;
        final SkillTree tree = hero.skillTree;
        if (hero.talents() != null) hero.talents().prepareShot(true);
        final SkillTree.GunShotData shotData = tree.prepareGunShot(hero);
        final int primaryTargetCell = Ballistica.cast(hero.pos, cell, false, true);
        final float finalDelay = firingDelay(hero);

        hero.sprite.centerEmitter().burst(SmokeParticle.FACTORY, 3);

        hero.sprite.zap(primaryTargetCell, Gun.this);
        hero.busy();

        Sample.INSTANCE.play(Assets.SND_BLAST);

        ((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).reset(
                hero.pos,
                primaryTargetCell,
                new Bullet(),
                new Callback() {
                    @Override
                    public void call() {
                        final int shots = shotsPerFire();
                        for (int i = 0; i < shots; i++) {
                            final float damageMult = shots > 1 ? 1f / shots : 1f;
                            final int targetCell = (i == 0) ? primaryTargetCell
                                    : Ballistica.cast(hero.pos, spreadCell(cell), false, true);

                            Char target = Actor.findChar(targetCell);
                            if (target != null && target != hero
                                    && (!RebornChallenges.active(RebornChallenges.POINT_BLANK) || Level.adjacent(hero.pos, target.pos))) {

                                int dmg = Math.max(1, (int)(Random.NormalIntRange(min(), max())
                                        * tree.getGunDamageModifier() * shotData.damageModifier * damageMult
                                        * (1 + hero.attribute(EquipmentAttribute.Type.RANGED) / 100f)
                                        + EssenceEffect.attackBonus(hero) * damageMult));

                                float attackSkill = hero.attackSkill(target, Gun.this)
                                        + tree.getRangedAttackSkillBonus()
                                        + shotData.attackSkillBonus;

                                float attackRoll = Random.Float(Math.max(0f, attackSkill)
                                        * (1 + hero.essenceBonus(Essence.Type.SIGHT) / 100f));
                                float defenseRoll = Random.Float(Math.max(0f, target.defenseSkill(hero)));

                                if (attackRoll >= defenseRoll) {

                                    int armor = (int)(Random.IntRange(0, Math.max(0, target.dr())) * damageMult);
                                    if (hero.talents() != null) armor = hero.talents().armorAfterPiercing(armor, true);
                                    dmg = Math.max(0, dmg - armor);
                                    if (hero.talents() != null) dmg = hero.talents().attack(target, dmg, true);
                                    dmg = target.defenseProc(hero, dmg);
                                    if (!hero.isAlive()) break;
                                    if (dmg < 0) continue;
                                    int healthBefore = target.HP;
                                    float executeThreshold = tree.getGunExecuteThreshold();
                                    if (executeThreshold > 0f && target.HP <= Math.max(1, (int)(target.HT * executeThreshold))) {
                                        damage(target, target.HP, hero);
                                    } else {
                                        damage(target, dmg, hero);
                                    }
                                    if (hero.talents() != null && healthBefore > 0 && !target.isAlive()) hero.talents().killed(target);
                                    onHit(target);

                                    if (tree.shouldFleeOnGunHit() || shotData.flee) {
                                        Buff.prolong(target, com.bilboldev.pixeldungeonskills.actors.buffs.Terror.class, 5f);

                                        for (Mob mob : Dungeon.level.mobs) {
                                            if (mob == null || mob == target || mob.HP <= 0) {
                                                continue;
                                            }

                                            if (Level.distance(hero.pos, mob.pos) <= 10) {
                                                Buff.prolong(mob, com.bilboldev.pixeldungeonskills.actors.buffs.Terror.class, 5f);
                                            }
                                        }
                                    }

                                    if (Dungeon.visible[target.pos]) {
                                        target.sprite.bloodBurstA(hero.sprite.center(), dmg);
                                    }
                                } else if (Dungeon.visible[target.pos]) {
                                    target.sprite.showStatus(CharSprite.NEUTRAL, target.defenseText());
                                    Sample.INSTANCE.play(Assets.SND_MISS);
                                }
                            }
                        }

                        curCharges--;
                        updateQuickslot();
                        tree.onGunFired();
                        Buff.detach(hero, Invisibility.class);
                        hero.spendAndNext(finalDelay);
                    }
                }
        );
    }

    private void damage(Char target, int damage, Hero hero) {
        if (Dungeon.rules().hasRebornEncounters()) target.damageFrom(damage, this, hero, CombatTextIcon.PHYSICAL);
        else target.damage(damage, this);
    }

    protected void onHit(Char target) { }

    private int spreadCell( int base ) {
        int[] offsets = { -1, 1, -Dungeon.level.WIDTH, Dungeon.level.WIDTH };
        int candidate = base + offsets[Random.Int(offsets.length)];
        return (candidate >= 0 && candidate < Dungeon.level.LENGTH) ? candidate : base;
    }

    @Override
    public boolean collect(Bag container) {
        if (!super.collect(container)) return false;
        if (container.owner instanceof Hero) ensureRearming((Hero)container.owner);
        return true;
    }

    @Override
    public void activate( Hero hero ) {
        super.activate(hero);
        ensureRearming(hero);
    }

    @Override
    public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
        if (!super.doUnequip(hero, collect, single)) return false;
        if (!ownedBy(hero)) onDetach();
        return true;
    }

    @Override
    protected void onDetach() {
        if (charger != null) {
            charger.detach();
            charger = null;
        }
    }

    public class Charger extends Buff {
        private float nextRearmDelay( Hero hero ) {
            float rearmSpeed = hero != null && hero.skillTree != null ? hero.skillTree.getGunRearmSpeedModifier() : 1f;
            return turnsPerCharge() / Math.max(0.1f, rearmSpeed);
        }

        @Override
        public boolean attachTo( Char target ) {
            if (!super.attachTo(target)) {
                return false;
            }
            spend(nextRearmDelay(target instanceof Hero ? (Hero) target : null));
            return true;
        }

        @Override
        public boolean act() {
            Hero hero = (Hero)target;
            if (!ownedBy(hero)) {
                onDetach();
                return true;
            }
            int maxAmmo = effectiveMaxAmmo(hero);
            if (curCharges > maxAmmo) {
                curCharges = maxAmmo;
            }

            if (curCharges < maxAmmo) {
                curCharges++;
                updateQuickslot();
            }

            spend(nextRearmDelay(target instanceof Hero ? (Hero) target : null));
            return true;
        }
    }

    @Override
    public String status() {
        return curCharges + "/" + effectiveMaxAmmo();
    }

    @Override
    public String info() {
        return info(Dungeon.hero);
    }

    @Override
    public String info(Hero viewer) {
        StringBuilder sb = new StringBuilder(super.info(viewer));
        sb.append("\n\n").append(Messages.get("custom.generated.necro_merc.ammo_status", curCharges, effectiveMaxAmmo(viewer)));
        sb.append("\n").append(Messages.get("custom.generated.necro_merc.rearms_every", (int)turnsPerCharge()));
        sb.append("\n\n").append(Messages.get("custom.generated.necro_merc.firearm_info"));
        return sb.toString();
    }

    @Override
    public boolean isUpgradable() {
        return true;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put(BUNDLE_CUR,  curCharges);
        bundle.put(BUNDLE_MAX,  maxCharges);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        maxCharges    = bundle.getInt(BUNDLE_MAX);
        curCharges    = bundle.getInt(BUNDLE_CUR);
        maxCharges = Math.max(10, maxCharges);

        curCharges = Math.max(0, curCharges);
    }
}
