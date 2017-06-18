package com.bilboldev.pixeldungeonskills;


import com.bilboldev.pixeldungeonskills.actors.buffs.Champ;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;

import java.util.ArrayList;


public enum Difficulties {

    NORMAL( 0 ), EASY( 1 ), HARD( 2 ), HELL( 3 ), SUICIDE( 4 ) , JUSTKILLME( 5 );


    private int difficulty;

    public static boolean canDisableChampions = false;

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

    public static final String[] EASY_DESC = {
            "- Start with 2 extra rations.",
            "- Start with 2 potions of healing.",
            "- Start with 200 Gold.",
            "- Mobs are Weak, do 25% less damage, take 10% more damage and have 15% less HP.",
            "- Champion spawn rate set to 10%."
    };

    public static final String[] NORMAL_DESC = {
            "- Mobs are standard.",
            "- Champion spawn rate set to 20%."
    };

    public static final String[] HARD_DESC = {
            "- Potion of healing heals 75% max hp.",
            "- Mobs are Strong, do 10% extra damage, take 10% less damage and have 20% more HP.",
            "- Champion spawn rate set to 30%."
    };

    public static final String[] HELL_DESC = {
            "- Potion of healing heals 50% max hp.",
            "- Mobs are Immortal, do 25% more damage, take 20% less damage and have 35% more HP.",
            "- Champion spawn rate set to 40%.",
            "- Hero starts with 4 less maxHP.",
            "- Hero gains 1 less maxHP on leveling."
    };

    public static final String[] SUICIDE_DESC = {
            "- Potion of healing heals 25% max hp.",
            "- Mobs are Godlike, do 45% more damage, take 30% less damage and have 60% more HP.",
            "- Champion spawn rate set to 50%.",
            "- Hero starts with 8 less maxHP.",
            "- Hero gains 3 less maxHP on leveling."
    };

    public static final String[] JUST_KILL_ME_DESC = {
            "- Potion of healing heals 10% max hp.",
            "- Mobs are Deities, do 60% more damage, take 40% less damage and have 75% more HP.",
            "- Champion spawn rate set to 100%.",
            "- Hero starts with 8 less maxHP.",
            "- Hero gains 3 less maxHP on leveling."
    };


    public String title() {

        switch (this) {
            case EASY:
                return "Easy";
            case NORMAL:
                return "Normal";
            case HARD:
                return "Hard";
            case HELL:
                return "Hell!";
            case SUICIDE:
                return "Suicide!!";
            case JUSTKILLME:
                return "Just Kill Me";
        }
        return "";
    }

    public String description() {

        switch (this) {
            case EASY:
                return join("\n", EASY_DESC);
            case NORMAL:
                return join("\n", NORMAL_DESC);
            case HARD:
                return join("\n", HARD_DESC);
            case HELL:
                return join("\n", HELL_DESC);
            case SUICIDE:
                return join("\n", SUICIDE_DESC);
            case JUSTKILLME:
                return join("\n", JUST_KILL_ME_DESC);
        }
        return "";
    }

    public String mobPrefix() {

        switch (this) {
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
            return ""; // Invalid difficulty
        }
    }

    public static String title(int difficulty)
    {
        try {
            return Difficulties.values()[difficulty].title();
        }
        catch (Exception ex)
        {
            return ""; // Invalid difficulty
        }
    }

    public int championChance() {
        return championChanceNatural() + championOffset;
    }

    public int championChanceNatural() {

        switch (this) {
            case EASY:
                return 1;
            case NORMAL:
                return 2;
            case HARD:
                return 3;
            case HELL:
                return 4;
            case SUICIDE:
                return 5;
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
            case EASY:
                return 0.75f;
            case NORMAL:
                return 1f;
            case HARD:
                return 1.1f;
            case HELL:
                return 1.25f;
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

    public float naturalMobDefenceModifier() { // dmg *= this

        switch (this) {
            case EASY:
                return 1.1f;
            case NORMAL:
                return 1f;
            case HARD:
                return 0.9f;
            case HELL:
                return 0.8f;
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
            case EASY:
                return 0.85f;
            case NORMAL:
                return 1f;
            case HARD:
                return 1.2f;
            case HELL:
                return 1.35f;
            case SUICIDE:
                return 1.6f;
            case JUSTKILLME:
                return 1.75f;
        }
        return 1f;
    }

    public int healingPotionLimit() {

        switch (this) {
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

    public String healingPotionMessage() {

        switch (this) {
            case EASY:
            case NORMAL:
                return "Your wounds heal completely.";

        }
        return "Your wounds are partially healed.";
    }



    public int difficultyHPLevelPenalty()
    {
        switch (this) {
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

    public void difficultyStartItemBonus()
    {
        switch (this) {
            case EASY:
                new PotionOfHealing().identify().collect();
                new PotionOfHealing().identify().collect();
                new Food().identify().collect();
                new Food().identify().collect();
                Dungeon.gold = 200;
        }
    }


    public void reset()
    {
        championOffset = 0;
        defenceOffset = 0;
        hpOffset = 0;
        attOffset = 0;
        disabledChampions.clear();
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
        return diff == 0 ? 1 : (diff == 1 ? 0 : diff);
    }

    static  String join (String delim, String ... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            if (i >= data.length-1) {break;}
            sb.append(delim);
        }
        return sb.toString();
    }
}
