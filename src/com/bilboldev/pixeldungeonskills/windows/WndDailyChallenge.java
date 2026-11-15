package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.DailyLoadout;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;
import com.bilboldev.pixeldungeonskills.SupporterEntitlements;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.SteamPageButton;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

public class WndDailyChallenge extends WndScrollable {
    private final long day;
    private final long lastToday = DailyRun.today();
    private final int lastTier = SupporterEntitlements.tier();
    private final boolean playing;

    public WndDailyChallenge(long day) {
        this(day, false);
    }

    public WndDailyChallenge(long day, boolean playing) {
        this.day = day;
        this.playing = playing;
        int width = boundedWidth(playing ? 165 : landscapeLayout() ? 280 : 185);
        if (playing) {
            IconTitle heading = new IconTitle(WndDaily.image(day), DailyRun.title(day));
            heading.setRect(0, 0, width, 0);
            HighlightedText description = new HighlightedText(8);
            description.setColor(0xDDD4C5, 0xECD095);
            description.text(DailyRun.objectiveDetails() + "\n\n" + DailyRun.clockText(), textWidth(width), false);
            layoutContent(width, heading, description, 6);
        } else {
            Preview preview = new Preview(day, Game.scene(), this::play);
            add(preview);
            float needed = preview.preferredHeight(width);
            camera.zoom = Math.min(camera.zoom, Game.height * MAX_SCREEN_FRACTION
                    / (needed - preview.description.height() + 16 + frameHeight()));
            resize(width, Math.min((int)Math.ceil(needed), maxFrameHeight() - frameHeight()));
            preview.setRect(0, 0, width, height);
        }
    }

    public static class Preview extends Component {
        private final EventHeading heading;
        private final HighlightedText description;
        private final ScrollPane scroll;
        private final LoadoutRow loadout;
        private final RedButton play;

        public Preview(long day, Group overlays, Runnable start) {
            boolean future = day > DailyRun.today() && !DailyRun.accessible(day);
            Image art = future ? new ItemSprite(ItemSpriteSheet.SMTH, null) : WndDaily.image(day);
            if (future) art.tint(0xC7B694, .8f);
            add(heading = new EventHeading(day, art, future ? "???" : DailyRun.title(day)));
            String text;
            if (DailyRun.accessible(day)) {
                text = DailyRun.briefing(day) + "\n\n" + DailyRun.result(day);
                add(loadout = new LoadoutRow(DailyRun.loadout(day), overlays));
                play = new ActionButton(Messages.get(DailyRun.history().contains(Long.toString(day)) ? "daily.replay" : "daily.play")) {
                    @Override protected void onClick() {
                        if (DailyRun.accessible(day)) start.run();
                    }
                };
            } else {
                loadout = null;
                if (DailyRun.requiredTier(day) < 0) {
                    text = Messages.get(future ? "daily.not_yet" : "daily.expired");
                    play = null;
                } else if (Game.isFreeDesktop()) {
                    text = Messages.get("daily.steam_replays") + "\n\n" + Messages.get("daily.event_returns");
                    play = new SteamPageButton();
                } else {
                    text = "_" + Messages.get("daily.locked_support") + "_\n\n" + Messages.get("daily.event_returns");
                    play = new ActionButton(Messages.get("support.open_google_play")) {
                        @Override protected void onClick() {
                            openUrl("https://play.google.com/store/apps/details?id=com.bilboldev.pixeldungeonskills");
                        }
                    };
                }
            }
            description = new HighlightedText(8);
            description.setColor(0xDDD4C5, 0xECD095);
            description.text(text, 160, false);
            add(scroll = new ScrollPane(description));
            if (play != null) add(play);
        }

        public float preferredHeight(float width) {
            measure(width);
            return heading.height() + description.height() + 6
                    + (loadout == null ? 0 : loadout.height() + 8)
                    + (play == null ? 0 : play.height() + 8);
        }

