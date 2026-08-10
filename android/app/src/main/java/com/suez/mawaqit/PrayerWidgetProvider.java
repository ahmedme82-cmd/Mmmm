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

    private static float[] pt(float t, float ax, float ay, float cx, float cy, float bx, float by) {
        float u = 1 - t;
        return new float[]{u*u*ax + 2*u*t*cx + t*t*bx, u*u*ay + 2*u*t*cy + t*t*by};
    }

    private static Bitmap buildArc(int rise, int set, int nowMins, Map<String,Integer> M, List<int[]> kar) {
        int W = 800, H = 400;
        Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        float x0=760, y0=235, x1=40, y1=235, cdx=400, cdy=8, cnx=400, cny=455;
        float span = Math.max(1, set - rise);
        float nightSpan = Math.max(1, 1440 - (set - rise));

        Paint horizon = new Paint(Paint.ANTI_ALIAS_FLAG);
        horizon.setStyle(Paint.Style.STROKE); horizon.setStrokeWidth(2);
        horizon.setColor(0x1FFFFFFF); horizon.setPathEffect(new DashPathEffect(new float[]{4,10},0));
        cv.drawLine(30, y0, 770, y0, horizon);

        Path day = new Path();   day.moveTo(x0,y0);   day.quadTo(cdx,cdy,x1,y1);
        Path night = new Path(); night.moveTo(x1,y1); night.quadTo(cnx,cny,x0,y0);

        Paint faint = new Paint(Paint.ANTI_ALIAS_FLAG);
        faint.setStyle(Paint.Style.STROKE); faint.setStrokeWidth(4);
        faint.setColor(0x29FFFFFF); faint.setStrokeCap(Paint.Cap.ROUND);
        cv.drawPath(day, faint);

        Paint faintN = new Paint(Paint.ANTI_ALIAS_FLAG);
        faintN.setStyle(Paint.Style.STROKE); faintN.setStrokeWidth(4);
        faintN.setColor(0x1AFFFFFF); faintN.setStrokeCap(Paint.Cap.ROUND);
        faintN.setPathEffect(new DashPathEffect(new float[]{2,10},0));
        cv.drawPath(night, faintN);

        PathMeasure pmD = new PathMeasure(day, false);   float lenD = pmD.getLength();
        PathMeasure pmN = new PathMeasure(night, false); float lenN = pmN.getLength();

        Paint terra = new Paint(Paint.ANTI_ALIAS_FLAG);
        terra.setStyle(Paint.Style.STROKE); terra.setStrokeWidth(11);
        terra.setColor(0xFFC46A5A); terra.setStrokeCap(Paint.Cap.ROUND);
        for (int[] r : kar) {
            float t1 = Math.max(0, Math.min(1, (r[0]-rise)/span));
            float t2 = Math.max(0, Math.min(1, (r[1]-rise)/span));
            if (t2 <= t1) continue;
            Path seg = new Path();
            pmD.getSegment(lenD*t1, lenD*t2, seg, true);
            cv.drawPath(seg, terra);
        }

        boolean isDay = nowMins >= rise && nowMins < set;
        float tc = Math.max(0, Math.min(1, (nowMins-rise)/span));
        float eN = (nowMins >= set) ? (nowMins-set) : (1440-set+nowMins);
        float tn = Math.max(0, Math.min(1, eN/nightSpan));

        Paint gold = new Paint(Paint.ANTI_ALIAS_FLAG);
        gold.setStyle(Paint.Style.STROKE); gold.setStrokeWidth(8);
        gold.setColor(0xFFD3A545); gold.setStrokeCap(Paint.Cap.ROUND);
        Paint silver = new Paint(Paint.ANTI_ALIAS_FLAG);
        silver.setStyle(Paint.Style.STROKE); silver.setStrokeWidth(6);
        silver.setColor(0xFFBFD8E0); silver.setStrokeCap(Paint.Cap.ROUND);

        if (isDay) {
            if (tc > 0.002f) { Path p = new Path(); pmD.getSegment(0, lenD*tc, p, true); cv.drawPath(p, gold); }
        } else {
            cv.drawPath(day, gold);
            if (tn > 0.002f) { Path p = new Path(); pmN.getSegment(0, lenN*tn, p, true); cv.drawPath(p, silver); }
        }

        Paint dotFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotFill.setStyle(Paint.Style.FILL); dotFill.setColor(0xFF0B3D36);
        Paint dotGold = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotGold.setStyle(Paint.Style.STROKE); dotGold.setStrokeWidth(5); dotGold.setColor(0xFFD3A545);
        Paint dotSilver = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotSilver.setStyle(Paint.Style.STROKE); dotSilver.setStrokeWidth(5); dotSilver.setColor(0xFFBFD8E0);
        Paint lbl = new Paint(Paint.ANTI_ALIAS_FLAG);
        lbl.setColor(0xB3FFFFFF); lbl.setTextSize(22); lbl.setTextAlign(Paint.Align.CENTER);

        String[] dayNames = {"شروق","ظهر","عصر","مغرب"};
        for (String n : dayNames) {
            Integer mv = M.get(n); if (mv == null) continue;
            float t = (mv-rise)/span;
            float[] q = pt(t, x0,y0, cdx,cdy, x1,y1);
            cv.drawCircle(q[0], q[1], 8, dotFill);
            cv.drawCircle(q[0], q[1], 8, dotGold);
            boolean edge = n.equals("شروق") || n.equals("مغرب");
            cv.drawText(n, q[0], edge ? q[1]-18 : q[1]+34, lbl);
        }
        String[] nightNames = {"عشاء","فجر"};
        for (String n : nightNames) {
            Integer mv = M.get(n); if (mv == null) continue;
            float ee = (mv >= set) ? (mv-set) : (1440-set+mv);
            float t = Math.max(0, Math.min(1, ee/nightSpan));
            float[] q = pt(t, x1,y1, cnx,cny, x0,y0);
            cv.drawCircle(q[0], q[1], 8, dotFill);
            cv.drawCircle(q[0], q[1], 8, dotSilver);
            cv.drawText(n, q[0], q[1]+34, lbl);
        }

        if (isDay) {
            float[] s = pt(tc, x0,y0, cdx,cdy, x1,y1);
            Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
            glow.setStyle(Paint.Style.FILL); glow.setColor(0x38D3A545);
            cv.drawCircle(s[0], s[1], 18, glow);
            Paint sf = new Paint(Paint.ANTI_ALIAS_FLAG);
            sf.setStyle(Paint.Style.FILL); sf.setColor(0xFFE6B354);
            cv.drawCircle(s[0], s[1], 8, sf);
            Paint rg = new Paint(Paint.ANTI_ALIAS_FLAG);
            rg.setStyle(Paint.Style.STROKE); rg.setStrokeWidth(3); rg.setColor(0xFFFFF3D6);
            cv.drawCircle(s[0], s[1], 8, rg);
        } else {
            float[] s = pt(tn, x1,y1, cnx,cny, x0,y0);
            Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
            glow.setStyle(Paint.Style.FILL); glow.setColor(0x2EE8F1F8);
            cv.drawCircle(s[0], s[1], 16, glow);
            Paint mf = new Paint(Paint.ANTI_ALIAS_FLAG);
            mf.setStyle(Paint.Style.FILL); mf.setColor(0xFFE8F1F8);
            cv.drawCircle(s[0], s[1], 7, mf);
            Paint rg = new Paint(Paint.ANTI_ALIAS_FLAG);
            rg.setStyle(Paint.Style.STROKE); rg.setStrokeWidth(2); rg.setColor(0xFFFFFFFF);
            cv.drawCircle(s[0], s[1], 7, rg);
        }

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
                String ft = data.optString("fajrTomorrow", "");
                boolean done = false;
                if (!ft.isEmpty()) {
                    try {
                        String[] hm = ft.split(":");
                        int fm = Integer.parseInt(hm[0])*60 + Integer.parseInt(hm[1]);
                        int df = (1440 - nowMins) + fm;
                        views.setTextViewText(R.id.widgetNextName, "فجر غدًا");
                        views.setTextViewText(R.id.widgetCountdown, String.format(Locale.US, "%02d:%02d", df/60, df%60));
                        done = true;
                    } catch (Exception ignored) {}
                }
                if (!done) {
                    views.setTextViewText(R.id.widgetNextName, "غدًا");
                    views.setTextViewText(R.id.widgetCountdown, "—");
                }
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