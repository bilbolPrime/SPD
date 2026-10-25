/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.items.scrolls;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;

public abstract class InventoryScroll extends Scroll {
    protected Message inventoryTitle = Message.key("items.stones.stoneofintuition.inv_title");
    protected WndBag.Mode mode = WndBag.Mode.ALL;
    private Hero reader;
    private boolean identifiedByUse;
    private boolean finished;

    @Override
    protected void doRead() {
        InventoryScroll scroll = (InventoryScroll)curItem;
        scroll.reader = curUser;
        scroll.identifiedByUse = !isKnown();
        scroll.finished = false;
        if (scroll.identifiedByUse) setKnown();
        GameScene.selectItem(scroll.itemSelector, mode, inventoryTitle);
    }

    protected abstract void onItemSelected(Item item);

    protected boolean accepts(Item item) {
        return true;
    }

    protected Message itemWarning(Item item) {
        return null;
    }

    private final WndBag.Listener itemSelector = item -> {
        if (finished) return;
        LocalCoop.withHero(reader, () -> {
            curUser = reader;
            curItem = this;
            if (item == null) {
                if (identifiedByUse) confirmCancellation();
                else refund();
            } else if (!accepts(item)) {
                refund();
            } else {
                Message warning = identifiedByUse ? null : itemWarning(item);
                if (warning == null) applyTo(item);
                else confirmItem(item, warning);
            }
        });
    };

    private void confirmItem(Item item, Message warning) {
        WndOptions window = new WndOptions(new ItemSprite(item), Message.itemName(item), warning,
                Message.key(Scroll.class, "continue_reading"), Message.key(Scroll.class, "stop")) {
            private boolean answered;

            @Override
            protected void onSelect(int index) {
                if (answered) return;
                answered = true;
                LocalCoop.withHero(reader, () -> {
                    if (index == 0) applyTo(item);
                    else refund();
                });
            }

            @Override
            public void onBackPressed() {
                hide();
                onSelect(1);
            }
        };
        window.menuModel().image = item.image();
        GameScene.show(window);
    }

    private void applyTo(Item item) {
        if (finished) return;
        if (!reader.isAlive() || !(reader.belongings.backpack.contains(item) || item.isEquipped(reader))) {
            refund();
            return;
        }
        finished = true;
        curUser = reader;
        curItem = this;
        onItemSelected(item);
        readAnimation();
        Sample.INSTANCE.play(Assets.SND_READ);
        Invisibility.dispel();
    }

    private void refund() {
        if (finished) return;
        finished = true;
        if (!collect(reader.belongings.backpack)) Dungeon.level.drop(this, reader.pos).sprite.drop();
    }

    private void confirmCancellation() {
        GameScene.show(new WndOptions(new ItemSprite(this), Message.itemName(this),
                Message.key("custom.generated.entry_do_you_really_want_to_cancel_this_scroll_usage_i_f8be76afdd"),
                Message.key(Scroll.class, "stop"), Message.key(Scroll.class, "continue_reading")) {
            private boolean answered;

            @Override
            protected void onSelect(int index) {
                if (answered || finished) return;
                answered = true;
                LocalCoop.withHero(reader, () -> {
                    if (index == 0) {
                        finished = true;
                        reader.spendAndNext(TIME_TO_READ);
                    } else {
                        GameScene.selectItem(itemSelector, mode, inventoryTitle);
                    }
                });
            }

            @Override
            public void onBackPressed() { }
        });
    }
}
