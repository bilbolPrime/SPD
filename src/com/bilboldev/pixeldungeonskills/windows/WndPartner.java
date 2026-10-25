package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import java.util.Locale;

public class WndPartner extends WndOptions {
    private static final PartySession.Ping[] PINGS = PartySession.Ping.values();

    public WndPartner(Hero partner) {
        super(Messages.capitalize(Messages.maybeTranslate(partner.heroClass.title())),
                partner.isAlive() ? text("health", partner.HP, partner.HT, partner.lvl) : text("fallen"),
                isDesktopRuntime() || landscapeLayout() ? 2 : 1,
                options());
    }

    private static String[] options() {
        String[] labels = new String[PINGS.length + (PartySession.network() ? 2 : 1)];
        labels[0] = text("go_to");
        for (int i = 0; i < PINGS.length; i++) {
            labels[i + 1] = text(PINGS[i].name().toLowerCase(Locale.ROOT));
        }
        if (PartySession.network()) labels[labels.length - 1] = text("leave");
        return labels;
    }

    @Override
    protected boolean fullWidthOption(int index) {
        return index == PINGS.length + 1
                || (index > 0 && index <= PINGS.length && PINGS[index - 1] == PartySession.Ping.ARE_YOU_THERE);
    }

    @Override
    protected void onSelect(int index) {
        if (index == 0) {
            PartySession.goToPartner();
        } else if (index <= PINGS.length) {
            PartySession.ping(PINGS[index - 1]);
        } else if (PartySession.network()) {
            GameScene.show(new WndOptions(text("leave"), text("leave_confirm"), text("leave"), text("stay")) {
                @Override
                protected void onSelect(int choice) {
                    if (choice == 0) PartySession.leave();
                }
            });
        }
    }

    private static String text(String key, Object... args) {
        return Messages.get(WndPartner.class, key, args);
    }
}
