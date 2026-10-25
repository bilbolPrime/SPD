package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndWelcome;
import com.bilboldev.pixeldungeonskills.windows.WndRebornWelcome;

public class VersionNewsInfo {
    public static boolean alreadySeen = false;
    private static boolean welcomeOpen;
    private static boolean rebornSeen, classicSeen;

    public static boolean haveMessage()
    {
        return !alreadySeen || welcomeOpen;
    }

    public static String getMessage()
    {
        markSeen();
        return currentMessage();
    }

    public static Window getWelcomeWindow()
    {
        prepareEntitlements();
        markSeen();
        welcomeOpen = true;
        return Presentation.reborn() ? new WndRebornWelcome(currentMessage()) : new WndWelcome(currentMessage());
    }

    public static void closeWelcome() {
        welcomeOpen = false;
    }

    public static void presentationChanged() {
        welcomeOpen = false;
        alreadySeen = Presentation.reborn() ? rebornSeen : classicSeen;
    }

    private static void markSeen() {
        alreadySeen = true;
        if (Presentation.reborn()) rebornSeen = true;
        else classicSeen = true;
    }

    public static void prepareEntitlements() {
        HiredMerc.archerMaidenUnlocked = PixelDungeon.maidenUnlocked() || PixelDungeon.donated() > 0;
        Difficulties.canDisableChampions = PixelDungeon.disableChampionsUnlocked() || PixelDungeon.donated() > 0;
    }

    private static String currentMessage() {
		String version = Game.version == null ? "???" : Game.version;
		String message = Messages.get(VersionNewsInfo.class, "message", version, Game.versionBuild);
        return message.replace(
                Messages.get(VersionNewsInfo.class, "community_links_line"),
                "");
    }
}
