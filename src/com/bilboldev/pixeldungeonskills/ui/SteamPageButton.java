package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.windows.WndWelcome;

public class SteamPageButton extends RedButton {
    public static final String URL = "https://store.steampowered.com/app/4696680/Skillful_Pixel_Dungeon/"
            + "?utm_source=desktop_client&utm_medium=internal&utm_campaign=free_version";

    public SteamPageButton() {
        super(Messages.get(WndWelcome.class, "open_steam"));
    }

    @Override
    protected void onClick() {
        Window.openUrl(URL);
    }
}
