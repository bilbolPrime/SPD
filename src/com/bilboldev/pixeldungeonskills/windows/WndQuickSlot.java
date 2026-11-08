package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartyWindows;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;

public class WndQuickSlot extends WndScrollable {
    private final Hero owner = Dungeon.hero;

    public WndQuickSlot(int slot) {
        menuModel = new MenuModel(slotName(slot), Message.empty());
        menuModel.view = MenuModel.View.QUICK_SLOT;
        menuModel.mode = slot;
        int width = boundedWidth(146);
        float top = heading(menuModel.title, width);
        IconActionButton skills = new IconActionButton(Messages.get("library.skill_tree")) {
            @Override protected void onClick() {
                if (PartyWindows.select(this)) return;
                hide();
                LocalCoop.withHero(owner, () -> GameScene.show(WndSkillsNew.quickslot(
                        skill -> LocalCoop.withHero(owner, () -> QuickSlot.bind(skill, slot)), 0)));
            }
        };
        skills.icon(Icons.SKILLS.get());
        IconActionButton inventory = new IconActionButton(Messages.get("windows.wndcontrols.action_inventory")) {
            @Override protected void onClick() {
                if (PartyWindows.select(this)) return;
                hide();
                LocalCoop.withHero(owner, () -> GameScene.selectItem(
                        item -> LocalCoop.withHero(owner, () -> QuickSlot.bind(item, slot)), WndBag.Mode.QUICKSLOT,
                        Message.key("custom.generated.entry_select_an_item_for_the_quickslot_7c094feb25")));
            }
        };
        inventory.icon(Icons.BACKPACK.get());
        float buttonWidth = (width - 4) / 2f;
        skills.setRect(0, top, buttonWidth, 42);
        inventory.setRect(buttonWidth + 4, top, buttonWidth, 42);
        float height = Math.max(42, Math.max(skills.reqHeight(), inventory.reqHeight()));
        skills.setSize(buttonWidth, height);
        inventory.setSize(buttonWidth, height);
        add(skills);
        add(inventory);
        menuModel.add(skills, Message.key("library.skill_tree"));
        menuModel.add(inventory, Message.key("windows.wndcontrols.action_inventory"));
        resize(width, (int)Math.ceil(top + height));
    }

    public WndQuickSlot(Item item) { this(item, null); }

    public WndQuickSlot(Skill skill) { this(null, skill); }

    private WndQuickSlot(Item item, Skill skill) {
        menuModel = new MenuModel(Message.key("windows.wndquickslot.title"), Message.empty());
        menuModel.view = MenuModel.View.QUICK_BIND;
        menuModel.item = item;
        menuModel.skill = skill;
        menuModel.selection = skill != null;
        int width = boundedWidth(146);
        float top = heading(menuModel.title, width);
        int count = QuickSlot.visibleSlots();
        int columns = Math.min(count, Math.max(1, (width + 4) / 30));
        for (int slot = 0; slot < count; slot++) {
            final int selected = slot;
            RedButton button = new RedButton("") {
                private final BitmapText number = PixelScene.createText(Integer.toString(selected + 1), 6);
                {
                    number.measure();
                    number.hardlight(titleColor());
                    add(number);
                }

                @Override protected void layout() {
                    super.layout();
                    if (number == null) return;
                    number.x = right() - number.width() - 2;
                    number.y = top() + 1;
                    if (icon != null) {
                        icon.x = PixelScene.align(x + (width - icon.width()) / 2);
                        icon.y = PixelScene.align(y + (height - icon.height()) / 2 + 2);
                    }
                }

                @Override protected void onClick() {
                    if (PartyWindows.select(this)) return;
                    hide();
                    LocalCoop.withHero(owner, () -> {
                        if (skill != null) QuickSlot.bind(skill, selected);
                        else QuickSlot.bind(item, selected);
                    });
                }
            };
            button.icon(slotImage(slot));
            int rowCount = Math.min(columns, count - slot / columns * columns);
            button.setRect((width - rowCount * 30 + 4) / 2f + slot % columns * 30,
                    top + slot / columns * 30, 26, 26);
            add(button);
            menuModel.add(button, slotName(slot));
        }
        top += ((count + columns - 1) / columns) * 30;
        if (skill != null) {
            RedButton sticky = new ActionButton(Messages.get("skills4.sticky")) {
                @Override protected void onClick() {
                    if (PartyWindows.select(this)) return;
                    hide();
                    LocalCoop.withHero(owner, () -> QuickSlot.bind(skill, -1));
                }
            };
            sticky.setRect(0, top, width, 22);
            sticky.setSize(width, Math.max(22, sticky.reqHeight()));
            add(sticky);
            menuModel.add(sticky, Message.key("skills4.sticky"));
            top = sticky.bottom();
        }
        resize(width, (int)Math.ceil(top));
    }

    private float heading(Message title, int width) {
        RenderedTextMultiline text = PixelScene.renderMultiline(title.toString(), 9);
        text.maxWidth(width);
        text.hardlight(titleColor());
        text.setPos((width - text.width()) / 2, 0);
        add(text);
        return text.bottom() + 6;
    }

    private static Message slotName(int slot) {
        return Message.key("windows.wndcontrols.action_quickslot_" + (slot + 1));
    }

    private Image slotImage(int slot) {
        Object value = QuickSlot.values[slot];
        if (value instanceof Integer) {
            Skill[] skills = LocalCoop.skills(owner);
            int ref = (Integer)value;
            return ref >= 0 && ref < skills.length ? new SkillSprite(skills[ref].image()) : null;
        }
        if (value instanceof Class) value = owner.belongings.getItem((Class<? extends Item>)value);
        return value instanceof Item ? new ItemSprite((Item)value) : null;
    }
}
