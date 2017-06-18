package com.bilboldev.pixeldungeonskills.actors.skills;




import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.windows.WndSkill;
import com.bilboldev.utils.Bundle;


public enum CurrentSkills{

    WARRIOR("warrior"), MAGE("mage"), ROGUE("rogue"), HUNTRESS("huntress") , HATSUNE("hatsune");


    public enum BRANCHES {PASSIVEA, PASSIVEB, ACTIVE}

    public static final String TYPE = "TYPE";
    public static final String UNLOCKED = "unlocked";

    public Skill branchPA = null;
    public Skill passiveA1 = null;
    public Skill passiveA2 = null;
    public Skill passiveA3 = null;

    public Skill branchPB = null;
    public Skill passiveB1 = null;
    public Skill passiveB2 = null;
    public Skill passiveB3 = null;

    public Skill branchA = null;
    public Skill active1 = null;
    public Skill active2 = null;
    public Skill active3 = null;

    public Skill lastUsed = null;

    public boolean skillUnlocked = false;

    public static Negotiations mercMenu = new Negotiations();

    private String type = "";

    private CurrentSkills(String type) {
        this.type = type;
    }

    public void init()
    {
        init(Dungeon.hero);
    }

    public void init(Hero hero)
    {
        hero.heroSkills = this;
        lastUsed = null;
        switch (this) {
            case WARRIOR:
                branchPA = new WarriorPassiveA();
                passiveA1 = new Endurance();
                passiveA2 = new Regeneration();
                passiveA3 = new Toughness();
                branchPB = new WarriorPassiveB();
                passiveB1 = new FirmHand();
                passiveB2 = new Aggression();
                passiveB3 = new Mastery();
                branchA = new WarriorActive();
                active1 = new Smash();
                active2 = new KnockBack();
                active3 = new Rampage();
                break;

            case MAGE:
                branchPA = new MagePassiveA();
                passiveA1 = new Spirituality();
                passiveA2 = new Meditation();
                passiveA3 = new SpiritArmor();
                branchPB = new MagePassiveB();
                passiveB1 = new Wizard();
                passiveB2 = new Sorcerer();
                passiveB3 = new Summoner();
                branchA = new MageActive();
                active1 = new SummonRat();
                //active2 = new SummonCrab();
                active2 = new Spark();
                active3 = new SummonSkeleton();
                break;

            case ROGUE:
                branchPA = new RoguePassiveA();
                passiveA1 = new Bandit();
                passiveA2 = new Stealth();
                passiveA3 = new LockSmith();
                branchPB = new RoguePassiveB();
                passiveB1 = new Venom();
                passiveB2 = new Scorpion();
                passiveB3 = new SilentDeath();
                branchA = new RogueActive();
                active1 = new SmokeBomb();
                //active2 = new DeadEye();
                active2 = new DoubleStab();
                active3 = new ShadowClone();
                break;

            case HUNTRESS:
                branchPA = new HuntressPassiveA();
                passiveA1 = new Fletching();
                passiveA2 = new Hunting();
                passiveA3 = new Awareness();
                branchPB = new HuntressPassiveB();
                passiveB1 = new Accuracy();
                passiveB2 = new KneeShot();
                passiveB3 = new IronTip();
                branchA = new HuntressActive();
                active1 = new SpiritArrow();
                active2 = new DoubleShot();
                active3 = new Bombvoyage();
                break;
            case HATSUNE:
                branchPA = new LegendPassiveA();
                passiveA1 = new Spirituality();
                passiveA1.level = 10;
                passiveA2 = new Meditation();
                passiveA2.level = 10;
                passiveA3 = new SpiritArmor();
                passiveA3.level = 10;
                passiveA3.active = true;
                branchPB = new LegendActiveA();
                passiveB1 = new SummonSkeletonArcher();
                passiveB1.level = 10;
                passiveB2 = new DarkBolt();
                passiveB2.level = 10;
                passiveB3 = new Dominance();
                passiveB3.level = 10;
                branchA = new LegendActiveA();
                active1 = new Echo();
                active1.level = 10;
                active2 = new SoulSpark();
                active2.level = 10;
                active3 = new SoulFury();
                active3.level = 10;
                Skill.availableSkill = 0;
                break;
        }
    }

    public int totalSpent(BRANCHES branch)
    {
        switch(branch)
        {
            case PASSIVEA: return passiveA1.level * passiveA1.upgradeCost() + passiveA2.level * passiveA2.upgradeCost() + passiveA3.level * passiveA3.upgradeCost();
            case PASSIVEB: return passiveB1.level * passiveB1.upgradeCost() + passiveB2.level * passiveB2.upgradeCost() + passiveB3.level * passiveB3.upgradeCost();
            case ACTIVE: return active1.level * active1.upgradeCost() + active2.level * active2.upgradeCost() + active3.level * active3.upgradeCost();
        }
        return 0;
    }

