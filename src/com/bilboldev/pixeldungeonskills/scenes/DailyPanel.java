package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.DailyReminders;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.SupporterEntitlements;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.windows.WndDaily;
import com.bilboldev.pixeldungeonskills.windows.WndDailyChallenge;

final class DailyPanel extends TitleHubPanel {
    private static long selectedDay = Long.MIN_VALUE;
    private final WndDaily.Calendar calendar;
    private final RebornChrome detailFrame;
    private WndDailyChallenge.Preview details;
    private long today = DailyRun.today();
    private int tier = SupporterEntitlements.tier();
    private final float detailX, detailY, detailW, maximumHeight;

    DailyPanel(TitleScene host) {
        super(host, Messages.get("daily.menu"));
        float available = screenW - 2 * cardX;
        cardW = portrait ? Math.min(260, available) : Math.min(254, (available - 16) * .44f);
        float rightWidth = portrait ? 0 : Math.min(300, available - cardW - 16);
        cardX = (screenW - cardW - (portrait ? 0 : rightWidth + 16)) / 2;
        cardY = Math.max(86, 48 + target);
        maximumHeight = screenH - cardY - 24;
        if (selectedDay < today - 3 || selectedDay > today + 3) selectedDay = today;

        float left = cardX + 12, top = cardY + 12;
        float calendarWidth = cardW - 24;
        calendar = new WndDaily.Calendar((int)calendarWidth, this::select);
        calendar.select(selectedDay);
        cardH = Math.min(maximumHeight, calendar.height() + 24);
        card();
        ScrollPane dates = calendar.scrollPane(calendar);
        add(dates);
        dates.setRect(left, top, calendarWidth, cardH - 24);
        if (DailyReminders.settings != null) {
            HubButton reminders = button(this, Messages.get("daily.reminders"), false,
                    () -> DailyReminders.settings.configure());
            reminders.icon(Icons.PREFS.get());
            reminders.setRect(left + calendarWidth - 22, top - 5, 22, 22);
        }
        detailX = cardX + cardW + 28;
        detailY = top;
        detailW = portrait ? 0 : rightWidth - 24;
        if (portrait) {
            detailFrame = null;
        } else {
            detailFrame = new RebornChrome(Chrome.Type.WINDOW);
            detailFrame.x = detailX - 12;
            detailFrame.y = cardY;
            add(detailFrame);
            showDetails();
        }
    }

    private void select(long day) {
        selectedDay = day;
        if (portrait) host.add(new WndDailyChallenge(day));
        else showDetails();
    }

    private void showDetails() {
        if (details != null) {
            remove(details);
            details.destroy();
        }
        details = preview();
        float height = Math.min(maximumHeight - 24, details.preferredHeight(detailW));
        detailFrame.size(detailW + 24, height + 24);
        add(details);
        details.setRect(detailX, detailY, detailW, height);
    }

    private WndDailyChallenge.Preview preview() {
        final long day = selectedDay;
        return new WndDailyChallenge.Preview(day, host, () -> {
            TitleScene.leavingForRun();
            DailyRun.start(day);
        });
    }

    @Override public void update() {
        if (today != DailyRun.today() || tier != SupporterEntitlements.tier()) {
            today = DailyRun.today();
            tier = SupporterEntitlements.tier();
            if (selectedDay < today - 3 || selectedDay > today + 3) selectedDay = today;
            calendar.select(selectedDay);
            if (!portrait) showDetails();
        }
        super.update();
    }
}
