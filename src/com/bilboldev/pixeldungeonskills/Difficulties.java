package com.bilboldev.pixeldungeonskills;


import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;

/**
 * Created by Moussa on 19-Jan-17.
 */
public enum Difficulties {

    NORMAL( 0 ), EASY( 1 ), HARD( 2 ), HELL( 3 ), SUICIDE( 3 );

    private int difficulty;

    private Difficulties( int difficulty ) {
        this.difficulty = difficulty;
    }

    public static final String[] EASY_DESC = {
            "- Start with 2 extra rations.",
            "- Start with 2 potions of healing.",
            "- Potion of healing identified from the start.",
            "- Mobs are Weak, do 25% less damage and have 15% less HP.",
            "- Champion spawn rate set to 10%."
    };

    public static final String[] NORMAL_DESC = {
            "- Mobs are standard.",
            "- Champion spawn rate set to 20%."
    };

    public static final String[] HARD_DESC = {
            "- Potion of healing heals 75% max hp.",
            "- Mobs are Strong, do 10% extra damage and have 20% more HP.",
            "- Champion spawn rate set to 30%."
    };

    public static final String[] HELL_DESC = {
            "- Potion of healing heals 50% max hp.",
            "- Mobs are Immortal, do 25% more damage and have 35% more HP.",
            "- Champion spawn rate set to 40%.",
            "- Hero starts with 4 less maxHP.",
            "- Hero gains 1 less maxHP on leveling."
    };

    public static final String[] SUICIDE_DESC = {
            "- Potion of healing heals 25% max hp.",
            "- Mobs are Godlike, do 45% more damage and have 60% more HP.",
            "- Champion spawn rate set to 50%.",
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
        }
        return 0;
    }

    public float damageModifier() {

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
        }
        return 1f;
    }

    public float mobHPModifier() {

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
        }
        return 100;
    }

    public String healingPotionMessage() {

        switch (this) {
            case EASY:
            case NORMAL:
                return "Your wounds heal completely.";
            case HARD:
            case SUICIDE:
                return "Your wounds are partially healed.";
        }
        return "";
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
        }
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
