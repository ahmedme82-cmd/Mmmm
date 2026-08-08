package com.suez.mawaqit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class PrayerWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TICK = "com.suez.mawaqit.WIDGET_TICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) render(context, mgr, id);
        scheduleTick(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TICK.equals(intent.getAction())) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(context, PrayerWidgetProvider.class));
            for (int id : ids) render(context, mgr, id);
        }
    }

    public static void refresh(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, PrayerWidgetProvider.class));
        for (int id : ids) render(context, mgr, id);
        scheduleTick(context);
    }

    private static void scheduleTick(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent i = new Intent(ACTION_TICK).setClass(context, PrayerWidgetProvider.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, 60000, pi);
    }

    private static void render(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_prayer);
        SharedPreferences prefs = context.getSharedPreferences("prayer_widget", Context.MODE_PRIVATE);
        String json = prefs.getString("data", null);

        if (json == null) {
            views.setTextViewText(R.id.widgetDate, "افتح التطبيق لتحميل المواقيت");
            views.setTextViewText(R.id.widgetNextName, "—");
            views.setTextViewText(R.id.widgetCountdown, "");
            mgr.updateAppWidget(id, views);
            return;
        }

        try {
            JSONObject data = new JSONObject(json);
            views.setTextViewText(R.id.widgetDate, data.optString("dateLabel", ""));

            Calendar now = Calendar.getInstance();
            Calendar next = null;
            String nextName = null;

            JSONArray prayers = data.optJSONArray("prayers");
            if (prayers != null) {
                for (int i = 0; i < prayers.length(); i++) {
                    JSONObject p = prayers.getJSONObject(i);
                    String t = p.optString("time", "");
                    try {
                        String[] hm = t.split(":");
                        Calendar c = (Calendar) now.clone();
                        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));
                        c.set(Calendar.MINUTE, Integer.parseInt(hm[1]));
                        c.set(Calendar.SECOND, 0);
                        if (c.after(now) && (next == null || c.before(next))) {
                            next = c;
                            nextName = p.optString("name", "");
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (next != null && nextName != null) {
                long diff = next.getTimeInMillis() - now.getTimeInMillis();
                int mins = (int) (diff / 60000);
                views.setTextViewText(R.id.widgetNextName, nextName);
                views.setTextViewText(R.id.widgetCountdown,
                        String.format(Locale.US, "%02d:%02d", mins / 60, mins % 60));
            } else {
                views.setTextViewText(R.id.widgetNextName, "غدًا");
                views.setTextViewText(R.id.widgetCountdown, "—");
            }

            views.removeAllViews(R.id.prayerRow1);
            views.removeAllViews(R.id.prayerRow2);
            if (prayers != null) {
                for (int i = 0; i < prayers.length(); i++) {
                    JSONObject p = prayers.getJSONObject(i);
                    RemoteViews cell = new RemoteViews(context.getPackageName(), R.layout.widget_cell);
                    cell.setTextViewText(R.id.cellName, p.optString("name", ""));
                    cell.setTextViewText(R.id.cellTime, p.optString("display", ""));
                    if (p.optString("name", "").equals(nextName)) {
                        cell.setInt(R.id.cellRoot, "setBackgroundResource", R.drawable.widget_cell_next);
                    }
                    views.addView(i < 3 ? R.id.prayerRow1 : R.id.prayerRow2, cell);
                }
            }

            JSONArray karaha = data.optJSONArray("karaha");
            views.removeAllViews(R.id.karahRow);
            if (karaha != null) {
                for (int i = 0; i < karaha.length(); i++) {
                    JSONObject k = karaha.getJSONObject(i);
                    RemoteViews cell = new RemoteViews(context.getPackageName(), R.layout.widget_kcell);
                    cell.setTextViewText(R.id.kName, k.optString("name", ""));
                    cell.setTextViewText(R.id.kRange, k.optString("range", ""));
                    views.addView(R.id.karahRow, cell);
                }
            }
        } catch (Exception e) {
            views.setTextViewText(R.id.widgetDate, "تعذر قراءة البيانات");
        }

        mgr.updateAppWidget(id, views);
    }
}
