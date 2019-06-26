package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

public class SettingsManager
{
    public static final String NAME_STATUS_PREFERENCES = "logMyTripStatus";

    public static final String KEY_PREF_AUTO_START_LOG_ALWAYS    = "pref_autoStartLogAlways";
    public static final String KEY_PREF_AUTO_START_LOG_BLUETOOTH = "pref_autoStartLogBluetooth";
    public static final String KEY_PREF_AUTO_START_MODE          = "pref_autoStartLogMode";
    public static final String KEY_PREF_BLUETOOTH_DEVICE_LIST    = "pref_bluetoothDeviceList";
    public static final String KEY_PREF_GPS_TIME_INTERVAL        = "pref_gpsTimeInterval";
    public static final String KEY_PREF_GPS_DISTANCE_INTERVAL    = "pref_gpsDistanceInterval";
    public static final String KEY_PREF_GPS_ACCURACY             = "pref_gpsAccuracy";

    public static final String KEY_STATUS_LOG_JOURNEY = "status_logJourney";
    public static final String KEY_STATUS_CURRENT_JOURNEY_ID = "status_currentJourneyId";
    public static final String KEY_STATUS_IS_WAITING_BLUETOOTH = "status_isWaitingBluetooth";

    public static final String KEY_STATUS_IS_LOGGING = "status_isLogging";

    public static boolean isAutoStartLogBluetooth (Context ctx)
    {
        return getSharedPrefs (ctx).getBoolean (KEY_PREF_AUTO_START_LOG_BLUETOOTH, false);
    }

    private static SharedPreferences getSharedPrefs (Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences (ctx);
    }

    public static void setAutoStartLog (Context ctx, boolean value)
    {
        SharedPreferences.Editor editor;

        editor = getSharedPrefs (ctx).edit ();
        editor.putBoolean (KEY_PREF_AUTO_START_LOG_BLUETOOTH, value);
        editor.commit ();
    }

    public static boolean isLogJourney (Context ctx)
    {
        return getSharedStatusPrefs (ctx).getBoolean (KEY_STATUS_LOG_JOURNEY, false);
    }

    private static SharedPreferences getSharedStatusPrefs (Context ctx)
    {
        return ctx.getSharedPreferences (NAME_STATUS_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean isAutoStartOnConnect (Context ctx)
    {
        int mode;

        mode = Integer.parseInt (getSharedPrefs (ctx).getString (KEY_PREF_AUTO_START_MODE, "0"));

        return (mode == 0);
    }

    public static void setLogJourney (Context ctx, boolean value)
    {
        SharedPreferences.Editor editor;

        editor = getSharedStatusPrefs (ctx).edit ();
        editor.putBoolean (KEY_STATUS_LOG_JOURNEY, value);
        editor.commit ();
    }

    public static long getCurrentJourneyId (Context ctx)
    {
        return getSharedStatusPrefs (ctx).getLong (KEY_STATUS_CURRENT_JOURNEY_ID, 0);
    }

    public static void setCurrentJourneyId (Context ctx, long id)
    {
        SharedPreferences.Editor editor;

        editor = getSharedStatusPrefs (ctx).edit ();
        editor.putLong (KEY_STATUS_CURRENT_JOURNEY_ID, id);
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

        editor = getSharedStatusPrefs (ctx).edit ();
        editor.putBoolean (KEY_STATUS_IS_WAITING_BLUETOOTH, value);
        editor.commit ();
    }

    public static boolean isWaitingBluetooth (Context ctx)
    {
        return getSharedStatusPrefs (ctx).getBoolean (KEY_STATUS_IS_WAITING_BLUETOOTH, false);
    }

    public static boolean isAutoStartLogAlways (Context ctx)
    {
        return getSharedPrefs (ctx).getBoolean (KEY_PREF_AUTO_START_LOG_ALWAYS, false);
    }
}
