package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Cripple;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.skills.Endurance;
import com.bilboldev.pixeldungeonskills.actors.skills.MercArcherSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercArcherSkillB;
import com.bilboldev.pixeldungeonskills.actors.skills.MercBruteSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercThiefSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercWizardSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.armor.ClothArmor;
import com.bilboldev.pixeldungeonskills.items.armor.LeatherArmor;
import com.bilboldev.pixeldungeonskills.items.food.ChargrilledMeat;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfRejuvenation;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Dagger;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Knuckles;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Mace;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.FrostBow;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.HashSet;

public class HiredMerc extends NPC {

    public static enum MERC_TYPES
    {
        Brute("Brute"), Wizard("Wizard"), Thief("Thief"), Archer("Archer"), ArcherMaiden("ArcherMaiden");
        public String type = "Brute";
        MERC_TYPES(String type) {this.type = type;}

        public String getName()
        {
            return type;
        }

        public int getHealth(int level)
        {
            switch (this)
            {
                case Brute: return 20 + level * 3;
                case Wizard: return 10 + level;
                case Thief: return 15 + level * 2;
                case Archer: return 15 + level * 2;
                case ArcherMaiden: return 17 + level * 2;
            }

            return 1;
        }

        public int getDamage(int level)
        {
            if(this == ArcherMaiden || this == Archer)
                return getDamageRanged(level);
            return getStrength(level) > 10 ? Random.IntRange( 1, getStrength(level) - 9 ) : 1;
        }

        public int getDamageRanged(int level)
        {
            return getStrength(level) > 3 ? Random.IntRange( 1, getStrength(level) - 3 ) : 1;
        }

        public int getDefence(int level)
        {
            switch (this)
            {
                case Brute: return 2 * level;
                case Wizard: return  level;
                case Thief: return 3 * level;
                case Archer: return 3 * level;
                case ArcherMaiden: return 3 + level  + getStrength(level);
            }
            return 1;
        }

        public String getDescription()
        {
            switch (this)
            {
                case Brute: return "Brutes are strong and slow but can tank a lot of damage.";
                case Wizard: return "Wizards cast spells but cannot take a lot of punishment. They are also weak.";
                case Thief: return "Thieves are very agile and have admirable strength.";
                case Archer: return "Archers support from a distance.";
                case ArcherMaiden: return "Only the select few achieve the title of Archer-Maiden.";
            }
            return "";
        }

        public float speedModifier()
        {
            switch (this)
            {
                case Brute: return 0.7f;
                case Wizard: return 1f;
                case Thief: return 1.5f;
                case Archer: return 1.2f;
                case ArcherMaiden: return 1.3f;
            }
            return 1f;
        }

        public int getImage()
        {
            switch (this)
            {
                case Brute: return 0;
                case Wizard: return 24;
                case Thief: return 48;
                case Archer: return 72;
                case ArcherMaiden: return 104;
            }
            return 0;
        }

        public int getWeaponPlaceHolder()
        {
            if(this == ArcherMaiden || this == Archer)
                return ItemSpriteSheet.EMPTY_BOW;
            return ItemSpriteSheet.WEAPON;
        }

        public int getArmorPlaceHolder()
        {
            return ItemSpriteSheet.ARMOR;
        }

        public Skill getSkill()
        {
            switch (this)
            {
                case Brute: return new MercBruteSkillA();
                case Wizard: return new MercWizardSkillA();
                case Thief: return new MercThiefSkillA();
                case Archer: return new MercArcherSkillA();
                case ArcherMaiden: return new MercArcherSkillB();
            }

            return new Endurance();
        }

        public void setSkills(HiredMerc merc)
        {
            merc.skill =  new MercBruteSkillA();
            switch (this)
            {
                case Brute: merc.skill = new MercBruteSkillA(); merc.skill.level = 1;break;
                case Wizard: merc.skill = new MercWizardSkillA();merc.skill.level = 1; break;
                case Thief: merc.skill = new MercThiefSkillA(); merc.skill.level = 1;break;
                case Archer: merc.skill = new MercArcherSkillA(); merc.skill.level = 1;break;
                case ArcherMaiden: merc.skill = new MercArcherSkillA();  merc.skill.level = 1; merc.skillb = new MercArcherSkillB(); merc.skillb.level = 4; break;
            }
        }

        public void setEquipment(HiredMerc merc)
        {
            switch (this)
            {
                case Brute:
                    merc.weapon = new Mace().identify();
                    merc.armor = new LeatherArmor().identify();
                    merc.carrying = new ChargrilledMeat();
                    break;
                case Wizard:
                merc.weapon = new Knuckles().identify();
                merc.armor = new ClothArmor().identify();
                merc.carrying = new PotionOfHealing();
                    break;
                case Thief:    merc.weapon = new Dagger().identify();
                    merc.armor = new ClothArmor().identify();
                    merc.carrying = new PotionOfHealing();
                    break;
                case Archer:   merc.weapon = new Bow(1);
                    merc.armor = new ClothArmor().identify();
                    merc.carrying = new PotionOfHealing();break;
                case ArcherMaiden: merc.weapon = new FrostBow(1);  merc.carrying = new PotionOfHealing().identify(); break;
            }
        }

