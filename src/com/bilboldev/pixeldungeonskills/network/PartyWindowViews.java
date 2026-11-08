package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.skills.CurrentSkills;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndItem;
import com.bilboldev.pixeldungeonskills.windows.WndSkill;
import com.bilboldev.pixeldungeonskills.windows.WndSkillNew;
import com.bilboldev.pixeldungeonskills.windows.WndSkills;
import com.bilboldev.pixeldungeonskills.windows.WndSkillsNew;
import com.bilboldev.pixeldungeonskills.windows.WndQuickSlot;
import com.bilboldev.pixeldungeonskills.windows.WndStorage;
import com.bilboldev.utils.Bundle;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import static com.bilboldev.pixeldungeonskills.network.PartyMessage.*;

final class PartyWindowViews {
    private static final int MERCENARIES = 128;

    private PartyWindowViews() { }

    static boolean describe(MenuModel menu, Bundle message) {
        if (menu.view == MenuModel.View.OPTIONS) return false;
        int ref = 0;
        if (menu.item != null) {
            ref = menu.item instanceof Gold ? -1 : items().indexOf(menu.item);
            if (ref < 0 && !(menu.item instanceof Gold)) return false;
        } else if (menu.skill != null) {
            ref = skillIndex(menu.skill);
            if (ref < 0) return false;
        }
        message.put("view", menu.view.ordinal());
        switch (menu.view) {
            case SKILLS:
            case QUICK_SKILLS:
            case QUICK_SLOT:
            case STORAGE:
                message.put("mode", menu.mode);
                break;
            case BAG:
                message.put("mode", menu.mode);

            case ITEM:
            case SKILL:
            case OLD_SKILL:
            case QUICK_BIND:
                message.put("ref", ref);
                break;
            default:
                break;
        }
        message.put("selection", menu.selection);
        if (menu.prompt != null) message.put("prompt", PartyText.encode(menu.prompt));
        return true;
    }

    static Window create(Bundle message) {
        MenuModel.View view = MenuModel.View.values()[message.getInt("view")];
        boolean selection = message.getBoolean("selection");
        Message prompt = message.contains("prompt") ? PartyText.decoded(message.getBundle("prompt")) : null;
        int ref = message.getInt("ref");
        switch (view) {
            case STORAGE:
                return new WndStorage(Dungeon.hero.storage, selection ? ignored -> { } : null,
                        WndStorage.Mode.values()[message.getInt("mode")], prompt);
            case BAG:
                Item bag = item(ref);
                if (!(bag instanceof Bag)) throw new IllegalArgumentException("Missing party bag");
                return new WndBag((Bag)bag, selection ? ignored -> { } : null,
                        WndBag.Mode.values()[message.getInt("mode")], prompt);
            case ITEM:
                return WndItem.forParty(item(ref), selection);
            case SKILLS:
                return new WndSkillsNew(selection ? ignored -> { } : null, prompt, message.getInt("mode"));
            case QUICK_SKILLS:
                return WndSkillsNew.quickslot(ignored -> { }, message.getInt("mode"));
            case QUICK_SLOT:
                return new WndQuickSlot(message.getInt("mode"));
            case QUICK_BIND:
                return selection ? new WndQuickSlot(skill(ref)) : new WndQuickSlot(item(ref));
            case OLD_SKILLS:
                return new WndSkills(selection ? ignored -> { } : null, prompt);
            case SKILL:
                return WndSkillNew.forParty(skill(ref), selection);
            case OLD_SKILL:
                return selection ? new WndSkill(skill(ref)) : new WndSkill(null, skill(ref));
            default:
                throw new IllegalArgumentException("Unknown party window");
        }
    }

    static void validate(JSONObject message) throws IOException, JSONException {
        int view = integer(message, "view", 1, MenuModel.View.values().length - 1);
        switch (MenuModel.View.values()[view]) {
            case SKILLS:
            case QUICK_SKILLS:
                fields(message, "token", "revision", "view", "mode", "selection", "?prompt");
                integer(message, "mode", 0, 2);
                break;
            case QUICK_SLOT:
                fields(message, "token", "revision", "view", "mode", "selection");
                integer(message, "mode", 0, QuickSlot.MAX_SLOTS - 1);
                break;
            case QUICK_BIND:
                fields(message, "token", "revision", "view", "ref", "selection");
                integer(message, "ref", 0, message.getBoolean("selection") ? MERCENARIES : 512);
                break;
            case BAG:
                fields(message, "token", "revision", "view", "ref", "mode", "selection", "?prompt");
                integer(message, "ref", 0, 512);
                integer(message, "mode", 0, WndBag.Mode.values().length - 1);
                break;
            case STORAGE:
                fields(message, "token", "revision", "view", "mode", "selection", "?prompt");
                integer(message, "mode", 0, WndStorage.Mode.values().length - 1);
                break;
            case ITEM:
                fields(message, "token", "revision", "view", "ref", "selection");
                integer(message, "ref", -1, 512);
                break;
            case SKILL:
            case OLD_SKILL:
                fields(message, "token", "revision", "view", "ref", "selection");
                integer(message, "ref", 0, MERCENARIES);
                break;
            default:
                fields(message, "token", "revision", "view", "selection", "?prompt");
                break;
        }
        integer(message, "revision", 1, Integer.MAX_VALUE);
        bool(message, "selection");
        if (message.has("prompt")) PartyText.validate(object(message, "prompt"), 512);
    }

    private static ArrayList<Item> items() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(Dungeon.hero.belongings.backpack);
        items.add(Dungeon.hero.belongings.bow);
        for (Item item : Dungeon.hero.belongings) items.add(item);
        return items;
    }

    private static Item item(int ref) {
        if (ref == -1) return new Gold(Dungeon.gold);
        ArrayList<Item> items = items();
        if (ref < 0 || ref >= items.size() || items.get(ref) == null)
            throw new IllegalArgumentException("Missing party item");
        return items.get(ref);
    }

    private static int skillIndex(Skill skill) {
        if (skill == CurrentSkills.mercMenu) return MERCENARIES;
        Skill[] skills = LocalCoop.skills(Dungeon.hero);
        for (int i = 0; i < skills.length; i++) if (skills[i] == skill) return i;
        return -1;
    }

    private static Skill skill(int ref) {
        if (ref == MERCENARIES) return CurrentSkills.mercMenu;
        Skill[] skills = LocalCoop.skills(Dungeon.hero);
        if (ref < 0 || ref >= skills.length) throw new IllegalArgumentException("Missing party skill");
        return skills[ref];
    }
}
