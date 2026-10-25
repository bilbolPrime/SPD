package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfExperience;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.SoulCrystal;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import java.util.ArrayList;

public enum Difficulties {

    NORMAL( 0 ), EASY( 1 ), HARD( 2 ), HELL( 3 ), SUICIDE( 4 ) , JUSTKILLME( 5 ), SUPEREASY( 6 );

    private int difficulty;

    public static boolean canDisableChampions = false;

    public static boolean is3d = true;

    private int championOffset = 0;
    public float hpOffset = 0;
    public float attOffset = 0;
    public float defOffset = 0;
    public float defenceOffset = 0;

    public enum isNightOverwrite {DEFAULT, ALWAYS_DAY, ALWAYS_NIGHT}
    public isNightOverwrite isNight = isNightOverwrite.DEFAULT;

    private ArrayList<Integer> disabledChampions = new ArrayList<>();

    private Difficulties( int difficulty ) {
        this.difficulty = difficulty;
        championOffset = 0;
        hpOffset = 0;
        attOffset = 0;
        defOffset = 0;
        disabledChampions.clear();
    }

    private String messageKey() {

        switch (this) {
            case SUPEREASY:
                return "custom.difficulty.super_easy";
            case EASY:
                return "custom.difficulty.easy";
            case NORMAL:
                return "custom.difficulty.normal";
            case HARD:
                return "custom.difficulty.hard";
            case HELL:
                return "custom.difficulty.hell";
            case SUICIDE:
                return "custom.difficulty.nightmare";
            case JUSTKILLME:
                return "custom.difficulty.just_kill_me";
        }
        return null;
    }

    public String title() {

        String key = messageKey();
        return key == null ? "" : Messages.get(key);
    }

    public String description() {

        String key = messageKey();
        return key == null ? "" : Messages.get(key + ".desc");
    }

    public String mobPrefix() {

        switch (this) {
            case SUPEREASY:
                return "Pathetic ";
            case EASY:
                return "Weak ";
            case NORMAL:
                return "";
            case HARD:
                return "Strong ";
            case HELL:
                return "Immortal ";
            case SUICIDE:
                return "Godlike ";
            case JUSTKILLME:
                return "Deity ";
        }
        return "";
    }

