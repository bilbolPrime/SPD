package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.HeroSkins;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.SupporterEntitlements;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.SteamPageButton;
import java.util.function.IntConsumer;

public final class WndHeroSkin extends WndScrollable {
    private final ControlRow content = new ControlRow();
    private final RedButton[] choices = new RedButton[2];
    private final IntConsumer chosen;
    private boolean finished;

    public WndHeroSkin(HeroClass hero, IntConsumer chosen) {
        this.chosen = chosen;
        int width = boundedWidth(210);
        int bodyWidth = textWidth(width);
        Component heading = InspectionTitle.create(LibraryCatalog.heroImage(hero.spritesheet(), 1),
                Messages.capitalize(Messages.maybeTranslate(hero.title())));
        heading.setRect(0, 0, width, 0);
        HighlightedText message = new HighlightedText(8);
        message.text(Messages.get("support.skin_choose"), bodyWidth, false);
        content.add(message);
        float cell = (bodyWidth - 6) / 2f;
        for (int i = 0; i < choices.length; i++) {
            final int skin = i;
            Image sprite = LibraryCatalog.heroImage(HeroSkins.sheet(hero, skin), 3);
            sprite.scale.set(3);
            choices[i] = new RedButton("") {
                { content.addControl(hotArea); }
                @Override protected void onClick() {
                    if (skin != 0 && !SupporterEntitlements.ownsSkins()) {
                        WndHeroSkin.this.parent.add(new LockedSkin(hero));
                    } else {
                        choose(skin);
                    }
                }
            };
            choices[i].setRect(i * (cell + 6), message.bottom() + 8, cell, 58);
            sprite.x = choices[i].left() + (cell - sprite.width()) / 2;
            sprite.y = choices[i].top() + 6;
            if (skin != 0 && !SupporterEntitlements.ownsSkins()) sprite.alpha(.55f);
            choices[i].add(sprite);
            content.add(choices[i]);
        }
        content.setSize(bodyWidth, choices[0].bottom());
        layoutContent(width, heading, content, 6);
    }

    @Override public void onBackPressed() {
        choose(0);
    }

    private void choose(int skin) {
        if (finished) return;
        finished = true;
        hide();
        chosen.accept(skin);
    }

    @Override protected ScrollPane createScrollPane(Component body) { return content.scrollPane(body); }

    private static final class LockedSkin extends WndScrollable {
        LockedSkin(HeroClass hero) {
            int width = boundedWidth(184);
            Component heading = InspectionTitle.create(LibraryCatalog.heroImage(HeroSkins.sheet(hero, 1), 3),
                    Messages.capitalize(Messages.maybeTranslate(hero.title())));
            heading.setRect(0, 0, width, 0);
            boolean steam = Game.isFreeDesktop();
            HighlightedText message = new HighlightedText(8);
            message.text(Messages.get(steam ? "support.skin_steam" : "support.skin_supporter"), textWidth(width), false);
            if (steam) {
                SteamPageButton buy = new SteamPageButton();
                buy.setSize(width, 26);
                layoutContent(width, heading, message, 6, buy);
            } else {
                RedButton store = new ActionButton(Messages.get("support.open_google_play")) {
                    @Override protected void onClick() {
                        openUrl("https://play.google.com/store/apps/details?id=com.bilboldev.pixeldungeonskills");
                    }
                };
                store.setSize(width, 26);
                layoutContent(width, heading, message, 6, store);
            }
        }
    }
}
