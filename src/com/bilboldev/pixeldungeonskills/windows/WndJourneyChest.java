package com.bilboldev.pixeldungeonskills.windows;

import java.io.IOException;
import java.util.ArrayList;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.JourneyRun;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.EquipableItem;
import com.bilboldev.pixeldungeonskills.items.KindOfWeapon;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.MissileWeapon;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.JourneyScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.RebornSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

public class WndJourneyChest extends WndScrollable {
    private final Hero hero = JourneyRun.selectedHero();
    private final boolean chestView;
    private final Bag bag;
    private final ArrayList<CampSlot> slots = new ArrayList<>();

    public WndJourneyChest(boolean chestView) {
        this(chestView, chestView ? JourneyRun.chest() : JourneyRun.selectedHero().belongings.backpack);
    }

    private WndJourneyChest(boolean chestView, Bag bag) {
        this.chestView = chestView;
        this.bag = bag;
        int width = boundedWidth(landscapeLayout() ? 232 : 168);
        Component header = new Component();
        IconTitle title = new IconTitle(chestView ? new ItemSprite(ItemSpriteSheet.CHEST, null)
                : Icons.get(hero.heroClass), chestView ? Messages.get("journey.chest")
                : Messages.capitalize(Message.heroClass(hero.heroClass).toString()));
        title.setRect(0, 0, width, 0);
        header.add(title);
        HighlightedText hint = new HighlightedText(6);
        hint.setColor(0xBDB6B0, 0xD9C18E);
        hint.text(Messages.get("journey.chest_hint"), width - 4, false);
        hint.setPos(0, title.bottom() + 5);
        header.add(hint);
        header.setSize(width, hint.bottom());
        if (!chestView) {
            ArrayList<Bag> bags = WndBag.carriedBags(hero);
            float tabWidth = (width - (bags.size() - 1) * 2) / (float)bags.size();
            float top = header.bottom() + 5;
            for (int i = 0; i < bags.size(); i++) {
                Bag target = bags.get(i);
                RedButton tab = new RedButton("") {
                    @Override protected void onClick() { WndJourneyChest.this.replace(false, target); }
                    @Override protected void layout() {
                        super.layout();
                        if (icon != null) {
                            icon.x = x + (width - icon.width()) / 2;
                            icon.y = y + (height - icon.height()) / 2;
                        }
                    }
                };
                Image icon = WndBag.bagIcon(target);
                icon.alpha(target == bag ? 1 : .6f);
                tab.icon(icon);
                tab.enable(target != bag);
                tab.setRect(i * (tabWidth + 2), top, tabWidth, 24);
                header.add(tab);
            }
            header.setSize(width, top + 24);
        }

        Component grid = new Component();
        grid.setSize(width, 0);
        int columns = landscapeLayout() ? 6 : 4;
        int size = Math.min(32, (width - (columns - 1) * 2) / columns);
        int index = 0;
        if (!chestView && bag == hero.belongings.backpack) {
            Item[] equipped = {hero.belongings.weapon, hero.belongings.armor,
                    hero.belongings.ring1, hero.belongings.ring2, hero.belongings.bow};
            int[] empty = {ItemSpriteSheet.WEAPON, ItemSpriteSheet.ARMOR,
                    ItemSpriteSheet.RING, ItemSpriteSheet.RING, ItemSpriteSheet.EMPTY_BOW};
            for (int i = 0; i < equipped.length; i++) {
                place(grid, equipped[i], true, empty[i], index++, columns, size);
            }
            index = ((index + columns - 1) / columns) * columns;
        }
        int start = index;
        for (Item item : bag.items) {
            if (!chestView && bag == hero.belongings.backpack && Bag.isHolder(item)) continue;
            place(grid, item, false, -1, index++, columns, size);
        }
        while (index - start < bag.size) place(grid, null, false, -1, index++, columns, size);
        grid.setSize(width, ((index + columns - 1) / columns) * (size + 2));

        IconActionButton inventory = new IconActionButton(Messages.get("journey.inventory")) {
            @Override protected void onClick() { WndJourneyChest.this.replace(false, hero.belongings.backpack); }
        };
        inventory.icon(Icons.BACKPACK.get());
        IconActionButton chest = new IconActionButton(Messages.get("journey.chest")) {
            @Override protected void onClick() { WndJourneyChest.this.replace(true, JourneyRun.chest()); }
        };
        chest.icon(new ItemSprite(ItemSpriteSheet.CHEST, null));
        inventory.setSize((width - 4) / 2f, 35);
        chest.setSize((width - 4) / 2f, 35);
        (chestView ? chest : inventory).textColor(titleColor());
        if (chestView) {
            layoutContent(width, header, grid, 4, inventory, chest);
        } else {
            int top = (int)Math.ceil(header.bottom()) + 4;
            int buttonHeight = (int)Math.ceil(Math.max(35, Math.max(inventory.reqHeight(), chest.reqHeight())));
            int fixedHeight = top + buttonHeight + 4;
            camera.zoom = Math.min(camera.zoom, Game.height * MAX_SCREEN_FRACTION / (fixedHeight + 16 + frameHeight()));
            int equipmentSlots = ((5 + columns - 1) / columns) * columns;
            int rows = (equipmentSlots + hero.belongings.backpack.size + columns - 1) / columns;
            int bodyHeight = Math.min(rows * (size + 2), Math.max(1, maxFrameHeight() - frameHeight() - fixedHeight));
            add(header);
            ScrollPane body = createScrollPane(grid);
            add(body);
            add(inventory);
            add(chest);
            resize(width, fixedHeight + bodyHeight);
            body.setRect(0, top, width, bodyHeight);
            inventory.setRect(0, top + bodyHeight + 4, (width - 4) / 2f, buttonHeight);
            chest.setRect((width + 4) / 2f, top + bodyHeight + 4, (width - 4) / 2f, buttonHeight);
        }
    }

