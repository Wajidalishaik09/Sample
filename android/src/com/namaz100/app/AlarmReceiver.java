package com.namaz100.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;

/** Shows prayer reminders and always keeps the next one scheduled (also after reboot). */
public class AlarmReceiver extends BroadcastReceiver {
    static final String ACTION = "com.namaz100.app.REMIND";
    static final String CHANNEL = "prayer";
    static final int FLAG_IMMUTABLE = 0x04000000;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            show(ctx, intent.getStringExtra("title"), intent.getStringExtra("body"));
        }
        scheduleNext(ctx);
    }

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("reminders", Context.MODE_PRIVATE); }

    static void store(Context c, String json) { prefs(c).edit().putString("list", json == null ? "[]" : json).apply(); }

    static void scheduleNext(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(c, AlarmReceiver.class).setAction(ACTION);
        try {
            JSONArray list = new JSONArray(prefs(c).getString("list", "[]"));
            long now = System.currentTimeMillis(), best = Long.MAX_VALUE;
            JSONObject next = null;
            for (int k = 0; k < list.length(); k++) {
                JSONObject o = list.getJSONObject(k);
                long t = o.getLong("t");
                if (t > now + 1000 && t < best) { best = t; next = o; }
            }
            if (next == null) {
                PendingIntent old = PendingIntent.getBroadcast(c, 7, i, PendingIntent.FLAG_NO_CREATE | FLAG_IMMUTABLE);
                if (old != null) am.cancel(old);
                return;
            }
            i.putExtra("title", next.optString("title", "Prayer time"));
            i.putExtra("body", next.optString("body", ""));
            PendingIntent pi = PendingIntent.getBroadcast(c, 7, i, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
            boolean exact = true;
            if (Build.VERSION.SDK_INT >= 31) {
                try { exact = (Boolean) AlarmManager.class.getMethod("canScheduleExactAlarms").invoke(am); }
                catch (Exception e) { exact = false; }
            }
            if (exact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, best, pi);
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, best, pi);
        } catch (Exception ignored) { }
    }

    static void show(Context c, String title, String body) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder b = new Notification.Builder(c);
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Class<?> chCls = Class.forName("android.app.NotificationChannel");
                Object ch = chCls.getConstructor(String.class, CharSequence.class, int.class)
                    .newInstance(CHANNEL, "Prayer reminders", 4 /* IMPORTANCE_HIGH */);
                chCls.getMethod("setDescription", String.class).invoke(ch, "Reminders to go to the Masjid for each prayer");
                NotificationManager.class.getMethod("createNotificationChannel", chCls).invoke(nm, ch);
                Method setCh = Notification.Builder.class.getMethod("setChannelId", String.class);
                setCh.invoke(b, CHANNEL);
            } catch (Exception ignored) { }
        }
        Intent open = new Intent(c, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, 1, open, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        b.setSmallIcon(R.drawable.ic_stat)
         .setColor(0xFF0F5A40)
         .setContentTitle(title)
         .setContentText(body)
         .setStyle(new Notification.BigTextStyle().bigText(body))
         .setContentIntent(pi)
         .setAutoCancel(true)
         .setCategory(Notification.CATEGORY_REMINDER)
         .setPriority(Notification.PRIORITY_HIGH)
         .setDefaults(Notification.DEFAULT_ALL);
        try { nm.notify((int) (System.currentTimeMillis() % 100000), b.build()); } catch (Exception ignored) { }
    }
}
