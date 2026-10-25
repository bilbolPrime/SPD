package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.SeedIcons;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartyPrompts;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import java.util.ArrayList;
import java.util.function.Consumer;

public class WndRunSeed extends WndScrollable {
    private static final int GAP = 4;
    private final int[] symbols;
    private final ArrayList<Choice> choices = new ArrayList<>();
    private final Slot[] slots = new Slot[RunSeed.SYMBOLS];
    private final Image[] previews = new Image[RunSeed.SYMBOLS];
    private final HighlightedText name = new HighlightedText(8);
    private final RedButton start;
    private final RedButton paste;
    private int selected;

    public WndRunSeed(int[] symbols, Consumer<RunSeed> onStart) {
        this.symbols = symbols;
        int width = boundedWidth(landscapeLayout() ? 280 : 160);
        Component heading = new Component();
        HighlightedText title = new HighlightedText(9);
        title.text(Messages.get(RunSeed.class, "choose"), width, false);
        title.setColor(titleColor(), titleColor());
        heading.add(title);
        int slotColumns = landscapeLayout() ? RunSeed.SYMBOLS : 3;
        float slotWidth = (width - GAP * (slotColumns - 1)) / (float)slotColumns;
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Slot(i);
            slots[i].setRect(i % slotColumns * (slotWidth + GAP),
                    title.bottom() + GAP + i / slotColumns * (29 + GAP), slotWidth, 29);
            heading.add(slots[i]);
        }
        name.setPos(0, slots[slots.length - 1].bottom() + GAP);
        heading.add(name);
        heading.setSize(width, name.top() + 21);

        Component content = new Component();
        int columns = landscapeLayout() ? 8 : 5;
        float cell = (textWidth(width) - GAP * (columns - 1)) / (float)columns;
        for (int i = 0; i < SeedIcons.COUNT; i++) {
            Choice choice = new Choice(i);
            choice.setRect(i % columns * (cell + GAP), i / columns * (cell + GAP), cell, cell);
            content.add(choice);
            choices.add(choice);
        }
        content.setSize(textWidth(width), ((choices.size() + columns - 1) / columns) * (cell + GAP) - GAP);
        HighlightedText notice = new HighlightedText(7);
        notice.text(Messages.get(RunSeed.class, "custom_notice"), width, false);
        start = new RedButton(Messages.get("windows.wndcontrols.done")) {
            @Override protected void onClick() {
                RunSeed seed = RunSeed.fromSymbols(symbols, true);
                hide();
                onStart.accept(seed);
            }
        };
        paste = new RedButton(Messages.get(RunSeed.class, "paste")) {
            @Override protected void onClick() {
                enable(false);
                PartyPrompts.paste(value -> {
                    if (!WndRunSeed.this.exists) return;
                    enable(true);
                    RunSeed seed = RunSeed.parse(value);
                    if (seed == null) {
                        Game.scene().add(new WndMessage(Messages.get(RunSeed.class, "invalid")));
                        return;
                    }
                    for (int i = 0; i < symbols.length; i++) symbols[i] = seed.symbol(i);
                    selected = 0;
                    refresh();
                });
            }
        };
        paste.setSize((width - GAP) / 2f, 20);
        start.setSize((width - GAP) / 2f, 20);
        layoutContent(width, heading, content, notice, GAP, paste, start);
        refresh();
    }

    private void choose(int id) {
        symbols[selected] = id;
        for (int next = 1; next <= slots.length; next++) {
            int slot = (selected + next) % slots.length;
            if (symbols[slot] < 0) {
                selected = slot;
                break;
            }
        }
        refresh();
    }

    private void refresh() {
        boolean complete = true;
        for (int i = 0; i < slots.length; i++) {
            if (previews[i] != null) {
                previews[i].killAndErase();
                previews[i].destroy();
                previews[i] = null;
            }
            slots[i].text(symbols[i] < 0 ? "?" : "");
            slots[i].highlight(i == selected);
            if (symbols[i] < 0) {
                complete = false;
                continue;
            }
            Image image = previews[i] = SeedIcons.entry(symbols[i]).image();
            image.scale.set(Math.min(23 / image.width, 23 / image.height));
            image.x = PixelScene.align(slots[i].left() + (slots[i].width() - image.width()) / 2);
            image.y = PixelScene.align(slots[i].top() + (slots[i].height() - image.height()) / 2);
            slots[i].add(image);
        }
        name.text(symbols[selected] < 0 ? Messages.get(RunSeed.class, "pick")
                : SeedIcons.entry(symbols[selected]).name(), width, false);
        for (Choice choice : choices) choice.background.brightness(symbols[selected] == choice.id ? 1.6f : 1f);
        start.enable(complete);
    }

    @Override protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override public void onClick(float x, float y) {
                for (Choice choice : choices) {
                    if (choice.inside(x, y)) {
                        Sample.INSTANCE.play(Assets.SND_CLICK);
                        choose(choice.id);
                        return;
                    }
                }
            }
        };
    }

    private final class Slot extends RedButton {
        private final int index;

        Slot(int index) {
            super("");
            this.index = index;
        }

        void highlight(boolean selected) {
            bg.brightness(selected ? 1.5f : 1f);
        }

        @Override protected void onClick() {
            selected = index;
            refresh();
        }
    }

    private static final class Choice extends Component {
        final int id;
        final NinePatch background;
        final Image image;

        Choice(int id) {
            this.id = id;
            background = Chrome.get(Chrome.Type.BUTTON);
            image = SeedIcons.entry(id).image();
            add(background);
            add(image);
        }

        @Override protected void layout() {
            background.x = x;
            background.y = y;
            background.size(width, height);
            image.scale.set(Math.min((width - 6) / image.width, (height - 6) / image.height));
            image.x = PixelScene.align(x + (width - image.width()) / 2);
            image.y = PixelScene.align(y + (height - image.height()) / 2);
        }
    }
}
