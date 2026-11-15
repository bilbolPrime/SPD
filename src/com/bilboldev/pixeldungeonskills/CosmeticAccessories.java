package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;

public final class CosmeticAccessories {
    public static final int COUNT = 6;
    private static final String SELECTED = "selected_accessory";
    private static final String COMPANION = "selected_companion_accessory";

    private CosmeticAccessories() { }

    public static String name(int accessory) {
        return Messages.get("accessories.name_" + accessory);
    }

    public static boolean available() {
        return SupporterEntitlements.ownsAccessories();
    }

    public static int selected() {
        return preferred(SELECTED);
    }

    private static int preferred(String key) {
        return available() ? valid(Preferences.INSTANCE.getInt(key, 0)) : 0;
    }

    public static int valid(int accessory) {
        return accessory >= 0 && accessory <= COUNT ? accessory : 0;
    }

    public static int selection(Hero hero) {
        return hero == null ? selected() : valid(PartySession.accessory(current(hero)));
    }

    public static void choose(Hero hero, int accessory) {
        if (valid(accessory) != accessory || accessory != 0 && !available()) return;
        if (hero != null) hero = current(hero);
        if (PartySession.network() && hero != null && hero != PartySession.localHero()) return;
        if (hero != null) hero.accessory = accessory;
        String key = !PartySession.network() && hero != null && LocalCoop.active()
                && hero != LocalCoop.primary() ? COMPANION : SELECTED;
        Preferences.INSTANCE.put(key, accessory);
        if (hero != null) PartySession.accessoryChanged(hero);
    }

    private static Hero current(Hero hero) {
        if (!LocalCoop.active()) return hero;
        Hero first = LocalCoop.primary();
        Hero second = LocalCoop.partner(first);
        if (hero == first || hero == second) return hero;
        if (first != null && first.id() == hero.id()) return first;
        return second != null && second.id() == hero.id() ? second : hero;
    }

    public static void startRun() {
        Hero first = LocalCoop.primary();
        if (first == null) return;
        first.accessory = selected();
        Hero second = LocalCoop.partner(first);
        if (second != null) second.accessory = PartySession.network() ? 0 : preferred(COMPANION);
    }

    public static Image image(int accessory) {
        if (accessory < 1 || accessory > COUNT) throw new IllegalArgumentException("Unknown accessory");
        return new Image("heroes/accessories/icons.png", (accessory - 1) * 16, 0, 16, 16);
    }
}
