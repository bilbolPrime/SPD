package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillTree;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Bundle;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Skill{

    public static final String AC_ADVANCE = "Advance";
    public static final String AC_ACTIVATE = "Activate";
    public static final String AC_DEACTIVATE = "Deactivate";

    public static final String AC_SUMMON = "Summon";
    public static final String AC_CAST = "Cast";

    public static final String FAIL_ADVANCE = "You do not have enough skill points to advance in this branch.";

    public static final String SKILL_LEVEL = "LEVEL";
    public static final String COOL_DOWN = "COOL_DOWN";

    public String tag = "";

    public static final int MAX_LEVEL = 10;

    public static final int MERC_MAX_LEVEL = 3;

    public static final int STARTING_SKILL = 1;

    public static int availableSkill = STARTING_SKILL;
    public static float skillTrack = 0;

    public static final float TIME_TO_USE = 1f;

    public String name = "Skill";
    public Message castText;
    public int level = 0;
    public int tier = 1;
    public int mana = 0;
    public int image = 0;

    public boolean active = false;

    public float availableAfter = 0f;
    public float useDelay = 0f;

    public boolean multiTargetActive = false;

    public boolean quickCast = false;

    public boolean requestUpgrade()
    {
        if(availableSkill >= 1 && level < MAX_LEVEL)
        {
            if(upgrade())
            {
                level++;
                availableSkill -= 1;

                return true;
            }
        }
        else
        {

        }

        return false;
    }

    protected boolean upgrade()
    {
        return false;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public float incomingDamageModifier()
    {
        return 1f;
    }

    public float damageModifier()
    {
        return 1f;
    }

    public float meleeSpeedModifier()
    {
        return 1f;
    }

    public float rangedDamageModifier()
    {
        return 1f;
    }

    public int gunAmmoBonus() {
        return 0;
    }

    public int gunToHitBonus() {
        return 0;
    }

    public float gunDamageModifier() {
        return 1f;
    }

    public float gunAttackSpeedModifier() {
        return 1f;
    }

    public float gunRearmSpeedModifier() {
        return 1f;
    }

    public boolean causesFlee() {
        return false;
    }

    public float executeThreshold() {
        return 0f;
    }

    public int wantedGoldBonus() {
        return 0;
    }

    public float headShotDamageModifier() {
        return 1f;
    }

    public int headShotToHitBonus() {
        return 0;
    }

    public void consumeHeadShot() {
    }

    public void applyToGunShot(Hero hero, SkillTree.GunShotData shotData) {
    }

    public int damageBonus(int hp)
    {
        return damageBonus(hp, false);
    }

    public int damageBonus(int hp, boolean castText)
    {
        return 0;
    }

    public int toHitBonus()
    {
        return 0;
    }

    public int healthRegenerationBonus()
    {
        return 0;
    }

    public int weaponLevelBonus()
    {
        return 0;
    }

    public int fletching() {return 0;}

    public int hunting() {return 0;}

    public boolean knocksBack()
    {
        return false;
    }

    public boolean AoEDamage()
    {
        return false;
    }

    public int manaRegenerationBonus() {return 0;}

    public int incomingDamageReduction(int damage) {return 0;}

    public int image() {
        return image;
    }

    public String info() {return "";}

    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if(hero.skillTree.canLevel(this)){
            actions.add(AC_ADVANCE);
        }
        return actions;
    }

    public void execute( Hero hero, String action ) {
        if(action == Skill.AC_ADVANCE){
            hero.skillTree.advance(this);
        }
    }

    public float getAlpha()
    {
        return 0.1f + level * 0.3f;
    }

    public int upgradeCost() {return tier;}

    protected int totalSpent()
    {
        return 0;
    }

    protected int nextUpgradeCost()
    {
        return 0;
    }

    protected boolean canUpgrade()
    {
        return false;
    }

    public String costUpgradeInfo()
    {
        return name + " is at level " + level + ".\n"

                + (level > 0 && mana > 0 ? "\nUsing " + name + " costs " + getManaCost() + " mana.": "");
    }

    public String extendedInfo(){
        return "";
    }

    public String requiresInfo(){
        return "";
    }

    public int getManaCost()
    {
        return mana;
    }

    public boolean coolDown(){
        return availableAfter > skillTrack;
    }

    public void castTextYell()
    {
        if(castText != null)
            Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, castText);

        if(useDelay != 0){
            availableAfter = skillTrack + useDelay;
        }
    }

    public float wandRechargeSpeedReduction()
    {
        return 1f;
    }

    public int summoningLimitBonus() {return 0;}

    public float wandDamageBonus()
    {
        return 1f;
    }

    public int lootBonus(int gold) { return 0;}

    public int stealthBonus(){return 0;}

    public boolean disableTrap() {return false;}

    public boolean venomousAttack() {return false;}

    public float venomDamageModifier() {return 1f;}

    public boolean instantKill() {return false;}

    public boolean dodgeChance(){return false;}

    public int toHitModifier(){return 0;}

    public int evasionDefenceBonus() {return 0;}

    public boolean cripple() {return false;}

    public int passThroughTargets(boolean shout)
    {
        return 0;
    }

    public boolean doubleShot()
    {
        return false;
    }

    public boolean doubleStab()
    {
        return false;
    }

    public boolean arrowToBomb()
    {
        return false;
    }

    public int aimedShot() { return 0; }

    public void mercSummon() {}

    public boolean goToSleep()
    {
        return false;
    }

    public boolean slowTarget() {return false;}

    public void storeInBundle(Bundle bundle)
    {
        bundle.put( SKILL_LEVEL + " " + tag, level );
        try {
            bundle.put(COOL_DOWN + " " + tag, availableAfter - skillTrack);
        }
     catch (Exception e){

     }
    }

    public void restoreInBundle(Bundle bundle)
    {
        level = bundle.getInt( SKILL_LEVEL + " " + tag);
        try
        {
            availableAfter = (float)  bundle.getFloat(COOL_DOWN + " " + tag);
        }
       catch (Exception e){

       }
    }

    public String highlight(String input){
		String localized = Messages.maybeTranslate(input);
        if (localized == null || localized.length() == 0) {
            return localized;
        }

        return "_" + localized + "_";
    }

    protected String necroMercText(String key, Object... args) {
        String messageKey = "custom.generated.necro_merc." + key;
        String text = Messages.get(messageKey);
        if (args == null) {
            return text;
        }
        for (Object arg : args) {
            text = text.replaceFirst("%s", Matcher.quoteReplacement(arg == null ? "" : arg.toString()));
        }
        return text;
    }

    protected String necroMercInfo(String key, String extendedInfo) {
        String text = necroMercText(key, extendedInfo);
        if (extendedInfo == null || extendedInfo.length() == 0 || text.indexOf(extendedInfo) >= 0) {
            return text;
        }
        return text + "\n\n" + extendedInfo;
    }

    protected String highlightNecroMercText(String key, Object... args) {
        return "_" + necroMercText(key, args) + "_";
    }

    public String costString(){
        return "\n\n" + highlight("This is a passive skill.");
    }

    public ArrayList<Class<? extends Skill>> getRequirements(){
        return new ArrayList<>();
    }

    public boolean canCast(){
        try
        {
            return Dungeon.hero.MP >= mana && !coolDown();
        }
        catch (Exception e){
            return false;
        }
    }

    public int sparkBonusDamage(){
        return 0;
    }
}
