package com.bilboldev.pixeldungeonskills.scenes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.Visual;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Rankings;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.effects.BadgeBanner;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.windows.WndBadge;
import com.bilboldev.utils.PointF;

abstract class TitleHubPanel extends TitleLayer {
    final TitleScene host;
    final boolean portrait = Game.height > Game.width;
    final float screenW = Camera.main.width, screenH = Camera.main.height;
    final float target = Math.max(28, 48 * Game.density / Camera.main.zoom);
    final ArrayList<HubButton> controls = new ArrayList<>();
    float cardX, cardY, cardW, cardH;

    static TitleHubPanel create(TitleScene host, TitleScene.Page page, TitleBackdrop backdrop) {
        switch (page) {
            case HEROES: return new Heroes(host, backdrop);
            case RANKINGS: return new Records(host);
            case BADGES: return new Gallery(host);
            case ABOUT: return new Archive(host);
            default: throw new IllegalArgumentException("No panel for " + page);
        }
    }

    TitleHubPanel(TitleScene host, String title) {
        this.host = host;
        camera = Camera.main;
        HubButton back = button(this, msg("back"), false, host::back);
        back.setRect(12, 42, 56, target);
        BitmapText heading = text(this, title, 13, 0xE6DAF7, 78, 0);
        fit(heading, screenW - 90);
        heading.y = PixelScene.align(back.centerY() - heading.baseLine() / 2 - 1);
        cardX = portrait ? 12 : 24;
        cardW = portrait ? screenW - 24 : Math.min(310, screenW * .46f);
        cardY = portrait ? Math.max(110, screenH * .36f) : Math.max(86, 48 + target);
        cardH = screenH - cardY - 36;
    }

    static String msg(String key, Object... values) {
        return Messages.get(TitleScene.class, key, values);
    }

    void card() {
        RebornChrome frame = new RebornChrome(Chrome.Type.WINDOW);
        frame.x = cardX;
        frame.y = cardY;
        frame.size(cardW, cardH);
        add(frame);
    }

    HubButton button(Group group, String label, boolean primary, Runnable action) {
        HubButton button = new HubButton(label, primary, action);
        group.add(button);
        controls.add(button);
        return button;
    }

    static BitmapText text(Group group, String label, int size, int color, float x, float y) {
        BitmapText text = PixelScene.createText(label, size);
        text.measure();
        text.hardlight(color);
        text.x = PixelScene.align(x);
        text.y = PixelScene.align(y);
        group.add(text);
        return text;
    }

    static BitmapTextMultiline paragraph(Group group, String label, int size, int color, float x, float y, float width) {
        BitmapTextMultiline text = PixelScene.createMultiline(label, size);
        text.maxWidth = Math.max(1, (int)width);
        text.measure();
        text.hardlight(color);
        text.x = PixelScene.align(x);
        text.y = PixelScene.align(y);
        group.add(text);
        return text;
    }

    static void fit(BitmapText text, float width) {
        float scale = Math.min(1, width / Math.max(1, text.width()));
        text.scale.set(text.scale.x * scale, text.scale.y * scale);
    }

    void pointerMoved(PointF point) {
        for (HubButton button : controls) {
            PointF local = point;
            if (point != null && button.camera() != Camera.main) {
                com.bilboldev.utils.Point screen = Camera.main.cameraToScreen(point.x, point.y);
                local = button.camera().screenToCamera(screen.x, screen.y);
            }
            button.hover(local != null && button.isActive() && button.isVisible() && button.inside(local.x, local.y));
        }
    }

    private static final class Heroes extends TitleHubPanel {
        private final TitleBackdrop backdrop;
        private final ArrayList<HeroChoice> heroes = new ArrayList<>();
        private Group details;

        Heroes(TitleScene host, TitleBackdrop backdrop) {
            super(host, msg(StartScene.companionLeader() == null ? "heroes_title" : "companion_title"));
            this.backdrop = backdrop;
            cardW = portrait ? screenW - 24 : Math.min(240, screenW * .38f);
            cardY = portrait ? screenH * .62f : Math.max(91, 48 + target);
            cardH = Math.max(108 + target, 66 + 2 * Math.max(30, target));
            cardY = Math.min(cardY, screenH - cardH - 16);
            card();
            for (int i = 0; i < TitleScene.HERO_CLASSES.length; i++) {
                HeroChoice choice = new HeroChoice(i);
                heroes.add(choice);
                add(choice);
            }
            if (TitleScene.selectedHero != null) StartScene.curClass = TitleScene.selectedHero;
            rebuildDetails();
            positionHeroes();
        }