        private void measure(float width) {
            heading.setRect(x, y, width, 0);
            description.text(description.text(), Math.max(1, (int)width - 4), false);
            if (loadout != null) loadout.setRect(x, y, width, 42);
            if (play != null) {
                play.setSize(width, 28);
                play.setSize(width, Math.max(28, play.reqHeight()));
            }
        }

        @Override protected void layout() {
            measure(width);
            float bottom = bottom();
            if (play != null) {
                play.setPos(x, bottom - play.height());
                bottom = play.top() - 8;
            }
            if (loadout != null) {
                loadout.setPos(x, bottom - loadout.height());
                bottom = loadout.top() - 8;
            }
            float top = heading.bottom() + 6;
            scroll.setRect(x, top, width, Math.max(1, bottom - top));
        }
    }

    private static class EventHeading extends Component {
        private final IconTitle title;
        private final BitmapText date;

        EventHeading(long day, Image art, String name) {
            add(title = new IconTitle(art, name));
            add(date = PixelScene.createText(DailyRun.date(day), 7));
            date.measure();
            date.hardlight(0xBBB3C8);
        }

        @Override protected void layout() {
            title.setRect(x, y, width - date.width() - 6, 0);
            date.x = right() - date.width();
            date.y = y + 3;
            height = Math.max(title.height(), date.height());
        }
    }

    private static class LoadoutRow extends Component {
        private final Image hero;
        private final RenderedTextMultiline label;
        private final RedButton skills, inventory;

        LoadoutRow(DailyLoadout loadout, Group overlays) {
            add(hero = LibraryCatalog.heroImage(loadout.hero.spritesheet(), loadout.hero.tier()));
            hero.scale.set(2);
            String role = (loadout.hero.subClass == HeroSubClass.NONE
                    ? Message.heroClass(loadout.hero.heroClass) : Message.subclass(loadout.hero.subClass, false)).toString();
            add(label = PixelScene.renderMultiline(Messages.get("daily.hero", loadout.hero.lvl, Messages.capitalize(role)), 7));
            label.hardlight(0xDDD4C5);
            add(skills = new IconActionButton(Messages.get("library.skill_tree")) {
                @Override protected void onClick() {
                    overlays.add(new WndSkillsNew(loadout.hero, loadout.freePoints));
                }
            });
            skills.icon(Icons.SKILLS.get());
            add(inventory = new IconActionButton(Messages.get("windows.wndcontrols.action_inventory")) {
                @Override protected void onClick() {
                    overlays.add(new WndBag(loadout.hero, loadout.hero.belongings.backpack));
                }
            });
            inventory.icon(Icons.BACKPACK.get());
        }

        @Override protected void layout() {
            float heroWidth = width / 3;
            label.maxWidth((int)heroWidth - 4);
            float buttonWidth = (width - heroWidth - 8) / 2;
            skills.setRect(x + heroWidth + 4, y, buttonWidth, 42);
            inventory.setRect(skills.right() + 4, y, buttonWidth, 42);
            float heroHeight = hero.height() + 4 + label.height();
            height = Math.max(heroHeight, Math.max(42, Math.max(skills.reqHeight(), inventory.reqHeight())));
            hero.x = PixelScene.align(x + (heroWidth - hero.width()) / 2);
            hero.y = PixelScene.align(y + (height - heroHeight) / 2);
            label.setPos(PixelScene.align(x + (heroWidth - label.width()) / 2), hero.y + hero.height() + 4);
            skills.setSize(buttonWidth, height);
            inventory.setRect(skills.right() + 4, y, buttonWidth, height);
        }
    }

    private void play() {
        if (!DailyRun.accessible(day)) {
            refresh();
            return;
        }
        hide();
        if (Game.scene() instanceof TitleScene) TitleScene.resetPresentation();
        DailyRun.start(day);
    }

    @Override public void update() {
        super.update();
        if (!playing && Window.topWindow() == this
                && (lastToday != DailyRun.today() || lastTier != SupporterEntitlements.tier())) refresh();
    }

    private void refresh() {
        Group host = parent;
        if (host == null) return;
        hide();
        host.add(new WndDailyChallenge(day));
    }
}
