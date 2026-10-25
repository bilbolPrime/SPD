package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.SupporterEntitlements;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RatKing;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.RatKingSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.SteamPageButton;
import com.bilboldev.pixeldungeonskills.ui.SupporterIcon;
import com.bilboldev.pixeldungeonskills.ui.SupporterPreview;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public final class WndHeroAppearance extends WndScrollable {
    private static final String RUN_PHRASE = "SkillfulPixelDungeon";
    private final ControlRow content = new ControlRow();
    private final RedButton[] choices = new RedButton[5];
    private final SupporterIcon[] icons = new SupporterIcon[5];
    private final SupporterPreview preview;
    private final HighlightedText name = new HighlightedText(9);
    private final RedButton equip;
    private final RedButton storePage;
    private final RatKingButton ratKing;
    private String typed = "";
    private int selected;
    private int lastActive = -1;
    private boolean lastOwned;

    public WndHeroAppearance() {
        this(null);
    }

    public WndHeroAppearance(Hero hero) {
        int width = boundedWidth(landscapeLayout() ? 320 : 184);
        int bodyWidth = textWidth(width);
        boolean wide = landscapeLayout() && bodyWidth >= 280;
        selected = Math.max(1, SupporterEntitlements.selectedAura());
        Component heading = InspectionTitle.create(Icons.SUPPORTED.get(), text("appearance"));
        heading.setRect(0, 0, width, 0);
        String greeting = Game.isFreeDesktop() ? "aura_steam" : "aura_google_play";
        HighlightedText message = new HighlightedText(8);
        message.text(text(greeting), bodyWidth, false);
        content.add(message);
        float top = message.bottom() + 8;
        float choicesX = wide ? 100 : 0;
        float cell = (bodyWidth - choicesX - 16) / 5f;
        for (int i = 0; i < choices.length; i++) {
            final int style = i + 1;
            choices[i] = new RedButton("") {
                { content.addControl(hotArea); }
                @Override protected void onClick() {
                    selected = style;
                    preview.style(style);
                    refresh();
                }
                @Override protected void layout() {
                    super.layout();
                    if (icon != null) {
                        icon.x = x + (width - icon.width()) / 2;
                        icon.y = y + (height - icon.height()) / 2;
                    }
                }
            };
            icons[i] = new SupporterIcon(style);
            choices[i].icon(icons[i]);
            choices[i].setRect(choicesX + i * (cell + 4), top, cell, 28);
            content.add(choices[i]);
        }
        preview = new SupporterPreview(selected, hero);
        preview.setRect(0, wide ? top : top + 34, wide ? 96 : bodyWidth, 88);
        content.add(preview);
        name.setPos(choicesX, wide ? top + 40 : preview.bottom());
        content.add(name);
        name.text(text("aura_1"), bodyWidth, false);
        float bottom = Math.max(name.bottom() + 8, preview.bottom());
        ratKing = Game.isFreeDesktop() ? new RatKingButton((int)(bodyWidth - choicesX)) : null;
        if (ratKing != null) {
            ratKing.setPos(choicesX, name.bottom() + 4);
            content.add(ratKing);
            bottom = Math.max(bottom, ratKing.bottom());
        }
        content.setSize(bodyWidth, bottom);
        equip = new ActionButton("") {
            @Override protected void onClick() {
                if (SupporterEntitlements.ownsAura(selected)) {
                    SupporterEntitlements.selectAura(SupporterEntitlements.selectedAura() == selected ? 0 : selected);
                    refresh();
                }
            }
        };
        storePage = Game.isFreeDesktop() ? new SteamPageButton() : new ActionButton(text("open_google_play")) {
            @Override protected void onClick() {
                openUrl("https://play.google.com/store/apps/details?id=com.bilboldev.pixeldungeonskills"
                        + "&utm_source=android_client&utm_medium=internal&utm_campaign=public_version");
            }
        };
        equip.text(text("aura_activate"));
        RedButton action = storePage;
        action.setSize(width, 24);
        layoutContent(width, heading, content, 6, action);
        equip.setRect(action.left(), action.top(), action.width(), action.height());
        add(equip);
        refresh();
    }

    public boolean type(char character) {
        if (!Game.isFreeDesktop()) return false;
        if (character == '\b') {
            if (!typed.isEmpty()) typed = typed.substring(0, typed.length() - 1);
            return false;
        }
        if (character < ' ') return false;
        typed += character;
        if (typed.length() > RUN_PHRASE.length()) typed = typed.substring(1);
        if (RUN_PHRASE.equalsIgnoreCase(typed) && SupporterEntitlements.unlockRunAuras()) {
            typed = "";
            refresh();
            return true;
        }
        return false;
    }

    @Override protected ScrollPane createScrollPane(Component body) {
        return content.scrollPane(body);
    }

    @Override public void update() {
        super.update();
        if (lastActive != SupporterEntitlements.selectedAura() || lastOwned != SupporterEntitlements.ownsAura(selected)) refresh();
    }

    private void refresh() {
        lastActive = SupporterEntitlements.selectedAura();
        lastOwned = SupporterEntitlements.ownsAura(selected);
        name.text(text("aura_" + selected), (int)(content.width() - name.left()), false);
        name.setColor(SupporterIcon.color(selected, 0), SupporterIcon.color(selected, 0));
        String action = lastActive == selected ? "aura_deactivate" : "aura_activate";
        equip.text(text(action));
        equip.visible = equip.active = lastOwned;
        storePage.visible = storePage.active = !lastOwned;
        if (ratKing != null) ratKing.refresh();
        for (int i = 0; i < choices.length; i++) {
            icons[i].alpha(i + 1 == selected ? 1 : .55f);
        }
    }

    private static String text(String key) {
        return Messages.get("support." + key);
    }

    private final class RatKingButton extends Button {
        private RatKingSprite sprite;
        private HighlightedText label;

        RatKingButton(int width) {
            content.addControl(hotArea);
            label.text(text("aura_run_unlocked"), width - 40, false);
            setSize(width, Math.max(40, label.height() + 8));
            refresh();
        }

        @Override protected void createChildren() {
            super.createChildren();
            sprite = new RatKingSprite();
            sprite.scale.set(2);
            add(sprite);
            label = new HighlightedText(8);
            label.setColor(0xE6CB83, 0xE6CB83);
            add(label);
        }

        void refresh() {
            label.text(text(SupporterEntitlements.ownsAura(selected)
                    ? "aura_run_unlocked" : "aura_rat_king"), (int)width - 40, false);
            layout();
        }

        @Override protected void layout() {
            super.layout();
            sprite.x = PixelScene.align(x + (width - sprite.width() - 8 - label.width()) / 2);
            sprite.y = PixelScene.align(y + (height - sprite.height()) / 2);
            label.setPos(sprite.x + sprite.width() + 8, PixelScene.align(y + (height - label.height()) / 2));
        }

        @Override protected void onClick() {
            WndHeroAppearance.this.parent.add(new RatKingHint());
        }
    }

    public final class RatKingHint extends WndScrollable {
        private RatKingHint() {
            int width = boundedWidth(landscapeLayout() ? 224 : 184);
            Component heading = InspectionTitle.create(new RatKingSprite(), Utils.capitalize(Messages.get(RatKing.class, "name")));
            heading.setRect(0, 0, width, 0);
            HighlightedText message = new HighlightedText(8);
            message.text(text("aura_rat_king_hint"), textWidth(width), false);
            layoutContent(width, heading, message, 6);
        }

        public void type(char character) {
            if (WndHeroAppearance.this.type(character)) hide();
        }
    }
}
