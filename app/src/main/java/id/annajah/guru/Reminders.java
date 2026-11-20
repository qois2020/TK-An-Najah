package id.annajah.guru;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Pengingat agenda & tugas guru. Daftar pengingat dikirim dari halaman web
 * (AndroidNotify.sync), disimpan, lalu dijadwalkan dengan AlarmManager.
 * Setelah HP dinyalakan ulang / aplikasi diperbarui, jadwal dipasang lagi.
 */
public class Reminders extends BroadcastReceiver {

    static final String PREF = "pengingat";
    static final String CHANNEL = "pengingat_agenda";
    static final String ACTION_FIRE = "id.annajah.guru.PENGINGAT";
    static final int BASE = 1000;
    static final int MAX = 60;

    public static void sync(Context c, String json) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString("json", json).apply();
        scheduleAll(c);
    }

    public static void scheduleAll(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        int old = p.getInt("count", 0);
        for (int i = 0; i < old; i++) {
            PendingIntent pi = PendingIntent.getBroadcast(c, BASE + i, fireIntent(c),
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pi != null) { am.cancel(pi); pi.cancel(); }
        }

        JSONArray arr;
        try { arr = new JSONArray(p.getString("json", "[]")); } catch (Exception e) { arr = new JSONArray(); }
        long now = System.currentTimeMillis();
        int n = 0;
        for (int i = 0; i < arr.length() && n < MAX; i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            long t = o.optLong("t", 0);
            if (t <= now) continue;
            Intent it = fireIntent(c)
                    .putExtra("title", o.optString("title", "Pengingat"))
                    .putExtra("text", o.optString("text", ""))
                    .putExtra("nid", n);
            PendingIntent pi = PendingIntent.getBroadcast(c, BASE + n, it,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
            n++;
        }
        p.edit().putInt("count", n).apply();
    }

    private static Intent fireIntent(Context c) {
        return new Intent(c, Reminders.class).setAction(ACTION_FIRE);
    }

    @Override
    public void onReceive(Context c, Intent i) {
        String a = i.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            scheduleAll(c);
            return;
        }
        if (ACTION_FIRE.equals(a)) {
            show(c, i.getStringExtra("title"), i.getStringExtra("text"), i.getIntExtra("nid", 0));
        }
    }

    static void show(Context c, String title, String text, int nid) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Pengingat agenda & tugas",
                    NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Pengingat agenda kelas dan tugas guru");
            nm.createNotificationChannel(ch);
            b = new Notification.Builder(c, CHANNEL);
        } else {
            b = new Notification.Builder(c);
        }
        Intent open = new Intent(c, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String body = text == null ? "" : text;
        b.setSmallIcon(R.drawable.ic_notif)
                .setColor(Color.parseColor("#14B8A6"))
                .setContentTitle(title == null ? "Pengingat" : title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pi);
        nm.notify(2000 + nid, b.build());
    }
}
