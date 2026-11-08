package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.DailyReminders;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.SupporterEntitlements;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.utils.Bundle;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WndDaily extends WndScrollable {
    private final ControlRow content = new ControlRow();
    private final DayButton[] days = new DayButton[7];
    private final HighlightedText date = new HighlightedText(7);
    private final HighlightedText event = new HighlightedText(9);
    private Image emblem;
    private long displayedDay = Long.MIN_VALUE;
    private int shownTier = -1;
    private final int bodyWidth;
    private final float emblemTop;

    @Override protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.PARCHMENT;
    }

    public WndDaily() {
        int width = boundedWidth(landscapeLayout() ? 300 : 228);
        bodyWidth = textWidth(width);
        HighlightedText title = new HighlightedText(12);
        title.setColor(0xE7C98F, 0xE7C98F);
        title.text(Messages.get("daily.menu"), bodyWidth - 12, false);
        title.setPos((bodyWidth - title.width()) / 2, 3);
        content.add(title);
        date.setColor(0xADA496, 0xADA496);
        content.add(date);
        date.setPos(0, title.bottom() + 5);
        date.text(DailyRun.date(DailyRun.today()), bodyWidth, false);
        emblemTop = date.bottom() + 12;

        RebornChrome mount = new RebornChrome(Chrome.Type.BUTTON).material(RebornChrome.Material.PARCHMENT);
        mount.x = (bodyWidth - 54) / 2f;
        mount.y = emblemTop - 3;
        mount.size(54, 54);
        mount.highlight(true);
        content.add(mount);
        rule(5, emblemTop + 22, mount.x - 12);
        rule(mount.x + 61, emblemTop + 22, bodyWidth - mount.x - 66);

        event.setColor(0xE3D7C3, 0xE3D7C3);
        content.add(event);
        float eventHeight = 0;
        for (int i = 0; i < DailyRun.EVENTS; i++) {
            event.text(Messages.get("daily.title_" + i), bodyWidth - 8, false);
            eventHeight = Math.max(eventHeight, event.height());
        }
        float rowTop = emblemTop + 58 + eventHeight + 10;
        float cell = (float)Math.floor((bodyWidth - 12) / 7f);
        float left = (bodyWidth - (cell * 7 + 12)) / 2;
        for (int i = 0; i < days.length; i++) {
            days[i] = new DayButton();
            days[i].setRect(left + i * (cell + 2), rowTop, cell, cell);
            content.add(days[i]);
        }
        refresh();
        HighlightedText rules = new HighlightedText(7);
        rules.setColor(0xB9B1A4, 0xE7C98F);
        rules.text(Messages.get("daily.rules"), bodyWidth, false);
        rules.setPos(0, rowTop + cell + 26);
        content.add(rules);
        content.setSize(bodyWidth, rules.bottom());
        if (DailyReminders.settings != null) {
            RedButton reminders = new ActionButton(Messages.get("daily.reminders")) {
                @Override protected void onClick() { DailyReminders.settings.configure(); }
            };
            reminders.setSize(width, 24);
            layoutContent(width, null, content, 5, reminders);
        } else layoutContent(width, null, content, 0);
    }

    private void rule(float x, float y, float length) {
        ColorBlock shadow = new ColorBlock(length, 1, 0xFF181717);
        shadow.x = x;
        shadow.y = y + 1;
        content.add(shadow);
        ColorBlock line = new ColorBlock(length, 1, 0xFF665640);
        line.x = x;
        line.y = y;
        content.add(line);
    }

    public static Image image(long day) {
        Image image = new Image(Assets.DAILY_CHALLENGES);
        image.frame(DailyRun.scenario(day) * 24, 0, 24, 24);
        return image;
    }

    @Override public void update() {
        super.update();
        if (displayedDay != DailyRun.today() || shownTier != SupporterEntitlements.tier()) refresh();
    }

    private void refresh() {
        displayedDay = DailyRun.today();
        shownTier = SupporterEntitlements.tier();
        date.text(DailyRun.date(displayedDay), bodyWidth, false);
        date.setPos(content.left() + (bodyWidth - date.width()) / 2, date.top());
        if (emblem != null) {
            content.remove(emblem);
            emblem.destroy();
        }
        emblem = image(displayedDay);
        emblem.scale.set(2);
        emblem.x = PixelScene.align(content.left() + (bodyWidth - emblem.width()) / 2);
        emblem.y = content.top() + emblemTop;
        content.add(emblem);
        event.text(DailyRun.title(displayedDay), bodyWidth - 8, false);
        event.setPos(content.left() + (bodyWidth - event.width()) / 2, content.top() + emblemTop + 57);
        Bundle history = DailyRun.history();
        for (int i = 0; i < days.length; i++) {
            long day = displayedDay + 3 - i;
            days[i].day(day, history.getBundle(Long.toString(day)));
        }
    }

    private void openDay(long day) {
        if (day > DailyRun.today()) return;
        parent.add(new WndDailyChallenge(day));
    }

    @Override protected ScrollPane createScrollPane(Component body) { return content.scrollPane(body); }

    private class DayButton extends RedButton {
        private long day;
        private Image art;
        private final Image marker = new Image(Assets.DAILY_CHALLENGES);
        private final HighlightedText caption = new HighlightedText(6);

        DayButton() {
            super("");
            content.addControl(hotArea);
            add(caption);
            marker.frame(0, 24, 10, 10);
            marker.visible = false;
            add(marker);
        }

        void day(long value, Bundle record) {
            day = value;
            if (art != null) {
                remove(art);
                art.destroy();
            }
            boolean future = day > displayedDay;
            art = future ? new ItemSprite(ItemSpriteSheet.SMTH, null) : image(day);
            if (future) art.tint(0xC7B694, .8f);
            art.scale.set(Math.min(1, (width - 4) / art.width()));
            art.alpha(future ? .8f : DailyRun.accessible(day) ? 1f : .8f);
            add(art);
            int result = -1;
            if (!future) {
                if (record.isNull()) {
                    if (day < displayedDay) result = 2;
                } else if (record.getInt("best") > 0 || record.getInt("first") == 1) result = 0;
                else if (record.getInt("first") >= 2) result = 1;
            }
            marker.visible = result >= 0;
            if (marker.visible) marker.frame(result * 10, 24, 10, 10);
            bringToFront(marker);
            enable(!future);
            if (bg instanceof RebornChrome) {
                ((RebornChrome)bg).material(day == displayedDay ? RebornChrome.Material.PARCHMENT : RebornChrome.Material.IRON);
                ((RebornChrome)bg).highlight(day == displayedDay);
            }
            SimpleDateFormat format = new SimpleDateFormat("d MMM", Messages.locale());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            caption.setColor(day == displayedDay ? 0xE7C98F : future ? 0x756F67 : 0xB9B1A4, 0xE7C98F);
            caption.text(day == displayedDay ? Messages.get("daily.today") : format.format(new Date(day * 86_400_000L)),
                    Math.max(1, (int)width), false);
            layout();
        }

        @Override protected void layout() {
            super.layout();
            if (art == null) return;
            art.x = PixelScene.align(x + (width - art.width()) / 2);
            art.y = PixelScene.align(y + (height - art.height()) / 2);
            marker.x = PixelScene.align(right() - marker.width() - 1);
            marker.y = PixelScene.align(y + 1);
            caption.setPos(x + (width - caption.width()) / 2, bottom() + 4);
        }

        @Override protected void onTouchUp() {
            super.onTouchUp();
            if (bg instanceof RebornChrome) ((RebornChrome)bg).highlight(day == displayedDay);
        }

        @Override protected void onClick() { openDay(day); }
    }
}