    @Override protected RebornChrome.Material rebornMaterial() { return RebornChrome.Material.IRON; }

    private void replace(boolean chest, Bag target) {
        Group host = parent;
        hide();
        host.add(new WndJourneyChest(chest, target));
    }

    private void place(Component grid, Item item, boolean equipped, int placeholder, int index, int columns, int size) {
        CampSlot slot = new CampSlot(item, equipped, placeholder);
        float left = (grid.width() - columns * (size + 2) + 2) / 2;
        slot.setRect(left + (index % columns) * (size + 2), (index / columns) * (size + 2), size, size);
        grid.add(slot);
        slots.add(slot);
    }

    @Override protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override public void onClick(float x, float y) {
                for (CampSlot slot : slots) {
                    if (slot.inside(x, y) && slot.item != null) {
                        inspect(slot.item, slot.equipped);
                        return;
                    }
                }
            }
        };
    }

    private void inspect(Item item, boolean equipped) {
        if (item instanceof Bag) {
            replace(chestView, (Bag)item);
            return;
        }
        parent.add(new CampItem(item, equipped));
    }

    private class CampSlot extends ItemSlot {
        final Item item;
        final boolean equipped;
        private final ColorBlock background;
        private final Image placeholder;

        CampSlot(Item item, boolean equipped, int emptyImage) {
            this.item = item;
            this.equipped = equipped;
            background = RebornSlot.create(1, 1, equipped ? 0xFF646252 : 0xFF4A4D44);
            addToBack(background);
            placeholder = item == null && emptyImage >= 0 ? new ItemSprite(emptyImage, null) : null;
            if (placeholder != null) {
                placeholder.alpha(.25f);
                add(placeholder);
            }
            item(item);

            com.bilboldev.input.Touchscreen.event.remove(hotArea);
        }

        @Override protected Hero viewer() { return hero; }

        @Override protected void layout() {
            super.layout();
            if (background == null) return;
            background.x = x;
            background.y = y;
            background.size(width, height);
            if (placeholder != null) {
                placeholder.x = x + (width - placeholder.width()) / 2;
                placeholder.y = y + (height - placeholder.height()) / 2;
            }
        }
    }

    private class CampItem extends WndScrollable {
        private final Item item;
        private final boolean equipped;

        CampItem(final Item item, boolean equipped) {
            this.item = item;
            this.equipped = equipped;
            int width = boundedWidth(170);
            IconTitle title = new IconTitle(new ItemSprite(item), Messages.capitalize(item.toString()));
            title.setRect(0, 0, width, 0);
            HighlightedText description = new HighlightedText(7);
            description.text(item.displayInfo(), textWidth(width));
            ArrayList<ActionButton> buttons = new ArrayList<>();
            String action = equipped ? EquipableItem.AC_UNEQUIP : EquipableItem.AC_EQUIP;
            if (!chestView && item instanceof EquipableItem && item.actions(hero).contains(action)) {
                ActionButton equipment = new ActionButton(item.actionName(action, hero)) {
                    @Override protected void onClick() {
                        if (!equipped && item.cursed && item.cursedKnown && !(item instanceof Bow)) {
                            parent.add(new WndOptions(Message.itemName(item),
                                    Message.key(EquipableItem.class, "cursed_confirm"),
                                    Message.key(EquipableItem.class, "equip_anyway"), Message.key(Item.class, "cancel")) {
                                @Override protected void onSelect(int index) { if (index == 0) chooseEquipment(); }
                            });
                        } else chooseEquipment();
                    }
                };
                equipment.setSize(width, 22);
                buttons.add(equipment);
            }
            if (!equipped) {
                ActionButton transfer = new ActionButton(Messages.get(chestView ? "journey.take" : "journey.store")) {
                    @Override protected void onClick() {
                        try {
                            boolean moved = chestView ? JourneyRun.withdraw(item) : JourneyRun.deposit(item);
                            if (moved) {
                                refresh();
                            } else {
                                parent.add(new WndMessage(Messages.get(chestView ? "journey.pack_full" : "journey.chest_full")));
                            }
                        } catch (IOException error) {
                            parent.add(new WndMessage(Messages.get("journey.save_failed")));
                        }
                    }
                };
                transfer.setSize(width, 22);
                buttons.add(transfer);
            }
            layoutContent(width, title, description, 5, buttons.toArray(new ActionButton[0]));
        }

        private void chooseEquipment() {
            if (!equipped && item instanceof MissileWeapon && !(item instanceof Bow)) {
                parent.add(new WndOptions(Message.key("custom.generated.entry_missile_weapon_1dc9f454b2"),
                        Message.key("custom.generated.entry_do_you_really_want_to_equip_it_as_a_melee_weapon_08f7984b51"),
                        Message.key("items.artifacts.chaliceofblood.yes"), Message.key("items.artifacts.chaliceofblood.no")) {
                    @Override protected void onSelect(int index) { if (index == 0) changeEquipment(null); }
                });
            } else if (!equipped && item instanceof Ring && hero.belongings.ring1 != null && hero.belongings.ring2 != null) {
                parent.add(new WndOptions(Message.key("custom.generated.entry_unequip_one_ring_a481cb6eb2"),
                        Message.key("custom.generated.entry_you_can_only_wear_two_rings_at_a_time_unequip_on_53bbc9ea4c"),
                        Message.capitalize(Message.item(hero.belongings.ring1)),
                        Message.capitalize(Message.item(hero.belongings.ring2))) {
                    @Override protected void onSelect(int index) {
                        changeEquipment(index == 0 ? hero.belongings.ring1 : hero.belongings.ring2);
                    }
                });
            } else changeEquipment(null);
        }

        private void changeEquipment(Ring ring) {
            Item previous = equipped ? item : item instanceof Bow ? hero.belongings.bow
                    : item instanceof KindOfWeapon ? hero.belongings.weapon
                    : item instanceof Armor ? hero.belongings.armor : ring;
            if (previous != null && previous.cursed && !(previous instanceof Bow)) {
                parent.add(new WndMessage(Message.key(EquipableItem.class, "unequip_cursed")));
                return;
            }
            if (!equipped && item instanceof IceWeapon && ((IceWeapon)item).heroClass() != hero.heroClass) {
                parent.add(new WndMessage(Message.key("items.weapon.ice.wrong_hero")));
                return;
            }
            try {
                if (JourneyRun.changeEquipment(item, !equipped, ring)) refresh();
                else parent.add(new WndMessage(Messages.get("journey.pack_full")));
            } catch (IOException error) {
                parent.add(new WndMessage(Messages.get("journey.save_failed")));
            }
        }

        private void refresh() {
            Group host = WndJourneyChest.this.parent;
            hide();
            WndJourneyChest.this.hide();
            host.add(new WndJourneyChest(chestView, bag));
            if (Game.scene() instanceof JourneyScene) ((JourneyScene)Game.scene()).refreshCamp();
        }
    }
}
