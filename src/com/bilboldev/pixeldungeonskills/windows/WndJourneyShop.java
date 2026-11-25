package com.bilboldev.pixeldungeonskills.windows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.JourneyRun;
import com.bilboldev.pixeldungeonskills.JourneyShop;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.JourneyScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.RebornSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

public class WndJourneyShop extends WndScrollable {
    private final Hero hero = JourneyRun.selectedHero();
    private final boolean selling;
    private final ArrayList<ShopSlot> slots = new ArrayList<>();

    public WndJourneyShop() { this(false); }

    private WndJourneyShop(boolean selling) {
        this.selling = selling;
        int width = boundedWidth(landscapeLayout() ? 232 : 168);
        Component header = new Component();
        IconTitle title = new IconTitle(shopkeeperImage(), Messages.get("journey.shop"));
        title.setRect(0, 0, width, 0);
        header.add(title);

        Image portrait = LibraryCatalog.heroImage(hero.spritesheet(), hero.tier());
        portrait.x = 0;
        portrait.y = title.bottom() + 5;
        header.add(portrait);
        BitmapText gold = PixelScene.createText(Integer.toString(JourneyRun.gold()), 7);
        gold.hardlight(titleColor());
        gold.measure();
        gold.x = width - gold.width() - 2;
        gold.y = portrait.y + (portrait.height() - gold.height()) / 2;
        header.add(gold);
        Image coins = new ItemSprite(ItemSpriteSheet.GOLD, null);
        coins.scale.set(.65f);
        coins.x = gold.x - coins.width() - 3;
        coins.y = portrait.y + (portrait.height() - coins.height()) / 2;
        header.add(coins);
        HighlightedText name = new HighlightedText(7);
        name.text(Messages.capitalize(Message.heroClass(hero.heroClass).toString()),
                Math.max(1, (int)(coins.x - portrait.x - portrait.width() - 7)), false);
        name.setPos(portrait.x + portrait.width() + 4, portrait.y + (portrait.height() - name.height()) / 2);
        header.add(name);
        HighlightedText hint = new HighlightedText(6);
        hint.setColor(0xBDB6B0, titleColor());
        String shopHint = Messages.get("journey.shop_hint");
        if (JourneyRun.campDiscount() > 0)
            shopHint += "\n" + Messages.get("journey.shop_discount", JourneyRun.campDiscount());
        hint.text(shopHint, textWidth(width), false);
        hint.setPos(0, Math.max(portrait.y + portrait.height(), name.bottom()) + 5);
        header.add(hint);
        header.setSize(width, hint.bottom());

        Component grid = new Component();
        int columns = landscapeLayout() ? 6 : 4;
        int size = (width - SCROLL_GUTTER - (columns - 1) * 3) / columns;
        if (selling) {
            for (Item item : hero.belongings.backpack) {
                if (!(item instanceof Bag) && JourneyRun.salePrice(item, item.quantity()) > 0)
                    place(grid, item, null, columns, size);
            }
        } else {
            List<JourneyShop.Entry> stock = JourneyRun.shop().stock();
            int count = JourneyRun.shop().capacity();
            for (JourneyShop.Entry entry : stock) count = Math.max(count, entry.slot() + 1);
            JourneyShop.Entry[] offers = new JourneyShop.Entry[count];
            for (JourneyShop.Entry entry : stock) offers[entry.slot()] = entry;
            for (JourneyShop.Entry entry : offers)
                place(grid, entry == null ? null : entry.item(), entry, columns, size);
        }
        grid.setSize(width, ((slots.size() + columns - 1) / columns) * (size + 14));
        if (slots.isEmpty()) {
            HighlightedText empty = new HighlightedText(7);
            empty.text(Messages.get(selling ? "journey.sell_empty" : "journey.shop_empty"), textWidth(width), false);
            grid.add(empty);
            grid.setSize(width, empty.height() + 4);
        }

        IconActionButton buy = new IconActionButton(Messages.get("journey.buy")) {
            @Override protected void onClick() { WndJourneyShop.this.replace(false); }
        };
        buy.icon(new ItemSprite(ItemSpriteSheet.GOLD, null));
        IconActionButton sell = new IconActionButton(Messages.get("journey.sell")) {
            @Override protected void onClick() { WndJourneyShop.this.replace(true); }
        };
        sell.icon(Icons.BACKPACK.get());
        buy.setSize((width - 4) / 2f, 35);
        sell.setSize((width - 4) / 2f, 35);
        (selling ? sell : buy).textColor(titleColor());
        layoutContent(width, header, grid, 4, buy, sell);
    }

    public static Image shopkeeperImage() {
        Image image = new Image(Assets.KEEPER);
        image.frame(14, 0, 14, 14);
        return image;
    }

    @Override protected RebornChrome.Material rebornMaterial() { return RebornChrome.Material.IRON; }

    private void replace(boolean sell) {
        Group host = parent;
        hide();
        host.add(new WndJourneyShop(sell));
    }