    public boolean canUpgrade(BRANCHES branch)
    {
        switch(branch)
        {
            case PASSIVEA: return passiveA3.level < Skill.MAX_LEVEL;
            case PASSIVEB: return passiveB3.level < Skill.MAX_LEVEL;
            case ACTIVE: return active3.level < Skill.MAX_LEVEL;
        }
        return false;
    }

    public int nextUpgradeCost(BRANCHES branch)
    {
        switch(branch)
        {
            case PASSIVEA: return (passiveA1.level < Skill.MAX_LEVEL ? passiveA1.upgradeCost() : (passiveA2.level < Skill.MAX_LEVEL ? passiveA2.upgradeCost() : passiveA3.upgradeCost()));
            case PASSIVEB: return (passiveB1.level < Skill.MAX_LEVEL ? passiveB1.upgradeCost() : (passiveB2.level < Skill.MAX_LEVEL ? passiveB2.upgradeCost() : passiveB3.upgradeCost()));
            case ACTIVE: return (active1.level < Skill.MAX_LEVEL ? active1.upgradeCost() : (active2.level < Skill.MAX_LEVEL ? active2.upgradeCost() : active3.upgradeCost()));
        }
        return 0;
    }

    public void advance(BRANCHES branch)
    {
        switch(branch)
        {
            case PASSIVEA: if(passiveA1.level < Skill.MAX_LEVEL) { passiveA1.requestUpgrade();} else if (passiveA2.level < Skill.MAX_LEVEL) { passiveA2.requestUpgrade();} else passiveA3.requestUpgrade();
                break;
            case PASSIVEB:if(passiveB1.level < Skill.MAX_LEVEL) { passiveB1.requestUpgrade();} else if (passiveB2.level < Skill.MAX_LEVEL) { passiveB2.requestUpgrade();} else passiveB3.requestUpgrade();
                break;
            case ACTIVE: if(active1.level < Skill.MAX_LEVEL) { active1.requestUpgrade();} else if (active2.level < Skill.MAX_LEVEL) { active2.requestUpgrade();} else active3.requestUpgrade();
                break;
        }
    }


    public void showLastUsed()
    {
        if(lastUsed != null)
            GameScene.show(new WndSkill(null, lastUsed));

    }

    public void storeInBundle( Bundle bundle )
    {
        bundle.put( TYPE, toString() );
        bundle.put(UNLOCKED, skillUnlocked);
        passiveA1.storeInBundle(bundle);
        passiveA2.storeInBundle(bundle);
        passiveA3.storeInBundle(bundle);
        passiveB1.storeInBundle(bundle);
        passiveB2.storeInBundle(bundle);
        passiveB3.storeInBundle(bundle);
        active1.storeInBundle(bundle);
        active2.storeInBundle(bundle);
        active3.storeInBundle(bundle);
    }

    public static CurrentSkills restoreFromBundle( Bundle bundle) {
        String value = bundle.getString( TYPE );
        try {
            return valueOf( value );
        } catch (Exception e) {
            return WARRIOR;
        }
    }

    public void restoreSkillsFromBundle( Bundle bundle ) {
        passiveA1.restoreInBundle(bundle);
        passiveA2.restoreInBundle(bundle);
        passiveA3.restoreInBundle(bundle);
        passiveB1.restoreInBundle(bundle);
        passiveB2.restoreInBundle(bundle);
        passiveB3.restoreInBundle(bundle);
        active1.restoreInBundle(bundle);
        active2.restoreInBundle(bundle);
        active3.restoreInBundle(bundle);
        skillUnlocked = bundle.getBoolean(UNLOCKED);
        if(skillUnlocked)
            unlockSkill();
    }

    public void unlockSkill()
    {
        skillUnlocked = true;
        int preserveLevel = 0;
        switch (this) {
            case WARRIOR:
                preserveLevel = active1.level;
                active1 = new Smite();
                active1.level = preserveLevel;
                break;
            case HUNTRESS:
                preserveLevel = active2.level;
                active2 = new TripleShot();
                active2.level = preserveLevel;
                break;
            case MAGE:
                preserveLevel = active3.level;
                active3 = new SummonSkeletonArcher();
                active3.level = preserveLevel;
                break;
            case ROGUE:
                preserveLevel = passiveB2.level;
                passiveB2 = new KOArrow();
                passiveB2.level = preserveLevel;
        }
    }

    public String unlockableSkillName()
    {
        switch (this) {
            case WARRIOR:
               return "Smite";
            case MAGE:
                return "Summon Skeleton Archer";
            case ROGUE:
                return "KO Arrow";
            case HUNTRESS:
                return "Triple Shot";
        }

        return "";
    }
}
