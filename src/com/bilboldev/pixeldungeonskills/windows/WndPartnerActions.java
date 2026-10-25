package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;

public class WndPartnerActions extends WndOptions {
    private final Hero hero;

    public WndPartnerActions() {
        super(Messages.capitalize(LocalCoop.partner(Dungeon.hero).className()), text("follow_info"),
                PartySession.network()
                        ? new String[]{text("follow"), text("swap"), text("ask_follow")}
                        : new String[]{text("follow"), text("swap")});
        hero = Dungeon.hero;
    }

    @Override
    protected void onSelect(int index) {

        Hero current = PartySession.network() ? PartySession.localHero() : hero;
        if (index == 0) PartySession.followPartner(current);
        else if (index == 1) {
            if (!PartySession.input(current, "swap", 0)) LocalCoop.withHero(current, () -> LocalCoop.swapPlaces(current));
        } else if (index == 2) PartySession.askToFollow();
    }

    private static String text(String key) {
        return Messages.get("windows.wndpartneractions." + key);
    }
}
