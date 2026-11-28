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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrayerWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TICK = "com.suez.mawaqit.WIDGET_TICK";

    private static final String[] ORDER = {"فجر", "شروق", "ظهر", "عصر", "مغرب", "عشاء"};

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

    /** minutes-of-day -> "4:52 ص" */
    private static String fmt12(int mins) {
        int h = mins / 60, m = mins % 60;
        String p = "ص";
        if (h == 0) h = 12;
        else if (h == 12) p = "م";
        else if (h > 12) { h -= 12; p = "م"; }
        return h + ":" + String.format(Locale.US, "%02d", m) + " " + p;
    }

    /** "2:36 م" -> "2:36" (خلايا الجدول بدون ص/م مثل التصميم) */
    private static String stripPeriod(String disp) {
        if (disp == null) return "";
        return disp.replace(" ص", "").replace(" م", "").trim();
    }

    private static void render(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_prayer);
        SharedPreferences prefs = context.getSharedPreferences("prayer_widget", Context.MODE_PRIVATE);
        String json = prefs.getString("data", null);

        int[] cellIds = {R.id.cFajr, R.id.cShuruq, R.id.cDhuhr, R.id.cAsr, R.id.cMaghrib, R.id.cIsha};
        int[] timeIds = {R.id.tFajr, R.id.tShuruq, R.id.tDhuhr, R.id.tAsr, R.id.tMaghrib, R.id.tIsha};

        if (json == null) {
            views.setTextViewText(R.id.widgetNextName, "—");
            views.setTextViewText(R.id.widgetNextTime, "");
            views.setTextViewText(R.id.widgetCountdown, "افتح التطبيق لتحميل المواقيت");
            for (int i = 0; i < ORDER.length; i++) {
                views.setTextViewText(timeIds[i], "—");
                views.setInt(cellIds[i], "setBackgroundResource", android.R.color.transparent);
            }
            mgr.updateAppWidget(id, views);
            return;
        }

        try {
            JSONObject data = new JSONObject(json);

            Calendar now = Calendar.getInstance();
            int nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

            Map<String, Integer> M = new HashMap<>();
            Map<String, String> D = new HashMap<>();
            JSONArray prayers = data.optJSONArray("prayers");
            if (prayers != null) {
                for (int i = 0; i < prayers.length(); i++) {
                    JSONObject p = prayers.getJSONObject(i);
                    String t = p.optString("time", "");
                    try {
                        String[] hm = t.split(":");
                        String name = p.optString("name", "");
                        M.put(name, Integer.parseInt(hm[0]) * 60 + Integer.parseInt(hm[1]));
                        D.put(name, p.optString("display", ""));
                    } catch (Exception ignored) {}
                }
            }

            // الصلاة القادمة: أول وقت بعد الآن، وإلا فجر الغد
            String nextLabel = null, nextTime = null, highlight = null;
            int diff = -1;
            for (String n : ORDER) {
                Integer v = M.get(n);
                if (v != null && v > nowMins) {
                    nextLabel = n;
                    nextTime = D.get(n);
                    diff = v - nowMins;
                    highlight = n;
                    break;
                }
            }
            if (nextLabel == null) {
                String ft = data.optString("fajrTomorrow", "");
                if (!ft.isEmpty()) {
                    try {
                        String[] hm = ft.split(":");
                        int fm = Integer.parseInt(hm[0]) * 60 + Integer.parseInt(hm[1]);
                        nextLabel = "فجر غدًا";
                        nextTime = fmt12(fm);
                        diff = (1440 - nowMins) + fm;
                    } catch (Exception ignored) {}
                }
            }

            if (nextLabel != null) {
                views.setTextViewText(R.id.widgetNextName, nextLabel);
                views.setTextViewText(R.id.widgetNextTime, nextTime != null ? nextTime : "");
                views.setTextViewText(R.id.widgetCountdown,
                        "متبقي " + String.format(Locale.US, "%02d:%02d", diff / 60, diff % 60));
            } else {
                views.setTextViewText(R.id.widgetNextName, "غدًا");
                views.setTextViewText(R.id.widgetNextTime, "");
                views.setTextViewText(R.id.widgetCountdown, "—");
            }

            // صف المواقيت الستة + إبراز خلية الصلاة القادمة
            for (int i = 0; i < ORDER.length; i++) {
                String disp = D.get(ORDER[i]);
                views.setTextViewText(timeIds[i],
                        disp != null && !stripPeriod(disp).isEmpty() ? stripPeriod(disp) : "—");
                views.setInt(cellIds[i], "setBackgroundResource",
                        ORDER[i].equals(highlight) ? R.drawable.widget_cell_next
                                                  : android.R.color.transparent);
            }
        } catch (Exception e) {
            views.setTextViewText(R.id.widgetCountdown, "تعذر قراءة البيانات");
        }

        mgr.updateAppWidget(id, views);
    }
}
