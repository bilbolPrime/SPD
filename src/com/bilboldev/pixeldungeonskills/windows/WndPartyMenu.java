package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.network.PartyWindows;
import com.bilboldev.utils.Bundle;

public class WndPartyMenu extends WndOptions {
    private final long token;

    public WndPartyMenu(long token, String title, String description, String[] choices) {
        this(token, null, title, description, choices);
    }

    public WndPartyMenu(long token, Image icon, String title, String description, String[] choices) {
        this(token, icon, title, description, choices, null, null);
    }

    public WndPartyMenu(long token, Image icon, String title, String description, String[] choices,
                        int[] images, int[] quantities) {
        super(icon, title, description, images == null ? 1 : 2, false, images, quantities, choices);
        this.token = token;
    }

    @Override
    protected void onSelect(int index) {
        PartyWindows.awaitResponse(token);
        Bundle choice = new Bundle();
        choice.put("token", (int)token);
        choice.put("index", index);
        PartySession.command("ui_choose", choice);
    }

    @Override
    public void onBackPressed() {
        PartyWindows.awaitResponse(token);
        Bundle choice = new Bundle();
        choice.put("token", (int)token);
        PartySession.command("ui_close", choice);
        super.onBackPressed();
    }
}