    public static String description(int difficulty)
    {
        try {
            return Difficulties.values()[difficulty].description();
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String title(int difficulty)
    {
        try {
            return Difficulties.values()[difficulty].title();
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public int championChance() {
        return championChanceNatural() + championOffset;
    }

    public int championChanceNatural() {

        switch (this) {
            case SUPEREASY:
            case EASY:
                return 1;
            case NORMAL:
                return 2;
            case HARD:
                return 3;
            case HELL:
                return 5;
            case SUICIDE:
                return 7;
            case JUSTKILLME:
                return 10;
        }
        return 0;
    }

    public int championDropChanceNatural() {

        switch (this) {
            case SUPEREASY:
            case EASY:
                return 1 + 2;
            case NORMAL:
                return 2 + 2;
            case HARD:
                return 3 + 2;
            case HELL:
                return 5 + 2;
            case SUICIDE:
                return 7 + 2;
            case JUSTKILLME:
                return 10;
        }
        return 0;
    }

    public float damageModifier()
    {
        return naturalDamageModifier() + attOffset;
    }

    public float naturalDamageModifier() {

        switch (this) {
            case SUPEREASY:
                return  0.5f;
            case EASY:
                return 0.75f;
            case NORMAL:
                return 1f;
            case HARD:
                return 1.1f;
            case HELL:
                return 1.35f;
            case SUICIDE:
                return 1.45f;
            case JUSTKILLME:
                return 1.6f;
        }
        return 1f;
    }

    public float mobDefenceModifier() {

        return naturalMobDefenceModifier() + defenceOffset;
    }

    public float naturalMobDefenceModifier() {

        switch (this) {
            case SUPEREASY:
                return  1.5f;
            case EASY:
                return 1.1f;
            case NORMAL:
                return 1f;
            case HARD:
                return 0.9f;
            case HELL:
                return 0.7f;
            case SUICIDE:
                return 0.7f;
            case JUSTKILLME:
                return 0.6f;
        }
        return 1f;
    }

    public float mobHPModifier() {

        return naturalMobHPModifier() + hpOffset;
    }

    public float naturalMobHPModifier() {

        switch (this) {
            case SUPEREASY:
                return 0.5f;
            case EASY:
                return 0.85f;
            case NORMAL:
                return 1f;
            case HARD:
                return 1.2f;
            case HELL:
                return 1.45f;
            case SUICIDE:
                return 1.6f;
            case JUSTKILLME:
                return 1.75f;
        }
        return 1f;
    }

    public int healingPotionLimit() {

        switch (this) {
            case SUPEREASY:
            case EASY:
            case NORMAL:
                return 100;
            case HARD:
                return 75;
            case HELL:
                return 50;
            case SUICIDE:
                return 25;
            case JUSTKILLME:
                return 15;
        }
        return 100;
    }

    public Message healingPotionMessage() {

        switch (this) {
            case SUPEREASY:
            case EASY:
            case NORMAL:
                return Message.key("custom.generated.entry_your_wounds_heal_completely_b495fde305");

        }
        return Message.key("custom.generated.entry_your_wounds_are_partially_healed_5320aaf43c");
    }

    public int difficultyHPLevelPenalty()
    {
        switch (this) {
            case SUPEREASY:
            case EASY:
            case NORMAL:
            case HARD:
                return 0;
            case HELL:
                return 1;
            case SUICIDE:
                return 3;
            case JUSTKILLME:
                return 3;
        }
        return 0;
    }

    public int difficultyHPStartPenalty()
    {
        switch (this) {
            case SUPEREASY:
            case EASY:
            case NORMAL:
            case HARD:
                return 0;
            case HELL:
                return 4;
            case SUICIDE:
                return 8;
            case JUSTKILLME:
                return 8;
        }
        return 0;
    }

    public int difficultySkillStartBonus()
    {

        return 0;
    }

    public void difficultyStartItemBonus()
    {
        switch (this) {
            case SUPEREASY:
                new PotionOfHealing().identify().collect();
                new PotionOfHealing().identify().collect();
                new PotionOfHealing().identify().collect();
                new PotionOfHealing().identify().collect();
                new PotionOfHealing().identify().collect();
                new Food().identify().collect();
                new Food().identify().collect();
                new Food().identify().collect();
                new Food().identify().collect();
                new Food().identify().collect();
                new SoulCrystal().identify().collect();
                new SoulCrystal().identify().collect();
                new SoulCrystal().identify().collect();
                Dungeon.gold = 500;
                break;
            case EASY:
                new PotionOfHealing().identify().collect();
                new PotionOfHealing().identify().collect();
                new Food().identify().collect();
                new Food().identify().collect();
                new SoulCrystal().identify().collect();
                new SoulCrystal().identify().collect();
                Dungeon.gold = 200;
                break;
            case JUSTKILLME:
                new PotionOfExperience().identify().collect();
                new PotionOfExperience().identify().collect();
                break;
        }
    }

    public boolean noSecrets(){
       switch (this){
           case EASY:
               return true;
       }

       return  false;
    }

    public void reset()
    {
        championOffset = 0;
        defenceOffset = 0;
        hpOffset = 0;
        attOffset = 0;
        disabledChampions.clear();
    }

    Runnable useDefaultsTemporarily() {
        final int oldChampion = championOffset;
        final float oldHP = hpOffset, oldAttack = attOffset, oldDef = defOffset, oldDefence = defenceOffset;
        final isNightOverwrite oldNight = isNight;
        final ArrayList<Integer> oldDisabled = new ArrayList<>(disabledChampions);
        reset();
        defOffset = 0;
        isNight = isNightOverwrite.DEFAULT;
        return new Runnable() {
            @Override
            public void run() {
                championOffset = oldChampion;
                hpOffset = oldHP;
                attOffset = oldAttack;
                defOffset = oldDef;
                defenceOffset = oldDefence;
                isNight = oldNight;
                disabledChampions.clear();
                disabledChampions.addAll(oldDisabled);
            }
        };
    }

    public void changeDefenceOffset(float change)
    {
        defenceOffset += change;
        if(mobDefenceModifier() > naturalMobDefenceModifier())
            defenceOffset = 0;
        if(mobDefenceModifier() < 0.1f)
            defenceOffset = 0.1f - naturalMobDefenceModifier();
    }

    public void changeChampionOffset(int change)
    {
        championOffset += change;
        if(championChance() < championChanceNatural())
            championOffset = 0;
        if(championChance() > 10)
            championOffset = 10 - championChanceNatural();
    }

    public void changeHPOffset(float change)
    {
        hpOffset += change;
        if(mobHPModifier() < naturalMobHPModifier())
            hpOffset = 0;
        if(mobHPModifier() > 2f)
            hpOffset = 2f - naturalMobHPModifier();
    }

    public void changeDamageOffset(float change)
    {
        attOffset += change;
        if(damageModifier() < naturalDamageModifier())
            attOffset = 0;
        if(damageModifier() > 2f)
            attOffset = 2f - naturalDamageModifier();
    }

    public boolean disableChampion(int champType)
    {
        return disabledChampions.contains(champType);
    }

    public boolean disableChampion(int champType, boolean disable)
    {
        if(disable && disabledChampions.size() == 3)
            return false;

        if(disable && disableChampion(champType) == false)
            disabledChampions.add(champType);

        if(!disable && disableChampion(champType))
            disabledChampions.remove((Object) champType);

        return true;
    }

    public void ToggleNight()
    {
        switch(isNight)
        {
            case ALWAYS_DAY:
                isNight = isNightOverwrite.ALWAYS_NIGHT;
                break;
            case ALWAYS_NIGHT:
                isNight = isNightOverwrite.DEFAULT;
                break;
            case DEFAULT:
                isNight = isNightOverwrite.ALWAYS_NIGHT;
                break;
        }
    }

    public void ToggleNight(boolean harder)
    {
        if(harder) {
            switch (isNight) {
                case DEFAULT:
                    isNight = isNightOverwrite.ALWAYS_NIGHT;
                    break;
                case ALWAYS_DAY:
                    isNight = isNightOverwrite.DEFAULT;
                    break;
            }
        }
        else
        {
            switch (isNight) {
                case DEFAULT:
                    isNight = isNightOverwrite.ALWAYS_DAY;
                    break;
                case ALWAYS_NIGHT:
                    isNight = isNightOverwrite.DEFAULT;
                    break;
            }
        }
    }

    public String GetToggleNightDesc()
    {
        switch(isNight)
        {
            case ALWAYS_DAY:
                return "Always Day";
            case ALWAYS_NIGHT:
                return "Always Night";
            case DEFAULT:
                return "Day/Night";
        }
        return "error";
    }
    public static int getNormalizedDifficulty(int diff)
    {
        if(diff == 0){
            return 1;
        }

        if(diff == 1){
            return 0;
        }

        if(diff == 2){
            return  3;
        }

        if(diff == 3){
            return  4;
        }

        if(diff == 4){
            return  5;
        }

        return 0;
    }

    public static void swap3D(){
        is3d = !is3d;
        PixelDungeon.enabled3d(is3d);
    }
}
