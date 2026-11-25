package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.JourneyRun;
import com.bilboldev.pixeldungeonskills.JourneyRun.SlotInfo;
import com.bilboldev.pixeldungeonskills.JourneyRun.SlotState;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.JourneyScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.SeedDisplay;
import java.io.IOException;
import java.util.ArrayList;

public class WndJourneySlots extends WndScrollable {
    private static final int GAP = 4;
    private final ArrayList<SlotCard> cards = new ArrayList<>();

    public WndJourneySlots() {
        int columns = landscapeLayout() ? 3 : 2;
        int width = boundedWidth(columns * 112 + (columns - 1) * GAP + SCROLL_GUTTER);
        IconTitle heading = new IconTitle(Icons.COMPASS.get(), Messages.get("journey.saves"));
        heading.setRect(0, 0, width, 0);
        Component content = new Component();
        float cardWidth = (textWidth(width) - (columns - 1) * GAP) / (float)columns;
        for (int slot = JourneyRun.FIRST_SLOT; slot <= JourneyRun.SLOT_COUNT; slot++) {
            SlotCard card = new SlotCard(JourneyRun.summary(slot));
            int index = slot - JourneyRun.FIRST_SLOT;
            card.setRect(index % columns * (cardWidth + GAP), index / columns * (88 + GAP), cardWidth, 88);
            cards.add(card);
            content.add(card);
        }
        int rows = (JourneyRun.SLOT_COUNT + columns - 1) / columns;
        content.setSize(textWidth(width), rows * 88 + (rows - 1) * GAP);
        layoutContent(width, heading, content, 6);
    }