    private void place(Component grid, Item item, JourneyShop.Entry entry, int columns, int size) {
        int index = slots.size();
        ShopSlot slot = new ShopSlot(item, entry);
        slot.setRect((index % columns) * (size + 3), (index / columns) * (size + 14), size, size + 11);
        grid.add(slot);
        slots.add(slot);
    }

    @Override protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override public void onClick(float x, float y) {
                for (ShopSlot slot : slots) {
                    if (slot.item != null && slot.inside(x, y)) {
                        WndJourneyShop.this.parent.add(new TradeItem(slot.item, slot.entry));
                        return;
                    }
                }
            }
        };
    }

    private class ShopSlot extends Component {
        final Item item;
        final JourneyShop.Entry entry;
        private final ItemSlot icon;
        private final ColorBlock background;
        private final BitmapText price;

        ShopSlot(Item item, JourneyShop.Entry entry) {
            this.item = item;
            this.entry = entry;
            background = RebornSlot.create(1, 1, entry != null && entry.held() ? 0xFF74603B : 0xFF4A4D44);
            add(background);
            if (item == null) {
                icon = null;
                price = null;
                return;
            }
            icon = new ItemSlot() {
                { Touchscreen.event.remove(hotArea); }
                @Override protected Hero viewer() { return hero; }
            };
            icon.item(item);
            add(icon);
            int amount = entry == null ? JourneyRun.salePrice(item, item.quantity())
                    : JourneyRun.purchasePrice(entry, hero);
            price = PixelScene.createText(Integer.toString(amount), 6);
            price.measure();
            price.hardlight(entry != null && amount > JourneyRun.gold() ? 0xAAAAAA : titleColor());
            add(price);
        }

        @Override protected void layout() {
            super.layout();
            if (background == null) return;
            background.x = x;
            background.y = y;
            background.size(width, height);
            if (icon == null) return;
            icon.setRect(x, y, width, height - 11);
            price.x = PixelScene.align(x + (width - price.width()) / 2);
            price.y = PixelScene.align(bottom() - price.height() - 2);
        }
    }

    private class TradeItem extends WndScrollable {
        TradeItem(Item item, JourneyShop.Entry entry) {
            int width = boundedWidth(170);
            IconTitle title = new IconTitle(new ItemSprite(item), Messages.capitalize(item.toString()));
            title.setRect(0, 0, width, 0);
            HighlightedText description = new HighlightedText(7);
            description.text(item.displayInfo(), textWidth(width));
            ArrayList<RedButton> actions = new ArrayList<>();
            if (entry != null) {
                int price = JourneyRun.purchasePrice(entry, hero);
                ActionButton buy = new ActionButton(Messages.get(WndTradeItem.class, "buy", price)) {
                    @Override protected void onClick() { trade(item, entry, item.quantity()); }
                };
                buy.enable(price <= JourneyRun.gold());
                actions.add(buy);
                actions.add(new ActionButton(Messages.get(entry.held() ? "journey.release" : "journey.hold")) {
                    @Override protected void onClick() { hold(entry); }
                });
            } else if (item.quantity() > 1) {
                actions.add(new ActionButton(Messages.get(WndTradeItem.class, "sell_1", JourneyRun.salePrice(item, 1))) {
                    @Override protected void onClick() { trade(item, null, 1); }
                });
                actions.add(new ActionButton(Messages.get(WndTradeItem.class, "sell_all", JourneyRun.salePrice(item, item.quantity()))) {
                    @Override protected void onClick() { trade(item, null, item.quantity()); }
                });
            } else {
                actions.add(new ActionButton(Messages.get(WndTradeItem.class, "sell", JourneyRun.salePrice(item, 1))) {
                    @Override protected void onClick() { trade(item, null, 1); }
                });
            }
            for (RedButton button : actions) button.setSize(width, 22);
            layoutContent(width, title, description, 5, actions.toArray(new RedButton[0]));
        }

        private void hold(JourneyShop.Entry entry) {
            try {
                if (!JourneyRun.hold(entry, !entry.held())) {
                    parent.add(new WndMessage(Messages.get("journey.trade_failed")));
                    return;
                }
                hide();
                WndJourneyShop.this.replace(false);
            } catch (IOException error) {
                parent.add(new WndMessage(Messages.get("journey.save_failed")));
            }
        }

        private void trade(Item item, JourneyShop.Entry entry, int amount) {
            try {
                boolean traded = entry == null ? JourneyRun.sell(item, amount, hero) : JourneyRun.buy(entry, hero);
                if (!traded) {
                    parent.add(new WndMessage(Messages.get(entry != null && JourneyRun.gold() >= JourneyRun.purchasePrice(entry, hero)
                            && JourneyRun.shop().stock().contains(entry) ? "journey.pack_full" : "journey.trade_failed")));
                    return;
                }
                hide();
                WndJourneyShop.this.replace(selling);
                if (Game.scene() instanceof JourneyScene) ((JourneyScene)Game.scene()).refreshCamp();
            } catch (IOException error) {
                parent.add(new WndMessage(Messages.get("journey.save_failed")));
            }
        }
    }
}
