package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager
{
    public static final String KEY_PREF_AUTO_START_LOG        = "pref_autoStartLog";
    public static final String KEY_PREF_LOG_TRIP              = "pref_autoLogTrip";
    public static final String KEY_PREF_BLUETOOTH_DEVICE_LIST = "pref_bluetoothDeviceList";
    public static final String KEY_PREF_GPS_TIME_INTERVAL     = "pref_gpsTimeInterval";
    public static final String KEY_PREF_GPS_DISTANCE_INTERVAL = "pref_gpsDistanceInterval";
    public static final String KEY_PREF_GPS_ACCURACY          = "pref_gpsAccuracy";

    public static boolean isAutoStartLog (Context ctx)
    {
        return getSharedPrefs (ctx).getBoolean (KEY_PREF_AUTO_START_LOG, false);
    }

    private static SharedPreferences getSharedPrefs (Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences (ctx);
    }

    public static void setAutoStartLog (Context ctx, boolean value)
    {
        SharedPreferences.Editor editor;

        editor = getSharedPrefs (ctx).edit ();
        editor.putBoolean (KEY_PREF_AUTO_START_LOG, value);
        editor.commit ();
    }

    public static boolean isLogTrip (Context ctx)
    {
        return getSharedPrefs (ctx).getBoolean (KEY_PREF_LOG_TRIP, false);
    }

    public static void setLogTrip (Context ctx, boolean value)
    {
        SharedPreferences.Editor editor;

        editor = getSharedPrefs (ctx).edit ();
        editor.putBoolean (KEY_PREF_LOG_TRIP, value);
        editor.commit ();
    }

    public static int getGpsTimeInterval (Context ctx)
    {
        return getIntValueFromString (ctx, KEY_PREF_GPS_TIME_INTERVAL, "0");
    }

    private static int getIntValueFromString (Context ctx, String key, String def)
    {
        return Integer.parseInt (getSharedPrefs (ctx).getString (key, def));
    }

    public static int getGpsDistanceInterval (Context ctx)
    {
        return getIntValueFromString (ctx, KEY_PREF_GPS_DISTANCE_INTERVAL, "10");
    }

    public static int getGpsAccuracy (Context ctx)
    {
        return getIntValueFromString (ctx, KEY_PREF_GPS_ACCURACY, "50");
    }
}
