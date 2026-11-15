package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.CosmeticAccessories;
import com.bilboldev.pixeldungeonskills.CosmeticPets;
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
    private final Choice[] choices = new Choice[5];
    private final Choice[] accessoryChoices = new Choice[CosmeticAccessories.COUNT];
    private final Choice[] petChoices = new Choice[CosmeticPets.COUNT];
    private final Choice noAura, noAccessory, noPet;
    private final Hero hero;
    private final SupporterPreview preview;
    private final HighlightedText name;
    private final HighlightedText accessoryName;
    private final HighlightedText petDescription;
    private final RedButton support;
    private final RatKingButton ratKing;
    private final int choiceWidth;
    private String typed = "";
    private int selected, selectedAccessory, selectedPet;
    private int lastActive = -1, lastAccessory = -1, lastPet = -1;
    private boolean lastOwned, lastAccessoriesOwned, lastPetsOwned;

    public WndHeroAppearance() {
        this(null);
    }

    public WndHeroAppearance(Hero hero) {
        this.hero = hero;
        int width = boundedWidth(landscapeLayout() ? 320 : 184);
        int bodyWidth = textWidth(width);
        boolean wide = landscapeLayout() && bodyWidth >= 280;
        selected = SupporterEntitlements.ownsAura(1) ? SupporterEntitlements.selectedAura() : 1;
        selectedPet = SupporterEntitlements.ownsPets() ? CosmeticPets.selected() : 1;
        selectedAccessory = CosmeticAccessories.available() ? CosmeticAccessories.selection(hero) : 1;
        Component heading = new IconTitle(Icons.SUPPORT.get(), text("appearance"));
        heading.setRect(0, 0, width, 0);
        String greeting = Game.isFreeDesktop() ? "aura_steam" : "aura_google_play";
        HighlightedText message = paragraph(text(greeting), 0, 0, bodyWidth);
        float top = message.bottom() + 6;
        preview = new SupporterPreview(selected, hero, hero == null ? 1 : hero.skin, selectedPet, selectedAccessory);
        preview.setRect(0, top, wide ? 100 : bodyWidth, 84);
        content.add(preview);
        float left = wide ? 108 : 0;
        choiceWidth = (int)(bodyWidth - left);
        float choicesTop = wide ? top : preview.bottom() + 2;
        HighlightedText auras = paragraph("_" + text("auras") + "_", left, choicesTop, choiceWidth);
        float cell = (choiceWidth - 15) / 6f;
        noAura = new Choice(Icons.CLOSE.get(), () -> chooseAura(0));
        noAura.setRect(left, auras.bottom() + 4, cell, 28);
        for (int i = 0; i < choices.length; i++) {
            final int style = i + 1;
            choices[i] = new Choice(new SupporterIcon(style), () -> chooseAura(style));
            choices[i].setRect(left + (i + 1) * (cell + 3), noAura.top(), cell, 28);
        }
        name = paragraph("", left, noAura.bottom() + 4, choiceWidth);
        float nameHeight = 0;
        for (int i = 0; i <= choices.length; i++) {
            name.text(i == 0 ? Messages.get("library.none") : text("aura_" + i), choiceWidth, false);
            nameHeight = Math.max(nameHeight, name.height());
        }
        HighlightedText headwear = paragraph("_" + text("accessories") + "_", left,
                name.top() + nameHeight + 8, choiceWidth);
        cell = (choiceWidth - 18) / 7f;
        noAccessory = new Choice(Icons.CLOSE.get(), () -> chooseAccessory(0));
        noAccessory.setRect(left, headwear.bottom() + 4, cell, 28);
        for (int i = 0; i < accessoryChoices.length; i++) {
            final int accessory = i + 1;
            accessoryChoices[i] = new Choice(CosmeticAccessories.image(accessory), () -> chooseAccessory(accessory));
            accessoryChoices[i].setRect(left + (i + 1) * (cell + 3), noAccessory.top(), cell, 28);
        }
        accessoryName = paragraph("", left, noAccessory.bottom() + 4, choiceWidth);
        float accessoryHeight = 0;
        for (int accessory = 0; accessory <= CosmeticAccessories.COUNT; accessory++) {
            accessoryName.text(accessory == 0 ? Messages.get("library.none") : CosmeticAccessories.name(accessory), choiceWidth, false);
            accessoryHeight = Math.max(accessoryHeight, accessoryName.height());
        }
        HighlightedText pets = paragraph("_" + Messages.get("library.pets") + "_", left,
                accessoryName.top() + accessoryHeight + 8, choiceWidth);
        noPet = new Choice(Icons.CLOSE.get(), () -> choosePet(0));
        noPet.setRect(left, pets.bottom() + 4, cell, 28);
        for (int i = 0; i < petChoices.length; i++) {
            final int pet = i + 1;
            Image sprite = CosmeticPets.image(pet);
            sprite.scale.set(sprite.scale.x * 2);
            petChoices[i] = new Choice(sprite, () -> choosePet(pet));
            petChoices[i].setRect(left + (i + 1) * (cell + 3), noPet.top(), cell, 28);
        }
        petDescription = paragraph("", left, noPet.bottom() + 5, choiceWidth);
        float descriptionHeight = 0;
        for (int pet = 0; pet <= CosmeticPets.COUNT; pet++) {
            petDescription.text(petText(pet), choiceWidth, false);
            descriptionHeight = Math.max(descriptionHeight, petDescription.height());
        }
        float bottom = Math.max(preview.bottom(), petDescription.top() + descriptionHeight);
        ratKing = Game.isFreeDesktop() ? new RatKingButton(bodyWidth) : null;
        if (ratKing != null) {
            ratKing.setPos(0, bottom + 6);
            content.add(ratKing);
            bottom = ratKing.bottom();
        }
        content.setSize(bodyWidth, bottom);
        support = Game.isFreeDesktop() ? new SteamPageButton() : new ActionButton(text("open_google_play")) {
            @Override protected void onClick() {
                openUrl("https://play.google.com/store/apps/details?id=com.bilboldev.pixeldungeonskills"
                        + "&utm_source=android_client&utm_medium=internal&utm_campaign=public_version");
            }
        };
        support.setSize(width, 24);
        layoutContent(width, heading, content, 6, support);
        refresh();
    }

    private static String text(String key) {
        return Messages.get("support." + key);
    }

    private HighlightedText paragraph(String text, float x, float y, int width) {
        HighlightedText label = new HighlightedText(8);
        label.setColor(0xDDD4C5, 0xECD095);
        label.text(text, width, false);
        label.setPos(x, y);
        content.add(label);
        return label;
    }

    private void chooseAura(int style) {
        selected = style;
        preview.style(style);
        if (style == 0 || SupporterEntitlements.ownsAura(style)) SupporterEntitlements.selectAura(style);
        refresh();
    }

    private void choosePet(int pet) {
        selectedPet = pet;
        preview.pet(pet);
        CosmeticPets.select(pet);
        refresh();
    }

    private void chooseAccessory(int accessory) {
        selectedAccessory = accessory;
        preview.accessory(accessory);
        CosmeticAccessories.choose(hero, accessory);
        refresh();
    }

    private String petText(int pet) {
        return pet == 0 ? Messages.get("library.none")
                : "_" + CosmeticPets.name(pet) + "_\n" + CosmeticPets.description(pet);
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
            chooseAura(selected);
            return true;
        }
        return false;
    }

    @Override protected ScrollPane createScrollPane(Component body) {
        return content.scrollPane(body);
    }

    @Override public void update() {
        super.update();
        if (lastActive != SupporterEntitlements.selectedAura() || lastPet != CosmeticPets.selected()
                || lastAccessory != CosmeticAccessories.selection(hero)
                || lastOwned != SupporterEntitlements.ownsAura(1) || lastPetsOwned != SupporterEntitlements.ownsPets()
                || lastAccessoriesOwned != CosmeticAccessories.available()) refresh();
    }

    private void refresh() {
        lastActive = SupporterEntitlements.selectedAura();
        lastPet = CosmeticPets.selected();
        lastAccessory = CosmeticAccessories.selection(hero);
        lastOwned = SupporterEntitlements.ownsAura(1);
        lastPetsOwned = SupporterEntitlements.ownsPets();
        lastAccessoriesOwned = CosmeticAccessories.available();
        name.text(selected == 0 ? Messages.get("library.none") : text("aura_" + selected), choiceWidth, false);
        name.setColor(SupporterIcon.color(selected, 0), SupporterIcon.color(selected, 0));
        petDescription.text(petText(selectedPet), choiceWidth, false);
        accessoryName.text(selectedAccessory == 0 ? Messages.get("library.none") : CosmeticAccessories.name(selectedAccessory), choiceWidth, false);
        noAura.mark(lastActive == 0, true);
        noAccessory.mark(lastAccessory == 0, true);
        noPet.mark(lastPet == 0, true);
        for (int i = 0; i < choices.length; i++) choices[i].mark(lastActive == i + 1, lastOwned);
        for (int i = 0; i < petChoices.length; i++) {
            petChoices[i].mark(lastPet == i + 1, lastPetsOwned && CosmeticPets.unlocked(i + 1));
        }
        for (int i = 0; i < accessoryChoices.length; i++) {
            accessoryChoices[i].mark(lastAccessory == i + 1, lastAccessoriesOwned);
        }
        if (ratKing != null) ratKing.refresh();
    }

    private final class Choice extends RedButton {
        private final ColorBlock selected;
        private final Runnable choose;

        Choice(Image image, Runnable choose) {
            super("");
            this.choose = choose;
            content.addControl(hotArea);
            icon(image);
            add(selected = new ColorBlock(1, 1, 0xFFE6C477));
            content.add(this);
        }

        void mark(boolean active, boolean available) {
            selected.visible = active;
            icon.alpha(available ? 1 : .4f);
        }

        @Override protected void layout() {
            super.layout();
            if (icon != null) {
                icon.x = x + (width - icon.width()) / 2;
                icon.y = y + (height - icon.height()) / 2;
            }
            if (selected != null) {
                selected.x = x + 4;
                selected.y = bottom() - 3;
                selected.size(width - 8, 1);
            }
        }

        @Override protected void onClick() { choose.run(); }
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
            label.text(text(SupporterEntitlements.ownsAura(1)
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
