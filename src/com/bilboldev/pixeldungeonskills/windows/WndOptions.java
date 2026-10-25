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
package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.ArrayList;

import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

public class WndOptions extends WndScrollable {

    private static final int WIDTH = 120;
    private static final int MARGIN = 2;
    private static final int TITLE_TEXT_SIZE = 9;
    private static final int MESSAGE_TEXT_SIZE = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_TEXT_SIZE = 9;

    private final ArrayList<OptionButton> buttons = new ArrayList<>();
    private final String headingText;
    private final String messageText;

    public WndOptions(Message title, Message message, Message... options) {
        this(null, title, message, options);
    }

    public WndOptions(Image icon, Message title, Message message, Message... options) {
        this(icon, title.toString(), message.toString(), 1, false, labels(options));
        menuModel = new MenuModel(title, message);
        for (int i = 0; i < options.length; i++) menuModel.add(i, options[i]);
    }

    public WndOptions(Image icon, Message title, Message message, Message[] options, int[] images, int[] quantities) {
        this(icon, title.toString(), message.toString(), 2, false, images, quantities, labels(options));
        menuModel = new MenuModel(title, message);
        for (int i = 0; i < options.length; i++) menuModel.add(i, options[i], images[i], quantities[i]);
    }

    private static String[] labels(Message[] options) {
        String[] labels = new String[options.length];
        for (int i = 0; i < options.length; i++) labels[i] = options[i].toString();
        return labels;
    }

    public WndOptions(String title, String message, String... options) {
        this(title, message, 1, options);
    }

    public WndOptions(Image icon, String title, String message, String... options) {
        this(icon, title, message, 1, true, options);
    }

    public WndOptions(String title, String message, int columns, String... options) {
        this(null, title, message, columns, true, options);
    }

    protected WndOptions(Image icon, String title, String message, int columns, boolean localize, String... options) {
        this(icon, title, message, columns, localize, null, null, options);
    }