        public int getStrength(int level)
        {
            switch (this)
            {
                case Brute: return 14 + (int) (level / 3);
                case Wizard: return 10 + (int) (level / 5);
                case Thief: return 13 + (int) (level / 4);
                case Archer: return 11 + (int) (level / 4);
                case ArcherMaiden: return 12 + (int) (level / 4);
            }
            return 0;
        }

        public int getSpecialSkillTime()
        {
            switch (this)
            {
                case Wizard: return 100;
            }
            return -1;
        }
    }

    public static final int COST_RATE = 15;

    public MERC_TYPES mercType = MERC_TYPES.Brute;

    @Override
    public Message nameMessage() {
        return MercenaryRecruitment.nameText(mercType);
    }

    public int rangedAttackCooldown = 0;
    public static final int RANGED_COOLDOWN = 5;

    public int skillCounter = 90;

    public Skill skill = null;
    public Skill skillb = null;
    public Skill skillc = null;

    public Item weapon = null;
    public Item armor = null;

    public Item carrying = null;

    private transient boolean meleeAttack;

    public boolean hackFix = false;

    public static final String MERC_TYPE = "merctype";

    {
        name = "Brute";
        spriteClass = MercSprite.class;

        viewDistance = 4;

        WANDERING = new Wandering();

        flying = false;
        state = WANDERING;
    }

    public int level;

    private static final String LEVEL	= "level";
    private static final String WEAPON		= "weapon";
    private static final String ARMOR		= "armor";

    public HiredMerc()
    {
        mercType.setSkills(this);
    }

    public HiredMerc(MERC_TYPES merc)
    {
        this.mercType = merc;

        mercType.setSkills(this);

    }

    public void equipWeapon(Item item)
    {
        if (!canEquip(item)) return;
        replaceWeapon(item);
    }

    public void unEquipWeapon()
    {
        replaceWeapon(null);
    }

    private void replaceWeapon(Item item)
    {
        if(weapon != null)
        {
            Dungeon.level.drop( weapon, pos, this ).sprite.drop();
        }
        weapon = item;
        refreshBonuses();
    }

    public void equipArmor(Item item)
    {
        if (!canEquip(item)) return;
        replaceArmor(item);
    }

    public void unEquipArmor()
    {
        replaceArmor(null);
    }

    private void replaceArmor(Item item)
    {
        if(armor != null)
        {
            Dungeon.level.drop( armor, pos, this ).sprite.drop();
        }
        armor = item;
        refreshBonuses();
        if (HP > 0 && sprite instanceof MercSprite) ((MercSprite)sprite).updateArmor();
    }

    public void equipItem(Item item)
    {
        unEquipItem();
        carrying = item;
    }

    public void unEquipItem()
    {
        if(carrying != null)
        {
            Dungeon.level.drop( carrying, pos, this ).sprite.drop();
        }
        carrying = null;
    }

    public int getArmorTier()
    {
        if(armor == null)
            return 0;
        return ((Armor) armor).tier;
    }

    public boolean canEquip(Item item)
    {
        if(item instanceof Weapon)
            return mercType.getStrength(level) >= ((Weapon)item).STR;
        if(item instanceof Armor)
            return mercType.getStrength(level) >= ((Armor)item).STR;
        return false;
    }

    public int attribute(EquipmentAttribute.Type type) {
        return EquipmentAttribute.bonus(this, type);
    }

    public void refreshBonuses() {
        if (!Dungeon.rules().hasRebornEncounters()) return;
        HT = (int)Math.min(Integer.MAX_VALUE, (long)mercType.getHealth(level)
                + attribute(EquipmentAttribute.Type.HEALTH));
        HP = Math.min(HP, HT);
    }