        private void select(HeroClass hero) {
            if (StartScene.companionLeader() != null && !StartScene.chooseCompanion(hero)) return;
            TitleScene.selectedHero = StartScene.curClass = hero;
            PixelDungeon.lastClass(hero.ordinal());
            backdrop.selectHeroes(hero, StartScene.companionLeader());
            rebuildDetails();
        }

        private void rebuildDetails() {
            if (details != null) {
                for (int i = controls.size() - 1; i >= 0; i--)
                    if (controls.get(i).parent == details) controls.remove(i);
                remove(details);
                details.destroy();
            }
            details = new Group();
            add(details);
            HeroClass hero = TitleScene.selectedHero;
            HeroClass leader = StartScene.companionLeader();
            if (leader != null) {
                String leaderName = Messages.capitalize(Messages.maybeTranslate(leader.title()));
                String heading = hero == null ? msg("companion_title")
                        : leaderName + " + " + Messages.capitalize(Messages.maybeTranslate(hero.title()));
                BitmapText title = text(details, heading, 12, 0xE6DAF7, cardX + 12, cardY + 15);
                fit(title, cardW - 24);
                paragraph(details, hero == null ? msg("companion_body", leaderName) : msg("party_departure"),
                        8, 0xB5ADC1, cardX + 12, cardY + 38, cardW - 24);
                return;
            }
            if (hero == null) {
                BitmapText title = text(details, msg("choose_hero"), 12, 0xE6DAF7, cardX + 12, cardY + 15);
                fit(title, cardW - 24);
                paragraph(details, msg("choose_hero_body"), 9, 0xB5ADC1,
                        cardX + 12, cardY + 38, cardW - 24);
                return;
            }
            BitmapText name = text(details, Messages.capitalize(Messages.maybeTranslate(hero.title())), 13,
                    0xE6DAF7, cardX + 12, cardY + 11);
            fit(name, cardW - 24);
            paragraph(details, msg("hero_" + hero.name().toLowerCase(Locale.ROOT)), 8, 0xBBB3C8,
                    cardX + 12, cardY + 30, cardW - 24);
            float row = Math.max(30, target);
            float top = cardY + cardH - 10 - row;
            float half = (cardW - 24) / 2;
            top -= row + 4;
            HubButton play = button(details, Messages.get(StartScene.class, "play"), true,
                    () -> StartScene.beginStartFlow(host, TitleScene.selectedHero));
            play.setRect(cardX + 10, top, half, row);
            HubButton gauntlet = button(details, msg("gauntlet"), false, () -> {
                StartScene.curClass = TitleScene.selectedHero;
                TitleScene.leavingForRun();
                GauntletRun.start();
            });
            gauntlet.setRect(cardX + 14 + half, top, half, row);
            button(details, Messages.get(StartScene.class, "party_mode"), false,
                    () -> StartScene.beginPartyFlow(host, TitleScene.selectedHero))
                    .setRect(cardX + 10, top + row + 4, cardW - 20, row);
        }

        private void positionHeroes() {
            float scale = backdrop.heroScale();
            float width = Math.max(target, (portrait ? 44 : 46) * scale);
            for (HeroChoice choice : heroes) {
                PointF point = backdrop.heroAnchor(choice.index);
                choice.setRect(point.x - width / 2, point.y - 25 * scale, width, 47 * scale);
            }
        }

        @Override
        public void update() {
            positionHeroes();
            super.update();
        }

        @Override
        public void draw() {

            positionHeroes();
            super.draw();
        }

        private final class HeroChoice extends Button {
            final int index;
            private BitmapText name;
            private ColorBlock marker;
            private float nameScaleX, nameScaleY;
            HeroChoice(int index) {
                this.index = index;
                name.text(Messages.capitalize(Messages.maybeTranslate(TitleScene.HERO_CLASSES[index].title())));
                name.measure();
            }

            @Override
            protected void createChildren() {
                super.createChildren();
                add(marker = new ColorBlock(12, 1, 0xFFD1B48A));
                add(name = PixelScene.createText(9));
                nameScaleX = name.scale.x;
                nameScaleY = name.scale.y;
            }

