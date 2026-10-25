package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.GamesInProgress;
import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.SaveSlots;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.ChallengeIcons;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.SeedDisplay;
import java.util.ArrayList;

public class WndSaveSlots extends WndScrollable {
    private static final int GAP = 4, CARD_WIDTH = 112;
    private final ArrayList<SlotCard> buttons = new ArrayList<>();
    private final boolean dockRight;

    public WndSaveSlots(HeroClass heroClass) {
        this(heroClass, false);
    }

    public WndSaveSlots(HeroClass heroClass, boolean dockRight) {
        this.dockRight = dockRight && Game.width > Game.height;
        int columns = this.dockRight ? 2 : Game.width > Game.height ? 3 : 1;
        int preferredWidth = columns * CARD_WIDTH + (columns - 1) * GAP + SCROLL_GUTTER;
        float availableWidth = this.dockRight ? Game.width * .54f - 16 : Game.width - 8f;
        camera.zoom = Math.min(camera.zoom, availableWidth / (preferredWidth + chrome.marginHor()));
        int windowWidth = boundedWidth(preferredWidth);
        float cardWidth = (textWidth(windowWidth) - (columns - 1) * GAP) / (float)columns;
        Component content = new Component();
        float cardHeight = 58;
        for (int slot = SaveSlots.FIRST; slot <= SaveSlots.COUNT; slot++) {
            SlotCard card = new SlotCard(heroClass, slot, GamesInProgress.check(heroClass, slot));
            card.setRect(0, 0, cardWidth, 58);
            cardHeight = Math.max(cardHeight, card.requiredHeight());
            content.add(card);
            buttons.add(card);
        }
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setRect((i % columns) * (cardWidth + GAP),
                    (i / columns) * (cardHeight + GAP), cardWidth, cardHeight);
        }
        int rows = (buttons.size() + columns - 1) / columns;
        content.setSize(textWidth(windowWidth), rows * cardHeight + (rows - 1) * GAP);
        layoutContent(windowWidth, null, content, 0);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (dockRight) {
            camera.x = Math.round(Game.width * .73f - camera.screenWidth() / 2);
            shadow.boxRect(camera.x / camera.zoom, camera.y / camera.zoom, chrome.width(), chrome.height());
        }
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                for (SlotCard card : buttons) if (card.inside(x, y)) {
                    if (card.seed != null && card.seed.inside(x, y)) {
                        card.seed.open();
                        return;
                    }
                    boolean replace = card.info != null && x >= card.right() - 16 && y < card.top() + 16;
                    Sample.INSTANCE.play(Assets.SND_CLICK);
                    hide();
                    onSelect(card.slot, replace);
                    break;
                }
            }
        };
    }

    protected void onSelect(int slot, boolean replace) {}

    private static final class SlotCard extends Component {
        final int slot;
        final GamesInProgress.Info info;
        final NinePatch background;
        final ColorBlock accent;
        final Image hero, depthIcon, replaceIcon;
        final RenderedTextMultiline number, title, mode, depth;
        final ChallengeIcons challenges;
        final SeedDisplay seed;

        SlotCard(HeroClass heroClass, int slot, GamesInProgress.Info info) {
            this.slot = slot;
            this.info = info;
            boolean playable = info != null && !info.unavailable;
            background = Chrome.get(Chrome.Type.TOAST);
            add(background);
            accent = new ColorBlock(1, 1, playable && info.rules.mode == GameplayMode.REBORN
                    ? 0xFFBC9BEA : playable ? 0xFFB6A46C : 0xFF686C68);
            add(accent);
            hero = HeroSprite.avatar(heroClass, playable ? info.armorTier : 1);
            hero.scale.set(2);
            if (!playable) hero.alpha(0.3f);
            add(hero);
            seed = playable && info.rules.mode == GameplayMode.REBORN ? new SeedDisplay(info.seed, 6, false) : null;
            if (seed != null) {
                seed.useScrollPaneInput();
                add(seed);
            }
            number = label(Integer.toString(slot), 5, 0x8E938C);
            title = label(info == null ? Messages.get(StartScene.class, "empty_slot")
                    : info.unavailable ? Messages.get(StartScene.class, "unavailable_slot")
                    : Messages.get(StartScene.class, "save_hero", info.level, Messages.capitalize(Messages.maybeTranslate(heroClass.title()))),
                    7, playable ? 0xF0E9D5 : 0x999E96);
            String difficulty = playable ? info.rules.mode == GameplayMode.REBORN ? info.rules.mode.title()
                    : Difficulties.title(info.difficulty) : "";
            mode = label(difficulty, 6, playable && info.rules.mode == GameplayMode.REBORN ? 0xBC9BEA : 0xB6A46C);
            challenges = new ChallengeIcons(playable ? info.challenges & RebornChallenges.ALL : 0, 12, 5, playable);
            challenges.useScrollPaneInput();
            add(challenges);
            depth = label(playable ? Integer.toString(info.depth) : "", 6, 0xCACFC2);
            depthIcon = Icons.DEPTH.get();
            depthIcon.scale.set(.75f);
            depthIcon.visible = playable;
            add(depthIcon);
            replaceIcon = Icons.CLOSE.get();
            replaceIcon.scale.set(0.6f);
            replaceIcon.alpha(0.65f);
            replaceIcon.visible = info != null;
            add(replaceIcon);
        }

        private RenderedTextMultiline label(String value, int size, int color) {
            RenderedTextMultiline label = PixelScene.renderMultiline("", size);
            label.text(value, false);
            label.hardlight(color);
            add(label);
            return label;
        }

        float requiredHeight() {
            float details = title.height() + 3 + Math.max(mode.height(), depth.height());
            if (challenges.height() > 0) details += 3 + challenges.height();
            return Math.max(58, 18 + Math.max(hero.height(), details) + (seed == null ? 0 : 20));
        }

        @Override
        protected void layout() {
            if (background == null) return;
            background.x = x;
            background.y = y;
            background.size(width, height);
            accent.x = x + 3;
            accent.y = y + 5;
            accent.size(1, height - 10);
            number.setPos(x + 7, y + 5);
            hero.x = PixelScene.align(x + 8);
            float detailsHeight = height - (seed == null ? 0 : 25);
            hero.y = PixelScene.align(y + (detailsHeight - hero.height()) / 2);
            if (seed != null) seed.setRect(x + 6, bottom() - 21, width - 12, 16);
            replaceIcon.x = right() - 11;
            replaceIcon.y = y + 5;
            float textX = x + 40;
            int textWidth = Math.max(1, (int)(right() - textX - 8));
            title.maxWidth(textWidth);
            mode.maxWidth(Math.max(1, textWidth - (int)(depthIcon.width() + depth.width() + 6)));
            float modeHeight = Math.max(mode.height(), depth.height());
            float textHeight = title.height() + (mode.text().isEmpty() ? 0 : 3 + modeHeight);
            if (challenges.height() > 0) textHeight += 3 + challenges.height();
            float textY = y + Math.max(12, (detailsHeight - textHeight) / 2);
            title.setPos(textX, textY);
            mode.setPos(textX, title.bottom() + 3);
            challenges.setPos(textX, mode.top() + modeHeight + 3);
            depthIcon.x = mode.right() + 3;
            depth.setPos(depthIcon.x + depthIcon.width() + 3, mode.top());
            depthIcon.y = PixelScene.align(depth.top() + 5 - depthIcon.height());
        }
    }
}