    protected WndOptions(Image icon, String title, String message, int columns, boolean localize,
                         int[] images, int[] quantities, String... options) {
        super();
        headingText = title == null ? "" : localize ? Messages.maybeTranslate(title) : title;
        messageText = message == null ? "" : localize ? Messages.maybeTranslate(message) : message;
        columns = Math.max(1, columns);
        int windowWidth = boundedWidth(images == null ? WIDTH * columns : WIDTH);
        int bodyWidth = Math.max(1, textWidth(windowWidth) - MARGIN * 2);
        int buttonWidth = Math.max(1, (bodyWidth - MARGIN * (columns - 1)) / columns);
        if (images != null) buttonWidth = Math.min(40, buttonWidth);

        Component heading = null;
        if (title != null && !title.isEmpty()) {
            RenderedTextMultiline label = PixelScene.renderMultiline(null, TITLE_TEXT_SIZE);
            label.text(headingText, false);
            applyTextScale(label, titleTextScale());
            label.hardlight(titleColor());
            label.maxWidth(windowWidth - MARGIN * 2);
            label.setPos(MARGIN, MARGIN);
            heading = label;
            if (icon != null) {
                icon.scale.set(2);
                label.maxWidth(Math.max(1, (int)(windowWidth - icon.width() - MARGIN * 3)));
                float height = Math.max(icon.height(), label.height());
                icon.x = MARGIN;
                icon.y = MARGIN + (height - icon.height()) / 2;
                label.setPos(icon.x + icon.width() + MARGIN, MARGIN + (height - label.height()) / 2);
                heading = new Component();
                heading.add(icon);
                heading.add(label);
                heading.setSize(windowWidth, height + MARGIN);
            }
        }

        Component content = new Component();
        float pos = 0;
        if (message != null && !message.isEmpty()) {
            RenderedTextMultiline explanation = PixelScene.renderMultiline(null, MESSAGE_TEXT_SIZE);
            explanation.text(messageText, false);
            applyTextScale(explanation, messageTextScale());
            explanation.maxWidth(bodyWidth);
            explanation.setPos(MARGIN, 0);
            content.add(explanation);
            pos = explanation.bottom() + MARGIN;
        }

        float cardHeight = images == null ? BUTTON_HEIGHT : buttonWidth;
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            OptionButton button = new OptionButton(options[i], buttonTextScale(), localize && shouldTranslateOptionLabels()) {
                @Override
                protected void onClick() {
                    hide();
                    onSelect(index);
                }
            };
            int optionWidth = fullWidthOption(i) ? bodyWidth : buttonWidth;
            if (images == null) {
                button.setRect(MARGIN, pos, optionWidth, BUTTON_HEIGHT);
                button.setSize(optionWidth, Math.max(BUTTON_HEIGHT, (float)Math.ceil(button.text.height()) + MARGIN * 2));
            } else {
                button.showItem(images[i], quantities[i]);
                button.setRect(MARGIN, pos, buttonWidth, buttonWidth);
            }
            cardHeight = Math.max(cardHeight, button.height());
            content.add(button);
            buttons.add(button);
            if (columns == 1) pos = button.bottom() + MARGIN;
        }
        if (columns > 1) {
            int cell = 0;
            float left = images == null ? MARGIN
                    : MARGIN + (bodyWidth - columns * buttonWidth - MARGIN * (columns - 1)) / 2f;
            for (int i = 0; i < buttons.size(); i++) {
                boolean fullWidth = fullWidthOption(i);
                if (fullWidth && cell % columns != 0) cell += columns - cell % columns;
                buttons.get(i).setRect(left + (cell % columns) * (buttonWidth + MARGIN),
                        pos + (cell / columns) * (cardHeight + MARGIN), fullWidth ? bodyWidth : buttonWidth, cardHeight);
                cell += fullWidth ? columns : 1;
            }
            pos += ((cell + columns - 1) / columns) * (cardHeight + MARGIN);
        }
        content.setSize(textWidth(windowWidth), pos);
        layoutContent(windowWidth, heading, content, MARGIN);
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                for (OptionButton button : buttons) {
                    if (button.click(x, y)) break;
                }
            }
        };
    }

    protected void onSelect(int index) {}

    protected boolean fullWidthOption(int index) {
        return false;
    }

    public String partyHeading() {
        return headingText;
    }

    public String partyDescription() {
        return messageText;
    }

    public String[] partyChoices() {
        String[] labels = new String[buttons.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = buttons.get(i).text.text();
        return labels;
    }

    public boolean selectForParty(int index) {
        if (!isPartyWindow() || parent == null || !active || !visible
                || index < 0 || index >= buttons.size()) return false;
        OptionButton button = buttons.get(index);
        if (!button.exists || !button.active || !button.visible) return false;
        hide();
        onSelect(index);
        return true;
    }

    protected float titleTextScale() {
        return 1f;
    }

    protected float messageTextScale() {
        return 1f;
    }

    protected float buttonTextScale() {
        return 1f;
    }

    protected boolean shouldTranslateOptionLabels() {
        return true;
    }

    private static void applyTextScale(RenderedTextMultiline text, float scale) {
        if (scale != 1f) text.zoom((1f / PixelScene.defaultZoom) * scale);
    }

    private static class OptionButton extends Component {
        private NinePatch bg;
        private RenderedTextMultiline text;
        private ItemSprite item;
        private BitmapText amount;

        OptionButton(String label, float textScale, boolean localize) {
            super();
            applyTextScale(text, textScale);
            text.text(label, localize);
        }

        void showItem(int image, int quantity) {
            text.visible = false;
            item = new ItemSprite(image, null);
            item.scale.set(2);
            add(item);
            amount = new BitmapText(quantity > 1 ? Integer.toString(quantity) : "", PixelScene.font1x);
            amount.measure();
            add(amount);
        }

        boolean click(float x, float y) {
            if (!inside(x, y)) return false;
            Sample.INSTANCE.play(Assets.SND_CLICK);
            onClick();
            return true;
        }

        @Override
        protected void createChildren() {
            bg = Chrome.get(Chrome.Type.BUTTON);
            add(bg);
            text = PixelScene.renderMultiline("", BUTTON_TEXT_SIZE);
            add(text);
        }

        @Override
        protected void layout() {
            bg.x = x;
            bg.y = y;
            bg.size(width, height);
            text.maxWidth(Math.max(1, (int)width - MARGIN * 2));
            text.setPos(PixelScene.align(x + (width - text.width()) / 2f),
                    PixelScene.align(y + (height - text.height()) / 2f));
            if (item != null) {
                item.x = PixelScene.align(x + (width - item.width()) / 2f);
                item.y = PixelScene.align(y + (height - item.height()) / 2f);
                amount.x = PixelScene.align(x + width - amount.width() - MARGIN);
                amount.y = PixelScene.align(y + MARGIN);
            }
        }

        protected void onClick() {}
    }
}