            @Override
            protected void layout() {
                super.layout();
                name.scale.set(nameScaleX, nameScaleY);
                fit(name, width - 4);
                name.x = PixelScene.align(x + (width - name.width()) / 2);
                name.y = PixelScene.align(y + 37 * backdrop.heroScale());
                marker.x = x + (width - marker.width()) / 2;
                marker.y = name.y + name.baseLine() + 2;
                HeroClass hero = TitleScene.HERO_CLASSES[index];
                HeroClass leader = StartScene.companionLeader();
                active = leader == null || (StartScene.choosingCompanion() && hero != leader);
                boolean selected = hero == TitleScene.selectedHero || hero == leader;
                marker.visible = selected;
                name.hardlight(selected ? 0xEAD6B5 : 0xC3BACD);
            }

            @Override
            protected void onTouchDown() {
                Sample.INSTANCE.play(Assets.SND_CLICK, .7f, .7f, 1);
            }

            @Override
            protected void onClick() {
                select(TitleScene.HERO_CLASSES[index]);
            }
        }
    }

    private static final class Records extends TitleHubPanel {
        Records(TitleScene host) {
            super(host, msg("records_title"));
            card();
            BitmapText count = text(this, msg("rank_summary", Rankings.INSTANCE.totalNumber, Rankings.INSTANCE.wonNumber),
                    9, 0xD8C6EA, cardX + 12, cardY + 12);
            fit(count, cardW - 24);
            Component content = new Component();
            ArrayList<RankingsScene.Record> rows = new ArrayList<>();
            float width = cardW - 24, y = 0;
            for (int i = 0; i < Rankings.INSTANCE.records.size(); i++) {
                ColorBlock fill = new ColorBlock(width, 39, i % 2 == 0 ? 0x661F1C30 : 0x331F1C30);
                fill.y = y;
                content.add(fill);
                RankingsScene.Record row = new RankingsScene.Record(i, i == Rankings.INSTANCE.lastRecord, Rankings.INSTANCE.records.get(i));
                row.useScrollPaneInput();
                row.setRect(4, y, width - 8, 39);
                content.add(row);
                rows.add(row);
                y += 43;
            }
            if (rows.isEmpty()) {
                BitmapTextMultiline empty = paragraph(content, Messages.get(RankingsScene.class, "no_games") + "\n\n" + msg("records_empty"),
                        9, 0xB8AFC7, 0, 8, width);
                y = empty.height() + 16;
            }
            content.setSize(width, y);
            ScrollPane scroll = new ScrollPane(content) {
                @Override
                public void onClick(float x, float y) {
                    for (RankingsScene.Record row : rows) {
                        if (row.inside(x, y)) {
                            row.openDetails();
                            break;
                        }
                    }
                }
            };
            add(scroll);
            scroll.setRect(cardX + 12, cardY + 34, width, cardH - 46);
        }
    }

    private static final class Gallery extends TitleHubPanel {
        Gallery(TitleScene host) {
            super(host, msg("gallery_title"));
            card();
            List<Badges.Badge> badges = Badges.filtered(true);
            text(this, msg("badge_summary", badges.size()), 9, 0xD8C6EA, cardX + 12, cardY + 12);
            Component content = new Component();
            float width = cardW - 24;
            int cols = Math.max(3, (int)(width / Math.max(38, target)));
            float size = width / cols;
            int count = Math.max(27, badges.size());
            for (int i = 0; i < count; i++) {
                ColorBlock stone = new ColorBlock(size - 4, size - 4, 0x991E1A2B);
                stone.x = i % cols * size + 2;
                stone.y = i / cols * size + 2;
                content.add(stone);
                Image icon = i < badges.size() ? BadgeBanner.image(badges.get(i).image) : new Image(Assets.LOCKED);
                icon.x = PixelScene.align(i % cols * size + (size - icon.width()) / 2);
                icon.y = PixelScene.align(i / cols * size + (size - icon.height()) / 2);
                if (i >= badges.size()) icon.alpha(.3f);
                content.add(icon);
            }
            content.setSize(width, (float)Math.ceil(count / (float)cols) * size);
            ScrollPane scroll = new ScrollPane(content) {
                @Override
                public void onClick(float x, float y) {
                    int col = (int)(x / size), row = (int)(y / size), index = row * cols + col;
                    if (x >= 0 && y >= 0 && col < cols && index < badges.size()) {
                        Sample.INSTANCE.play(Assets.SND_CLICK, .7f, .7f, 1.2f);
                        host.add(new WndBadge(badges.get(index)));
                    }
                }
            };
            add(scroll);
            scroll.setRect(cardX + 12, cardY + 34, width, cardH - 46);
        }
    }

