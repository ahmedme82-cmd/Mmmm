package com.suez.mawaqit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    private static float[] pt(float t, float x0, float y0, float cx, float cy, float x1, float y1) {
        float u = 1 - t;
        return new float[]{u*u*x0 + 2*u*t*cx + t*t*x1, u*u*y0 + 2*u*t*cy + t*t*y1};
    }

    private static Bitmap buildArc(int rise, int set, int nowMins, Map<String,Integer> M, List<int[]> kar) {
        int W = 800, H = 250;
        Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        float x0=760, y0=195, x1=40, y1=195, cx=400, cy=8;
        float span = Math.max(1, set - rise);

        Paint horizon = new Paint(Paint.ANTI_ALIAS_FLAG);
        horizon.setStyle(Paint.Style.STROKE); horizon.setStrokeWidth(2);
        horizon.setColor(0x1FFFFFFF); horizon.setPathEffect(new DashPathEffect(new float[]{4,10},0));
        cv.drawLine(30, y0, 770, y0, horizon);

        Path full = new Path();
        full.moveTo(x0, y0); full.quadTo(cx, cy, x1, y1);

        Paint faint = new Paint(Paint.ANTI_ALIAS_FLAG);
        faint.setStyle(Paint.Style.STROKE); faint.setStrokeWidth(5);
        faint.setColor(0x29FFFFFF); faint.setStrokeCap(Paint.Cap.ROUND);
        cv.drawPath(full, faint);

        PathMeasure pm = new PathMeasure(full, false);
        float len = pm.getLength();

        Paint terra = new Paint(Paint.ANTI_ALIAS_FLAG);
        terra.setStyle(Paint.Style.STROKE); terra.setStrokeWidth(11);
        terra.setColor(0xFFC46A5A); terra.setStrokeCap(Paint.Cap.ROUND);
        for (int[] r : kar) {
            float t1 = Math.max(0, Math.min(1, (r[0]-rise)/span));
            float t2 = Math.max(0, Math.min(1, (r[1]-rise)/span));
            if (t2 <= t1) continue;
            Path seg = new Path();
            pm.getSegment(len*t1, len*t2, seg, true);
            cv.drawPath(seg, terra);
        }

        float tc = Math.max(0, Math.min(1, (nowMins-rise)/span));
        if (tc > 0.002f) {
            Paint gold = new Paint(Paint.ANTI_ALIAS_FLAG);
            gold.setStyle(Paint.Style.STROKE); gold.setStrokeWidth(8);
            gold.setColor(0xFFD3A545); gold.setStrokeCap(Paint.Cap.ROUND);
            Path prog = new Path();
            pm.getSegment(0, len*tc, prog, true);
            cv.drawPath(prog, gold);
        }

        Paint dotStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotStroke.setStyle(Paint.Style.STROKE); dotStroke.setStrokeWidth(5); dotStroke.setColor(0xFFD3A545);
        Paint dotFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotFill.setStyle(Paint.Style.FILL); dotFill.setColor(0xFF0B3D36);
        Paint lbl = new Paint(Paint.ANTI_ALIAS_FLAG);
        lbl.setColor(0xB3FFFFFF); lbl.setTextSize(26); lbl.setTextAlign(Paint.Align.CENTER);
        String[] names = {"شروق","ظهر","عصر","مغرب"};
        for (String n : names) {
            Integer mv = M.get(n); if (mv == null) continue;
            float[] q = pt((mv-rise)/span, x0,y0,cx,cy,x1,y1);
            cv.drawCircle(q[0], q[1], 9, dotFill);
            cv.drawCircle(q[0], q[1], 9, dotStroke);
            cv.drawText(n, q[0], q[1]+42, lbl);
        }

        float[] s = pt(tc, x0,y0,cx,cy,x1,y1);
        Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        glow.setStyle(Paint.Style.FILL); glow.setColor(0x38D3A545);
        cv.drawCircle(s[0], s[1], 20, glow);
        Paint sunF = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunF.setStyle(Paint.Style.FILL); sunF.setColor(0xFFE6B354);
        cv.drawCircle(s[0], s[1], 9, sunF);
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE); ring.setStrokeWidth(3); ring.setColor(0xFFFFF3D6);
        cv.drawCircle(s[0], s[1], 9, ring);

        return bmp;
    }

    private static int mk(String h, String mi, String p) {
        int hh = Integer.parseInt(h);
        if ("م".equals(p) && hh < 12) hh += 12;
        if ("ص".equals(p) && hh == 12) hh = 0;
        return hh*60 + Integer.parseInt(mi);
    }

    private static int[] parseRange(String s) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d{1,2}):(\\d{2})\\s*[-–]\\s*(\\d{1,2}):(\\d{2})\\s*(ص|م)").matcher(s);
        if (!m.find()) return null;
        return new int[]{ mk(m.group(1), m.group(2), m.group(5)), mk(m.group(3), m.group(4), m.group(5)) };
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
            int nowMins = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE);

            Map<String,Integer> M = new HashMap<>();
            Map<String,String> D = new HashMap<>();
            JSONArray prayers = data.optJSONArray("prayers");
            if (prayers != null) {
                for (int i=0;i<prayers.length();i++) {
                    JSONObject p = prayers.getJSONObject(i);
                    String t = p.optString("time","");
                    try {
                        String[] hm = t.split(":");
                        String name = p.optString("name","");
                        M.put(name, Integer.parseInt(hm[0])*60 + Integer.parseInt(hm[1]));
                        D.put(name, p.optString("display",""));
                    } catch (Exception ignored) {}
                }
            }
            Integer riseI = M.get("شروق"), setI = M.get("مغرب");
            int rise = riseI != null ? riseI : 0;
            int set  = setI != null ? setI : 0;

            String[] order = {"فجر","شروق","ظهر","عصر","مغرب","عشاء"};
            String nextName = null; int nextM = 0;
            for (String n : order) {
                Integer v = M.get(n);
                if (v != null && v > nowMins) { nextName = n; nextM = v; break; }
            }
            if (nextName != null) {
                int df = nextM - nowMins;
                views.setTextViewText(R.id.widgetNextName, nextName);
                views.setTextViewText(R.id.widgetCountdown, String.format(Locale.US, "%02d:%02d", df/60, df%60));
            } else {
                views.setTextViewText(R.id.widgetNextName, "غدًا");
                views.setTextViewText(R.id.widgetCountdown, "—");
            }

            Map<String,Integer> timeIds = new HashMap<>();
            timeIds.put("فجر", R.id.tFajr); timeIds.put("شروق", R.id.tShuruq);
            timeIds.put("ظهر", R.id.tDhuhr); timeIds.put("عصر", R.id.tAsr);
            timeIds.put("مغرب", R.id.tMaghrib); timeIds.put("عشاء", R.id.tIsha);
            for (Map.Entry<String,Integer> e : timeIds.entrySet()) {
                String disp = D.get(e.getKey());
                views.setTextViewText(e.getValue(), disp != null ? disp : "—");
            }

            List<int[]> kar = new ArrayList<>();
            int[] nameIds = {R.id.k1n, R.id.k2n, R.id.k3n};
            int[] rangeIds = {R.id.k1r, R.id.k2r, R.id.k3r};
            JSONArray karaha = data.optJSONArray("karaha");
            if (karaha != null) {
                for (int i=0; i<karaha.length() && i<3; i++) {
                    JSONObject k = karaha.getJSONObject(i);
                    views.setTextViewText(nameIds[i], k.optString("name",""));
                    views.setTextViewText(rangeIds[i], k.optString("range",""));
                    int[] r = parseRange(k.optString("range",""));
                    if (r != null) kar.add(r);
                }
            }

            if (rise > 0 && set > rise) {
                views.setImageViewBitmap(R.id.widgetArc, buildArc(rise, set, nowMins, M, kar));
            }
        } catch (Exception e) {
            views.setTextViewText(R.id.widgetDate, "تعذر قراءة البيانات");
        }

        mgr.updateAppWidget(id, views);
    }
}