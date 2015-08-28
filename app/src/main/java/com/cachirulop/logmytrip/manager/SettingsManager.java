
package com.cachirulop.logmytrip.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager
{
    public static final String KEY_PREF_AUTO_START_LOG        = "pref_autoStartLog";
    public static final String KEY_PREF_LOG_TRIP              = "pref_autoLogTrip";
    public static final String KEY_PREF_BLUETOOTH_DEVICE_LIST = "pref_bluetoothDeviceList";

    public static boolean getAutoStartLog (Context ctx)
    {
        return getSharedPrefs (ctx).getBoolean (KEY_PREF_AUTO_START_LOG,
                                                false);
    }

    public static boolean isLogTrip(Context ctx)
    {
        return getSharedPrefs (ctx).getBoolean (KEY_PREF_LOG_TRIP,
                                                false);
    }
    
    public static void setLogTrip (Context ctx, boolean value)
    {
        SharedPreferences.Editor editor;
        
        editor = getSharedPrefs (ctx).edit ();
        editor.putBoolean (KEY_PREF_LOG_TRIP, value);
        editor.commit ();
    }
    
    private static SharedPreferences getSharedPrefs (Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences (ctx);
    }
}