    private static final class Archive extends TitleHubPanel {
        Archive(TitleScene host) {
            super(host, msg("archive_title"));
            card();
            Component content = new Component();
            float width = cardW - 24;
            BitmapTextMultiline credits = paragraph(content, msg("credits"), 9, 0xD4CADF, 0, 0, width);
            float y = credits.height() + 16;
            HubButton discord = button(content, msg("visit_discord"), false,
                    () -> Window.openUrl("https://discord.gg/3xYzwun"));
            discord.useScrollPaneInput();
            discord.setRect(0, y, width, target);
            y += target + 4;
            content.setSize(width, y);
            ScrollPane scroll = new ScrollPane(content) {
                @Override
                public void onClick(float x, float y) {
                    for (HubButton control : controls)
                        if (control.parent == content && control.inside(x, y)) control.onClick();
                }
            };
            add(scroll);
            scroll.setRect(cardX + 12, cardY + 12, width, cardH - 24);
        }
    }

    static final class HubButton extends Button {
        private ColorBlock border, fill;
        private BitmapText text;
        private float textScaleX, textScaleY;
        private final Runnable action;
        private final boolean primary;
        private boolean hovered;
        HubButton(String label, boolean primary, Runnable action) {
            this.primary = primary;
            this.action = action;
            label(label);
            palette();
        }

        void useScrollPaneInput() {
            Touchscreen.event.remove(hotArea);
        }

        @Override
        protected void createChildren() {
            super.createChildren();
            add(border = new ColorBlock(1, 1, 0xFFFFFFFF));
            add(fill = new ColorBlock(1, 1, 0xFFFFFFFF));
            add(text = PixelScene.createText(10));
            textScaleX = text.scale.x;
            textScaleY = text.scale.y;
        }
        void label(String value) {
            if (!value.equals(text.text())) {
                text.text(value);
                text.measure();
                layout();
            }
        }

        @Override
        protected void layout() {
            super.layout();
            border.x = x;
            border.y = y;
            border.size(width, height);
            fill.x = x+1;
            fill.y = y+1;
            fill.size(Math.max(0, width-2), Math.max(0, height-2));
            text.scale.set(textScaleX, textScaleY);
            fit(text, Math.max(1, width-12));
            text.x = PixelScene.align(x + (width - text.width()) / 2);
            text.y = PixelScene.align(y + (height - text.baseLine()) / 2 - 1);
        }

        private void palette() {
            boolean lit = hovered || pressed;
            border.hardlight(lit ? 0xE1C18A : primary ? 0xB89962 : 0x795C91);
            fill.hardlight(lit ? 0x38332D : primary ? 0x28262D : 0x1D1E24);
            text.hardlight(primary || lit ? 0xEADBC3 : 0xD8D2DE);
        }
        void hover(boolean value) {
            hovered = value;
            palette();
        }

        @Override
        protected void onTouchDown() {
            palette();
            Sample.INSTANCE.play(Assets.SND_CLICK, .7f, .7f, 1);
        }

        @Override
        protected void onTouchUp() {
            palette();
        }

        @Override
        protected void onClick() {
            action.run();
        }
    }
}

class TitleLayer extends Group {
    float opacity = 1;
    @Override
    public void draw() {
        if (opacity <= 0) return;
        if (opacity >= 1) {
            super.draw();
            return;
        }
        ArrayList<Visual> visuals = new ArrayList<>();
        ArrayList<Float> alphas = new ArrayList<>();
        fade(this, visuals, alphas);
        super.draw();
        for (int i = 0; i < visuals.size(); i++) visuals.get(i).am = alphas.get(i);
    }

    private void fade(Group group, ArrayList<Visual> visuals, ArrayList<Float> alphas) {
        for (int i = 0; i < group.length; i++) {
            Gizmo child = group.memberAt(i);
            if (child instanceof Visual) {
                Visual visual = (Visual)child;
                visuals.add(visual);
                alphas.add(visual.am);
                visual.am *= opacity;
            } else if (child instanceof Group) fade((Group)child, visuals, alphas);
        }
    }
}
