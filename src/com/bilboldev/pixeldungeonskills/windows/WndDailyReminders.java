package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyReminders;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import java.util.Locale;

public class WndDailyReminders extends WndScrollable {
    private final DailyReminders.Settings settings;
    private final BitmapText hourText;
    private final BitmapText minuteText;
    private final ControlRow clock = new ControlRow();
    private final ReminderTitle heading;
    private final Component content = new Component();
    private final HighlightedText details = new HighlightedText(6);
    private final RedButton toggle;
    private final RedButton save;
    private final RedButton permissions;
    private ScrollPane body;
    private boolean requested;
    private boolean enabled;
    private float stateTimer;
    private int hour;
    private int minute;

    public WndDailyReminders() {
        settings = DailyReminders.settings;
        int time = Math.max(0, Math.min(1439, settings.minute()));
        hour = time / 60;
        minute = time % 60;
        int width = boundedWidth(180);
        requested = settings.requested();
        enabled = settings.enabled();
        heading = new ReminderTitle(enabled);
        heading.setRect(0, 0, width, 0);
        content.add(details);

        clock.setSize(width, 90);
        float left = (width - 106) / 2f;
        for (int column = 0; column < 2; column++) {
            boolean hours = column == 0;
            float x = left + column * 58;
            BitmapText label = PixelScene.createText(Messages.get(hours ? "daily.hour" : "daily.minute"), 6);
            label.measure();
            label.x = x + (48 - label.width()) / 2;
            clock.add(label);
            RedButton up = adjust(hours, 1);
            up.setRect(x, 12, 48, 24);
            RedButton down = adjust(hours, -1);
            down.setRect(x, 66, 48, 24);
        }
        hourText = PixelScene.createText(14);
        minuteText = PixelScene.createText(14);
        clock.add(hourText);
        clock.add(minuteText);
        BitmapText colon = PixelScene.createText(":", 14);
        colon.measure();
        colon.x = width / 2f - colon.width() / 2;
        colon.y = 42;
        clock.add(colon);
        refreshTime();

        toggle = new ActionButton("") {
            @Override protected void onClick() {
                settings.save(hour * 60 + minute, !settings.requested());
                stateTimer = 0;
            }
        };
        float actionWidth = landscapeLayout() ? (width - 5) / 2f : width;
        toggle.setSize(actionWidth, 22);
        save = new ActionButton(Messages.get("daily.reminder_save")) {
            @Override protected void onClick() {
                settings.save(hour * 60 + minute, settings.requested());
                hide();
            }
        };
        save.setSize(actionWidth, 22);
        permissions = new ActionButton(Messages.get("daily.reminder_settings")) {
            @Override protected void onClick() { settings.openSettings(); }
        };
        permissions.setSize(width, 22);
        add(permissions);
        refreshState(width);
    }

    @Override public void update() {
        super.update();
        stateTimer -= Game.elapsed;
        if (stateTimer > 0) return;
        stateTimer = .25f;
        boolean nextRequested = settings.requested();
        boolean nextEnabled = settings.enabled();
        if (requested != nextRequested || enabled != nextEnabled) {
            requested = nextRequested;
            enabled = nextEnabled;
            heading.enabled(enabled);
            refreshState(width);
        }
    }

    private void refreshState(int width) {
        boolean blocked = requested && !enabled;
        details.text(Messages.get(blocked ? "daily.reminder_blocked" : "daily.reminder_details"), textWidth(width));
        content.setSize(width, details.height());
        toggle.text(Messages.get(requested ? "daily.reminders_off" : "daily.reminders_on"));
        permissions.visible = permissions.active = blocked;
        layoutContent(width, heading, content, clock, 5, blocked
                ? new RedButton[]{toggle, save, permissions} : new RedButton[]{toggle, save});
    }

    @Override protected ScrollPane createScrollPane(Component content) {
        if (body == null) body = super.createScrollPane(content);
        return body;
    }

    private RedButton adjust(boolean hours, int step) {
        RedButton button = new RedButton(step > 0 ? "+" : "-") {
            @Override protected void onClick() {
                if (hours) hour = (hour + step + 24) % 24;
                else minute = (minute + step + 60) % 60;
                refreshTime();
            }
        };
        clock.add(button);
        return button;
    }

    private void refreshTime() {
        hourText.text(String.format(Locale.ROOT, "%02d", hour));
        minuteText.text(String.format(Locale.ROOT, "%02d", minute));
        hourText.measure();
        minuteText.measure();
        float left = (clock.width() - 106) / 2f;
        hourText.x = clock.left() + left + (48 - hourText.width()) / 2;
        minuteText.x = clock.left() + left + 58 + (48 - minuteText.width()) / 2;
        hourText.y = minuteText.y = clock.top() + 42;
    }

    private static class ReminderTitle extends IconTitle {
        private final Image state;
        private final MovieClip rat;
        private final TextureFilm frames;
        private final MovieClip.Animation idle;
        private boolean enabled;
        private float resting;
        private float ratY;
        private boolean growing = true;

        ReminderTitle(boolean enabled) {
            this.enabled = enabled;
            resting = enabled ? 0 : 1;
            rat = new MovieClip(Assets.RAT);
            frames = new TextureFilm(rat.texture, 16, 15);
            idle = new MovieClip.Animation(2, true).frames(frames, 0, 0, 0, 1);
            if (enabled) rat.play(idle);
            else rat.frame(frames.get(13));
            rat.scale.set(2);
            icon(rat);
            label(Messages.get("daily.reminders"));
            add(state = new Image());
            stateIcon();
        }

        void enabled(boolean value) {
            enabled = value;
            rat.play(null);
            stateIcon();
            state.alpha(0);
            layout();
        }

        @Override public void update() {
            super.update();
            float goal = enabled ? 0 : 1;
            if (resting != goal) {
                resting = enabled ? Math.max(0, resting - Game.elapsed / .45f)
                        : Math.min(1, resting + Game.elapsed / .45f);
                int frame = Math.round(resting * 3);
                rat.frame(frames.get(frame == 0 ? 0 : 10 + frame));
                if (enabled && resting == 0) rat.play(idle);
            } else {
                state.alpha(Math.min(1, state.am + Game.elapsed / .15f));
            }

            rat.y = ratY + resting * 4;
            float largest = enabled ? 1.3f : 1.2f;
            float step = Game.elapsed * (enabled ? 2 : .5f);
            state.scale.set(growing ? Math.min(largest, state.scale.x + step)
                    : Math.max(1, state.scale.x - step));
            if (state.scale.x >= largest) growing = false;
            else if (state.scale.x <= 1) growing = true;
        }

        private void stateIcon() {
            state.copy(Icons.get(enabled ? Icons.ALERT : Icons.SLEEP));
            state.origin.set(enabled ? 2.5f : state.width / 2, enabled ? state.height - 2.5f : state.height / 2);
            state.scale.set(1);
            growing = true;
        }

        @Override protected void layout() {
            super.layout();
            if (state != null) {
                state.x = imIcon.x + (enabled ? 21 : 3);
                state.y = imIcon.y + (enabled ? 0 : 4);
                ratY = imIcon.y;
                rat.y = ratY + resting * 4;
            }
        }
    }
}
