package com.bilboldev.pixeldungeonskills;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.bilboldev.pixeldungeonskills.messages.Languages;
import com.bilboldev.pixeldungeonskills.network.PartyConnection;
import com.bilboldev.utils.Bundle;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AndroidDailyReminders extends BroadcastReceiver {
    private static final String CHANNEL = "daily-events";
    private static final String ALARM = "com.bilboldev.pixeldungeonskills.DAILY_REMINDER";
    private static final int PERMISSION = 2713;

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("daily-reminders", Context.MODE_PRIVATE);
    }

    public static void install(PixelDungeon activity) {
        DailyReminders.settings = new DailyReminders.Settings() {
            @Override public boolean enabled() { return preferences(activity).getBoolean("enabled", false) && allowed(activity); }
            @Override public boolean requested() { return preferences(activity).getBoolean("enabled", false); }
            @Override public int minute() { return preferences(activity).getInt("minute", 15 * 60); }
            @Override public void save(int minute, boolean enabled) {
                if (minute < 0 || minute >= 24 * 60) throw new IllegalArgumentException("Invalid reminder time");
                activity.runOnUiThread(() -> {
                    preferences(activity).edit().putInt("minute", minute).apply();
                    if (enabled) {
                        enable(activity);
                    } else {
                        preferences(activity).edit().putBoolean("enabled", false).apply();
                        schedule(activity);
                        cancelNotice(activity);
                    }
                });
            }
            @Override public void openSettings() { activity.runOnUiThread(() -> AndroidDailyReminders.openSettings(activity)); }
            @Override public void played(long day) {
                activity.runOnUiThread(() -> {
                    if (day != DailyRun.today()) return;
                    cancelNotice(activity);
                    schedule(activity);
                });
            }
        };
        opened(activity.getIntent());
    }

    private static void configureTime(PixelDungeon activity) {
        if (!activity.isFinishing()) DailyReminders.configureRequested = true;
    }

    private static void enable(PixelDungeon activity) {
        SharedPreferences prefs = preferences(activity);
        prefs.edit().putBoolean("enabled", true)
                .putLong("expires", System.currentTimeMillis() + 3 * 86_400_000L).apply();
        channel(activity);
        if (Build.VERSION.SDK_INT >= 33 && activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            if (prefs.getBoolean("permission_asked", false)
                    && !activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                configureTime(activity);
            } else {
                prefs.edit().putBoolean("permission_asked", true).apply();
                activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION);
            }
        } else {
            schedule(activity);
        }
    }

    public static void permissionResult(PixelDungeon activity, int request, int[] grants) {
        if (request != PERMISSION) return;
        schedule(activity);
        if (!allowed(activity)) configureTime(activity);
    }

    public static void refresh(Context context) {
        SharedPreferences prefs = preferences(context);
        if (!prefs.getBoolean("enabled", false)) return;
        long now = System.currentTimeMillis();
        boolean expired = prefs.getLong("expires", 0) <= now;
        prefs.edit().putLong("expires", now + 3 * 86_400_000L).apply();
        if (!expired) {

            for (int i = 0; i < 3; i++) {
                if (alarm(context, i, 0, PendingIntent.FLAG_NO_CREATE) != null) return;
            }
        }
        schedule(context);
    }

    public static void opened(Intent intent) {
        if (intent != null && intent.getBooleanExtra("open_daily", false)) {
            intent.removeExtra("open_daily");
            DailyReminders.requested = true;
        }
    }

    private static boolean allowed(Context context) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return false;
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || (Build.VERSION.SDK_INT >= 24 && !manager.areNotificationsEnabled())) return false;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = manager.getNotificationChannel(CHANNEL);
            if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) return false;
        }
        return true;
    }

    private static void openSettings(Context context) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean channelBlocked = manager != null && manager.areNotificationsEnabled();
            intent = new Intent(channelBlocked ? Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS : Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            if (channelBlocked) intent.putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL);
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    private static void cancelNotice(Context context) {
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(9104);
    }

    private static Context notificationContext(Context context) {
        String code = context.getSharedPreferences("PixelDungeon", Context.MODE_PRIVATE).getString("language", null);
        Languages language = code == null ? Languages.matchLocale(Locale.getDefault()) : Languages.matchCode(code);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(language.locale());
        return context.createConfigurationContext(config);
    }

    private static void channel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.createNotificationChannel(new NotificationChannel(CHANNEL,
                notificationContext(context).getString(R.string.daily_reminder_title), NotificationManager.IMPORTANCE_LOW));
    }

    private static PendingIntent alarm(Context context, int index, long day, int flags) {
        Intent intent = new Intent(context, AndroidDailyReminders.class).setAction(ALARM).putExtra("scheduled_day", day);
        return PendingIntent.getBroadcast(context, 9100 + index, intent,
                flags | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
    }

    private static void schedule(Context context) {
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        for (int i = 0; i < 3; i++) {
            PendingIntent pending = alarm(context, i, 0, PendingIntent.FLAG_NO_CREATE);
            if (pending != null) {
                manager.cancel(pending);
                pending.cancel();
            }
        }
        SharedPreferences prefs = preferences(context);
        long now = System.currentTimeMillis(), expires = prefs.getLong("expires", 0);
        if (!prefs.getBoolean("enabled", false) || expires <= now || !allowed(context)) return;
        Calendar time = Calendar.getInstance();
        int minute = prefs.getInt("minute", 15 * 60);
        time.set(Calendar.HOUR_OF_DAY, minute / 60);
        time.set(Calendar.MINUTE, minute % 60);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        if (time.getTimeInMillis() <= now) time.add(Calendar.DATE, 1);
        long today = DailyRun.today();
        for (int i = 0; i < 3; i++, time.add(Calendar.DATE, 1)) {
            long trigger = time.getTimeInMillis();
            if (trigger > expires) break;
            long day = Math.floorDiv(trigger + time.getTimeZone().getOffset(trigger), 86_400_000L);
            if (day == today && (prefs.getLong("last_day", -1) == day || played(context, day))) continue;
            manager.setWindow(AlarmManager.RTC_WAKEUP, trigger, 60 * 60_000L,
                    alarm(context, i, day, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    private static boolean played(Context context, long day) {
        try (InputStream input = context.openFileInput(DailyRun.HISTORY)) {
            Bundle history = Bundle.read(input);
            if (history == null) return false;
            Bundle entry = history.getBundle(Long.toString(day));
            return !entry.isNull() && entry.getInt("attempts") > 0 && entry.getInt("revision") == DailyRun.REVISION
                    && PartyConnection.FORK_ID.equals(entry.getString("fork"));
        } catch (IOException missing) {
            return false;
        }
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (!ALARM.equals(intent.getAction())) {
            schedule(context);
            return;
        }
        long now = System.currentTimeMillis();
        long day = Math.floorDiv(now + TimeZone.getDefault().getOffset(now), 86_400_000L);
        if (intent.getLongExtra("scheduled_day", day) != day) return;
        try {
            SharedPreferences prefs = preferences(context);
            if (!prefs.getBoolean("enabled", false) || prefs.getLong("expires", 0) < now
                    || prefs.getLong("last_day", -1) == day || !allowed(context)
                    || played(context, day)) return;
            NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;
            Context translated = notificationContext(context);
            int event = DailyRun.scenario(day);
            String title = translated.getResources().getStringArray(R.array.daily_reminder_events)[event];
            String body = translated.getResources().getStringArray(R.array.daily_reminder_messages)[event];
            channel(context);
            Intent launch = new Intent(context, PixelDungeon.class).putExtra("open_daily", true)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent open = PendingIntent.getActivity(context, 9104, launch,
                    PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
            Notification.Builder notice = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, CHANNEL) : new Notification.Builder(context);
            notice.setSmallIcon(R.drawable.ic_daily_reminder)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_daily_logo))
                    .setContentTitle(title)
                    .setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body))
                    .setContentIntent(open).setOnlyAlertOnce(true);
            manager.notify(9104, notice.build());
            prefs.edit().putLong("last_day", day).apply();
        } catch (SecurityException denied) {
            return;
        } finally {
            schedule(context);
        }
    }
}
