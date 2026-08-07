package com.suez.mawaqit;

import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridgePlugin extends Plugin {
    @PluginMethod()
    public void updateWidget(PluginCall call) {
        String json = call.getString("json");
        if (json != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("prayer_widget", Context.MODE_PRIVATE);
            prefs.edit().putString("data", json).apply();
            PrayerWidgetProvider.refresh(getContext());
        }
        call.resolve();
    }
}