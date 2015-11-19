package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

public class SettingsManager
{
    public static final String KEY_PREF_AUTO_START_LOG        = "pref_autoStartLog";
    public static final String KEY_PREF_LOG_TRIP              = "pref_autoLogTrip";
    public static final String KEY_PREF_AUTO_START_MODE = "pref_autoStartLogMode";
    public static final String KEY_PREF_BLUETOOTH_DEVICE_LIST = "pref_bluetoothDeviceList";
    public static final String KEY_PREF_GPS_TIME_INTERVAL     = "pref_gpsTimeInterval";
    public static final String KEY_PREF_GPS_DISTANCE_INTERVAL = "pref_gpsDistanceInterval";
    public static final String KEY_PREF_GPS_ACCURACY          = "pref_gpsAccuracy";
    public static final String KEY_PREF_CURRENT_TRIP_ID = "pref_currentTripId";
    public static final String KEY_PREF_IS_WAITING_BLUETOOTH = "pref_isWaitingBluetooth";

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

    public static boolean isAutostartOnConnect (Context ctx)
    {
        int mode;

        mode = Integer.parseInt (getSharedPrefs (ctx).getString (KEY_PREF_AUTO_START_MODE, "0"));

        return (mode == 0);
    }

    public static void setLogTrip (Context ctx, boolean value)
    {
        SharedPreferences.Editor editor;

        editor = getSharedPrefs (ctx).edit ();
        editor.putBoolean (KEY_PREF_LOG_TRIP, value);
        editor.commit ();
    }

    public static long getCurrentTripId (Context ctx)
    {
        return getSharedPrefs (ctx).getLong (KEY_PREF_CURRENT_TRIP_ID, 0);
    }

    public static void setCurrentTripId (Context ctx, long id)
    {
        SharedPreferences.Editor editor;

        editor = getSharedPrefs (ctx).edit ();
        editor.putLong (KEY_PREF_CURRENT_TRIP_ID, id);
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

    public static Set<String> getBluetoothDeviceList (Context ctx)
    {
        return getSharedPrefs (ctx).getStringSet (KEY_PREF_BLUETOOTH_DEVICE_LIST, null);
    }

    public static void setIsWaitingBluetooth (Context ctx, boolean value)
    {
        SharedPreferences.Editor editor;

        editor = getSharedPrefs (ctx).edit ();
        editor.putBoolean (KEY_PREF_IS_WAITING_BLUETOOTH, value);
        editor.commit ();
    }

    public static boolean isWaitingBluetooth (Context ctx)
    {
        return getSharedPrefs (ctx).getBoolean (KEY_PREF_IS_WAITING_BLUETOOTH, false);
    }

}
