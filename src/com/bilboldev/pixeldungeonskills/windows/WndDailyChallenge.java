package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.DailyLoadout;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.noosa.Game;
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

public class WndDailyChallenge extends WndScrollable {
    private final long day;
    private final long lastToday = DailyRun.today();
    private final int lastTier = SupporterEntitlements.tier();
    private final RedButton play;
    private final boolean playing;

    public WndDailyChallenge(long day) {
        this(day, false);
    }

    public WndDailyChallenge(long day, boolean playing) {
        this.day = day;
        this.playing = playing;
        int width = boundedWidth(playing ? 165 : landscapeLayout() ? 280 : 185);
        int bodyWidth = textWidth(width);
        boolean future = day > lastToday;
        Image art = future && !DailyRun.accessible(day) ? new ItemSprite(ItemSpriteSheet.SMTH, null) : WndDaily.image(day);
        if (future && !DailyRun.accessible(day)) art.tint(0xC7B694, .8f);
        Component heading = playing ? new IconTitle(art, DailyRun.title(day))
                : new EventHeading(art, future && !DailyRun.accessible(day) ? "???" : DailyRun.title(day));
        heading.setRect(0, 0, width, 0);
        String text = "";
        Component loadout = null;
        if (playing) {
            text = DailyRun.objectiveDetails() + "\n\n" + DailyRun.clockText();
            play = null;
        } else if (DailyRun.accessible(day)) {
            text = DailyRun.briefing(day) + "\n\n" + DailyRun.result(day);
            loadout = new LoadoutRow(DailyRun.loadout(day));
            loadout.setSize(width, 42);
            play = new ActionButton(Messages.get(DailyRun.history().contains(Long.toString(day)) ? "daily.replay" : "daily.play")) {
                @Override protected void onClick() { play(); }
            };
        } else if (DailyRun.requiredTier(day) < 0) {
            text = Messages.get(future ? "daily.not_yet" : "daily.expired");
            play = null;
        } else if (Game.isFreeDesktop()) {
            text = Messages.get("daily.steam_replays") + "\n\n" + Messages.get("daily.event_returns");
            play = new SteamPageButton();
        } else {
            text = "_" + Messages.get("daily.locked_support") + "_\n\n"
                    + Messages.get("daily.event_returns");
            play = new ActionButton(Messages.get("support.open_google_play")) {
                @Override protected void onClick() {
                    openUrl("https://play.google.com/store/apps/details?id=com.bilboldev.pixeldungeonskills");
                }
            };
        }
        HighlightedText description = new HighlightedText(8);
        description.setColor(0xDDD4C5, 0xECD095);
        description.text(text, bodyWidth, false);
        if (play != null) {
            play.setSize(width, 25);
            layoutContent(width, heading, description, loadout, 6, play);
        } else layoutContent(width, heading, description, 6);
    }

    private class EventHeading extends Component {
        private final IconTitle title;
        private final BitmapText date;

        EventHeading(Image art, String name) {
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

    private class LoadoutRow extends Component {
        private final Image hero;
        private final RenderedTextMultiline label;
        private final RedButton skills, inventory;

        LoadoutRow(DailyLoadout loadout) {
            add(hero = LibraryCatalog.heroImage(loadout.hero.spritesheet(), loadout.hero.tier()));
            hero.scale.set(2);
            String role = (loadout.hero.subClass == HeroSubClass.NONE
                    ? Message.heroClass(loadout.hero.heroClass) : Message.subclass(loadout.hero.subClass, false)).toString();
            add(label = PixelScene.renderMultiline(Messages.get("daily.hero", loadout.hero.lvl, Messages.capitalize(role)), 7));
            label.hardlight(0xDDD4C5);
            add(skills = new IconActionButton(Messages.get("library.skill_tree")) {
                @Override protected void onClick() {
                    WndDailyChallenge.this.parent.add(new WndSkillsNew(loadout.hero, loadout.freePoints));
                }
            });
            skills.icon(Icons.SKILLS.get());
            add(inventory = new IconActionButton(Messages.get("windows.wndcontrols.action_inventory")) {
                @Override protected void onClick() {
                    WndDailyChallenge.this.parent.add(new WndBag(loadout.hero, loadout.hero.belongings.backpack));
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