    @Override
    public int defenseProc( Char enemy, int damage ) {
        try {
            if (armor != null) {
                damage = ((Armor) armor).proc(enemy, this, damage);
            }
            damage *= skill.incomingDamageModifier();
        }
        catch (Exception ex)
        {

        }
        refreshBonuses();

        standGuard = false;
        if (owner().talents() != null) {
            damage = (int)((long)damage * (100 - Math.min(40, owner().talents().bonus(
                    Talent.Effect.MERC_GUARD))) / 100);
        }
        return damage;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {

        rangedAttackCooldown = 0;

        if (enemy instanceof Mob) {
            if(mercType != MERC_TYPES.Archer && mercType != MERC_TYPES.ArcherMaiden  || Level.adjacent( pos, enemy.pos ))
                ((Mob)enemy).aggro( this );
        }
        try {
        if (weapon != null && !(weapon instanceof Bow)) {
            ((Weapon)weapon).proc( this, enemy, damage );
        }

            if(weapon instanceof Bow)
            {
                ((Bow) weapon).bowSpecial(enemy);
            }
        }
        catch (Exception ex)
        {

        }
        refreshBonuses();

        try
        {
            if(skill.venomousAttack())
                Buff.affect(enemy, Poison.class).set(Poison.durationFactor(enemy));

            if(skill.cripple())
                Buff.affect(enemy, Cripple.class);
        }
        catch (Exception ex)
        {

        }

        return damage;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        refreshBonuses();
        super.storeInBundle( bundle );
        bundle.put( LEVEL, level );
        bundle.put( MERC_TYPE, mercType);
        bundle.put( WEAPON, weapon );
        bundle.put( ARMOR, armor );
        bundle.put("carrying", carrying);
        bundle.put("mercskill", skill.level);
        bundle.put("stand_guard", standGuard);
        bundle.put("skill_counter", skillCounter);
        bundle.put("ranged_cooldown", rangedAttackCooldown);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {

        if (!bundle.contains(MERC_TYPE)) {
            HP = 0;
            return;
        }
        super.restoreFromBundle( bundle );
        mercType = MERC_TYPES.valueOf(bundle.getString(MERC_TYPE));
        mercType.setSkills(this);
        level = bundle.getInt(LEVEL);
        defenseSkill = mercType.getDefence(level);
        weapon = (Item)bundle.get(WEAPON);
        armor = (Item)bundle.get(ARMOR);
        carrying = (Item)bundle.get("carrying");
        skillLevel(bundle.getInt("mercskill"));
        standGuard = bundle.getBoolean("stand_guard");
        skillCounter = bundle.getInt("skill_counter");
        rangedAttackCooldown = bundle.getInt("ranged_cooldown");
        refreshBonuses();
    }

    public void spawn( int level ) {
        this.level = level;

        HT = mercType.getHealth(level);
        refreshBonuses();
        HP = HT;
        defenseSkill = mercType.getDefence(level);

        name = mercType.getName();
    }

    public void spawn( int level, int maintainHP) {
        this.level = level;

        HT = mercType.getHealth(level);
        HP = maintainHP;
        refreshBonuses();
        name =  mercType.getName();
        defenseSkill = mercType.getDefence(level);

    }

    public void skillLevel(int level)
    {
        if(skill != null)
            skill.level = level;
    }

    public void level()
    {
        level++;
        HT = mercType.getHealth(level);
        refreshBonuses();
        HP = HT;
        try
        {
            sprite.showStatus( CharSprite.POSITIVE, Message.key("custom.generated.entry_stronger_by_the_second_30c2020f61"));

        }
      catch (Exception e){}
        if(skill.level < Skill.MERC_MAX_LEVEL)
            skill.level++;
    }

    public String getNameAndLevel()
    {
        return mercType.getName() + " (LvL " + level + (mercType != MERC_TYPES.ArcherMaiden ? " STR: " + mercType.getStrength(level) + ")" : ")");
    }

    @Override
    public CharSprite sprite() {
        CharSprite s = super.sprite();
        if(s instanceof MercSprite)
            ((MercSprite)s).updateArmor( mercType );
        return s;
    }

    @Override
    public int attackSkill( Char target ) {
        return defenseSkill + attribute(EquipmentAttribute.Type.ACCURACY);
    }

    @Override
    public int defenseSkill(Char enemy) {
        return enemySeen && !paralysed ? defenseSkill + attribute(EquipmentAttribute.Type.EVASION) : 0;
    }

    @Override
    public int dr() {
        return super.dr() + attribute(EquipmentAttribute.Type.ARMOR);
    }

    @Override
    public boolean attack(Char enemy) {
        if (enemy == null) return false;
        boolean previous = meleeAttack;
        meleeAttack = Level.adjacent(pos, enemy.pos);
        try {
            return super.attack(enemy);
        } finally {
            meleeAttack = previous;
        }
    }

    @Override
    public int damageRoll()
    {
        int damage;
        if (weapon == null) damage = mercType.getDamage(level);
        else if (weapon instanceof Bow) damage = (int)(mercType.getDamage(level) * 1.2f);
        else damage = ((Weapon)weapon).damageRoll(this);
        boolean ranged = !meleeAttack && (mercType == MERC_TYPES.Archer || mercType == MERC_TYPES.ArcherMaiden);
        damage *= 1 + attribute(ranged ? EquipmentAttribute.Type.RANGED : EquipmentAttribute.Type.MELEE) / 100f;
        if (owner().talents() != null) damage = (int)Math.min(Integer.MAX_VALUE,
                (long)damage * (100L + owner().talents().bonus(Talent.Effect.MERC_DAMAGE)) / 100);
        return damage;
    }

    @Override
    protected boolean canAttack( Char enemy ) {

        if(mercType != MERC_TYPES.Archer && mercType != MERC_TYPES.ArcherMaiden)
            return super.canAttack(enemy);

        if(mercType == MERC_TYPES.ArcherMaiden)
            return Ballistica.castMaiden(pos, enemy.pos) == enemy.pos;

        return Ballistica.cast( pos, enemy.pos, false, true ) == enemy.pos;
    }

    @Override
    public boolean doAttack(Char enemy)
    {
        if(Level.adjacent(pos, enemy.pos) || mercType != MERC_TYPES.Archer && mercType != MERC_TYPES.ArcherMaiden)
            return super.doAttack(enemy);

        if(rangedAttackCooldown < RANGED_COOLDOWN)
        {
            sprite.showStatus(CharSprite.NEUTRAL, Message.key("custom.generated.entry_reloading_sir_ee0e387808"));
            spend( attackDelay() );
            next();
            return false;
        }

        return super.doAttack(enemy);
    }

    private boolean standGuard = false;

    public void standGuard(boolean standGuard){
        this.standGuard = standGuard;
        if(sprite != null)
            sprite.showStatus(CharSprite.NEUTRAL, standGuard ? Message.key("custom.generated.entry_standing_guard_e4ea2add65") : Message.key("custom.generated.entry_got_your_back_c4ade2ebe0"));
    }

    public boolean getStandGuard(){
        return standGuard;
    }

    @Override
    protected boolean act() {

        spend( 0.01f );

        skillCounter++;

        rangedAttackCooldown++;

        if(hackFix == false)
        {
            ((MercSprite)super.sprite).updateArmor();
            hackFix = true;
        }

        if(mercType.getSpecialSkillTime() != -1 && skillCounter % mercType.getSpecialSkillTime() == 0)
            skill.mercSummon();

        if (HP <= 0) {

            die( null );
            return true;
        } else {
            if(!standGuard)
                return super.act();
            else
            {
                spend( TICK );
                return true;
            }
        }
    }

    @Override
    public void die(Object src)
    {

        if(carrying instanceof PotionOfHealing || carrying instanceof PotionOfRejuvenation)
        {
            GLog.p("actors.mobs.npcs.hiredmerc.drink", Message.actor(this), Message.itemName(carrying));
            super.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4);
            int before = Math.max(0, HP);
            HP = HT;
            showRecovery(HP - before, false);
            if (carrying instanceof PotionOfRejuvenation) {
                before = MP;
                MP = MMP;
                showRecovery(MP - before, true);
            }
            carrying = null;
            return;
        }

        super.die(src);

        unEquipWeapon();
        unEquipArmor();
        unEquipItem();

        Dungeon.hero.hiredMerc = null;

    }

    public void disband() {
        HP = 0;
        super.die(null);
        unEquipWeapon();
        unEquipArmor();
        unEquipItem();
        if (Dungeon.hero.hiredMerc == this) Dungeon.hero.hiredMerc = null;
    }

    protected Char chooseEnemy() {

        if (enemy == null || !enemy.isAlive()) {
            HashSet<Mob> enemies = new HashSet<Mob>();
            for (Mob mob:Dungeon.level.mobs) {
                if (mob.hostile && Level.fieldOfView[mob.pos]) {
                    enemies.add( mob );
                }
            }

            return enemies.size() > 0 ? Random.element( enemies ) : null;

        } else {

            return enemy;

        }
    }

    @Override
    public String description() {
        return
                mercType.getDescription();
    }

    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
    static {
        IMMUNITIES.add( Poison.class );
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return IMMUNITIES;
    }

    private class Wandering implements AiState {

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {

            if(pos == Dungeon.hero.pos)
                return true;

            if (enemyInFOV) {

                enemySeen = true;

                notice();
                state = HUNTING;
                target = enemy.pos;

            } else {

                enemySeen = false;

                int oldPos = pos;
                if (getCloser( Dungeon.hero.pos )) {
                    spend( 1 / (speed() * mercType.speedModifier()) );
                    return moveSprite( oldPos, pos );
                } else {
                    spend( TICK );
                }

            }
            return true;
        }

        @Override
        public String status() {
            return stateMessage( "wandering" );
        }
    }

    @Override
    public void interact() {
        if(pos == Dungeon.hero.pos)
            return;

        int curPos = pos;

        moveSprite( pos, Dungeon.hero.pos );
        move( Dungeon.hero.pos );

        Dungeon.hero.sprite.move( Dungeon.hero.pos, curPos );
        Dungeon.hero.move( curPos );

        Dungeon.hero.spend( 1 / Dungeon.hero.speed() );
        Dungeon.hero.busy();
    }
}