    @Override protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override public void onClick(float x, float y) {
                for (SlotCard card : cards) if (card.inside(x, y)) {
                    Sample.INSTANCE.play(Assets.SND_CLICK);
                    if (card.info.state != SlotState.EMPTY && x >= card.right() - 20 && y < card.top() + 20) {
                        confirmDelete(card.info.slot);
                    } else if (card.seed != null && card.seed.inside(x, y)) {
                        card.seed.open();
                    } else {
                        select(card.info);
                    }
                    return;
                }
            }
        };
    }

    private void select(SlotInfo info) {
        if (info.state == SlotState.EMPTY) {
            parent.add(new NewJourney(info.slot));
        } else if (info.state == SlotState.UNAVAILABLE) {
            parent.add(new WndMessage(Messages.get("journey.start_failed")));
        } else {
            try {
                JourneyRun.open(info.slot);
                if (!JourneyRun.active()) refresh();
            } catch (IOException error) {
                parent.add(new WndMessage(Messages.get("journey.start_failed")));
            }
        }
    }

    private void confirmDelete(int slot) {
        parent.add(new WndOptions(Messages.get("windows.wndgameinprogress.erase_warn_title"),
                Messages.get("windows.wndgameinprogress.erase_warn_body"),
                Messages.get("windows.wndgameinprogress.erase_warn_yes"),
                Messages.get("windows.wndgameinprogress.erase_warn_no")) {
            @Override protected void onSelect(int index) {
                if (index != 0) return;
                try {
                    JourneyRun.delete(slot);
                    refresh();
                } catch (IOException error) {
                    WndJourneySlots.this.parent.add(new WndMessage(Messages.get("journey.save_failed")));
                }
            }
        });
    }

    private void refresh() {
        Group host = parent;
        hide();
        host.add(new WndJourneySlots());
    }

    private static final class SlotCard extends Component {
        final SlotInfo info;
        final NinePatch background = Chrome.get(Chrome.Type.TOAST);
        final BitmapText title;
        final float titleScale;
        final Image close = Icons.CLOSE.get();
        final Image[] heroes = new Image[JourneyRun.HEROES.length];
        final Image[] sleep = new Image[JourneyRun.HEROES.length];
        final SeedDisplay seed;
        final RenderedTextMultiline detail = PixelScene.renderMultiline("", 6);

        SlotCard(SlotInfo info) {
            this.info = info;
            add(background);
            title = PixelScene.createText(Messages.get("journey.slot", info.slot), 8);
            title.measure();
            titleScale = title.scale.x;
            title.hardlight(0xE6C891);
            add(title);
            close.scale.set(.6f);
            close.alpha(.7f);
            close.visible = info.state != SlotState.EMPTY;
            add(close);
            for (int i = 0; i < heroes.length; i++) {
                HeroClass type = JourneyRun.HEROES[i];
                heroes[i] = info.state == SlotState.SAVED && !info.alive(type)
                        ? new ItemSprite(ItemSpriteSheet.BONES, null)
                        : HeroSprite.avatar(type, info.state == SlotState.SAVED ? info.armorTier(type) : 1, 0);
                if (info.state != SlotState.SAVED) heroes[i].alpha(.25f);
                add(heroes[i]);
                sleep[i] = Icons.SLEEP.get();
                sleep[i].scale.set(.65f);
                sleep[i].visible = info.state == SlotState.SAVED && info.resting(type);
                add(sleep[i]);
            }
            seed = info.state == SlotState.SAVED && RunSeed.valid(info.seed)
                    ? new SeedDisplay(new RunSeed(info.seed, info.custom), 6, false) : null;
            if (seed != null) {
                seed.useScrollPaneInput();
                add(seed);
            }
            String status = info.state == SlotState.EMPTY ? Messages.get(StartScene.class, "empty_slot")
                    : info.state == SlotState.UNAVAILABLE ? Messages.get(StartScene.class, "unavailable_slot")
                    : Messages.get(RunSeed.class, "title") + (seed == null ? ": " + info.seed : "");
            detail.text(status, false);
            detail.hardlight(0xBDB6AD);
            add(detail);
        }

        @Override protected void layout() {
            if (title == null) return;
            background.x = x;
            background.y = y;
            background.size(width, height);
            title.scale.set(titleScale);
            title.scale.set(title.scale.x * Math.min(1, (width - 25) / Math.max(1, title.width())));
            title.x = PixelScene.align(x + 7);
            title.y = PixelScene.align(y + 6);
            close.x = right() - close.width() - 5;
            close.y = y + 6;
            float cell = (width - 14) / heroes.length;
            for (int i = 0; i < heroes.length; i++) {
                Image hero = heroes[i];
                hero.scale.set(Math.min(1.65f, (cell - 2) / hero.width));
                hero.x = PixelScene.align(x + 7 + i * cell + (cell - hero.width()) / 2);
                hero.y = PixelScene.align(y + 49 - hero.height());
                sleep[i].x = PixelScene.align(hero.x + hero.width() - 5);
                sleep[i].y = PixelScene.align(hero.y - sleep[i].height());
            }
            detail.maxWidth((int)width - 14);
            detail.setPos(x + 7, y + 55);
            if (seed != null) seed.setRect(x + 7, y + 67, width - 14, 16);
        }
    }

    private static final class NewJourney extends WndScrollable {
        private final int slot;
        private final Component preview = new Component();
        private final Component unknown = new Component();
        private RunSeed seed;
        private SeedDisplay display;

        NewJourney(int slot) {
            this.slot = slot;
            int width = boundedWidth(220);
            IconTitle heading = new IconTitle(Icons.COMPASS.get(), Messages.get("journey.slot", slot));
            heading.setRect(0, 0, width, 0);
            preview.setSize(textWidth(width), 40);
            BitmapText label = PixelScene.createText(Messages.get(RunSeed.class, "title"), 8);
            label.measure();
            preview.add(label);
            float cell = preview.width() / RunSeed.SYMBOLS;
            for (int i = 0; i < RunSeed.SYMBOLS; i++) {
                BitmapText symbol = PixelScene.createText("?", 10);
                symbol.measure();
                symbol.x = PixelScene.align(i * cell + (cell - symbol.width()) / 2);
                symbol.y = 18;
                unknown.add(symbol);
            }
            preview.add(unknown);
            showSeed();
            RedButton choose = new RedButton(Messages.get(RunSeed.class, "choose")) {
                @Override protected void onClick() {
                    int[] symbols = new int[RunSeed.SYMBOLS];
                    for (int i = 0; i < symbols.length; i++) symbols[i] = seed == null ? -1 : seed.symbol(i);
                    NewJourney.this.parent.add(new WndRunSeed(symbols, chosen -> {
                        if (!NewJourney.this.exists) return;
                        seed = chosen;
                        showSeed();
                    }));
                }
            };
            RedButton random = new RedButton(Messages.get(RunSeed.class, "random")) {
                @Override protected void onClick() {
                    seed = null;
                    showSeed();
                }
            };
            RedButton start = new RedButton(Messages.get(RunSeed.class, "start")) {
                @Override protected void onClick() {
                    enable(false);
                    try {
                        JourneyRun.create(NewJourney.this.slot, seed == null ? RunSeed.random().code : seed.code, seed != null);
                        Game.switchScene(JourneyScene.class);
                    } catch (IOException error) {
                        enable(true);
                        NewJourney.this.parent.add(new WndMessage(Messages.get("journey.start_failed")));
                    }
                }
            };
            choose.setSize((width - GAP) / 2f, 24);
            random.setSize((width - GAP) / 2f, 24);
            start.setSize(width, 24);
            layoutContent(width, heading, preview, GAP, choose, random, start);
        }

        private void showSeed() {
            if (display != null) {
                display.killAndErase();
                display.destroy();
                display = null;
            }
            unknown.visible = seed == null;
            if (seed == null) return;
            display = new SeedDisplay(seed, 6, false);
            preview.add(display);
            display.setRect(preview.left(), preview.top() + 14, preview.width(), 22);
        }
    }
}
