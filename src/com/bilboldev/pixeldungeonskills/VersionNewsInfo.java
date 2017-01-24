package com.bilboldev.pixeldungeonskills;


import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndWelcome;

/**
 * Created by Moussa on 23-Jan-17.
 */
public class VersionNewsInfo {
    public static int versionBuild = 0;
    public static String message = "Welcome to Skillful PixelDungeon!\n \n \n"
            + "The game has been given a complete makeover in terms of class skills and balance.\n \n"
            + "Old ranking details cannot be displayed at this time, but rest assured they are not lost.\n \n"
            + "As of 0.2.2b (Build 25): \n"
            + "- Fixed an exploit allowing infinite skill points... You rascals you..\n"
            + "- Fixed bug preventing Spirituality skill from saving its level properly.\n"
            + "- Mage summoned units are weaker but last longer, limited to 3 + Summoner level.\n"
            + "- Summoner skill no longer increases number of summoned units on casting Summon.\n"
            + "- Skills that are cast or summon spend some time.\n \n"
            + "Have fun :D";

    public static boolean alreadySeen = false;

    public static boolean haveMessage()
    {
        return !alreadySeen;
    }

    public static String getMessage()
    {
        alreadySeen = true;
        return message;
    }

    public static Window getWelcomeWindow()
    {
        alreadySeen = true;
        return (new WndWelcome(message));
    }
}
